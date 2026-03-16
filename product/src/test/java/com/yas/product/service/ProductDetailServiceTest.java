package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Product;
import com.yas.product.model.ProductImage;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailInfoVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @InjectMocks
    private ProductDetailService productDetailService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setSlug("test-product");
        product.setPublished(true);
        product.setThumbnailMediaId(1L);

        ProductImage image = new ProductImage();
        image.setImageId(2L);
        product.setProductImages(List.of(image));
    }

    @Test
    void getProductDetailById_WhenProductExistsAndPublished_ShouldReturnDetailInfo() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(new NoFileMediaVm(1L, "thumb", "", "", "http://thumb"));
        when(mediaService.getMedia(2L)).thenReturn(new NoFileMediaVm(2L, "img", "", "", "http://img"));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getThumbnail().url()).isEqualTo("http://thumb");
        assertThat(result.getProductImages()).hasSize(1);
    }

    @Test
    void getProductDetailById_WhenProductDoesNotExist_ShouldThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productDetailService.getProductDetailById(1L));
    }

    @Test
    void getProductDetailById_WhenProductIsNotPublished_ShouldThrowNotFoundException() {
        product.setPublished(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(NotFoundException.class, () -> productDetailService.getProductDetailById(1L));
    }

    @Test
    void getProductDetailById_WhenProductHasVariations_ShouldReturnVariations() {
        product.setHasOptions(true);

        Product variation = new Product();
        variation.setId(2L);
        variation.setName("Variation 1");
        variation.setSlug("variation-1");
        variation.setPublished(true);
        variation.setPrice(150.0);
        product.setProducts(List.of(variation));

        com.yas.product.model.ProductOption option = new com.yas.product.model.ProductOption();
        option.setId(1L);
        option.setName("Color");

        com.yas.product.model.ProductOptionCombination combination = new com.yas.product.model.ProductOptionCombination();
        combination.setProduct(variation);
        combination.setProductOption(option);
        combination.setValue("Red");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combination));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertThat(result.getVariations()).hasSize(1);
        assertThat(result.getVariations().get(0).name()).isEqualTo("Variation 1");
        assertThat(result.getVariations().get(0).options()).containsEntry(1L, "Red");
    }
}
