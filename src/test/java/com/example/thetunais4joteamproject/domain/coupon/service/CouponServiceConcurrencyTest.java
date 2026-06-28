package com.example.thetunais4joteamproject.domain.coupon.service;

import com.example.thetunais4joteamproject.domain.coupon.dto.IssueCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.entity.Coupon;
import com.example.thetunais4joteamproject.domain.coupon.facade.CouponIssueFacade;
import com.example.thetunais4joteamproject.domain.coupon.repository.CouponRepository;
import com.example.thetunais4joteamproject.domain.coupon.repository.MemberCouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponServiceConcurrencyTest {

    @Autowired
    private CouponIssueFacade couponIssueFacade; // 분산 락 퍼사드 주입

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private MemberCouponRepository memberCouponRepository;

    @Test
    @DisplayName("Redis 분산 락 테스트 - 100명이 동시에 선착순 쿠폰을 신청하면 대기열을 거쳐 정확히 재고가 0이 되어야 한다")
    void issueCoupon_DistributedLock_Concurrency_Success() throws InterruptedException {
        // given: 재고가 정확히 100개인 쿠폰 정적 팩토리 메서드로 세팅
        Coupon coupon = Coupon.of(
                "선착순 100명 초특가 할인 쿠폰",
                5000,
                30000,
                100,
                LocalDateTime.now().plusDays(7)
        );
        Coupon savedCoupon = couponRepository.save(coupon);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when: 100개의 스레드가 분산 락 관문을 통과하며 쿠폰 발급 요청
        for (int i = 1; i <= threadCount; i++) {
            long memberId = i;
            executorService.submit(() -> {
                try {
                    IssueCouponRequest request = new IssueCouponRequest(savedCoupon.getId());
                    // 분산 락 발급 메서드 타격
                    couponIssueFacade.issueCouponWithLock(memberId, request);
                } catch (Exception e) {
                    System.out.println("분산 락 관문 통과 실패 혹은 재고 소진: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: 데이터 무결성 검증
        Coupon finalCoupon = couponRepository.findById(savedCoupon.getId()).orElseThrow();
        long totalIssuedCount = memberCouponRepository.count();

        System.out.println("Redis 분산 락 적용 후 최종 남은 수량: " + finalCoupon.getRemainingQuantity());
        System.out.println("유저들에게 실제 발급 완료된 수량: " + totalIssuedCount);

        // 락이 완벽하게 줄을 세웠으므로 유실 데이터 없이 0개와 100개 스펙이 증명되어야 함
        assertThat(finalCoupon.getRemainingQuantity()).isEqualTo(0);
        assertThat(totalIssuedCount).isEqualTo(100);
    }
}