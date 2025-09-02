package com.phraiz.back.payment.service;

import com.phraiz.back.common.enums.Plan;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.exception.custom.InternalServerException;
import com.phraiz.back.payment.dto.request.PaymentRequestDTO;
import com.phraiz.back.payment.dto.response.PaymentResponseDTO;
import com.phraiz.back.payment.dto.request.SubscriptionActionRequestDTO;
import com.phraiz.back.payment.domain.TossPayment;
import com.phraiz.back.payment.domain.MemberSubscriptionInfo;
import com.phraiz.back.payment.exception.PaymentErrorCode;
import com.phraiz.back.payment.repository.TossPaymentRepository;
import com.phraiz.back.payment.repository.MemberSubscriptionInfoRepository;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentService tossPaymentService;
    private final TossPaymentRepository tossPaymentRepository;
    private final MemberSubscriptionInfoRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final HttpSession session; // HttpSession 주입

    //Todo. 정기 결제(구독) 구현 - 현재는 '결제'만 구현된 상태

    /**
     * 구독 결제를 처리하는 서비스 로직
     * @param memberId 회원 ID
     * @param dto 결제 요청 DTO
     * @return 결제 응답 DTO
     * @throws RuntimeException 결제 승인 실패, DB 저장 실패 등
     */
    @Transactional
    public PaymentResponseDTO processSubscriptionPayment(String memberId, PaymentRequestDTO dto) {
        // 1. 세션에 저장된 금액과 요청된 금액 비교
        Long savedAmount = (Long) session.getAttribute(dto.orderId());
        if (savedAmount == null || !savedAmount.equals(dto.amount())) {
            session.removeAttribute(dto.orderId());
            // 결제 금액이 일치하지 않는 경우
            throw new BusinessLogicException(PaymentErrorCode.AMOUNT_MISMATCH);
        }

        // 2. 토스 결제 승인 API 호출
        ResponseEntity<Map> tossResponse = tossPaymentService.confirmPayment(dto.tossPaymentKey(), dto.orderId(), dto.amount());

        if (!tossResponse.getStatusCode().is2xxSuccessful()) {
            throw new InternalServerException(PaymentErrorCode.PAYMENT_CONFIRMATION_FAILED);
        }

        Map<String, Object> responseMap = tossResponse.getBody();
        if (!"DONE".equals(responseMap.get("status"))) {
            throw new BusinessLogicException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        // 3. DB에 결제 및 구독 정보 저장
        TossPayment tossPayment = TossPayment.builder()
                .paymentId((String) responseMap.get("paymentKey"))
                .tossOrderId((String) responseMap.get("orderId"))
                .tossPaymentKey((String) responseMap.get("paymentKey"))
                .tossPaymentMethod(TossPayment.TossPaymentMethod.valueOf((String) responseMap.get("method")))
                .tossPaymentStatus(TossPayment.TossPaymentStatus.valueOf((String) responseMap.get("status")))
                .totalAmount((Long) responseMap.get("totalAmount"))
                .approvedAt(LocalDateTime.parse((String) responseMap.get("approvedAt")))
                .requestedAt(LocalDateTime.now())
                .build();
        tossPaymentRepository.save(tossPayment);

        Member member = memberRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new BusinessLogicException(PaymentErrorCode.MEMBER_NOT_FOUND));

        // 4. member의 Plan 정보 업데이트
        member.setPlanId(Plan.fromName(dto.plan()).getPlanId());
        memberRepository.save(member);

        // payType:String -> PayType 변환
        MemberSubscriptionInfo.PayType payType = MemberSubscriptionInfo.PayType.fromName(dto.payType());

        // 5. Subscription Table Insert
        MemberSubscriptionInfo subscription = MemberSubscriptionInfo.builder()
                .member(member)
                .plan(Plan.fromName(dto.plan()).getPlanId())
                .tossPayment(tossPayment)
                .payType(payType)
                .startDate(LocalDateTime.now())
                .nextRenewalDate(payType == MemberSubscriptionInfo.PayType.MONTHLY ? LocalDateTime.now().plusMonths(1) : LocalDateTime.now().plusYears(1))
                .isActive(true)
                .initialAmount(dto.amount())
                .paidAmount(dto.amount())
                .build();
        subscriptionRepository.save(subscription);

        return PaymentResponseDTO.builder()
                .status("SUCCESS")
                .message("구독 결제가 성공적으로 처리되었습니다.")
                .paymentKey(dto.tossPaymentKey())
                .build();
    }

    /**
     * 구독 해지
     * @param memberId 회원 ID
     * @param dto 구독 해지 요청 DTO
     * @return 결제 응답 DTO
     * @throws RuntimeException 구독 정보를 찾을 수 없거나, 권한이 없는 경우
     */
    @Transactional
    public PaymentResponseDTO cancelSubscription(String memberId, SubscriptionActionRequestDTO dto) {
        MemberSubscriptionInfo subscription = subscriptionRepository.findByTossPayment_TossPaymentKey(dto.tossPaymentKey())
                .orElseThrow(() -> new BusinessLogicException(PaymentErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (!subscription.getMember().getMemberId().toString().equals(memberId)) {
            throw new BusinessLogicException(PaymentErrorCode.UNAUTHORIZED_ACCESS);
        }

        subscription.setIsActive(false);
        subscription.setCancellationDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        // member table plan 업데이트(PLAN FREE로 변경)
        Member member = memberRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new BusinessLogicException(PaymentErrorCode.MEMBER_NOT_FOUND));
        member.setPlanId(Plan.FREE.getPlanId());
        memberRepository.save(member);

        //Todo. 토스 정기 결제 해지

        return PaymentResponseDTO.builder()
                .status("SUCCESS")
                .message("구독이 성공적으로 해지되었습니다.")
                .paymentKey(dto.tossPaymentKey())
                .build();
    }

    /**
     * 미사용 환불 (전액 환불)
     * @param memberId 회원 ID
     * @param dto 환불 요청 DTO
     * @return 결제 응답 DTO
     * @throws RuntimeException 구독 정보를 찾을 수 없거나, 환불 조건이 충족되지 않거나, 토스 API 호출 실패 등
     */
    @Transactional
    public PaymentResponseDTO refundUnused(String memberId, SubscriptionActionRequestDTO dto) {
        MemberSubscriptionInfo subscription = subscriptionRepository.findByTossPayment_TossPaymentKey(dto.tossPaymentKey())
                .orElseThrow(() -> new BusinessLogicException(PaymentErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (!subscription.getMember().getMemberId().toString().equals(memberId)) {
            throw new BusinessLogicException(PaymentErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (Boolean.TRUE.equals(subscription.getIsActive())) {
            throw new BusinessLogicException(PaymentErrorCode.SUBSCRIPTION_ACTIVE);
        }

        if (subscription.getStartDate().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException(PaymentErrorCode.REFUND_PERIOD_EXPIRED);
        }

        // 토스 결제 취소 API 호출
        ResponseEntity<Map> tossResponse = tossPaymentService.canceltotalPayment(
                subscription.getTossPayment().getPaymentId(), "미사용 환불"
        );

        if (!tossResponse.getStatusCode().is2xxSuccessful()) {
            throw new InternalServerException(PaymentErrorCode.TOSS_API_CALL_FAILED);
        }

        subscription.setCancellationDate(LocalDateTime.now());
        subscription.setRefundAmount(subscription.getPaidAmount());
        subscriptionRepository.save(subscription);

        return PaymentResponseDTO.builder()
                .status("SUCCESS")
                .message("미사용 결제가 성공적으로 환불되었습니다.")
                .paymentKey(dto.tossPaymentKey())
                .build();
    }
}