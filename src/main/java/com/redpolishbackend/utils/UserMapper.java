package com.redpolishbackend.utils;

import com.redpolishbackend.dto.UserDto;
import com.redpolishbackend.entity.User;

public class UserMapper {

    public static UserDto maptoUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        if (user.getLast_name() != null) {
            userDto.setLast_name(user.getLast_name());
        }
        return userDto;
    }

    public static User maptoUser(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        if (userDto.getLast_name() != null) {
            user.setLast_name(userDto.getLast_name());
        }
        if (userDto.getPassword() != null) {
            user.setPassword(userDto.getPassword());
        }
        return user;
    }
}
