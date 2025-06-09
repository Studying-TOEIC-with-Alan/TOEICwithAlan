package com.estsoft.project3.file;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewFileRepository extends JpaRepository<ReviewFile, Long> {

    List<ReviewFile> findByReview_ReviewId(Long reviewId);

}
