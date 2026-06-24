package com.example.thetunais4joteamproject.domain.user.entity;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "member")
public class Member extends BaseEntity {

    private static final String DEFAULT_ROLE = "USER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    protected Member() {
    }

    private Member(String email, String password, String name, String phoneNumber, String role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public static Member create(String email, String password, String name, String phoneNumber) {
        return new Member(email, password, name, phoneNumber, DEFAULT_ROLE);
    }

    public Long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}