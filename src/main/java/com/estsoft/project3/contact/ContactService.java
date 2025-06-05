package com.estsoft.project3.contact;

import com.estsoft.project3.Image.Image;
import com.estsoft.project3.Image.ImageDto;
import com.estsoft.project3.Image.ImageRepository;
import com.estsoft.project3.Image.ImageStorageService;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final ImageStorageService imageStorageService;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ContactResponseDto saveContact(User user, ContactRequestDto requestDto) {
        try {
            Contact contact = requestDto.toEntity(user);

            contactRepository.save(contact);

            if (requestDto.getImages() != null) {
                for (ImageDto dto : requestDto.getImages()) {
                    Image image = dto.toEntity(contact);
                    imageRepository.save(image);
                }
            }

            return new ContactResponseDto(contact);
        } catch (Exception ex) {
            throw new IllegalArgumentException("문의 저장 중 문제가 발생했습니다.");
        }
    }

    public List<Contact> getAllContactsSortedByDate(boolean newestFirst) {
        Sort sort = newestFirst
            ? Sort.by(Sort.Direction.DESC, "createDate")
            : Sort.by(Sort.Direction.ASC, "createDate");
        return contactRepository.findAll(sort);
    }

    public Contact getContactById(Long id) {
        return contactRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("글을 찾을 수 없습니다. id=" + id));
    }

    @Transactional
    public Contact updateContact(Long id, ContactRequestDto requestDto, User currentUser) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("글을 찾을 수 없습니다. id=" + id));

        if (!currentUser.isOwner(contact)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        contact.update(requestDto.getTitle(), requestDto.getContent());
        return contactRepository.save(contact);
    }

    @Transactional
    public void deleteContact(Long contactId, User currentUser) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact를 찾을 수 없습니다."));

        if (!(currentUser.isOwner(contact) || currentUser.isAdmin())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        List<Image> images = contact.getImages();

        if (images != null && !images.isEmpty()) {
            List<String> imageKeys = images.stream()
                .map(Image::getFilename)
                .collect(Collectors.toList());

            imageStorageService.deleteImagesByKeys(imageKeys);
        }

        contactRepository.delete(contact);
    }

    public List<Contact> getContactsByUser(User user) {
        return contactRepository.findByUser(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public void updateContactStatus(Long contactId, String status) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new IllegalArgumentException("해당 ID의 민원이 존재하지 않습니다: " + contactId));

        contact.setStatus(Contact.Status.valueOf(status));
        contactRepository.save(contact);
    }
}