package com.estsoft.project3.contact;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContactResponseDto> saveContact(
        @RequestBody ContactRequestDto contactRequestDto) {

        ContactResponseDto responseDto = contactService.saveContact(contactRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<ContactResponseDto>> getAllContacts() {
        List<Contact> contacts = contactService.getAllContacts();
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
        @RequestBody ContactRequestDto requestDto) {

        Contact updatedContact = contactService.updateContact(id, requestDto);
        ContactResponseDto responseDto = new ContactResponseDto(updatedContact);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }
}
