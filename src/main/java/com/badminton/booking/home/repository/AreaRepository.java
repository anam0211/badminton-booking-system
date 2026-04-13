package com.badminton.booking.home.repository;

import com.badminton.booking.domain.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {
    List<Area> findAll();
}
