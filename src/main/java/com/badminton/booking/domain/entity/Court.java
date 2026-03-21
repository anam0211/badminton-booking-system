package com.badminton.booking.domain.entity;

import com.badminton.booking.common.base.BaseEntity;
import com.badminton.booking.common.enums.CourtStatus;
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
@Table(name = "courts")
public class Court extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 100)
    @Column(length = 100)
    private String type;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CourtStatus status = CourtStatus.AVAILABLE;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

}