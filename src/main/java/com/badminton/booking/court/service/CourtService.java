package com.badminton.booking.court.service;

import com.badminton.booking.domain.entity.Court;

import java.util.List;

public interface CourtService {

    Court getById(Long id);

    List<Court> getByBranch(Long branchId);

    void create(Court court);

    void update(Long id, Court court);

    void delete(Long id);
}