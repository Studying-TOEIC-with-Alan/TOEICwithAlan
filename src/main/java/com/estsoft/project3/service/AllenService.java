package com.estsoft.project3.service;

import com.estsoft.project3.domain.Allen;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.AllenRequest;
import com.estsoft.project3.repository.AllenRepository;
import com.estsoft.project3.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AllenService {
    private final AllenRepository allenRepository;
    private final UserRepository userRepository;

    public AllenService(AllenRepository allenRepository, UserRepository userRepository) {
        this.allenRepository = allenRepository;
        this.userRepository = userRepository;
    }

    //Insert Allen
    public void insertAllen(AllenRequest allenRequest) {
        User user = userRepository.findById(allenRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        Allen allen = new Allen(user, allenRequest.getCategory(), allenRequest.getInputText(), allenRequest.getSummary());
        allenRepository.save(allen);
    }

}
