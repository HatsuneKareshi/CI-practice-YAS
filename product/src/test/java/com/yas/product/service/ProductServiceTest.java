package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Product;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.product.ProductListVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MediaService mediaService;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ProductOptionRepository productOptionRepository;
    @Mock
    private ProductOptionValueRepository productOptionValueRepository;
    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;
    @Mock
    private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setSlug("test-product");
        product.setStockTrackingEnabled(false);
        product.setPrice(100.0);
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProductDetailVm() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        var result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Product");
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void getLatestProducts_WhenCountGreaterThanZero_ShouldReturnProductList() {
        Pageable pageable = PageRequest.of(0, 5);
        when(productRepository.getLatestProducts(pageable)).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getLatestProducts(5);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Test Product");
    }

    @Test
    void getLatestProducts_WhenCountIsZeroOrLess_ShouldReturnEmptyList() {
        List<ProductListVm> result = productService.getLatestProducts(0);

        assertThat(result).isEmpty();
    }

    @Test
    void createProduct_ValidProductPostVm_ShouldSaveAndReturnProduct() {
        com.yas.product.viewmodel.product.ProductPostVm postVm = new com.yas.product.viewmodel.product.ProductPostVm(
                "Test Product", "test-product", 1L, List.of(1L), "Short Desc", "Description", "Specs",
                "SKU-123", "GTIN-123", 100.0, com.yas.product.model.enumeration.DimensionUnit.CM, 10.0, 10.0, 10.0,
                100.0,
                true, true, true, true, true,
                "Meta Title", "Meta Keyword", "Meta Desc", 1L, java.util.Collections.emptyList(),
                java.util.Collections.emptyList(), java.util.Collections.emptyList(), java.util.Collections.emptyList(),
                java.util.Collections.emptyList(),
                1L);

        when(brandRepository.findById(1L)).thenReturn(Optional.of(new com.yas.product.model.Brand()));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(new com.yas.product.model.Category()));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        when(productRepository.findBySlugAndIsPublishedTrue("test-product")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("GTIN-123")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-123")).thenReturn(Optional.empty());

        var result = productService.createProduct(postVm);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Product");
        verify(productRepository, org.mockito.Mockito.times(1)).save(any(Product.class));
        verify(productCategoryRepository).saveAll(any());
        verify(productImageRepository).saveAll(any());
    }

    @Test
    void updateProduct_ValidProductPutVm_ShouldUpdateExistingProduct() {
        com.yas.product.viewmodel.product.ProductPutVm putVm = new com.yas.product.viewmodel.product.ProductPutVm(
                "Updated Product", "updated-product", 200.0, true, true, true, true, true,
                1L, List.of(1L), "Short Desc", "Description", "Specs", "SKU-321", "GTIN-321",
                100.0, com.yas.product.model.enumeration.DimensionUnit.CM, 10.0, 10.0, 10.0,
                "Meta", "Meta", "Meta", 1L, java.util.Collections.emptyList(), java.util.Collections.emptyList(),
                java.util.Collections.emptyList(), java.util.Collections.emptyList(), java.util.Collections.emptyList(),
                1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(new com.yas.product.model.Brand()));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(new com.yas.product.model.Category()));

        com.yas.product.model.ProductOption option = new com.yas.product.model.ProductOption();
        option.setId(1L);
        when(productOptionRepository.findAllByIdIn(any())).thenReturn(List.of(option));

        productService.updateProduct(1L, putVm);

        assertThat(product.getName()).isEqualTo("Updated Product");
        assertThat(product.getSlug()).isEqualTo("updated-product");
        verify(productRepository).saveAll(any());
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldUnpublishProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        assertThat(product.isPublished()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void getProductSlug_WhenProductExists_ShouldReturnSlugGetVm() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        var result = productService.getProductSlug(1L);

        assertThat(result.slug()).isEqualTo("test-product");
        assertThat(result.productVariantId()).isNull();
    }

    @Test
    void getProductByIds_ShouldReturnProductListVms() {
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));

        var result = productService.getProductByIds(List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Test Product");
    }

    @Test
    void getProductByCategoryIds_ShouldReturnProductListVms() {
        when(productRepository.findByCategoryIdsIn(List.of(1L))).thenReturn(List.of(product));

        var result = productService.getProductByCategoryIds(List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).slug()).isEqualTo("test-product");
    }

    @Test
    void getProductByBrandIds_ShouldReturnProductListVms() {
        when(productRepository.findByBrandIdsIn(List.of(1L))).thenReturn(List.of(product));

        var result = productService.getProductByBrandIds(List.of(1L));

        assertThat(result).hasSize(1);
    }
}
