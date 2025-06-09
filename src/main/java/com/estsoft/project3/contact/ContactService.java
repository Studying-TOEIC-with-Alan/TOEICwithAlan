package com.estsoft.project3.contact;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.file.ContactFile;
import com.estsoft.project3.file.ContactFileRepository;
import com.estsoft.project3.file.FileDto;
import com.estsoft.project3.file.FileStorageService;
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
    private final FileStorageService fileStorageService;
    private final ContactFileRepository contactFileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ContactResponseDto saveContact(User user, ContactRequestDto requestDto) {
        try {
            Contact contact = requestDto.toEntity(user);

            contactRepository.save(contact);

            if (requestDto.getImages() != null) {
                for (FileDto dto : requestDto.getImages()) {
                    ContactFile image = dto.toEntity(contact);
                    contactFileRepository.save(image);
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
    public Contact updateContact(Long contactId, ContactRequestDto dto, User user) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new IllegalArgumentException("해당 문의가 존재하지 않습니다."));

        if (!contact.getUser().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("문의 작성자만 수정할 수 있습니다.");
        }

        contact.update(dto.getTitle(), dto.getContent());

        List<ContactFile> existingImages = contactFileRepository.findByContact_ContactId(
            contactId);
        List<String> newFilePaths = dto.getImages().stream()
            .map(com.estsoft.project3.file.FileDto::getFilePath)
            .collect(Collectors.toList());

        List<String> s3KeysToDelete = existingImages.stream()
            .filter(img -> !newFilePaths.contains(img.getFilePath()))
            .map(com.estsoft.project3.file.ContactFile::getFilePath)
            .collect(Collectors.toList());

        fileStorageService.deleteImagesByKeys(s3KeysToDelete);

        for (ContactFile image : existingImages) {
            if (!newFilePaths.contains(image.getFilePath())) {
                contactFileRepository.delete(image);
            }
        }

        for (FileDto fileDto : dto.getImages()) {
            boolean alreadyExists = existingImages.stream()
                .anyMatch(img -> img.getFilePath().equals(fileDto.getFilePath()));

            if (!alreadyExists) {
                ContactFile newImage = new ContactFile();
                newImage.setFilename(fileDto.getFilename());
                newImage.setFilePath(fileDto.getFilePath());
                newImage.setContact(contact);
                contactFileRepository.save(newImage);
            }
        }

        return contact;
    }


    @Transactional
    public void deleteContact(Long contactId, User currentUser) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact를 찾을 수 없습니다."));

        if (!(currentUser.isOwner(contact) || currentUser.isAdmin())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        List<ContactFile> images = contact.getImages();

        if (images != null && !images.isEmpty()) {
            List<String> imageKeys = images.stream()
                .map(com.estsoft.project3.file.ContactFile::getFilename)
                .collect(Collectors.toList());

            fileStorageService.deleteImagesByKeys(imageKeys);
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
}