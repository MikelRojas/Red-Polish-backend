package com.redpolishbackend.service.impl;

import com.redpolishbackend.service.NotificacionService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    private final JavaMailSender mailSender;

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public NotificacionServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarCorreo(String destino, String asunto, String cuerpo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destino);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);

        mailSender.send(mensaje);
        System.out.println("Correo enviado a: " + destino);
    }

    @Override
    public void enviarSms(String numero, String mensaje) {
        Message.creator(
                new PhoneNumber(numero),     // Destino
                new PhoneNumber(twilioPhoneNumber), // Origen (Twilio)
                mensaje
        ).create();

        System.out.println("SMS enviado a: " + numero);
    }
}