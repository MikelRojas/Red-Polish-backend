package com.redpolishbackend.service;

import java.time.LocalDate;
import java.time.LocalTime;

public interface BusyDayService {
    void addBusyDay(LocalDate fecha, LocalTime hour);
}
