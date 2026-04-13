package com.badminton.booking.branch.service;

import com.badminton.booking.branch.dto.request.BranchRequest;
import com.badminton.booking.branch.dto.response.BranchListItem;
import com.badminton.booking.branch.dto.response.BranchResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BranchService {

    Page<BranchListItem> searchBranches(String keyword, Integer areaId, String status, int page, int size);

    BranchResponse getBranchById(Long id);

    BranchResponse getBranchDetail(Long id);

    BranchResponse createBranch(BranchRequest request);

    BranchResponse updateBranch(Long id, BranchRequest request);

    void deleteBranch(Long id);

    void toggleBranchStatus(Long id);
}
