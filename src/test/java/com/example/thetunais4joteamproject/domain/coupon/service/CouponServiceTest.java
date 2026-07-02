package com.example.thetunais4joteamproject.domain.coupon.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.thetunais4joteamproject.domain.coupon.dto.CreateCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.dto.IssueCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.dto.RestoreCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.dto.UseCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.entity.Coupon;
import com.example.thetunais4joteamproject.domain.coupon.entity.MemberCoupon;
import com.example.thetunais4joteamproject.domain.coupon.entity.MemberCouponStatus;
import com.example.thetunais4joteamproject.domain.coupon.repository.CouponRepository;
import com.example.thetunais4joteamproject.domain.coupon.repository.MemberCouponRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

	@InjectMocks
	private CouponService couponService;

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private MemberCouponRepository memberCouponRepository;

	@Test
	@DisplayName("신규 쿠폰 생성 시 만료일이 현재 시점보다 과거라면 INVALID_COUPON_EXPIRATION 예외가 발생한다.")
	void createCoupon_InvalidExpiration_ThrowsException() {
		// given
		LocalDateTime pastExpiration = LocalDateTime.now().minusDays(1);
		CreateCouponRequest request = new CreateCouponRequest(
			"과거 쿠폰", 1000, 10000, 100, pastExpiration
		);

		// when & then
		assertThatThrownBy(() -> couponService.createCoupon(request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_COUPON_EXPIRATION);
	}

	@Test
	@DisplayName("신규 쿠폰 생성 정상 조건 시 쿠폰이 정상적으로 저장되고 ID가 반환된다.")
	void createCoupon_Success() {
		// given
		LocalDateTime futureExpiration = LocalDateTime.now().plusDays(7);
		CreateCouponRequest request = new CreateCouponRequest(
			"정상 쿠폰", 1000, 10000, 100, futureExpiration
		);

		// when
		// CouponService의 createCoupon은 엔티티를 save한 뒤 엔티티의 getId()를 반환하는데,
		// JPA 영속화 과정에서 save 호출 시 JPA는 엔티티의 참조 자체를 수정하거나 새로 반환합니다.
		// Coupon.java에선 coupon.getId()를 호출하므로, save 메서드 호출 시 id가 주입되도록 stubbing해야 합니다.
		willAnswer(invocation -> {
			Coupon coupon = invocation.getArgument(0);
			ReflectionTestUtils.setField(coupon, "id", 1L);
			return coupon;
		}).given(couponRepository).save(any(Coupon.class));

		Long couponId = couponService.createCoupon(request);

		// then
		assertThat(couponId).isEqualTo(1L);
		verify(couponRepository, times(1)).save(any(Coupon.class));
	}

	@Test
	@DisplayName("쿠폰 발급 시 존재하지 않는 쿠폰 ID이면 COUPON_NOT_FOUND 예외가 발생한다.")
	void issueCoupon_NotFound_ThrowsException() {
		// given
		Long memberId = 1L;
		IssueCouponRequest request = new IssueCouponRequest(999L);
		given(couponRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> couponService.issueCoupon(memberId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_NOT_FOUND);
	}

	@Test
	@DisplayName("쿠폰 발급 시 유효기간이 지난 쿠폰이면 COUPON_EXPIRED 예외가 발생한다.")
	void issueCoupon_Expired_ThrowsException() {
		// given
		Long memberId = 1L;
		IssueCouponRequest request = new IssueCouponRequest(1L);
		Coupon expiredCoupon = Coupon.of("만료쿠폰", 1000, 10000, 100, LocalDateTime.now().minusDays(1));
		ReflectionTestUtils.setField(expiredCoupon, "id", 1L);

		given(couponRepository.findById(1L)).willReturn(Optional.of(expiredCoupon));

		// when & then
		assertThatThrownBy(() -> couponService.issueCoupon(memberId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_EXPIRED);
	}

	@Test
	@DisplayName("쿠폰 발급 시 이미 해당 쿠폰을 발급받았다면 COUPON_ALREADY_ISSUED 예외가 발생한다.")
	void issueCoupon_AlreadyIssued_ThrowsException() {
		// given
		Long memberId = 1L;
		IssueCouponRequest request = new IssueCouponRequest(1L);
		Coupon coupon = Coupon.of("쿠폰", 1000, 10000, 100, LocalDateTime.now().plusDays(1));
		ReflectionTestUtils.setField(coupon, "id", 1L);

		given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));
		given(memberCouponRepository.existsByMemberIdAndCouponId(memberId, 1L)).willReturn(true);

		// when & then
		assertThatThrownBy(() -> couponService.issueCoupon(memberId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_ALREADY_ISSUED);
	}

	@Test
	@DisplayName("쿠폰 발급 시 잔여 수량이 부족하면 COUPON_OUT_OF_STOCK 예외가 발생한다.")
	void issueCoupon_OutOfStock_ThrowsException() {
		// given
		Long memberId = 1L;
		IssueCouponRequest request = new IssueCouponRequest(1L);
		Coupon outOfStockCoupon = Coupon.of("수량소진쿠폰", 1000, 10000, 0, LocalDateTime.now().plusDays(1));
		ReflectionTestUtils.setField(outOfStockCoupon, "id", 1L);

		given(couponRepository.findById(1L)).willReturn(Optional.of(outOfStockCoupon));
		given(memberCouponRepository.existsByMemberIdAndCouponId(memberId, 1L)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> couponService.issueCoupon(memberId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_OUT_OF_STOCK);
	}

	@Test
	@DisplayName("쿠폰 발급 성공 시 잔여 수량이 1 감소하고 회원 쿠폰 정보가 정상 등록된다.")
	void issueCoupon_Success() {
		// given
		Long memberId = 1L;
		IssueCouponRequest request = new IssueCouponRequest(1L);
		Coupon coupon = Coupon.of("정상쿠폰", 1000, 10000, 10, LocalDateTime.now().plusDays(1));
		ReflectionTestUtils.setField(coupon, "id", 1L);

		given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));
		given(memberCouponRepository.existsByMemberIdAndCouponId(memberId, 1L)).willReturn(false);

		willAnswer(invocation -> {
			MemberCoupon memberCoupon = invocation.getArgument(0);
			ReflectionTestUtils.setField(memberCoupon, "id", 100L);
			return memberCoupon;
		}).given(memberCouponRepository).save(any(MemberCoupon.class));

		// when
		Long memberCouponId = couponService.issueCoupon(memberId, request);

		// then
		assertThat(memberCouponId).isEqualTo(100L);
		assertThat(coupon.getRemainingQuantity()).isEqualTo(9);
		verify(memberCouponRepository, times(1)).save(any(MemberCoupon.class));
	}

	@Test
	@DisplayName("쿠폰 사용 시 존재하지 않는 회원 쿠폰 ID이면 COUPON_NOT_FOUND 예외가 발생한다.")
	void useCoupon_NotFound_ThrowsException() {
		// given
		Long memberId = 1L;
		UseCouponRequest request = new UseCouponRequest(999L, 20000);
		given(memberCouponRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> couponService.useCoupon(memberId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_NOT_FOUND);
	}

	@Test
	@DisplayName("쿠폰 사용 시 로그인 유저와 소유 유저가 다르면 UNAUTHORIZED 예외가 발생한다.")
	void useCoupon_Unauthorized_ThrowsException() {
		// given
		Long memberId = 1L;
		UseCouponRequest request = new UseCouponRequest(1L, 20000);
		Coupon coupon = Coupon.of("쿠폰", 1000, 10000, 10, LocalDateTime.now().plusDays(1));
		MemberCoupon memberCoupon = MemberCoupon.of(2L, coupon); // 소유자는 2L
		ReflectionTestUtils.setField(memberCoupon, "id", 1L);

		given(memberCouponRepository.findByIdWithLock(1L)).willReturn(Optional.of(memberCoupon));

		// when & then
		assertThatThrownBy(() -> couponService.useCoupon(memberId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
	}

	@Test
	@DisplayName("쿠폰 사용 시 최소 주문 금액 미달이면 INVALID_COUPON_ORDER_PRICE 예외가 발생한다.")
	void useCoupon_InvalidOrderPrice_ThrowsException() {
		// given
		Long memberId = 1L;
		UseCouponRequest request = new UseCouponRequest(1L, 5000); // 주문 금액 5000원
		Coupon coupon = Coupon.of("쿠폰", 1000, 10000, 10, LocalDateTime.now().plusDays(1)); // 최소 10000원
		MemberCoupon memberCoupon = MemberCoupon.of(memberId, coupon);
		ReflectionTestUtils.setField(memberCoupon, "id", 1L);

		given(memberCouponRepository.findByIdWithLock(1L)).willReturn(Optional.of(memberCoupon));

		// when & then
		assertThatThrownBy(() -> couponService.useCoupon(memberId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_COUPON_ORDER_PRICE);
	}

	@Test
	@DisplayName("쿠폰 사용 시 쿠폰 할인 금액이 주문 금액보다 크면 INVALID_COUPON_DISCOUNT_PRICE 예외가 발생한다.")
	void useCoupon_InvalidDiscountPrice_ThrowsException() {
		// given
		Long memberId = 1L;
		UseCouponRequest request = new UseCouponRequest(1L, 10000);
		Coupon coupon = Coupon.of("쿠폰", 20000, 10000, 10, LocalDateTime.now().plusDays(1));
		MemberCoupon memberCoupon = MemberCoupon.of(memberId, coupon);
		ReflectionTestUtils.setField(memberCoupon, "id", 1L);

		given(memberCouponRepository.findByIdWithLock(1L)).willReturn(Optional.of(memberCoupon));

		// when & then
		assertThatThrownBy(() -> couponService.useCoupon(memberId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_COUPON_DISCOUNT_PRICE);
	}

	@Test
	@DisplayName("쿠폰 할인 금액 계산 시 쿠폰 할인 금액이 주문 금액보다 크면 INVALID_COUPON_DISCOUNT_PRICE 예외가 발생한다.")
	void calculateDiscountPrice_InvalidDiscountPrice_ThrowsException() {
		// given
		Long memberId = 1L;
		Long memberCouponId = 1L;
		Coupon coupon = Coupon.of("쿠폰", 20000, 10000, 10, LocalDateTime.now().plusDays(1));
		MemberCoupon memberCoupon = MemberCoupon.of(memberId, coupon);
		ReflectionTestUtils.setField(memberCoupon, "id", memberCouponId);

		given(memberCouponRepository.findById(memberCouponId)).willReturn(Optional.of(memberCoupon));

		// when & then
		assertThatThrownBy(() -> couponService.calculateDiscountPrice(memberId, memberCouponId, 10000))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_COUPON_DISCOUNT_PRICE);
	}

	@Test
	@DisplayName("쿠폰 사용 정상 조건 시 쿠폰 상태가 USED로 변경되고 사용일시가 저장된다.")
	void useCoupon_Success() {
		// given
		Long memberId = 1L;
		UseCouponRequest request = new UseCouponRequest(1L, 20000);
		Coupon coupon = Coupon.of("쿠폰", 1000, 10000, 10, LocalDateTime.now().plusDays(1));
		MemberCoupon memberCoupon = MemberCoupon.of(memberId, coupon);
		ReflectionTestUtils.setField(memberCoupon, "id", 1L);

		given(memberCouponRepository.findByIdWithLock(1L)).willReturn(Optional.of(memberCoupon));

		// when
		couponService.useCoupon(memberId, request);

		// then
		assertThat(memberCoupon.getCouponStatus()).isEqualTo(MemberCouponStatus.USED);
		assertThat(memberCoupon.getUsedAt()).isNotNull();
	}

	@Test
	@DisplayName("쿠폰 복구 시 존재하지 않는 회원 쿠폰 ID이면 COUPON_NOT_FOUND 예외가 발생한다.")
	void restoreCoupon_NotFound_ThrowsException() {
		// given
		Long memberId = 1L;
		RestoreCouponRequest request = new RestoreCouponRequest(999L);
		given(memberCouponRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> couponService.restoreCoupon(memberId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_NOT_FOUND);
	}

	@Test
	@DisplayName("쿠폰 복구 시 로그인 유저와 소유 유저가 다르면 UNAUTHORIZED 예외가 발생한다.")
	void restoreCoupon_Unauthorized_ThrowsException() {
		// given
		Long memberId = 1L;
		RestoreCouponRequest request = new RestoreCouponRequest(1L);
		Coupon coupon = Coupon.of("쿠폰", 1000, 10000, 10, LocalDateTime.now().plusDays(1));
		MemberCoupon memberCoupon = MemberCoupon.of(2L, coupon); // 소유자는 2L
		ReflectionTestUtils.setField(memberCoupon, "id", 1L);

		given(memberCouponRepository.findByIdWithLock(1L)).willReturn(Optional.of(memberCoupon));

		// when & then
		assertThatThrownBy(() -> couponService.restoreCoupon(memberId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
	}

	@Test
	@DisplayName("쿠폰 복구 시 사용하지 않은 쿠폰이면 COUPON_NOT_USED 예외가 발생한다.")
	void restoreCoupon_NotUsed_ThrowsException() {
		// given
		Long memberId = 1L;
		RestoreCouponRequest request = new RestoreCouponRequest(1L);
		Coupon coupon = Coupon.of("쿠폰", 1000, 10000, 10, LocalDateTime.now().plusDays(1));
		MemberCoupon memberCoupon = MemberCoupon.of(memberId, coupon); // UNUSED 상태
		ReflectionTestUtils.setField(memberCoupon, "id", 1L);

		given(memberCouponRepository.findByIdWithLock(1L)).willReturn(Optional.of(memberCoupon));

		// when & then
		assertThatThrownBy(() -> couponService.restoreCoupon(memberId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_NOT_USED);
	}

	@Test
	@DisplayName("쿠폰 복구 정상 조건 시 쿠폰 상태가 UNUSED로 변경되고 사용일시가 null이 된다.")
	void restoreCoupon_Success() {
		// given
		Long memberId = 1L;
		RestoreCouponRequest request = new RestoreCouponRequest(1L);
		Coupon coupon = Coupon.of("쿠폰", 1000, 10000, 10, LocalDateTime.now().plusDays(1));
		MemberCoupon memberCoupon = MemberCoupon.of(memberId, coupon);
		// 강제로 USED 상태로 변경 (use 기능 적용 후 검증을 위해 사용 상태로 임의 셋팅)
		memberCoupon.use(20000);
		ReflectionTestUtils.setField(memberCoupon, "id", 1L);

		given(memberCouponRepository.findByIdWithLock(1L)).willReturn(Optional.of(memberCoupon));

		// when
		couponService.restoreCoupon(memberId, request);

		// then
		assertThat(memberCoupon.getCouponStatus()).isEqualTo(MemberCouponStatus.UNUSED);
		assertThat(memberCoupon.getUsedAt()).isNull();
	}

	@Test
	@DisplayName("내부 쿠폰 복구 시 사용된 쿠폰이면 UNUSED로 복구한다.")
	void restoreCouponIfUsed_UsedCoupon_RestoresCoupon() {
		// given
		Long memberId = 1L;
		Long memberCouponId = 1L;
		Coupon coupon = Coupon.of("쿠폰", 1000, 10000, 10, LocalDateTime.now().plusDays(1));
		MemberCoupon memberCoupon = MemberCoupon.of(memberId, coupon);
		memberCoupon.use(20000);
		ReflectionTestUtils.setField(memberCoupon, "id", memberCouponId);

		given(memberCouponRepository.findByIdWithLock(memberCouponId)).willReturn(Optional.of(memberCoupon));

		// when
		couponService.restoreCouponIfUsed(memberId, memberCouponId);

		// then
		assertThat(memberCoupon.getCouponStatus()).isEqualTo(MemberCouponStatus.UNUSED);
		assertThat(memberCoupon.getUsedAt()).isNull();
	}

	@Test
	@DisplayName("내부 쿠폰 복구 시 사용 전 쿠폰이면 예외 없이 유지한다.")
	void restoreCouponIfUsed_UnusedCoupon_DoesNothing() {
		// given
		Long memberId = 1L;
		Long memberCouponId = 1L;
		Coupon coupon = Coupon.of("쿠폰", 1000, 10000, 10, LocalDateTime.now().plusDays(1));
		MemberCoupon memberCoupon = MemberCoupon.of(memberId, coupon);
		ReflectionTestUtils.setField(memberCoupon, "id", memberCouponId);

		given(memberCouponRepository.findByIdWithLock(memberCouponId)).willReturn(Optional.of(memberCoupon));

		// when
		couponService.restoreCouponIfUsed(memberId, memberCouponId);

		// then
		assertThat(memberCoupon.getCouponStatus()).isEqualTo(MemberCouponStatus.UNUSED);
		assertThat(memberCoupon.getUsedAt()).isNull();
	}
}
