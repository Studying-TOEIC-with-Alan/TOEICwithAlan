package com.estsoft.project3.contact;

import com.estsoft.project3.Image.Image;
import com.estsoft.project3.Image.ImageDto;
import com.estsoft.project3.Image.ImageRepository;
import com.estsoft.project3.Image.ImageStorageService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final ImageStorageService imageStorageService;
    private final ImageRepository imageRepository;

    @Transactional
    public ContactResponseDto saveContact(ContactRequestDto requestDto) {
        Contact contact = requestDto.toEntity();

        contactRepository.save(contact);

        if (requestDto.getImages() != null) {
            for (ImageDto dto : requestDto.getImages()) {
                Image image = dto.toEntity(contact);
                imageRepository.save(image);
            }
        }

        return new ContactResponseDto(contact);
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    public Contact getContactById(Long id) {
        return contactRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Contact를 찾을 수 없습니다. id=" + id));
    }

    public Contact updateContact(Long id, ContactRequestDto requestDto) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Contact를 찾을 수 없습니다. id=" + id));

        contact.update(requestDto.getTitle(), requestDto.getContent(), requestDto.getStatus());
        return contactRepository.save(contact);
    }

    @Transactional
    public void deleteContact(Long contactId) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact를 찾을 수 없습니다."));

        List<Image> images = contact.getImages();

        if (images != null && !images.isEmpty()) {
            List<String> imageKeys = images.stream()
                .map(Image::getFilename)
                .collect(Collectors.toList());

            imageStorageService.deleteImagesByKeys(imageKeys);
        }

        contactRepository.delete(contact);
    }
}