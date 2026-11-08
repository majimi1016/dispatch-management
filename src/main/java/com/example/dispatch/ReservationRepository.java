package com.example.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 指定した引取日の予約を、昼→夜、登録順に取得
    List<Reservation> findByPickupDateOrderByShiftAscIdAsc(LocalDate pickupDate);
}