package com.bankflow.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Role Entity representing user authorization authority.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 30)
    private RoleType name;

    public Role(RoleType name) {
        this.name = name;
    }
}
