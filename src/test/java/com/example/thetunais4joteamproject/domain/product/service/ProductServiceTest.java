package com.example.thetunais4joteamproject.domain.product.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.thetunais4joteamproject.domain.product.dto.*;
import com.example.thetunais4joteamproject.domain.product.entity.*;
import com.example.thetunais4joteamproject.domain.product.repository.*;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@InjectMocks
	private ProductService productService;

	@Mock
	private ProductRepository productRepository;
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private ProductOptionRepository productOptionRepository;
	@Mock
	private ProductSearchService productSearchService;

	@Test
	@DisplayName("상품 생성 시 존재하지 않는 카테고리 ID이면 CATEGORY_NOT_FOUND 예외를 발생시킨다.")
	void createProduct_ThrowsCategoryNotFound() {
		// given
		CreateProductRequest request = CreateProductRequest.of(999L, "상품", 1000, "설명", null, Collections.emptyList());
		given(categoryRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> productService.createProduct(1L, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
	}

	@Test
	@DisplayName("상품 생성 시 정상적으로 상품 마스터 및 하위 옵션들을 저장한다.")
	void createProduct_Success() {
		// given
		Category category = mock(Category.class);
		CreateProductRequest.ProductOptionRequest optionReq = new CreateProductRequest.ProductOptionRequest("옵션1", 10,
			0);
		CreateProductRequest request = CreateProductRequest.of(1L, "새 상품", 15000, "상세설명", null, List.of(optionReq));

		given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

		// when
		Long productId = productService.createProduct(1L, request);

		// then
		verify(productRepository, times(1)).save(any(Product.class));
		verify(productOptionRepository, times(1)).save(any(ProductOption.class));
	}

	@Test
	@DisplayName("상품 상세 조회 시 존재하지 않는 상품이면 PRODUCT_NOT_FOUND 예외가 발생한다.")
	void getProductDetail_NotFound_ThrowsException() {
		// given
		given(productRepository.findById(1L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> productService.getProductDetail(1L))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
	}

	@Test
	@DisplayName("상품 상세 조회 시 논리 삭제(DELETED)된 상품이면 PRODUCT_NOT_FOUND 예외가 발생한다.")
	void getProductDetail_DeletedProduct_ThrowsException() {
		// given
		Product deletedProduct = mock(Product.class);
		given(deletedProduct.getStatus()).willReturn(ProductStatus.DELETED);
		given(productRepository.findById(1L)).willReturn(Optional.of(deletedProduct));

		// when & then
		assertThatThrownBy(() -> productService.getProductDetail(1L))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
	}

	@Test
	@DisplayName("상품 카테고리별 조회 시 카테고리가 존재하지 않으면 CATEGORY_NOT_FOUND 예외가 발생한다.")
	void getProductsByCategory_ThrowsCategoryNotFound() {
		// given
		given(categoryRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> productService.getProductsByCategory(999L))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
	}

	@Test
	@DisplayName("상품 수정 시 존재하지 않는 카테고리 ID이면 CATEGORY_NOT_FOUND 예외를 발생시킨다.")
	void updateProduct_ThrowsCategoryNotFound() {
		// given
		Product product = mock(Product.class);
		UpdateProductRequest request = UpdateProductRequest.of(999L, "수정된 상품", 20000, "수정된 설명", null);

		given(productRepository.findById(1L)).willReturn(Optional.of(product));
		given(categoryRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> productService.updateProduct(1L, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
	}

	@Test
	@DisplayName("상품 삭제 시 soft delete가 정상 수행되고 옵션들이 모두 품절 상태로 변경된다.")
	void deleteProduct_Success() {
		// given
		Product product = mock(Product.class);
		ProductOption option = mock(ProductOption.class);

		given(productRepository.findById(1L)).willReturn(Optional.of(product));
		given(productOptionRepository.findAllByProductId(1L)).willReturn(List.of(option));

		// when
		productService.deleteProduct(1L);

		// then
		verify(product, times(1)).changeStatus(ProductStatus.DELETED);
		verify(option, times(1)).updateOptionDetails(eq(0), anyInt(), eq(OptionStatus.SOLDOUT));
	}

	@Test
	@DisplayName("대표 재고 변경 시 상품의 첫 번째 옵션 재고가 올바르게 업데이트된다.")
	void updateRepresentativeStock_Success() {
		// given
		ProductOption option = mock(ProductOption.class);
		RepresentStockRequest request = new RepresentStockRequest(50);

		given(productOptionRepository.findTopByProductIdOrderByIdAsc(1L)).willReturn(Optional.of(option));

		// when
		productService.updateRepresentativeStock(1L, request);

		// then
		verify(option, times(1)).updateStock(50);
	}

	@Test
	@DisplayName("옵션 재고 변경 시 입력 재고가 0개이면 강제로 품절 상태로 변경된다.")
	void updateOptionStocks_ZeroStock_ForcesSoldOut() {
		// given
		ProductOption option = mock(ProductOption.class);
		UpdateOptionRequest request = UpdateOptionRequest.of(1L, 0, 1000, OptionStatus.ON_SALE);

		given(productOptionRepository.findById(1L)).willReturn(Optional.of(option));

		// when
		productService.updateOptionStocks(1L, List.of(request));

		// then
		verify(option, times(1)).updateOptionDetails(0, 1000, OptionStatus.SOLDOUT);
	}

	@Test
	@DisplayName("no-offset 상품 목록 조회 시 size가 0 이하이면 BAD_REQUEST 예외가 발생한다.")
	void getAllProductsNoOffset_InvalidSize_ThrowsException() {
		// when & then
		assertThatThrownBy(() -> productService.getAllProductsNoOffset(10L, 0))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST);
	}

	@Test
	@DisplayName("no-offset 상품 목록 조회 시 정상적으로 상품 목록을 응답한다.")
	void getAllProductsNoOffset_Success() {
		// given
		Product product1 = mock(Product.class);
		Product product2 = mock(Product.class);
		Category category = mock(Category.class);

		given(category.getName()).willReturn("카테고리");

		given(product1.getId()).willReturn(10L);
		given(product1.getName()).willReturn("상품1");
		given(product1.getPrice()).willReturn(1000);
		given(product1.getDescription()).willReturn("설명1");
		given(product1.getStatus()).willReturn(ProductStatus.ON_SALE);
		given(product1.getCategory()).willReturn(category);

		given(product2.getId()).willReturn(9L);
		given(product2.getName()).willReturn("상품2");
		given(product2.getPrice()).willReturn(2000);
		given(product2.getDescription()).willReturn("설명2");
		given(product2.getStatus()).willReturn(ProductStatus.ON_SALE);
		given(product2.getCategory()).willReturn(category);

		given(productRepository.findAllByNoOffset(10L, 2)).willReturn(List.of(product1, product2));

		// when
		List<GetAllProductResponse> response = productService.getAllProductsNoOffset(10L, 2);

		// then
		assertThat(response).hasSize(2);
		assertThat(response.get(0).id()).isEqualTo(10L);
		assertThat(response.get(1).id()).isEqualTo(9L);
		assertThat(response.get(0).categoryName()).isEqualTo("카테고리");
		verify(productRepository, times(1)).findAllByNoOffset(10L, 2);
	}
}
