package com.estsoft.project3.file;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactFileRepository extends JpaRepository<ContactFile, Long> {

    List<ContactFile> findByContact_ContactId(Long contactId);

}
