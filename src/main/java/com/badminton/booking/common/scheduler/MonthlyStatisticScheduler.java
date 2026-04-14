package com.badminton.booking.common.scheduler;

import com.badminton.booking.dashboard.service.impl.DashboardServiceImpl;
import com.badminton.booking.domain.entity.Branch;
import com.badminton.booking.domain.entity.MonthlyStatistic;
import com.badminton.booking.dashboard.repository.BranchRepository;
import com.badminton.booking.dashboard.repository.MonthlyStatisticRepository;
import com.badminton.booking.dashboard.dto.response.BranchOverviewResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MonthlyStatisticScheduler {

    BranchRepository branchRepository;
    MonthlyStatisticRepository monthlyStatisticRepository;
    DashboardServiceImpl dashboardServiceImp;

    /**
     * Cron expression: Giây Phút Giờ Ngày Tháng Thứ
     * "0 5 0 1 * ?" -> Chạy vào lúc 00:05:00 ngày 1 hàng tháng
     */
    @Scheduled(cron = "0 5 0 1 * ?")
    @Transactional
    public void generateMonthlyStatistics() {
        log.info("============= BẮT ĐẦU JOB CHỐT SỔ THỐNG KÊ THÁNG =============");

        // 1. Xác định tháng cần thống kê (Chạy ngày 1/5 thì sẽ tính cho tháng 4)
        YearMonth previousMonth = YearMonth.from(LocalDate.now()).minusMonths(1);
        int month = previousMonth.getMonthValue();
        int year = previousMonth.getYear();

        log.info("Đang xử lý dữ liệu cho Tháng {}/{}", month, year);

        List<Branch> branches = branchRepository.findAll();

        for (Branch branch : branches) {
            try {
                BranchOverviewResponse overview = dashboardServiceImp.realtimeOverviewByMonth(branch.getId(), year, month, null);

                MonthlyStatistic statistic = new MonthlyStatistic();
                statistic.setBranch(branch);
                statistic.setMonth(month);
                statistic.setYear(year);
                statistic.setTotalRevenue(overview.getTotalRevenue());
                statistic.setTotalBookings(overview.getBookingSummary().getTotalBookings());
                statistic.setCompletedBookings(overview.getBookingSummary().getCompletedBookings());
                statistic.setCancelledBookings(overview.getBookingSummary().getCancelledBookings());


                monthlyStatisticRepository.save(statistic);

                log.info("-> Chốt sổ thành công cho cơ sở ID: {}", branch.getId());

            } catch (Exception e) {
                log.error("-> LỖI khi chốt sổ cơ sở ID {}: {}", branch.getId(), e.getMessage());
            }
        }
        log.info("============= KẾT THÚC JOB CHỐT SỔ THÁNG =============");
    }
}