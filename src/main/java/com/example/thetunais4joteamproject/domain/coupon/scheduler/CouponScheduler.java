package com.example.thetunais4joteamproject.domain.coupon.scheduler;

import com.example.thetunais4joteamproject.domain.coupon.repository.MemberCouponRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

	private final MemberCouponRepository memberCouponRepository;

	/**
	 * 매일 자정(00:00:00)마다 만료된 쿠폰을 찾아 자동으로 일괄 청소한다.
	 */
	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void expireOutdatedCoupons() {
		log.info("[만료 쿠폰 스케줄러] 자동 상태 갱신 배치를 시작합니다. 기준 시간: {}", LocalDateTime.now());

		LocalDateTime now = LocalDateTime.now();
		// 벌크 연산을 실행하고 만료 처리된 행(row)의 개수를 반환받는다.
		int updatedCount = memberCouponRepository.updateExpiredCoupons(now);

		log.info("[만료 쿠폰 스케줄러] 청소가 완료되었습니다. 총 {}건의 쿠폰이 만료(EXPIRED) 처리되었습니다.", updatedCount);
	}
}