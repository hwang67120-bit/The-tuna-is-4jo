package com.example.thetunais4joteamproject.domain.coupon.facade;

import com.example.thetunais4joteamproject.domain.coupon.dto.IssueCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.service.CouponService;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CouponIssueFacade {

    private final RedissonClient redissonClient;
    private final CouponService couponService;

    /**
     * Redis 분산 락 제어 및 쿠폰 발급 진입점
     */
    public Long issueCouponWithLock(Long memberId, IssueCouponRequest request) {
        // 1. 쿠폰 ID를 기반으로 고유한 락 키 생성
        String lockKey = "LOCK:COUPON:" + request.couponId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 2. 락 획득 시도 (최대 10초 동안 대기, 락 획득 시 1초간 유지 후 자동 해제 안전장치)
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

            if (!available) {
                throw BusinessException.from(ErrorCode.COUPON_OUT_OF_STOCK); // 락 획득 실패 시 예외 처리
            }

            // 3. 락 획득 성공 시, 실제 DB 트랜잭션을 타는 서비스 로직 호출
            return couponService.issueCoupon(memberId, request);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw BusinessException.from(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            // 4. 모든 비즈니스가 끝나고 트랜잭션이 완전히 커밋된 후 안전하게 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
