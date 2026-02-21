package com.redpolishbackend.controller;

import com.redpolishbackend.dto.MailBody;
import com.redpolishbackend.entity.ForgotPassword;
import com.redpolishbackend.entity.User;
import com.redpolishbackend.repository.ForgotPasswordRepository;
import com.redpolishbackend.repository.UserRepository;
import com.redpolishbackend.service.EmailService;
import com.redpolishbackend.utils.ChangePassword;
import com.redpolishbackend.utils.PasswordUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

@AllArgsConstructor
@RestController
@RequestMapping("/api/forgotPassword")
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ForgotPasswordRepository forgotPasswordRepository;

    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<String> verifyMail(@PathVariable String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));

        int otp = otpGenerator();
        MailBody mailBody = MailBody.builder()
                .to(email)
                .text("Esta es tu contraseña de un solo uso: " + otp)
                .subject("Peticion de olvide mi contraseña")
                .build();

        ForgotPassword fpOld = forgotPasswordRepository.findByUser(user).orElse(null);
        if(fpOld != null) {
            forgotPasswordRepository.deleteOtpById(fpOld.getFpid());
        }

        ForgotPassword fp = ForgotPassword.builder()
                .otp(otp)
                .expiration(new Date(System.currentTimeMillis()+70*1000))
                .user(user)
                .build();
        emailService.sendSimpleMail(mailBody);
        forgotPasswordRepository.save(fp);

        return ResponseEntity.ok("Se ha enviado el email de verifycacion");
    }

    @PostMapping("/verifyOtp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp,@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        ForgotPassword fp = forgotPasswordRepository.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new RuntimeException("Clave dynamica invalida para el email: " + email));

        if (fp.getExpiration().before(Date.from(Instant.now()))){
            forgotPasswordRepository.deleteOtpById(fp.getFpid());
            return new ResponseEntity<>("La clave dinamica expiro!!", HttpStatus.EXPECTATION_FAILED);
        }

        forgotPasswordRepository.deleteOtpById(fp.getFpid());

        return ResponseEntity.ok("Clave dinamica verificada");
    }

    @PostMapping("/changePassword/{email}")
    public ResponseEntity<String> changePasaword(@RequestBody ChangePassword changePassword, @PathVariable String email) {
        if(!Objects.equals(changePassword.password(), changePassword.repeatPassword())){
            return new ResponseEntity<>("Por favor ingrese la contraseña otra vez!", HttpStatus.EXPECTATION_FAILED);
        }

        String encodedPassword = PasswordUtils.hashPassword(changePassword.password());
        userRepository.updatePassword(email, encodedPassword);

        return ResponseEntity.ok("Clave actualizada correctamente!");
    }

    private Integer otpGenerator(){
        Random random = new Random();
        return random.nextInt(100_000,999_999);
    }
}
