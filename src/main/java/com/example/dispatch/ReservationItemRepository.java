package com.example.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long> {

    // 集計結果の1行(品目名と合計トン数)
    interface ItemTotal {
        String getItemName();
        BigDecimal getTotal();
    }

    // 指定した日の、品目ごとの合計トン数(指定した状態は除く)
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
}