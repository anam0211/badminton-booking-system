package com.badminton.booking.branch.service;

import com.badminton.booking.common.exception.AccessDeniedCustomException;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import com.badminton.booking.domain.entity.Branch;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.domain.repository.UserRepository;
import com.badminton.booking.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BranchSecurityService {

    private final UserRepository userRepository;

    public void ensureUserCanManageBranch(Long branchId) {
        User currentUser = getCurrentUserOrThrow();
        ensureUserCanManageBranch(currentUser, branchId);
    }

    public void ensureUserCanManageBranch(User user, Long branchId) {
        if (user.getRole().getName().equals("ADMIN")) {
            return;
        }

        if (user.getRole().getName().equals("BRANCH_ADMIN")) {
            if (user.getManagedBranch() == null || !user.getManagedBranch().getId().equals(branchId)) {
                throw new AccessDeniedCustomException("Bạn chỉ được quản lý chi nhánh của mình.");
            }
            return;
        }

        throw new AccessDeniedCustomException("Bạn không có quyền quản lý chi nhánh.");
    }

    public Optional<Branch> getManagedBranchForCurrentUser() {
        User currentUser = getCurrentUserOrThrow();
        if (currentUser.getRole().getName().equals("BRANCH_ADMIN")) {
            return Optional.ofNullable(currentUser.getManagedBranch());
        }
        return Optional.empty();
    }

    public boolean canUserAccessBranch(Long branchId) {
        User currentUser = getCurrentUserOrThrow();
        if (currentUser.getRole().getName().equals("ADMIN")) {
            return true;
        }
        if (currentUser.getRole().getName().equals("BRANCH_ADMIN")) {
            return currentUser.getManagedBranch() != null && currentUser.getManagedBranch().getId().equals(branchId);
        }
        return false;
    }

    private User getCurrentUserOrThrow() {
        String email = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new AccessDeniedCustomException("Bạn chưa đăng nhập."));
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng"));
    }
}
