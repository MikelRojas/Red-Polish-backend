package com.redpolishbackend.repository;

import java.util.List;
import java.util.Optional;

import com.redpolishbackend.entity.Cita;
import com.redpolishbackend.entity.Services;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Services, Long> {

    @Query("SELECT s FROM Services s WHERE s.id = :serviceId")
    Services findByServiceId(Long serviceId);

    List<Services> findByPromotionId(Long promotionId);
}
