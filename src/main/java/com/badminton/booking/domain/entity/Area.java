package com.badminton.booking.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "areas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;
}
