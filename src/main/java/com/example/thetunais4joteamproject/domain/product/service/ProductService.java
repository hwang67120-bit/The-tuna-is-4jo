package com.example.thetunais4joteamproject.domain.product.service;

import java.util.List;

import com.example.thetunais4joteamproject.domain.product.dto.CreateProductRequest;
import com.example.thetunais4joteamproject.domain.product.dto.RepresentStockRequest;
import com.example.thetunais4joteamproject.domain.product.dto.UpdateOptionRequest;
import com.example.thetunais4joteamproject.domain.product.entity.Category;
import com.example.thetunais4joteamproject.domain.product.entity.OptionStatus;
import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.entity.ProductStatus;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.domain.product.repository.CategoryRepository;
import com.example.thetunais4joteamproject.domain.product.repository.ProductOptionRepository;
import com.example.thetunais4joteamproject.domain.product.repository.ProductRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 기본 설정으로 성능 최적화
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductOptionRepository productOptionRepository;

    /**
     * 상품 생성
     */
    @Transactional // 데이터 변경이 일어나므로 쓰기 트랜잭션 선언
    public Long createProduct(CreateProductRequest request) {
        // 1. 카테고리 존재 여부 확인 (없으면 예외 처리)
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> BusinessException.from(ErrorCode.CATEGORY_NOT_FOUND));

        // 2. 상품 마스터 생성 및 저장
        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .status(ProductStatus.ON_SALE) // 초기 상태는 판매 중
                .build();

        Product savedProduct = productRepository.save(product);

        // 3. 단일 재고 위임 처리를 위한 '기본 옵션' 자동 최초 생성
        ProductOption defaultOption = ProductOption.builder()
                .product(savedProduct)
                .optionName("기본 옵션")
                .optionStock(0) // 초기 재고는 0
                .status(OptionStatus.SOLDOUT) // 재고가 0이므로 초기 상태는 품절
                .build();

        productOptionRepository.save(defaultOption);

        return savedProduct.getId();
    }

    /**
     * 상품 세부 옵션 및 상태/추가금액 변경
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void updateOptionStocks(Long productId, List<UpdateOptionRequest> requests) {
        productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.PRODUCT_NOT_FOUND));
        for (UpdateOptionRequest request : requests) {
            ProductOption option = productOptionRepository.findById(request.getOptionId())
                    .orElseThrow(() -> BusinessException.from(ErrorCode.OPTION_NOT_FOUND));

            // 엔티티 메서드 호출하여 재고, 추가금액, 상태를 한 번에 갱신
            if (request.getOptionStock() == 0) {
                option.updateOptionDetails(0, request.getAdditionalPrice(), OptionStatus.SOLDOUT);
            } else {
                option.updateOptionDetails(request.getOptionStock(), request.getAdditionalPrice(), request.getStatus());
            }
        }
    }

    /**
     * 상품 대표 재고 변경
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true) // 상품 수정 시 Redis 검색 캐시 전체 무효화
    public void updateRepresentativeStock(Long productId, RepresentStockRequest request) {
        // 1. 상품 존재 여부 확인
        productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.PRODUCT_NOT_FOUND));

        // 2. 내부적으로 생성해 둔 '기본 옵션' 조회
        ProductOption defaultOption = productOptionRepository.findTopByProductIdOrderByIdAsc(productId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.DEFAULT_OPTION_NOT_FOUND));

        // 3. 기본 옵션 엔티티에 재고 수정 위임
        int targetStock = request.getStockQuantity();
        defaultOption.updateStock(targetStock);

        // 비즈니스 룰: 수량이 0이면 SOLDOUT, 1 이상이면 ON_SALE 자동 전환
        if (targetStock == 0) {
            defaultOption.changeStatus(OptionStatus.SOLDOUT);
        } else {
            defaultOption.changeStatus(OptionStatus.ON_SALE);
        }
    }
}