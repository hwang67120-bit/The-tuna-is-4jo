package com.example.thetunais4joteamproject.domain.product.service;

import java.util.List;

import com.example.thetunais4joteamproject.domain.product.dto.CreateProductRequest;
import com.example.thetunais4joteamproject.domain.product.dto.GetAllProductResponse;
import com.example.thetunais4joteamproject.domain.product.dto.GetCategoryProductsResponse;
import com.example.thetunais4joteamproject.domain.product.dto.GetProductDetailResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Long createProduct(Long memberId, CreateProductRequest request) {
        // 1. 카테고리 존재 여부 확인 (없으면 예외 처리)
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> {
                return BusinessException.from(ErrorCode.CATEGORY_NOT_FOUND);
            });

        Product product = Product.of(
            null,
                category,
                request.name(),
                request.price(),
                request.description(),
                ProductStatus.ON_SALE
        );

        productRepository.save(product);

        return product.getId();
    }

    /**
     * 상품 세부 옵션 및 상태/추가금액 변경
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void updateOptionStocks(Long productId, List<UpdateOptionRequest> requests) {
        for (UpdateOptionRequest request : requests) {
            ProductOption option = productOptionRepository.findById(request.optionId())
                .orElseThrow(() -> {
                    return BusinessException.from(ErrorCode.OPTION_NOT_FOUND);
                });

            option.updateOptionDetails(
                request.optionStock(),
                request.additionalPrice(),
                request.status()
            );
        }
    }

    /**
     * 상품 대표 재고 변경
     */
    @Transactional
    public void updateRepresentativeStock(Long productId, RepresentStockRequest request) {
        ProductOption representativeOption = productOptionRepository.findTopByProductIdOrderByIdAsc(productId)
            .orElseThrow(() -> {
                return BusinessException.from(ErrorCode.OPTION_NOT_FOUND);
            });

        representativeOption.updateStock(request.stockQuantity());
    }

    /**
     * 상품 목록 조회
     */
    public Page<GetAllProductResponse> getAllProducts (Pageable pageable){
        // 페이지 번호가 음수로 들어오는 비정상적인 접근을 사전에 방어.
        if (pageable.getPageNumber() < 0) {
            throw BusinessException.from(ErrorCode.BAD_REQUEST);
        }

        Page<Product> products = productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.ON_SALE,
            pageable);

        // 정적 팩토리 메서드를 활용해 엔티티 리스트를 DTO 리스트로 매핑.
        return products.map(GetAllProductResponse::from);
    }

    /**
     * 상품 상세 조회
     */
    public GetProductDetailResponse getProductDetail (Long productId){
        // 상품이 존재하지 않으면 비즈니스 예외를 던진다.
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> {
                return BusinessException.from(ErrorCode.PRODUCT_NOT_FOUND);
            });

        // 해당 상품에 등록된 모든 세부 옵션 리스트를 조회.
        List<ProductOption> wpOptions = productOptionRepository.findAllByProductId(productId);

        // 옵션 엔티티 리스트를 정적 팩토리 메서드를 통해 응답 DTO 규격으로 변환.
        List<GetProductDetailResponse.ProductOptionResponse> optionResponses = wpOptions.stream()
            .map(GetProductDetailResponse.ProductOptionResponse::from)
            .toList();

        // 최종 상세 조회 결합 DTO를 반환.
        return GetProductDetailResponse.of(product, optionResponses);
    }

    /**
     * 상품 카테고리별 조회 (User)
     */
    public GetCategoryProductsResponse getProductsByCategory(Long categoryId) {
        // 입력된 카테고리 아이디로 카테고리를 조회.
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    return BusinessException.from(ErrorCode.CATEGORY_NOT_FOUND);
                });

        // 해당 카테고리에 포함된 판매 중 상태의 상품 목록 조회
        List<Product> products = productRepository.findAllByCategoryIdAndStatus(categoryId, ProductStatus.ON_SALE);

        // 조회된 상품 엔티티 목록을 하위 응답 DTO 규격으로 변환
        List<GetCategoryProductsResponse.CategoryProductResponse> productResponses = products.stream()
                .map((Product product) -> {
                    return GetCategoryProductsResponse.CategoryProductResponse.from(product);
                })
                .toList();

        // 카테고리 정보와 상품 목록 결합 객체 반환
        return GetCategoryProductsResponse.of(category, productResponses);
    }
}
