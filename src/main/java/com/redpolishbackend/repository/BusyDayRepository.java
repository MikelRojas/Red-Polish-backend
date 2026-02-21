package com.redpolishbackend.repository;

import com.redpolishbackend.entity.BusyDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface BusyDayRepository extends JpaRepository<BusyDay, Long> {
    boolean existsByDateAndHour(LocalDate date, LocalTime hour);

    @Modifying
    @Query("DELETE FROM BusyDay b WHERE b.date < :fecha")
    int deleteByDateBefore(LocalDate fecha);
}
