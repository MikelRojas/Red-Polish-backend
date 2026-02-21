package com.redpolishbackend.service;

public interface NotificacionService {
    void enviarCorreo(String destino, String asunto, String cuerpo);
    void enviarSms(String numero, String mensaje);
}
