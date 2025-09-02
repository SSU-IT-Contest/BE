package com.phraiz.back.payment.repository;

import com.phraiz.back.payment.domain.TossPayment;
import org.springframework.data.jpa.repository.JpaRepository;

// TossPayment 엔티티의 리포지토리
public interface TossPaymentRepository extends JpaRepository<TossPayment, String> {
}