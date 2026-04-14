package com.badminton.booking.court.service.impl;

import com.badminton.booking.court.repository.CourtCourtRepository;
import com.badminton.booking.domain.entity.Court;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourtServiceImpl implements com.badminton.booking.court.service.CourtService {

    private final CourtCourtRepository courtRepository;

    @Override
    public Court getById(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }

    @Override
    public List<Court> getByBranch(Long branchId) {
        return courtRepository.findByBranchIdAndIsDeletedFalse(branchId);
    }

    @Override
    public void create(Court court) {
        courtRepository.save(court);
    }

    @Override
    public void update(Long id, Court req) {
        Court c = getById(id);
        c.setName(req.getName());
        c.setType(req.getType());
        c.setStatus(req.getStatus());
        courtRepository.save(c);
    }

    @Override
    public void delete(Long id) {
        Court c = getById(id);
        c.setIsDeleted(true);
    }
}