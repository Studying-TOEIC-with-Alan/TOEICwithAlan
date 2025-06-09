package com.estsoft.project3.service;

import org.springframework.stereotype.Service;

@Service
public class ChatRoomAccessService {

    public boolean canEnter(Long grade, int roomId) {
        if (roomId == 0) {
            return grade >= 0;
        }
        if (roomId == 1) {
            return grade >= 1;
        }
        if (roomId == 2) {
            return grade >= 2;
        }
        return false;
    }
}