package com.estsoft.project3.service;

import com.estsoft.project3.contact.ContactRepository;
import com.estsoft.project3.contact.ContactResponseDto;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    public void updateUserGradeAndScore(Long userId, Long grade, Long score) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        user.setGrade(grade);
        user.setScore(score);
        userRepository.save(user);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> findByNicknameContaining(String nickname, Pageable pageable) {
        return userRepository.findByNicknameContaining(nickname, pageable);
    }

    public Page<ContactResponseDto> getPagedContactsForAdmin(boolean newestFirst,
        Pageable pageable) {
        Sort sort = newestFirst
            ? Sort.by(Sort.Direction.DESC, "createDate")
            : Sort.by(Sort.Direction.ASC, "createDate");

        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            sort);
        return contactRepository.findAll(sortedPageable)
            .map(ContactResponseDto::new);
    }


}
