package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Product;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailInfoVm;
import java.util.ArrayList;
import java.util.Collections;
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
        when(mediaService.getMedia(1L)).thenReturn(new NoFileMediaVm(1L, "thumb", "", "", "http://thumb"));
        when(mediaService.getMedia(2L)).thenReturn(new NoFileMediaVm(2L, "img", "", "", "http://img"));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertThat(result.getVariations()).hasSize(1);
        assertThat(result.getVariations().get(0).name()).isEqualTo("Variation 1");
        assertThat(result.getVariations().get(0).options()).containsEntry(1L, "Red");
    }

    @Test
    void getProductDetailById_WhenMainProductHasNoThumbnailAndNoImages_ShouldReturnEmptyMediaParts() {
        product.setThumbnailMediaId(null);
        product.setProductImages(null);
        product.setAttributeValues(Collections.emptyList());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertThat(result.getThumbnail()).isNull();
        assertThat(result.getProductImages()).isEmpty();
    }

    @Test
    void getProductDetailById_WhenVariationIsUnpublished_ShouldBeFilteredOut() {
        product.setHasOptions(true);
        Product unpublished = new Product();
        unpublished.setId(3L);
        unpublished.setName("Variation hidden");
        unpublished.setSlug("variation-hidden");
        unpublished.setPublished(false);
        unpublished.setPrice(99.0);
        unpublished.setProductImages(new ArrayList<>());

        product.setProducts(List.of(unpublished));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(new NoFileMediaVm(1L, "thumb", "", "", "http://thumb"));
        when(mediaService.getMedia(2L)).thenReturn(new NoFileMediaVm(2L, "img", "", "", "http://img"));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertThat(result.getVariations()).isEmpty();
    }

    @Test
    void getProductDetailById_WhenVariationHasOptions_ShouldMapOptionValueMap() {
        product.setHasOptions(true);
        Product variation = new Product();
        variation.setId(4L);
        variation.setName("Variation 2");
        variation.setSlug("variation-2");
        variation.setPublished(true);
        variation.setPrice(160.0);
        variation.setProductImages(new ArrayList<>());
        product.setProducts(List.of(variation));

        ProductOption option = new ProductOption();
        option.setId(10L);
        option.setName("Size");

        ProductOptionCombination combination = new ProductOptionCombination();
        combination.setProduct(variation);
        combination.setProductOption(option);
        combination.setValue("XL");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combination));
        when(mediaService.getMedia(1L)).thenReturn(new NoFileMediaVm(1L, "thumb", "", "", "http://thumb"));
        when(mediaService.getMedia(2L)).thenReturn(new NoFileMediaVm(2L, "img", "", "", "http://img"));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertThat(result.getVariations()).hasSize(1);
        assertThat(result.getVariations().getFirst().options()).containsEntry(10L, "XL");
    }
}
