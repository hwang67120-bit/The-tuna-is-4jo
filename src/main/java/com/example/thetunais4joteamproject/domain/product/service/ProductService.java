package com.example.thetunais4joteamproject.domain.product.service;

import java.util.List;

import com.example.thetunais4joteamproject.domain.product.dto.CreateProductRequest;
import com.example.thetunais4joteamproject.domain.product.dto.GetAllProductResponse;
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
    public Long createProduct(Long memberId, CreateProductRequest createProductRequest) {
        // 1. 카테고리 존재 여부 확인 (없으면 예외 처리)
        Category category = categoryRepository.findById(createProductRequest.getCategoryId())
                .orElseThrow(() -> BusinessException.from(ErrorCode.CATEGORY_NOT_FOUND));

        // 2. 상품 마스터 생성 및 저장
        Product product = Product.of(
                memberId,
                category,
                createProductRequest.getName(),
                createProductRequest.getPrice(),
                createProductRequest.getDescription(),
                ProductStatus.ON_SALE
        );

        Product savedProduct = productRepository.save(product);

        // 단일 재고 제어 진입점 확보를 위한 기본 옵션 자동 생성 룰을 적용
        ProductOption defaultOption = ProductOption.of(
                savedProduct,
                "기본 옵션",
                0,
                0,
                OptionStatus.SOLDOUT
        );

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

    /**
     * 상품 목록 조회
     */
    public Page<GetAllProductResponse> getAllProducts(Pageable pageable) {
        // 페이지 번호가 음수로 들어오는 비정상적인 접근을 사전에 방어.
        if (pageable.getPageNumber() < 0) {
            throw BusinessException.from(ErrorCode.BAD_REQUEST);
        }

        Page<Product> products = productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.ON_SALE, pageable);

        // 정적 팩토리 메서드를 활용해 엔티티 리스트를 DTO 리스트로 매핑.
        return products.map(GetAllProductResponse::from);
    }

    /**
     * 상품 상세 조회
     */
    public GetProductDetailResponse getProductDetail(Long productId) {
        // // 상품이 존재하지 않으면 비즈니스 예외를 던집니다.
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
}
