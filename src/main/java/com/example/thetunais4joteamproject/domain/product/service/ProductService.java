package com.example.thetunais4joteamproject.domain.product.service;

import java.util.List;

import com.example.thetunais4joteamproject.domain.product.dto.CreateProductRequest;
import com.example.thetunais4joteamproject.domain.product.dto.GetAllProductResponse;
import com.example.thetunais4joteamproject.domain.product.dto.GetCategoryProductsResponse;
import com.example.thetunais4joteamproject.domain.product.dto.GetProductDetailResponse;
import com.example.thetunais4joteamproject.domain.product.dto.RepresentStockRequest;
import com.example.thetunais4joteamproject.domain.product.dto.UpdateOptionRequest;
import com.example.thetunais4joteamproject.domain.product.dto.UpdateProductRequest;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductSearchService productSearchService;

    /**
     * 상품 생성
     */
    @Transactional
    public Long createProduct(Long memberId, CreateProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> BusinessException.from(ErrorCode.CATEGORY_NOT_FOUND));

        Product product = Product.of(
            memberId,
            category,
            request.name(),
            request.price(),
            request.description(),
            ProductStatus.ON_SALE
        );

        productRepository.save(product);

        if (request.options() != null && !request.options().isEmpty()) {
            for (CreateProductRequest.ProductOptionRequest optionReq : request.options()) {
                OptionStatus initialStatus = optionReq.optionStock() == 0 ? OptionStatus.SOLDOUT : OptionStatus.ON_SALE;

                ProductOption option = ProductOption.of(
                    product,
                    optionReq.optionName(),
                    optionReq.optionStock(),
                    optionReq.additionalPrice(),
                    initialStatus
                );
                productOptionRepository.save(option);
            }
        }

        return product.getId();
    }

    /**
     * 상품 세부 옵션 및 상태/추가금액 변경
     */
    @Transactional
    public void updateOptionStocks(Long productId, List<UpdateOptionRequest> requests) {
        for (UpdateOptionRequest updateReq : requests) {
            ProductOption option = productOptionRepository.findById(updateReq.optionId())
                .orElseThrow(() -> BusinessException.from(ErrorCode.OPTION_NOT_FOUND));

            int inputStock = updateReq.optionStock();
            OptionStatus finalizedStatus = updateReq.status();

            if (inputStock == 0) {
                finalizedStatus = OptionStatus.SOLDOUT;
            }

            option.updateOptionDetails(
                inputStock,
                updateReq.additionalPrice(),
                finalizedStatus
            );
        }
    }

    /**
     * 상품 대표 재고 변경
     */
    @Transactional
    public void updateRepresentativeStock(Long productId, RepresentStockRequest request) {
        ProductOption representativeOption = productOptionRepository.findTopByProductIdOrderByIdAsc(productId)
            .orElseThrow(() -> BusinessException.from(ErrorCode.OPTION_NOT_FOUND));

        representativeOption.updateStock(request.stockQuantity());
    }

    /**
     * 상품 목록 조회 (No-Offset 대용량 쿼리 최적화 완료)
     */
    public List<GetAllProductResponse> getAllProductsNoOffset(Long lastProductId, int size) {
        if (size <= 0) {
            throw BusinessException.from(ErrorCode.BAD_REQUEST);
        }

        List<Product> products = productRepository.findAllByNoOffset(lastProductId, size);

        return products.stream().map(GetAllProductResponse::from).toList();
    }

    /**
     * 상품 상세 조회
     * value: 캐시의 고유 이름 (Redis 내부에서 접두사로 쓰임)
     * key: 파라미터로 넘어온 productId별로 각각 캐싱 공간을 분리
     */
    @Cacheable(value = "productDetail", key = "#productId", cacheManager = "cacheManager")
    public GetProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> BusinessException.from(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() == ProductStatus.DELETED) {
            throw BusinessException.from(ErrorCode.PRODUCT_NOT_FOUND);
        }

        List<ProductOption> wpOptions = productOptionRepository.findAllByProductId(productId);

        List<GetProductDetailResponse.ProductOptionResponse> optionResponses = wpOptions.stream()
            .map(GetProductDetailResponse.ProductOptionResponse::from)
            .toList();

        return GetProductDetailResponse.of(product, optionResponses);
    }

    /**
     * 상품 카테고리별 조회 (User)
     */
    public GetCategoryProductsResponse getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> BusinessException.from(ErrorCode.CATEGORY_NOT_FOUND));

        List<Product> products = productRepository.findAllByCategoryIdAndStatus(categoryId, ProductStatus.ON_SALE);

        List<GetCategoryProductsResponse.CategoryProductResponse> productResponses = products.stream()
            .map(GetCategoryProductsResponse.CategoryProductResponse::from)
            .toList();

        return GetCategoryProductsResponse.of(category, productResponses);
    }

    /**
     * 상품 수정 (Admin)
     */
    @Transactional
    @CacheEvict(value = "productDetail", key = "#productId")
    public void updateProduct(Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> BusinessException.from(ErrorCode.PRODUCT_NOT_FOUND));

        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> BusinessException.from(ErrorCode.CATEGORY_NOT_FOUND));

        product.updateProduct(
            category,
            request.name(),
            request.price(),
            request.description()
        );

        productSearchService.evictSearchCache();
    }

    /**
     * 상품 삭제 (Admin - Soft Delete)
     */
    @Transactional
    @CacheEvict(value = "productDetail", key = "#productId")
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> BusinessException.from(ErrorCode.PRODUCT_NOT_FOUND));

        product.changeStatus(ProductStatus.DELETED);

        productSearchService.evictSearchCache();

        List<ProductOption> options = productOptionRepository.findAllByProductId(productId);
        for (ProductOption option : options) {
            option.updateOptionDetails(
                0,
                option.getAdditionalPrice(),
                OptionStatus.SOLDOUT
            );
        }
    }

    /**
     * [주문 연동] 특정 옵션의 재고 차감 (Facade 분산 락 안에서 안전하게 실행됨)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decreaseOptionStock(Long optionId, int quantity) {
        // 1. 실제 재고를 쥐고 있는 영속성 옵션 객체 확보
        ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.OPTION_NOT_FOUND));

        // 2. 현재 재고와 차감 수량 비교 정밀 검증
        if (option.getOptionStock() < quantity) {
            throw BusinessException.from(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        // 3. 차감 계산 및 품절(SOLDOUT) 상태 스위칭 조건문 전개
        int calculatedStock = option.getOptionStock() - quantity;
        OptionStatus targetStatus = calculatedStock == 0 ? OptionStatus.SOLDOUT : option.getStatus();

        // 4. 엔티티 내부 캡슐화 메서드 호출 (기존에 정의된 도메인 메서드 스펙 활용)
        option.updateOptionDetails(
                calculatedStock,
                option.getAdditionalPrice(),
                targetStatus
        );
    }
}