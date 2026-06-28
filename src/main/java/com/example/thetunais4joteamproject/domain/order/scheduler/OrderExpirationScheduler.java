package com.example.thetunais4joteamproject.domain.order.scheduler;

import com.example.thetunais4joteamproject.domain.order.facade.OrderFacade;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

	private static final long CHECK_DELAY_MILLISECONDS = 60_000L;

	private final OrderFacade orderFacade;

	@Scheduled(fixedDelay = CHECK_DELAY_MILLISECONDS)
	public void expirePendingOrders() {
		orderFacade.expirePendingOrders();
	}
}
