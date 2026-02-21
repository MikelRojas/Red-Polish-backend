package com.redpolishbackend.controller;

import com.redpolishbackend.dto.AuthResponseDto;
import com.redpolishbackend.dto.LoginRequestDto;
import com.redpolishbackend.dto.UserDto;
import com.redpolishbackend.service.AuthService;
import com.redpolishbackend.service.JwtService;
import com.redpolishbackend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;
    private AuthService authService;
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {

        // Assign default role
        userDto.setRol("Usuario");

        UserDto savedUser = userService.createUser(userDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // Endpoint mejorado para login que genera y devuelve JWT
    @PostMapping("/sign_in")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        AuthResponseDto authResponse = authService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
        return ResponseEntity.ok(authResponse);
    }

    // Endpoint para obtener usuario por email
    @GetMapping("/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String userEmail) {
        UserDto userDto = userService.getUserByEmail(userEmail);
        return ResponseEntity.ok(userDto);
    }

    // Endpoint protegido con JWT: actualización de usuario
    @PostMapping("/update/{email}")   //Update user (Gestionar usuario)
    public ResponseEntity<UserDto> updateUser(
            @PathVariable("email") String email,
            @RequestBody UserDto userDto,
            @RequestHeader("Authorization") String token) {

        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserDto updatedUser = userService.updateUser(email, userDto);
        return ResponseEntity.ok(updatedUser);
    }
}
