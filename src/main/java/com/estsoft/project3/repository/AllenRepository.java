package com.estsoft.project3.repository;

import com.estsoft.project3.domain.Allen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AllenRepository extends JpaRepository<Allen, Long> {
    List<Allen> findAllByUserUserIdAndCategoryAndInputText(Long userId, String category, String inputText);

    //Get latest allen input of a user with specific category and user starting input text
    Allen findFirstByUserUserIdAndCategoryAndInputTextStartingWithOrderByCreatedDateDesc(Long userId, String category, String inputText);

}
