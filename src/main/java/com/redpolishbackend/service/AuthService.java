package com.redpolishbackend.service;

import com.redpolishbackend.dto.AuthResponseDto;

public interface AuthService {
    AuthResponseDto authenticate(String email, String password);

}