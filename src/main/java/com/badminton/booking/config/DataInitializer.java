package com.badminton.booking.config;

import com.badminton.booking.common.enums.BranchStatus;
import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.common.enums.UserStatus;
import com.badminton.booking.domain.entity.*;
import com.badminton.booking.domain.repository.RoleRepository;
import com.badminton.booking.domain.repository.UserRepository;
import com.badminton.booking.booking.repository.CourtRepository;
import com.badminton.booking.booking.repository.TimeSlotRepository;
import com.badminton.booking.dashboard.repository.BranchRepository;
import com.badminton.booking.home.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AreaRepository areaRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final CourtRepository courtRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initUsers();
        initSampleData();
    }

    private void initRoles() {
        roleRepository.findByName(RoleName.ADMIN.name())
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN.name()).build()));

        roleRepository.findByName(RoleName.CUSTOMER.name())
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.CUSTOMER.name()).build()));

        roleRepository.findByName(RoleName.BRANCH_ADMIN.name())
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.BRANCH_ADMIN.name()).build()));
    }

    private void initUsers() {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN.name()).orElseThrow();
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER.name()).orElseThrow();
        Role branchAdminRole = roleRepository.findByName(RoleName.BRANCH_ADMIN.name()).orElseThrow();

        if (!userRepository.existsByEmailIgnoreCase("admin@demo.local")) {
            userRepository.save(User.builder()
                    .email("admin@demo.local")
                    .password(passwordEncoder.encode("123456"))
                    .fullName("System Admin")
                    .role(adminRole)
                    .status(UserStatus.ACTIVE)
                    .isDeleted(false)
                    .build());
        }

        if (!userRepository.existsByEmailIgnoreCase("customer@demo.local")) {
            userRepository.save(User.builder()
                    .email("customer@demo.local")
                    .password(passwordEncoder.encode("123456"))
                    .fullName("Demo Customer")
                    .role(customerRole)
                    .status(UserStatus.ACTIVE)
                    .isDeleted(false)
                    .build());
        }

        if (!userRepository.existsByEmailIgnoreCase("branchadmin@demo.local")) {
            userRepository.save(User.builder()
                    .email("branchadmin@demo.local")
                    .password(passwordEncoder.encode("123456"))
                    .fullName("Demo Branch Admin")
                    .role(branchAdminRole)
                    .status(UserStatus.ACTIVE)
                    .isDeleted(false)
                    .build());
        }
    }

    private void initSampleData() {
        if (!timeSlotRepository.findAll().isEmpty()) {
            return;
        }

        Area area1 = areaRepository.save(Area.builder().name("Quận 1").build());
        Area area2 = areaRepository.save(Area.builder().name("Quận 3").build());
        Area area3 = areaRepository.save(Area.builder().name("Quận 7").build());
        Area area4 = areaRepository.save(Area.builder().name("Quận Bình Thạnh").build());

        List<TimeSlot> slots = List.of(
                timeSlotRepository.save(TimeSlot.builder().slotName("06:00 - 07:00").startTime(LocalTime.of(6, 0)).endTime(LocalTime.of(7, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("07:00 - 08:00").startTime(LocalTime.of(7, 0)).endTime(LocalTime.of(8, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("08:00 - 09:00").startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(9, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("09:00 - 10:00").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("10:00 - 11:00").startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("11:00 - 12:00").startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(12, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("12:00 - 13:00").startTime(LocalTime.of(12, 0)).endTime(LocalTime.of(13, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("13:00 - 14:00").startTime(LocalTime.of(13, 0)).endTime(LocalTime.of(14, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("14:00 - 15:00").startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(15, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("15:00 - 16:00").startTime(LocalTime.of(15, 0)).endTime(LocalTime.of(16, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("16:00 - 17:00").startTime(LocalTime.of(16, 0)).endTime(LocalTime.of(17, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("17:00 - 18:00").startTime(LocalTime.of(17, 0)).endTime(LocalTime.of(18, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("18:00 - 19:00").startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(19, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("19:00 - 20:00").startTime(LocalTime.of(19, 0)).endTime(LocalTime.of(20, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("20:00 - 21:00").startTime(LocalTime.of(20, 0)).endTime(LocalTime.of(21, 0)).build()),
                timeSlotRepository.save(TimeSlot.builder().slotName("21:00 - 22:00").startTime(LocalTime.of(21, 0)).endTime(LocalTime.of(22, 0)).build())
        );

        Branch branch1 = createBranch("Badminton Center Quận 1", "123 Đồng Khởi, Quận 1, TP.HCM",
                area1, BranchStatus.OPEN, 4.8f, 156,
                "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=800&q=80",
                "https://images.unsplash.com/photo-1521537634581-0dced2fee2ef?w=800&q=80",
                slots, new BigDecimal("60000"), new BigDecimal("80000"));

        Branch branch2 = createBranch("Sân Cầu Lông Bình Thạnh", "45 Nguyễn Văn Đậu, Bình Thạnh, TP.HCM",
                area4, BranchStatus.OPEN, 4.5f, 98,
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800&q=80",
                "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=800&q=80",
                slots, new BigDecimal("55000"), new BigDecimal("75000"));

        Branch branch3 = createBranch("Badminton VIP Quận 3", "78 Nguyễn Thiện Thuật, Quận 3, TP.HCM",
                area2, BranchStatus.OPEN, 4.6f, 72,
                "https://images.unsplash.com/photo-1521537634581-0dced2fee2ef?w=800&q=80",
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800&q=80",
                slots, new BigDecimal("70000"), new BigDecimal("90000"));

        Branch branch4 = createBranch("Sân Cầu Lông Quận 7", "12 Đường số 3, Quận 7, TP.HCM",
                area3, BranchStatus.OPEN, 4.3f, 45,
                "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=800&q=80",
                "https://images.unsplash.com/photo-1521537634581-0dced2fee2ef?w=800&q=80",
                slots, new BigDecimal("50000"), new BigDecimal("70000"));
    }

    private Branch createBranch(String name, String address, Area area, BranchStatus status,
                                 Float avgRating, Integer totalReviews,
                                 String image1, String image2,
                                 List<TimeSlot> slots, BigDecimal weekdayPrice, BigDecimal weekendPrice) {

        Branch branch = Branch.builder()
                .name(name)
                .address(address)
                .area(area)
                .status(status)
                .averageRating(avgRating)
                .totalReviews(totalReviews)
                .isDeleted(false)
                .build();

        branch.getBranchImages().add(BranchImage.builder().branch(branch).imageUrl(image1).build());
        if (image2 != null) {
            branch.getBranchImages().add(BranchImage.builder().branch(branch).imageUrl(image2).build());
        }

        for (TimeSlot slot : slots) {
            BigDecimal price = isWeekendSlot(slot) ? weekendPrice : weekdayPrice;
            branch.getPrices().add(Price.builder()
                    .branch(branch)
                    .timeSlot(slot)
                    .courtType("Standard")
                    .price(price)
                    .build());
        }

        return branchRepository.save(branch);
    }

    private boolean isWeekendSlot(TimeSlot slot) {
        int hour = slot.getStartTime().getHour();
        return (hour >= 17 && hour < 22);
    }
}
