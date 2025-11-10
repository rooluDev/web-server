package com.example.was.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 20, nullable = false)
    private String id;

    private String username;
    private String password;
    private String email;
}