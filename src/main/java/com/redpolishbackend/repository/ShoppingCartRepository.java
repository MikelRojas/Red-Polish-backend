package com.redpolishbackend.repository;

import com.redpolishbackend.entity.Product;
import com.redpolishbackend.entity.ShoppingCart;
import com.redpolishbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface  ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    Optional<ShoppingCart> findByUserAndProduct(User user, Product product);

    List<ShoppingCart> findByUser(User user);

    Optional<ShoppingCart> findById(Long id);

}
