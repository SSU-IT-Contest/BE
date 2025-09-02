package com.phraiz.back.payment.repository;

import com.phraiz.back.payment.domain.MemberSubscriptionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// MemberSubscriptionInfo 엔티티의 리포지토리
public interface MemberSubscriptionInfoRepository extends JpaRepository<MemberSubscriptionInfo, Long> {
    Optional<MemberSubscriptionInfo> findByTossPayment_TossPaymentKey(String tossPaymentKey);
}