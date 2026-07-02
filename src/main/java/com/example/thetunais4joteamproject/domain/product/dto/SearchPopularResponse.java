package com.example.thetunais4joteamproject.domain.product.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SearchPopularResponse(
	List<SearchPopularItem> keywords,
	LocalDateTime aggregatedAt
) {
	public static SearchPopularResponse of(List<SearchPopularItem> keywords, LocalDateTime aggregatedAt) {
		return new SearchPopularResponse(keywords, aggregatedAt);
	}

	public record SearchPopularItem(
		int rank,
		String keyword,
		Double score
	) {
	}
}

