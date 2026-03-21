package com.badminton.booking.domain.entity;

import com.badminton.booking.common.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 255)
    @NotNull
    @Column(nullable = false)
    private String title;

    @NotNull
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;
}