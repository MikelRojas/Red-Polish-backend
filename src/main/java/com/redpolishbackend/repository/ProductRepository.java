package com.redpolishbackend.repository;

import com.redpolishbackend.entity.Category;
import com.redpolishbackend.entity.Product;
import com.redpolishbackend.entity.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByPromotionId(Long promotionId);
}
