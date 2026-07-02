package com.example.thetunais4joteamproject.domain.refund;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.thetunais4joteamproject.domain.coupon.service.CouponService;
import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.entity.PaymentStatus;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGateway;
import com.example.thetunais4joteamproject.domain.payment.repository.PaymentRepository;
import com.example.thetunais4joteamproject.domain.refund.dto.RefundRequest;
import com.example.thetunais4joteamproject.domain.refund.dto.RefundResponse;
import com.example.thetunais4joteamproject.domain.refund.entity.Refund;
import com.example.thetunais4joteamproject.domain.refund.entity.RefundStatus;
import com.example.thetunais4joteamproject.domain.refund.repository.RefundRepository;
import com.example.thetunais4joteamproject.domain.refund.service.RefundService;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class RefundTest {

	@Mock
	private RefundRepository refundRepository;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PaymentGateway paymentGateway;

	@Mock
	private CouponService couponService;

	private RefundService refundService;

	@BeforeEach
	void setUp() {
		refundService = new RefundService(
			refundRepository,
			paymentRepository,
			memberRepository,
			paymentGateway,
			couponService
		);
	}

	@Test
	void requestRefund_정상_요청이면_환불_요청을_생성한다() {
		// given
		Long memberId = 1L;
		Member member = createMember(memberId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		RefundRequest request = new RefundRequest(payment.getId(), "단순 변심", 26000);
		Refund refund = createRefund(100L, payment, member, request.reason(), request.refundAmount());

		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(refundRepository.existsByPaymentIdAndStatusIn(eq(payment.getId()), anyList()))
			.willReturn(false);
		given(refundRepository.save(any(Refund.class))).willReturn(refund);

		// when
		RefundResponse response = refundService.requestRefund(memberId, request);

		// then
		assertThat(response.refundId()).isEqualTo(refund.getId());
		assertThat(response.paymentId()).isEqualTo(payment.getId());
		assertThat(response.refundAmount()).isEqualTo(26000);
		assertThat(response.status()).isEqualTo(RefundStatus.REQUESTED);
	}

	@Test
	void requestRefund_결제가_존재하지_않으면_예외가_발생한다() {
		// given
		RefundRequest request = new RefundRequest(10L, "단순 변심", 26000);

		given(paymentRepository.findById(request.paymentId())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> refundService.requestRefund(1L, request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
	}

	@Test
	void requestRefund_회원이_존재하지_않으면_예외가_발생한다() {
		// given
		Long memberId = 1L;
		Member member = createMember(memberId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		RefundRequest request = new RefundRequest(payment.getId(), "단순 변심", 26000);

		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(memberRepository.findById(memberId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> refundService.requestRefund(memberId, request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
	}

	@Test
	void requestRefund_결제_소유자가_요청자가_아니면_예외가_발생한다() {
		// given
		Long ownerId = 1L;
		Long requestMemberId = 2L;
		Member owner = createMember(ownerId);
		Member requester = createMember(requestMemberId);
		Payment payment = createPaidPayment(10L, owner, "pay-123", 26000);
		RefundRequest request = new RefundRequest(payment.getId(), "단순 변심", 26000);

		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(memberRepository.findById(requestMemberId)).willReturn(Optional.of(requester));

		// when & then
		assertThatThrownBy(() -> refundService.requestRefund(requestMemberId, request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
	}

	@Test
	void requestRefund_결제_상태가_PAID가_아니면_예외가_발생한다() {
		// given
		Long memberId = 1L;
		Member member = createMember(memberId);
		Payment payment = createPendingPayment(10L, member, "pay-123", 26000);
		RefundRequest request = new RefundRequest(payment.getId(), "단순 변심", 26000);

		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

		// when & then
		assertThatThrownBy(() -> refundService.requestRefund(memberId, request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_PAYMENT_STATUS);
	}

	@Test
	void requestRefund_환불_금액이_결제_금액보다_크면_예외가_발생한다() {
		// given
		Long memberId = 1L;
		Member member = createMember(memberId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		RefundRequest request = new RefundRequest(payment.getId(), "단순 변심", 27000);

		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

		// when & then
		assertThatThrownBy(() -> refundService.requestRefund(memberId, request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_REFUND_AMOUNT);
	}

	@Test
	void requestRefund_이미_REQUESTED_환불이_있으면_예외가_발생한다() {
		// given
		Long memberId = 1L;
		Member member = createMember(memberId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		RefundRequest request = new RefundRequest(payment.getId(), "단순 변심", 26000);

		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(refundRepository.existsByPaymentIdAndStatusIn(eq(payment.getId()), anyList()))
			.willReturn(true);

		// when & then
		assertThatThrownBy(() -> refundService.requestRefund(memberId, request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.ALREADY_REQUESTED_REFUND);
	}

	@Test
	void requestRefund_이미_COMPLETED_환불이_있으면_예외가_발생한다() {
		// given
		Long memberId = 1L;
		Member member = createMember(memberId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		RefundRequest request = new RefundRequest(payment.getId(), "단순 변심", 26000);

		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(refundRepository.existsByPaymentIdAndStatusIn(eq(payment.getId()), anyList()))
			.willReturn(true);

		// when & then
		assertThatThrownBy(() -> refundService.requestRefund(memberId, request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.ALREADY_REQUESTED_REFUND);
	}

	@Test
	void approveRefund_정상_승인이면_결제와_환불을_완료_처리한다() {
		// given
		Long adminId = 99L;
		Member member = createMember(1L);
		Member admin = createMember(adminId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		Refund refund = createRefund(100L, payment, member, "단순 변심", 26000);

		given(refundRepository.findById(refund.getId())).willReturn(Optional.of(refund));
		given(memberRepository.findById(adminId)).willReturn(Optional.of(admin));

		// when
		RefundResponse response = refundService.approveRefund(adminId, refund.getId());

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
		assertThat(response.status()).isEqualTo(RefundStatus.COMPLETED);
		verify(paymentGateway).cancelPayment(payment.getPortonePaymentId(), refund.getReason());
	}

	@Test
	void approveRefund_쿠폰을_사용한_주문이면_쿠폰을_복구한다() {
		// given
		Long adminId = 99L;
		Long memberCouponId = 30L;
		Member member = createMember(1L);
		Member admin = createMember(adminId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		ReflectionTestUtils.setField(payment.getOrder(), "memberCouponId", memberCouponId);
		Refund refund = createRefund(100L, payment, member, "단순 변심", 26000);

		given(refundRepository.findById(refund.getId())).willReturn(Optional.of(refund));
		given(memberRepository.findById(adminId)).willReturn(Optional.of(admin));

		// when
		refundService.approveRefund(adminId, refund.getId());

		// then
		verify(couponService).restoreCouponIfUsed(member.getId(), memberCouponId);
	}

	@Test
	void approveRefund_환불_요청이_존재하지_않으면_예외가_발생한다() {
		// given
		Long refundId = 100L;

		given(refundRepository.findById(refundId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> refundService.approveRefund(99L, refundId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.REFUND_NOT_FOUND);
	}

	@Test
	void approveRefund_관리자가_존재하지_않으면_예외가_발생한다() {
		// given
		Long adminId = 99L;
		Member member = createMember(1L);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		Refund refund = createRefund(100L, payment, member, "단순 변심", 26000);

		given(refundRepository.findById(refund.getId())).willReturn(Optional.of(refund));
		given(memberRepository.findById(adminId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> refundService.approveRefund(adminId, refund.getId()))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
	}

	@Test
	void approveRefund_이미_COMPLETED_상태면_예외가_발생한다() {
		// given
		Long adminId = 99L;
		Member member = createMember(1L);
		Member admin = createMember(adminId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		Refund refund = createRefund(100L, payment, member, "단순 변심", 26000);

		refund.complete(admin);

		given(refundRepository.findById(refund.getId())).willReturn(Optional.of(refund));
		given(memberRepository.findById(adminId)).willReturn(Optional.of(admin));

		// when & then
		assertThatThrownBy(() -> refundService.approveRefund(adminId, refund.getId()))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_REFUND_STATUS);
	}

	@Test
	void approveRefund_이미_REJECTED_상태면_예외가_발생한다() {
		// given
		Long adminId = 99L;
		Member member = createMember(1L);
		Member admin = createMember(adminId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		Refund refund = createRefund(100L, payment, member, "단순 변심", 26000);

		refund.reject(admin, "환불 조건 불충족");

		given(refundRepository.findById(refund.getId())).willReturn(Optional.of(refund));
		given(memberRepository.findById(adminId)).willReturn(Optional.of(admin));

		// when & then
		assertThatThrownBy(() -> refundService.approveRefund(adminId, refund.getId()))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_REFUND_STATUS);
	}

	@Test
	void approveRefund_PG_취소_실패_시_환불_상태가_COMPLETED_되지_않는다() {
		// given
		Long adminId = 99L;
		Member member = createMember(1L);
		Member admin = createMember(adminId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		Refund refund = createRefund(100L, payment, member, "단순 변심", 26000);

		given(refundRepository.findById(refund.getId())).willReturn(Optional.of(refund));
		given(memberRepository.findById(adminId)).willReturn(Optional.of(admin));
		willThrow(new RuntimeException("PG cancel failed"))
			.given(paymentGateway)
			.cancelPayment(payment.getPortonePaymentId(), refund.getReason());

		// when
		RefundResponse response = refundService.approveRefund(adminId, refund.getId());

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.FAILED);
		assertThat(refund.getStatus()).isNotEqualTo(RefundStatus.COMPLETED);
		assertThat(response.status()).isEqualTo(RefundStatus.FAILED);
	}

	@Test
	void rejectRefund_정상_거절이면_환불을_거절_처리한다() {
		// given
		Long adminId = 99L;
		Member member = createMember(1L);
		Member admin = createMember(adminId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		Refund refund = createRefund(100L, payment, member, "단순 변심", 26000);

		given(refundRepository.findById(refund.getId())).willReturn(Optional.of(refund));
		given(memberRepository.findById(adminId)).willReturn(Optional.of(admin));

		// when
		RefundResponse response = refundService.rejectRefund(adminId, refund.getId(), "환불 조건 불충족");

		// then
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.REJECTED);
		assertThat(refund.getRejectionReason()).isEqualTo("환불 조건 불충족");
		assertThat(response.status()).isEqualTo(RefundStatus.REJECTED);
	}

	@Test
	void rejectRefund_환불_요청이_존재하지_않으면_예외가_발생한다() {
		// given
		Long refundId = 100L;

		given(refundRepository.findById(refundId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> refundService.rejectRefund(99L, refundId, "환불 조건 불충족"))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.REFUND_NOT_FOUND);
	}

	@Test
	void rejectRefund_관리자가_존재하지_않으면_예외가_발생한다() {
		// given
		Long adminId = 99L;
		Member member = createMember(1L);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		Refund refund = createRefund(100L, payment, member, "단순 변심", 26000);

		given(refundRepository.findById(refund.getId())).willReturn(Optional.of(refund));
		given(memberRepository.findById(adminId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> refundService.rejectRefund(adminId, refund.getId(), "환불 조건 불충족"))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
	}

	@Test
	void rejectRefund_이미_COMPLETED_상태면_거절_불가_예외가_발생한다() {
		// given
		Long adminId = 99L;
		Member member = createMember(1L);
		Member admin = createMember(adminId);
		Payment payment = createPaidPayment(10L, member, "pay-123", 26000);
		Refund refund = createRefund(100L, payment, member, "단순 변심", 26000);

		refund.complete(admin);

		given(refundRepository.findById(refund.getId())).willReturn(Optional.of(refund));
		given(memberRepository.findById(adminId)).willReturn(Optional.of(admin));

		// when & then
		assertThatThrownBy(() -> refundService.rejectRefund(adminId, refund.getId(), "환불 조건 불충족"))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_REFUND_STATUS);
	}

	private Member createMember(Long memberId) {
		Member member = Member.create(
			"user" + memberId + "@test.com",
			"password",
			"member" + memberId,
			"010-0000-0000"
		);
		ReflectionTestUtils.setField(member, "id", memberId);

		return member;
	}

	private Payment createPendingPayment(Long paymentId, Member member, String portonePaymentId, Integer amount) {
		Order order = Order.of(
			member,
			"ORD-" + paymentId,
			amount,
			0,
			0,
			amount
		);
		ReflectionTestUtils.setField(order, "id", paymentId);

		Payment payment = Payment.createPendingPayment(order, portonePaymentId, amount, amount);
		ReflectionTestUtils.setField(payment, "id", paymentId);

		return payment;
	}

	private Payment createPaidPayment(Long paymentId, Member member, String portonePaymentId, Integer amount) {
		Payment payment = createPendingPayment(paymentId, member, portonePaymentId, amount);
		payment.complete();

		return payment;
	}

	private Refund createRefund(
		Long refundId,
		Payment payment,
		Member requester,
		String reason,
		Integer refundAmount
	) {
		Refund refund = Refund.create(payment, requester, reason, refundAmount);
		ReflectionTestUtils.setField(refund, "id", refundId);

		return refund;
	}
}
