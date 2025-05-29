package com.estsoft.project3.service;

import com.estsoft.project3.domain.Til;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.TilRequest;
import com.estsoft.project3.repository.TilRepository;
import com.estsoft.project3.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TilService {
    private final TilRepository tilRepository;
    private final UserRepository userRepository;

    public TilService(TilRepository tilRepository, UserRepository userRepository) {
        this.tilRepository = tilRepository;
        this.userRepository = userRepository;
    }

    //Insert TIL
    public void insertTIL(TilRequest tilRequest) {
        User user = userRepository.findById(tilRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        Til til = new Til(user, tilRequest.getTitle(), tilRequest.getSummary());
        tilRepository.save(til);
    }

    //Get TIL list by user id
    public List<Til> getTILsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return tilRepository.findAllByUserUserId(userId);
    }

    //Get single TIL
    public Til getTIL(Long tilId) {
        Til til = tilRepository.findById(tilId)
                .orElseThrow(() -> new RuntimeException("TIL 없음"));

        return til;
    }

    //Update TIL
    public void updateTIL(TilRequest tilRequest) {
        Til til = tilRepository.findById(tilRequest.getTilId())
                .orElseThrow(() -> new RuntimeException("TIL 없음"));

        til.setTitle(tilRequest.getTitle());
        til.setSummary(tilRequest.getSummary());

        tilRepository.save(til);
    }

    //Delete TIL
    public void deleteTIL(Long tilId) {
        tilRepository.deleteById(tilId);
    }

}
