package com.estsoft.project3.repository;

import com.estsoft.project3.domain.Til;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TilRepository extends JpaRepository<Til, Long> {
    List<Til> findAllByUserUserId(Long userId);

}
