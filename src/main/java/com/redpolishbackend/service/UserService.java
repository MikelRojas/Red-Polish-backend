package com.redpolishbackend.service;

import com.redpolishbackend.dto.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUserByEmail(String email);
    UserDto updateUser(String email, UserDto userDto);
}
