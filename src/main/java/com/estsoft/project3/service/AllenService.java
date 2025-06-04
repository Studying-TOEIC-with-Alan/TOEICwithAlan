package com.estsoft.project3.service;

import com.estsoft.project3.domain.Allen;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.AllenRequest;
import com.estsoft.project3.repository.AllenRepository;
import com.estsoft.project3.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
        Long userId = allenRequest.getUserId();
        String category = allenRequest.getCategory();
        String inputText = allenRequest.getInputText();
        String summary = allenRequest.getSummary();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        List<Allen> allen = allenRepository.findAllByUserUserIdAndCategoryAndInputText(userId,category,inputText);

        if (allen.isEmpty()) {
            allenRepository.save(new Allen(user, category, inputText, summary));
        }
    }

    //Get Allen
    public Allen GetLastAllenByUserAndCatAndInput (Long userId, String category, String inputText) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return allenRepository.findFirstByUserUserIdAndCategoryAndInputTextStartingWithOrderByCreatedDateDesc(userId, category, inputText);
    }

}
