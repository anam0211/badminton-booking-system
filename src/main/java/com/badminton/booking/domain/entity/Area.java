package com.badminton.booking.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "areas")
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "area", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Branch> branches = new ArrayList<>();

    @Transient
    public int getBranchCount() {
        return branches != null ? branches.size() : 0;
    }

}