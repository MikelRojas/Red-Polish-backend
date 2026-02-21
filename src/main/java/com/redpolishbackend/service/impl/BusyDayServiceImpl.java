package com.redpolishbackend.service.impl;

import com.redpolishbackend.entity.BusyDay;
import com.redpolishbackend.repository.BusyDayRepository;
import com.redpolishbackend.service.BusyDayService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class BusyDayServiceImpl implements BusyDayService {
    private final BusyDayRepository busyDayRepository;
    private static final Logger logger = Logger.getLogger(BusyDayServiceImpl.class.getName());


    public void addBusyDay(LocalDate fecha, LocalTime hour) {
        if (!busyDayRepository.existsByDateAndHour(fecha, hour)) {
            BusyDay nuevo = new BusyDay();
            nuevo.setDate(fecha);
            nuevo.setHour(hour);
            busyDayRepository.save(nuevo);
            logger.info("Se agregó un nuevo día ocupado: " + fecha);
        } else {
            logger.info("El día " + fecha + " ya está marcado como ocupado.");
        }
    }


    @Scheduled(cron = "0 0 0 */2 * ?")
    @Transactional
    public void deleteOld() {
        LocalDate hoy = LocalDate.now();
        int eliminados = busyDayRepository.deleteByDateBefore(hoy);
        logger.info("Se eliminaron " + eliminados + " días ocupados que ya pasaron.");
    }
}
