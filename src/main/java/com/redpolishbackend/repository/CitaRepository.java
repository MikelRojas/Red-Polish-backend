package com.redpolishbackend.repository;

import com.redpolishbackend.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByServiceIdAndDate(Long serviceId, LocalDate date);

    @Query("""
        SELECT c FROM Cita c
        WHERE c.state != 'CANCELADA EXITOSA'
    """)
    List<Cita> findByDateCancel(LocalDate date);


    List<Cita> findByUserId(Long userId);

    List<Cita> findByUserIdAndDateAfter(Long userId, LocalDate fechaDesde);

    @Modifying
    @Query("DELETE FROM Cita c WHERE c.state = 'CANCELADA EXITOSA'")
    void deleteCancelAppointments();

    @Query("SELECT c FROM Cita c")
    List<Cita> findAllAppointments();

    Optional<Cita> findByDateAndHourAndUserIdAndServiceIdAndState(LocalDate date, LocalTime hour, Long userId, Long serviceId, String state);

    @Modifying
    @Query("DELETE FROM Cita c WHERE c.date < :fechaLimite")
    int deleteOldAppointments(LocalDate fechaLimite);

    @Query("""
        SELECT c FROM Cita c
        WHERE c.date = :fecha
        AND c.state IN :estados
    """)
    List<Cita> findByDateAndStateIn(LocalDate fecha, List<String> estados);

    boolean existsByUserIdAndState(Long userId, String state);


}