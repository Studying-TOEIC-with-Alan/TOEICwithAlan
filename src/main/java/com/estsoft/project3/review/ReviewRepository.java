package com.estsoft.project3.review;

import com.estsoft.project3.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByUser(User user);
}
