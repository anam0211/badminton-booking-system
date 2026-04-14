
package com.badminton.booking.court.service;

import com.badminton.booking.domain.entity.Price;
import com.badminton.booking.court.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final PriceRepository repo;

    public List<Price> getByBranch(Long branchId) {
        return repo.findByBranchId(branchId);
    }

    public Price save(Price p) {
        return repo.save(p);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
