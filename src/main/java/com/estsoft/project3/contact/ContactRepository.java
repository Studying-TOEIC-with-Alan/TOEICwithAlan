package com.estsoft.project3.contact;

import com.estsoft.project3.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByUser(User user);
}