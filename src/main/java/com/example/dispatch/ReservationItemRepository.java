package com.example.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long> {

    // ===== 予約一覧用:その日の品目ごとの合計 =====
    interface ItemTotal {
        String getItemName();
        BigDecimal getTotal();
    }

    @Query("""
            select ri.item.name as itemName, sum(ri.quantity) as total
            from ReservationItem ri
            where ri.reservation.pickupDate = :date
              and ri.reservation.status <> :excluded
            group by ri.item.name
            order by ri.item.name
            """)
    List<ItemTotal> sumByItem(@Param("date") LocalDate date,
                              @Param("excluded") ReservationStatus excluded);

    // ===== 月別まとめ用(↓ 追加):日付・品目ごとの合計 =====
    interface DailyItemTotal {
        LocalDate getPickupDate();
        Long getItemId();
        BigDecimal getTotal();
    }

    @Query("""
            select ri.reservation.pickupDate as pickupDate,
                   ri.item.id                as itemId,
                   sum(ri.quantity)          as total
            from ReservationItem ri
            where ri.reservation.pickupDate between :start and :end
              and ri.reservation.status = :status
            group by ri.reservation.pickupDate, ri.item.id
            """)
    List<DailyItemTotal> sumDailyByItem(@Param("start") LocalDate start,
                                        @Param("end") LocalDate end,
                                        @Param("status") ReservationStatus status);

    // ===== 年別まとめ用(↓ 追加):月・品目ごとの合計 =====
    interface MonthlyItemTotal {
        Integer getMonthNo();
        Long getItemId();
        BigDecimal getTotal();
    }

    @Query("""
            select month(ri.reservation.pickupDate) as monthNo,
                   ri.item.id                       as itemId,
                   sum(ri.quantity)                 as total
            from ReservationItem ri
            where ri.reservation.pickupDate between :start and :end
              and ri.reservation.status = :status
            group by month(ri.reservation.pickupDate), ri.item.id
            """)
    List<MonthlyItemTotal> sumMonthlyByItem(@Param("start") LocalDate start,
                                            @Param("end") LocalDate end,
                                            @Param("status") ReservationStatus status);
}
