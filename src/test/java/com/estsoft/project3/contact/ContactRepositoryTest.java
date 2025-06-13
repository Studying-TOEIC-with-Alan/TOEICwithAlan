package com.estsoft.project3.contact;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
@Transactional
class ContactRepositoryTest {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    public User createUser(String email) {
        return userRepository.findByEmailAndIsActive(email, "Y").orElseGet(() ->
            userRepository.save(User.builder()
                .email(email)
                .nickname("tester")
                .role(Role.ROLE_USER)
                .isActive("Y")
                .provider("google")
                .build()));
    }

    @Test
    void findByUser() {
        User user = createUser("user@example.com");

        Contact contact1 = Contact.builder()
            .title("Title 1")
            .content("Content 1")
            .user(user)
            .build();

        Contact contact2 = Contact.builder()
            .title("Title 2")
            .content("Content 2")
            .user(user)
            .build();

        contactRepository.save(contact1);
        contactRepository.save(contact2);

        List<Contact> contacts = contactRepository.findByUser(user);

        assertThat(contacts).isNotNull();
        assertThat(contacts).hasSize(2);
        assertThat(contacts).extracting("title").containsExactlyInAnyOrder("Title 1", "Title 2");
    }
}