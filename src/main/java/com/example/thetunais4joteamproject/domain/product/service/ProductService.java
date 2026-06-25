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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductOptionRepository productOptionRepository;

    /**
     * 상품 생성
     */
    @Transactional
    public Long createProduct(Long memberId, CreateProductRequest request) {
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
    } // 🎯 범인이었던 createProduct 메서드의 닫는 중괄호 자리를 완벽히 찾아 고정했습니다!

    /**
     * 상품 세부 옵션 및 상태/추가금액 변경
     */
    @Transactional
    public void updateOptionStocks(Long productId, List<UpdateOptionRequest> requests) {
        for (UpdateOptionRequest updateReq : requests) {
            ProductOption option = productOptionRepository.findById(updateReq.optionId())
                .orElseThrow(() -> {
                    return BusinessException.from(ErrorCode.OPTION_NOT_FOUND);
                });

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
            .orElseThrow(() -> {
                return BusinessException.from(ErrorCode.OPTION_NOT_FOUND);
            });

        representativeOption.updateStock(request.stockQuantity());
    }

    /**
     * 상품 목록 조회
     */
    public Page<GetAllProductResponse> getAllProducts(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw BusinessException.from(ErrorCode.BAD_REQUEST);
        }

        Page<Product> products = productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.ON_SALE, pageable);

        return products.map((Product product) -> {
            return GetAllProductResponse.from(product);
        });
    }

    /**
     * 상품 상세 조회
     */
    public GetProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> {
                return BusinessException.from(ErrorCode.PRODUCT_NOT_FOUND);
            });

        List<ProductOption> wpOptions = productOptionRepository.findAllByProductId(productId);

        List<GetProductDetailResponse.ProductOptionResponse> optionResponses = wpOptions.stream()
            .map((ProductOption option) -> {
                return GetProductDetailResponse.ProductOptionResponse.from(option);
            })
            .toList();

        return GetProductDetailResponse.of(product, optionResponses);
    }

    /**
     * 상품 카테고리별 조회 (User)
     */
    public GetCategoryProductsResponse getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> {
                return BusinessException.from(ErrorCode.CATEGORY_NOT_FOUND);
            });

        List<Product> products = productRepository.findAllByCategoryIdAndStatus(categoryId, ProductStatus.ON_SALE);

        List<GetCategoryProductsResponse.CategoryProductResponse> productResponses = products.stream()
            .map((Product product) -> {
                return GetCategoryProductsResponse.CategoryProductResponse.from(product);
            })
            .toList();

        return GetCategoryProductsResponse.of(category, productResponses);
    }

    /**
     * 상품 수정 (Admin)
     */
    @Transactional
    public void updateProduct(Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> {
                return BusinessException.from(ErrorCode.PRODUCT_NOT_FOUND);
            });

        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> {
                return BusinessException.from(ErrorCode.CATEGORY_NOT_FOUND);
            });

        product.updateProduct(
            category,
            request.name(),
            request.price(),
            request.description()
        );
    }

    /**
     * 상품 삭제 (Admin - Soft Delete)
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> {
                return BusinessException.from(ErrorCode.PRODUCT_NOT_FOUND);
            });

        product.changeStatus(ProductStatus.DELETED);

        List<ProductOption> options = productOptionRepository.findAllByProductId(productId);
        for (ProductOption option : options) {
            option.updateOptionDetails(
                0,
                option.getAdditionalPrice(),
                OptionStatus.SOLDOUT
            );
        }
    }
}