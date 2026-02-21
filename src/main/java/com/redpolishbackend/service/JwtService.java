package com.redpolishbackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRol(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String userEmail) {
        return generateToken(new HashMap<>(), userEmail);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            String userEmail
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userEmail)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, String email) {
        final String tokenEmail = extractEmail(token).trim();
        email = email.trim();

        System.out.println("Token recibido: " + token);
        System.out.println("Email extraído del token: " + tokenEmail);
        System.out.println("Email esperado: " + email);
        System.out.println("Clave secreta usada: " + secretKey);

        return (tokenEmail.equals(email)) && !isTokenExpired(token);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String tokenEmail = extractEmail(token).trim();
        String userEmail = userDetails.getUsername().trim();

        System.out.println("Token recibido: " + token);
        System.out.println("Email extraído del token: " + tokenEmail);
        System.out.println("Email del UserDetails: " + userDetails.getUsername());
        System.out.println("Clave secreta usada: " + secretKey);

        return (tokenEmail.equals(userEmail)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
    }
}