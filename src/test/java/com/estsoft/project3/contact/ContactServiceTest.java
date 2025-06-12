package com.estsoft.project3.contact;

import static org.assertj.core.api.Assertions.assertThat;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.file.ContactFileRepository;
import com.estsoft.project3.file.FileDto;
import com.estsoft.project3.file.FileStorageService;
import com.estsoft.project3.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
@RequiredArgsConstructor
class ContactServiceTest {

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ContactFileRepository contactFileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private User createUser(String email) {
        return userRepository.findByEmail(email).orElseGet(() ->
            userRepository.save(User.builder()
                .email(email)
                .nickname("tester")
                .role(Role.ROLE_USER)
                .isActive("Y")
                .provider("google")
                .build()));
    }

    @Test
    @DisplayName("문의 저장 및 이미지 저장 테스트")
    void saveContactTest() {
        User user = createUser("test@example.com");

        List<FileDto> images = List.of(
            new FileDto("file1.jpg", "path/to/file1.jpg"),
            new FileDto("file2.jpg", "path/to/file2.jpg")
        );
        ContactRequestDto requestDto = new ContactRequestDto("문의 내용", "문의 제목", images);

        ContactResponseDto responseDto = contactService.saveContact(user, requestDto);

        assertThat(responseDto.getTitle()).isEqualTo("문의 제목");
        assertThat(responseDto.getContent()).isEqualTo("문의 내용");

        Contact saved = contactRepository.findById(responseDto.getContactId()).orElseThrow();
        if (saved.getImages() != null) {
            assertThat(saved.getImages()).hasSize(2);
        }
    }

    @Test
    @DisplayName("전체 문의 조회 정렬 테스트")
    void getAllContactsSortedByDateTest() throws InterruptedException {
        contactRepository.deleteAll();
        User user = createUser("test@example.com");

        Contact c1 = contactRepository.save(Contact.builder()
            .title("first")
            .content("first content")
            .user(user)
            .build());

        Thread.sleep(10);

        Contact c2 = contactRepository.save(Contact.builder()
            .title("second")
            .content("second content")
            .user(user)
            .build());

        List<Contact> newestFirst = contactService.getAllContactsSortedByDate(true);
        assertThat(newestFirst.get(0).getTitle()).isEqualTo("second");

        List<Contact> oldestFirst = contactService.getAllContactsSortedByDate(false);
        assertThat(oldestFirst.get(0).getTitle()).isEqualTo("first");
    }

    @Test
    @DisplayName("ID로 문의 단건 조회 테스트")
    void getContactByIdTest() {
        User user = createUser("test@example.com");

        Contact contact = contactRepository.save(Contact.builder()
            .title("title")
            .content("content")
            .user(user)
            .build());

        Contact found = contactService.getContactById(contact.getContactId());

        assertThat(found.getTitle()).isEqualTo("title");
        assertThat(found.getContent()).isEqualTo("content");
    }

    @Test
    @DisplayName("문의 수정 테스트")
    void updateContactTest() {
        User user = createUser("test@example.com");
        Contact contact = contactRepository.save(Contact.builder()
            .title("old title")
            .content("old content")
            .user(user)
            .build());

        List<FileDto> images = List.of(new FileDto("newfile.jpg", "new/path/file.jpg"));
        ContactRequestDto updateDto = new ContactRequestDto("new content", "new title", images);

        Contact updated = contactService.updateContact(contact.getContactId(), updateDto, user);

        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo("new title");
        assertThat(updated.getContent()).isEqualTo("new content");

        if (updated.getImages() != null) {
            assertThat(updated.getImages())
                .anyMatch(img -> "newfile.jpg".equals(img.getFilename()));
        }
    }

    @Test
    @DisplayName("문의 삭제 권한 및 삭제 테스트")
    void deleteContactTest() {
        User user = createUser("test@example.com");
        Contact contact = contactRepository.save(Contact.builder()
            .title("to be deleted")
            .content("content")
            .user(user)
            .build());

        contactService.deleteContact(contact.getContactId(), user);

        assertThat(contactRepository.findById(contact.getContactId())).isEmpty();
    }

    @Test
    @DisplayName("사용자별 문의글 조회 테스트")
    void getContactsByUserTest() {
        User user = createUser("test@example.com");

        contactRepository.save(Contact.builder()
            .title("user's contact")
            .content("content")
            .user(user)
            .build());

        List<Contact> contacts = contactService.getContactsByUser(user);

        assertThat(contacts).isNotEmpty();
        assertThat(contacts.get(0).getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("문의 상태 변경 테스트")
    void updateContactStatusTest() {
        User user = createUser("test@example.com");

        Contact contact = contactRepository.save(Contact.builder()
            .title("status test")
            .content("content")
            .user(user)
            .build());

        contactService.updateContactStatus(contact.getContactId(), "IN_PROGRESS");

        Contact updated = contactRepository.findById(contact.getContactId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Contact.Status.IN_PROGRESS);
    }

}