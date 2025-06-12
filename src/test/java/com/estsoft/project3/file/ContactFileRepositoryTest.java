package com.estsoft.project3.file;

import static org.assertj.core.api.Assertions.assertThat;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.contact.Contact;
import com.estsoft.project3.contact.ContactRepository;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Import(MockS3ClientConfig.class)
@ActiveProfiles("test")
class ContactFileRepositoryTest {

    @Autowired
    private ContactFileRepository contactFileRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("조회")
    void findByContactId() {
        User user = User.builder()
            .provider("test-provider")
            .email("tester@example.com")
            .nickname("tester")
            .role(Role.ROLE_USER)
            .isActive("Y")
            .build();

        user = userRepository.save(user);

        Contact contact = new Contact();
        contact.setUser(user);
        Contact savedContact = contactRepository.save(contact);

        ContactFile file1 = new ContactFile();
        file1.setContact(savedContact);
        file1.setFilename("file1.jpg");
        file1.setFilePath("http://example.com/file1.jpg");

        ContactFile file2 = new ContactFile();
        file2.setContact(savedContact);
        file2.setFilename("file2.jpg");
        file2.setFilePath("http://example.com/file2.jpg");

        contactFileRepository.save(file1);
        contactFileRepository.save(file2);

        List<ContactFile> files = contactFileRepository.findByContact_ContactId(
            savedContact.getContactId());

        assertThat(files).isNotNull();
        assertThat(files).hasSize(2);
        assertThat(files).extracting("Filename")
            .containsExactlyInAnyOrder("file1.jpg", "file2.jpg");
    }
}