package com.redpolishbackend.repository;

import com.redpolishbackend.entity.Product;
import com.redpolishbackend.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
}
