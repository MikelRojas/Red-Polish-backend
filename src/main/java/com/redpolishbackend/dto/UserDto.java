package com.redpolishbackend.dto;


import com.redpolishbackend.entity.ForgotPassword;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String last_name;
    private String email;
    private String password;
    private String rol = "Usuario";
}
