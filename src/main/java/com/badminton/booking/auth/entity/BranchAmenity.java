package com.badminton.booking.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branch_amenities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "amenity_name", nullable = false, length = 100)
    private String amenityName;
}