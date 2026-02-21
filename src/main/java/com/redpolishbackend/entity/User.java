package com.redpolishbackend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Usuarios")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="nombre")
    private String name;
    @Column(name="apellido")
    private String last_name;
    @Column(name="correo_electronico", nullable = false, unique = true)
    private String email;
    @Column(name="contraseña")
    private String password;
    @Column(name="rol")
    private String rol;

    @OneToOne(mappedBy = "user")
    private ForgotPassword forgotPassword;

    public User(Long id, String name, String last_name, String email, String password, String rol) {
        this.id = id;
        this.name = name;
        this.last_name = last_name;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }
}
