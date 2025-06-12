package com.estsoft.project3.controller;

import com.estsoft.project3.contact.Contact;
import com.estsoft.project3.contact.ContactResponseDto;
import com.estsoft.project3.contact.ContactService;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactViewController {

    private final ContactService contactService;

    @Autowired
    public ContactViewController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/contacts/new")
    public String showCreateContactForm() {

        return "contact";
    }

    @GetMapping("/contacts/{id}/edit")
    public String showEditReviewForm(@PathVariable Long id, Model model,
        @AuthenticationPrincipal OAuth2User principal) {

        String currentUserEmail = principal.getAttribute("email");
        Contact contact = contactService.getContactById(id);

        if (!contact.getUser().getEmail().equals(currentUserEmail)) {
            return "redirect:/error";
        }
        model.addAttribute("contact", new ContactResponseDto(contact));
        model.addAttribute("contactId", id);
        return "contact";
    }

    @GetMapping("/contacts/{id}")
    public String showContactDetail(@PathVariable Long id, Model model,
        @AuthenticationPrincipal OAuth2User principal) {
        Contact contact = contactService.getContactById(id);
        ContactResponseDto responseDto = new ContactResponseDto(contact);

        model.addAttribute("contact", responseDto);

        String currentUserEmail = principal.getName();
        boolean isOwner = contact.getUser().getEmail().equals(currentUserEmail);
        boolean isAdmin = contact.getUser().getRole() == Role.ROLE_ADMIN;

        model.addAttribute("hasAccess", isOwner);
        model.addAttribute("hasDeleteAccess", isOwner || isAdmin);
        return "contact-detail";
    }

    @GetMapping("/contacts")
    public String getMyContactsPage(@AuthenticationPrincipal OAuth2User principal,
        @RequestParam(name = "sort", defaultValue = "newest") String sort,
        Model model) {
        String email = principal.getAttribute("email");
        User user = contactService.getUserByEmail(email);
        List<Contact> contacts = contactService.getContactsByUser(user);

        boolean newestFirst = sort.equalsIgnoreCase("newest");
        contacts.sort((a, b) -> {
            if (newestFirst) {
                return b.getCreateDate().compareTo(a.getCreateDate());
            } else {
                return a.getCreateDate().compareTo(b.getCreateDate());
            }
        });

        List<ContactResponseDto> responseDto = contacts.stream()
            .map(ContactResponseDto::new)
            .collect(Collectors.toList());

        model.addAttribute("contacts", responseDto);
        model.addAttribute("sort", sort);
        model.addAttribute("user", user);
        model.addAttribute("userId", user.getUserId());
        model.addAttribute("role", String.valueOf(user.getRole()));
        model.addAttribute("nickname", user.getNickname());
        return "contact-main";
    }


}
