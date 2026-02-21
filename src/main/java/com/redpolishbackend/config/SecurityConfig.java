package com.redpolishbackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    //To  make new commit
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults -> {})
                .authorizeHttpRequests(auth -> auth
                        // Permitir explícitamente todas las solicitudes OPTIONS (preflight CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/users/register", "/api/users/sign_in/**","/api/forgotPassword/**"
                        ,"/api/categories/**", "/api/products/**", "/api/services/get-services",
                                "/api/products/get_all", "/api/products/get/**", "/api/promotions",
                                "/api/services/get_all", "/api/services/get/**","/api/promotions/{id}",
                                //Citas
                                "/api/citas/get_all/**",
                                "/api/citas/busy/**",
                                "/api/citas/get/**"
                        ).permitAll()
                        .requestMatchers("/api/users/update/**",
                                "/api/products/create/**", "/api/products/delete/**",
                                "/api/products/update/**","/api/cart/**",
                                "/api/services/create/**", "/api/services/delete/**", "/api/services/update/**",                      // GET de promociones públicas
                                "/api/promotions/**", "/api/payments/**",
                                "/api/citas/add/**", "/api/payments/sinpe/**", "api/citas/update-state/", "api/citas/cancel/**", "api/citas/cancel/**","api/citas/update-state/",
                                "api/citas/admin/all/**", "api/citas/history/**",
                                "/api/promotions/send/**", "/api/citas/busy_day/add/**"
                                ).authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
