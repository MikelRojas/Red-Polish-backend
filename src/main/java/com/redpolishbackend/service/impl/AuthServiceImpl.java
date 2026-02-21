package com.redpolishbackend.service.impl;

import com.redpolishbackend.dto.AuthResponseDto;
import com.redpolishbackend.dto.UserDto;
import com.redpolishbackend.entity.User;
import com.redpolishbackend.exception.ResourceNotFoundException;
import com.redpolishbackend.mapper.UserMapper;
import com.redpolishbackend.repository.UserRepository;
import com.redpolishbackend.service.AuthService;
import com.redpolishbackend.service.JwtService;
import com.redpolishbackend.utils.PasswordUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private UserRepository userRepository;
    private JwtService jwtService;

    @Override
    public AuthResponseDto authenticate(String email, String password) {
        // Buscar usuario por email usando Example
        User userExample = new User();
        userExample.setEmail(email);

        Example<User> example = Example.of(userExample);

        User user = userRepository.findBy(example, query -> query.first().orElse(null));
        if (user == null) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }

        // Verificar contraseña
        if (!PasswordUtils.checkPassword(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Generar token JWT
        String jwtToken = jwtService.generateToken(user.getEmail());

        // Mapear usuario a DTO
        UserDto userDto = UserMapper.maptoUserDto(user);

        // Crear y retornar respuesta
        return AuthResponseDto.builder()
                .token(jwtToken)
                .user(userDto)
                .build();
    }

}