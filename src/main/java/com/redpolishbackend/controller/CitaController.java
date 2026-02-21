package com.redpolishbackend.controller;

import com.redpolishbackend.dto.*;
import com.redpolishbackend.entity.Cita;
import com.redpolishbackend.entity.User;
import com.redpolishbackend.repository.UserRepository;
import com.redpolishbackend.service.BusyDayService;
import com.redpolishbackend.service.CitaService;
import com.redpolishbackend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private static final Logger logger = Logger.getLogger(CitaController.class.getName());

    private final CitaService appointmentService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BusyDayService busyDayService;

    @GetMapping("/busy/service")
    public ResponseEntity<List<HoraOcupadaDTO>> getBusyHoursPerServices(
            @RequestParam Long servicio_id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<HoraOcupadaDTO> busy = appointmentService.getBusyHours(servicio_id, fecha);
        return ResponseEntity.ok(busy);
    }

    @GetMapping("/history/{email}")
    public ResponseEntity<List<Cita>> getAppointmentHistory(
            @PathVariable("email") String email,
            @RequestHeader("Authorization") String token) {
        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Cita> appointments = appointmentService.getAppointmentsHistory(email);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/admin/all/{email}")
    public ResponseEntity<List<Cita>> getAllAppointmentsAdmin(
            @PathVariable("email") String email,
            @RequestHeader("Authorization") String tokenHeader,
            HttpServletRequest request
    ) {
        String jwtToken = tokenHeader.startsWith("Bearer ")
                ? tokenHeader.substring(7)
                : tokenHeader;

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!"Administrador".equalsIgnoreCase(user.getRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Cita> todasCitas = appointmentService.getAll();
        return ResponseEntity.ok(todasCitas);
    }

    @GetMapping("/busy")
    public ResponseEntity<List<HoraOcupadaDTO>> getBusyHoursPerDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<HoraOcupadaDTO> ocupadas = appointmentService.getBusyAllHours(fecha);
        return ResponseEntity.ok(ocupadas);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<List<Cita>> getAppointmentsById(@PathVariable Long id) {
        List<Cita> appointments = appointmentService.getAppointmentsByUserId(id);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/get_all")
    public ResponseEntity<List<Cita>> getAll() {
        List<Cita> appointments = appointmentService.getAll();
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/update-state/{id}/{email}/{new_state}")
    public ResponseEntity<CitaDto> updateAppointmentState(
            @PathVariable("email") String email,
            @PathVariable("id") Long id,
            @PathVariable("new_state") String new_state,
            @RequestHeader("Authorization") String token) {
        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CitaDto updatedAppointment = appointmentService.updateAppointmentState(id, new_state);

        return ResponseEntity.ok(updatedAppointment);
    }

    @PutMapping("/cancel/{id}/{email}")
    public ResponseEntity<CitaDto> cancelAppointment(
            @PathVariable("email") String email,
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String token
    ) {
        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }
        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            CitaDto canceled = appointmentService.updateAppointmentState(id, "CANCELADA PENDIENTE");
            return ResponseEntity.ok(canceled);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/cancel_admin/{id}/{email}")
    public ResponseEntity<CitaDto> cancelAppointmentAdmin(
            @PathVariable("email") String email,
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String token
    ) {
        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }
        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!isAdmin(jwtToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            CitaDto canceled = appointmentService.updateAppointmentState(id, "CANCELADA EXITOSA");
            return ResponseEntity.ok(canceled);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private boolean isAdmin(String token) {
        try {
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            return "Administrador".equalsIgnoreCase(user.getRol());
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/busy_day/add/{email}")
    public ResponseEntity<String> agregarDiaOcupado(
            @RequestHeader("Authorization") String token,
            @PathVariable("email") String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hour
    ) {

        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido");
        }

        try {
            busyDayService.addBusyDay(fecha, hour);
            return ResponseEntity.ok("Día ocupado registrado exitosamente: " + fecha);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar el día ocupado");
        }
    }

}
