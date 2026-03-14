package com.badminton.booking.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branch_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
}