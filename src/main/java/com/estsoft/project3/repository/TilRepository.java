package com.estsoft.project3.repository;

import com.estsoft.project3.domain.Til;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TilRepository extends JpaRepository<Til, Long> {
    Page<Til> findAllByUserUserIdOrderByUpdatedDateDesc(Long userId, Pageable pageable);
}
