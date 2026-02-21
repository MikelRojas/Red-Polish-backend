package com.redpolishbackend.mapper;

import com.redpolishbackend.dto.UserDto;
import com.redpolishbackend.entity.User;

public class UserMapper {

    public static UserDto maptoUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getLast_name(),
                user.getEmail(),
                user.getPassword(),
                user.getRol()
        );
    }

    public static User maptoUser(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getLast_name(),
                userDto.getEmail(),
                userDto.getPassword(),
                userDto.getRol()
        );
    }
}
