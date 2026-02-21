package com.redpolishbackend.mapper;

import com.redpolishbackend.dto.CitaDto;
import com.redpolishbackend.entity.Cita;
import com.redpolishbackend.entity.Services;
import com.redpolishbackend.entity.User;

public class CitaMapper {

    public static CitaDto mapToCitaDto(Cita cita) {
        return new CitaDto(
                cita.getId(),
                cita.getDate(),
                cita.getHour(),
                cita.getState(),
                cita.getUser() != null ? cita.getUser().getId() : null,
                cita.getService() != null ? cita.getService().getId() : null
        );
    }

    public static Cita mapToCita(CitaDto dto, User user, Services service) {
        return new Cita(
                dto.getId(),
                dto.getDate(),
                dto.getHour(),
                dto.getState(),
                user,
                service
        );
    }
}