package com.estsoft.project3.review;

import com.estsoft.project3.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<com.estsoft.project3.review.Review, Long> {

    List<com.estsoft.project3.review.Review> findByUser(User user);

}
