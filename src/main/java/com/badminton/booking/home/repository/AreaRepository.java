package com.badminton.booking.home.repository;

import com.badminton.booking.domain.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {
    List<Area> findAll();

    @Query("SELECT a FROM Area a ORDER BY a.name ASC")
    List<Area> findAllOrderByName();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
