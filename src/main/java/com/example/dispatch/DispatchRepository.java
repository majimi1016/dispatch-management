package com.example.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    List<Dispatch> findAllByOrderByDispatchDateAsc();

    // 指定した日の配達を、昼→夜、会社名の順、登録順に取得
    List<Dispatch> findByDispatchDateOrderByShiftAscCompanyNameAscIdAsc(LocalDate dispatchDate);
}
