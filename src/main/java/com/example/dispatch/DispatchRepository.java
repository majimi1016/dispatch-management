package com.example.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    List<Dispatch> findAllByOrderByDispatchDateAsc();

    // 指定した日の配達を、昼→夜、会社名の順、登録順に取得
    List<Dispatch> findByDispatchDateOrderByShiftAscCompanyNameAscIdAsc(LocalDate dispatchDate);

    // 集計結果の1行(日付・会社・車格ごとの台数)
    interface DailyCount {
        LocalDate getDispatchDate();
        Long getCompanyId();
        VehicleSize getVehicleSize();
        Long getTotal();
    }

    // 期間内の台数を、日付・会社・車格ごとに合計する(shift が null なら昼夜合計)
    @Query("""
            select d.dispatchDate as dispatchDate,
                   d.company.id   as companyId,
                   d.vehicleSize  as vehicleSize,
                   sum(d.quantity) as total
            from Dispatch d
            where d.dispatchDate between :start and :end
              and (:shift is null or d.shift = :shift)
            group by d.dispatchDate, d.company.id, d.vehicleSize
            """)
    List<DailyCount> sumDaily(@Param("start") LocalDate start,
                              @Param("end") LocalDate end,
                              @Param("shift") Shift shift);
}