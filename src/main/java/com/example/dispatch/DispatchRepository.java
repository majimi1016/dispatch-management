package com.example.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    List<Dispatch> findAllByOrderByDispatchDateAsc();
}
