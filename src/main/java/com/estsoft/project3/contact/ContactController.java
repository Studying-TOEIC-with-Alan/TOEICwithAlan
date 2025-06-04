package com.estsoft.project3.contact;

import com.estsoft.project3.domain.User;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;
    private final ContactRepository contactRepository;

    public ContactController(ContactService contactService, ContactRepository contactRepository) {
        this.contactService = contactService;
        this.contactRepository = contactRepository;
    }

    @PostMapping
    public ResponseEntity<ContactResponseDto> saveContact(
        @RequestBody ContactRequestDto contactRequestDto,
        @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");

        User user = contactService.getUserByEmail(email);

        ContactResponseDto responseDto = contactService.saveContact(user, contactRequestDto);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<ContactResponseDto>> getAllContacts() {
        List<Contact> contacts = contactService.getAllContactsSortedByDate(
            true);
        List<ContactResponseDto> responseList = contacts.stream()
            .map(ContactResponseDto::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponseDto> getContactById(@PathVariable Long id) {
        Contact contact = contactService.getContactById(id);
        ContactResponseDto responseDto = new ContactResponseDto(contact);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponseDto> updateContact(
        @PathVariable Long id,
        @RequestBody ContactRequestDto requestDto,
        @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");

        User user = contactService.getUserByEmail(email);

        Contact updatedContact = contactService.updateContact(id, requestDto, user);
        ContactResponseDto responseDto = new ContactResponseDto(updatedContact);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id,
        @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");

        User user = contactService.getUserByEmail(email);

        contactService.deleteContact(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<ContactResponseDto>> getMyContacts(
        @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");

        User user = contactService.getUserByEmail(email);

        List<Contact> contacts = contactService.getContactsByUser(user);

        List<ContactResponseDto> responseList = contacts.stream()
            .map(ContactResponseDto::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseList);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
        @RequestBody ContactStatusUpdateDto request) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contact not found"));

        contact.setStatus(request.toStatusEnum());
        contactRepository.save(contact);

        return ResponseEntity.ok().build();
    }
}
