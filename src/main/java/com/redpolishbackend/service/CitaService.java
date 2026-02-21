package com.redpolishbackend.service;

import com.redpolishbackend.dto.CitaDto;
import com.redpolishbackend.dto.HoraOcupadaDTO;
import com.redpolishbackend.entity.Cita;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {
    CitaDto createAppointment(CitaDto citaDto);
    List<HoraOcupadaDTO> getBusyHours(Long servicio_id, LocalDate fecha);
    List<HoraOcupadaDTO> getBusyAllHours(LocalDate fecha);
    void eliminarCitasCanceladas();
    List<Cita> getAppointmentsByUserId(Long id);
    List<Cita> getAll();
    CitaDto updateAppointmentState(Long id, String newState);

    List<Cita> getAppointmentsHistory(String email);
}