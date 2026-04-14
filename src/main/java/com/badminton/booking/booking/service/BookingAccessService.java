package com.badminton.booking.booking.service;

import com.badminton.booking.booking.repository.BookingDetailRepository;
import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.domain.entity.Booking;
import com.badminton.booking.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingAccessService {

    private final BookingDetailRepository bookingDetailRepository;

    public boolean canViewBooking(User viewer, Booking booking) {
        if (viewer == null || booking == null || booking.getUser() == null) {
            return false;
        }

        return isBookingOwner(viewer, booking)
                || isSystemAdmin(viewer)
                || canManageBookingAsAdmin(viewer, booking);
    }

    public boolean canManageBookingAsAdmin(User viewer, Booking booking) {
        if (viewer == null || booking == null) {
            return false;
        }

        if (isSystemAdmin(viewer)) {
            return true;
        }

        if (!isBranchAdmin(viewer)) {
            return false;
        }

        Long managedBranchId = getManagedBranchId(viewer);
        return managedBranchId != null
                && bookingDetailRepository.existsByBooking_IdAndCourt_Branch_Id(booking.getId(), managedBranchId);
    }

    public boolean hasAdminAccess(User user) {
        return isSystemAdmin(user) || isBranchAdmin(user);
    }

    public boolean isSystemAdmin(User user) {
        return hasRole(user, RoleName.ADMIN);
    }

    public boolean isBranchAdmin(User user) {
        return hasRole(user, RoleName.BRANCH_ADMIN);
    }

    public Long getManagedBranchId(User user) {
        return user != null && user.getManagedBranch() != null
                ? user.getManagedBranch().getId()
                : null;
    }

    private boolean isBookingOwner(User viewer, Booking booking) {
        return viewer != null
                && booking != null
                && booking.getUser() != null
                && viewer.getId() != null
                && viewer.getId().equals(booking.getUser().getId());
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user != null
                && user.getRole() != null
                && user.getRole().getName() != null
                && roleName.name().equalsIgnoreCase(user.getRole().getName());
    }
}
