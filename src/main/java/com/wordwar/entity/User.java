package com.wordwar.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) private String username;
    @Column(unique = true, nullable = false) private String email;
    @Column(nullable = false) private String password;

    private int totalScore = 0;
    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private String role = "PLAYER";
    private LocalDateTime createdAt = LocalDateTime.now();
}
