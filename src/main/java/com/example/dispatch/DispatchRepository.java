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

    // ===== 月別まとめ用 =====
    interface DailyCount {
        LocalDate getDispatchDate();
        Long getCompanyId();
        VehicleSize getVehicleSize();
        Long getTotal();
    }

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

    // ===== 年別まとめ用(↓ 追加) =====
    interface MonthlyCount {
        Integer getMonthNo();
        Long getCompanyId();
        VehicleSize getVehicleSize();
        Long getItemId();
        Long getTotal();
    }

    // 期間内の台数を、月・会社・車格・品目ごとに合計する
    @Query("""
            select month(d.dispatchDate) as monthNo,
                   d.company.id          as companyId,
                   d.vehicleSize         as vehicleSize,
                   d.item.id             as itemId,
                   sum(d.quantity)       as total
            from Dispatch d
            where d.dispatchDate between :start and :end
            group by month(d.dispatchDate), d.company.id, d.vehicleSize, d.item.id
            """)
    List<MonthlyCount> sumMonthly(@Param("start") LocalDate start,
                                  @Param("end") LocalDate end);
}
