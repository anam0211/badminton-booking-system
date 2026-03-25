package com.badminton.booking.booking.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badminton.booking.domain.entity.Branch;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
}