package com.redpolishbackend.repository;

import com.redpolishbackend.entity.ForgotPassword;
import com.redpolishbackend.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {

    @Query("select fp from ForgotPassword fp where fp.otp = ?1 and fp.user = ?2")
    Optional<ForgotPassword> findByOtpAndUser(Integer otp, User user);

    @Query("select fp from ForgotPassword fp where fp.user = ?1")
    Optional<ForgotPassword> findByUser(User user);


    @Modifying
    @Transactional
    @Query("delete from ForgotPassword fp where fp.fpid = ?1")
    void deleteOtpById(Integer fpid);

}
