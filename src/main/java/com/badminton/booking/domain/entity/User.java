package com.badminton.booking.domain.entity;

import com.badminton.booking.common.base.BaseEntity;
import com.badminton.booking.common.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(nullable = false, unique = true)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(nullable = false)
    private String password;

    @Size(max = 100)
    @NotNull
    @Column(nullable = false, length = 100)
    private String fullName;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    @Size(max = 500)
    @Column(length = 500)
    private String avatarUrl;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "managed_area_id")
    private Area managedArea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

}