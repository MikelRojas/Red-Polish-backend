package com.redpolishbackend.service.impl;

import com.redpolishbackend.dto.*;
import com.redpolishbackend.entity.*;
import com.redpolishbackend.exception.ResourceNotFoundException;
import com.redpolishbackend.mapper.CitaMapper;
import com.redpolishbackend.repository.BusyDayRepository;
import com.redpolishbackend.repository.CitaRepository;
import com.redpolishbackend.repository.ServiceRepository;
import com.redpolishbackend.repository.UserRepository;
import com.redpolishbackend.service.CitaService;
import com.redpolishbackend.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private static final Logger logger = Logger.getLogger(CitaServiceImpl.class.getName());

    private final CitaRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final NotificacionService notificacionService;
    private  final UserRepository userRepository;
    private final CitaRepository citaRepository;
    private final BusyDayRepository busyDayRepository;

    @Override
    public CitaDto createAppointment(CitaDto citaDto) {
        User user = userRepository.findById(citaDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));;
        System.out.println(citaDto);
        Services service = serviceRepository.findByServiceId(citaDto.getServiceId());


        Cita appointment = CitaMapper.mapToCita(citaDto,user,service);
        Cita savedAppointment = appointmentRepository.save(appointment);
        return CitaMapper.mapToCitaDto(savedAppointment);
    }


    public void cancelAppointment(Long Id) {
        Cita appointment = appointmentRepository.findById(Id).
                orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        appointmentRepository.delete(appointment);
    }

    public List<HoraOcupadaDTO> getBusyHours(Long ServiceId, LocalDate fecha) {
        Services service = serviceRepository.findById(ServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        List<Cita> citas = appointmentRepository.findByServiceIdAndDate(ServiceId, fecha);

        List<HoraOcupadaDTO> horasOcupadas = citas.stream()
                .map(cita -> new HoraOcupadaDTO(
                        cita.getDate(),
                        cita.getHour(),
                        service.getDuration()
                ))
                .toList();

        return horasOcupadas;
    }

    @Override
    public List<HoraOcupadaDTO> getBusyAllHours(LocalDate fecha) {
        List<Cita> citas = appointmentRepository.findByDateCancel(fecha);
        List<BusyDay> busyDays = busyDayRepository.findAll();

        List<HoraOcupadaDTO> horasOcupadas = new ArrayList<>(
                citas.stream()
                        .map(cita -> new HoraOcupadaDTO(
                                cita.getDate(),
                                cita.getHour(),
                                cita.getService().getDuration()
                        ))
                        .toList()
        );

        for (BusyDay busyDay : busyDays) {
            horasOcupadas.add(new HoraOcupadaDTO(busyDay.getDate(), busyDay.getHour(), "60"));
        }

        return horasOcupadas;
    }


    @Override
    public void eliminarCitasCanceladas() {
        appointmentRepository.deleteCancelAppointments();
        logger.info("Se han eliminado todas las citas canceladas");
    }

    @Override
    public List<Cita> getAppointmentsByUserId(Long userId) {
        return new ArrayList<>(appointmentRepository.findByUserId(userId));
    }

    @Override
    public List<Cita> getAll() {
        List<Cita> appointments = appointmentRepository.findAllAppointments();
        return new ArrayList<>(appointments);
    }

    @Override
    public CitaDto updateAppointmentState(Long id, String newState) {
        Cita appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getState().equals(newState.toUpperCase())) {
            throw new IllegalArgumentException("Estado no válido: " + newState);
        }

        appointment.setState(newState);
        Cita updatedAppointment = appointmentRepository.save(appointment);
        return CitaMapper.mapToCitaDto(updatedAppointment);
    }

    @Override
    public List<Cita> getAppointmentsHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        LocalDate tresMesesAtras = LocalDate.now().minusMonths(3);

        return citaRepository.findByUserIdAndDateAfter(user.getId(), tresMesesAtras);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void programarEliminacionCitasCanceladas() {
        eliminarCitasCanceladas();
        LocalDate fechaLimite = LocalDate.now().minusMonths(2);
        int eliminadas = appointmentRepository.deleteOldAppointments(fechaLimite);
        logger.info("Se eliminaron " + eliminadas + " citas con más de 2 meses de antigüedad.");
    }

    @Scheduled(cron = "0 0 6 * * ?") // A las 6:00 a.m. todos los días
    @Transactional
    public void sendRemindersForTomorrow() {
        LocalDate mañana = LocalDate.now().plusDays(1);

        List<String> estadosOK = List.of("PENDIENTE", "CONFIRMADA", "CANCELADA PENDIENTE");

        List<Cita> citas = citaRepository.findByDateAndStateIn(mañana, estadosOK);

        for (Cita cita : citas) {
            String correo = cita.getUser().getEmail();
            String asunto = "Recordatorio: cita mañana a las " +
                    cita.getHour().format(DateTimeFormatter.ofPattern("HH:mm"));
            String cuerpo = String.format("""
        ¡Hola %s! 🌟

        Te recordamos tu cita programada para mañana en RedPolish:

        🛎️  Servicio: %s
        📅  Fecha: %s
        ⏰  Hora: %s

        📍  Ubicación: Cedral, Ciudad Quesada
        📞  Contacto: +506 8358-2929

        Si necesitas reprogramar o cancelar, contáctanos con antelación.

        ¡Gracias por confiar en nosotros! 💙
        """,
                    cita.getUser().getName(),
                    cita.getService().getName(),
                    cita.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    cita.getHour().format(DateTimeFormatter.ofPattern("HH:mm"))
            );

            notificacionService.enviarCorreo(correo, asunto, cuerpo);
            logger.info("Enviado recordatorio a " + correo + " para cita del " + cita.getDate() + " a las " + cita.getHour());
        }
    }

}
