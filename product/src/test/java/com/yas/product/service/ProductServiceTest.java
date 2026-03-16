package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.ProductRelated;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeGroup;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.model.enumeration.FilterExistInWhSelection;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.product.viewmodel.product.ProductListGetFromCategoryVm;
import com.yas.product.viewmodel.product.ProductQuantityPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import com.yas.product.viewmodel.product.ProductListVm;
import com.yas.product.viewmodel.product.ProductThumbnailGetVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    @Test
    void getProductSlug_WhenProductIsVariant_ShouldReturnParentSlugAndVariantId() {
        Product parent = new Product();
        parent.setSlug("parent-slug");
        product.setParent(parent);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        var result = productService.getProductSlug(1L);

        assertThat(result.slug()).isEqualTo("parent-slug");
        assertThat(result.productVariantId()).isEqualTo(1L);
    }

    @Test
    void getProductsForWarehouse_ShouldMapRepositoryResult() {
        when(productRepository.findProductForWarehouse("ip", "sku", List.of(1L), "ALL"))
            .thenReturn(List.of(product));

        var result = productService.getProductsForWarehouse("ip", "sku", List.of(1L), FilterExistInWhSelection.ALL);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(product.getId());
    }

    @Test
    void updateProductQuantity_ShouldUpdateOnlyMatchedProducts() {
        Product p1 = buildProduct(10L, true, 10L);
        Product p2 = buildProduct(11L, true, 20L);
        when(productRepository.findAllByIdIn(List.of(10L, 11L))).thenReturn(List.of(p1, p2));

        productService.updateProductQuantity(List.of(
            new ProductQuantityPostVm(10L, 99L),
            new ProductQuantityPostVm(11L, 77L)
        ));

        assertThat(p1.getStockQuantity()).isEqualTo(99L);
        assertThat(p2.getStockQuantity()).isEqualTo(77L);
        verify(productRepository).saveAll(List.of(p1, p2));
    }

    @Test
    void subtractStockQuantity_ShouldMergeDuplicateIdsAndNeverGoBelowZero() {
        Product trackEnabled = buildProduct(100L, true, 10L);
        Product trackingDisabled = buildProduct(200L, false, 50L);
        when(productRepository.findAllByIdIn(List.of(100L, 100L, 200L, 300L, 400L)))
            .thenReturn(List.of(trackEnabled, trackingDisabled));

        productService.subtractStockQuantity(List.of(
            new ProductQuantityPutVm(100L, 8L),
            new ProductQuantityPutVm(100L, 5L),
            new ProductQuantityPutVm(200L, 2L),
            new ProductQuantityPutVm(300L, 1L),
            new ProductQuantityPutVm(400L, 1L)
        ));

        assertThat(trackEnabled.getStockQuantity()).isEqualTo(0L);
        assertThat(trackingDisabled.getStockQuantity()).isEqualTo(50L);
        verify(productRepository).saveAll(List.of(trackEnabled, trackingDisabled));
    }

    @Test
    void restoreStockQuantity_ShouldIncreaseStockForTrackingEnabledOnly() {
        Product trackEnabled = buildProduct(500L, true, 10L);
        Product trackingDisabled = buildProduct(600L, false, 20L);
        when(productRepository.findAllByIdIn(List.of(500L, 600L))).thenReturn(List.of(trackEnabled, trackingDisabled));

        productService.restoreStockQuantity(List.of(
            new ProductQuantityPutVm(500L, 4L),
            new ProductQuantityPutVm(600L, 9L)
        ));

        assertThat(trackEnabled.getStockQuantity()).isEqualTo(14L);
        assertThat(trackingDisabled.getStockQuantity()).isEqualTo(20L);
        verify(productRepository).saveAll(List.of(trackEnabled, trackingDisabled));
    }

    @Test
    void getProductCheckoutList_ShouldFillThumbnailWhenMediaUrlPresent() {
        Product checkoutProduct = buildProduct(700L, true, 5L);
        checkoutProduct.setThumbnailMediaId(999L);

        Page<Product> page = new PageImpl<>(List.of(checkoutProduct), PageRequest.of(0, 20), 1);
        when(productRepository.findAllPublishedProductsByIds(eq(List.of(700L)), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(999L)).thenReturn(new NoFileMediaVm(999L, "", "", "", "http://cdn/img.jpg"));

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 20, List.of(700L));

        assertThat(result.productCheckoutListVms()).hasSize(1);
        assertThat(result.productCheckoutListVms().getFirst().thumbnailUrl()).isEqualTo("http://cdn/img.jpg");
    }

    @Test
    void getProductCheckoutList_ShouldKeepEmptyThumbnailWhenMediaUrlEmpty() {
        Product checkoutProduct = buildProduct(701L, true, 5L);
        checkoutProduct.setThumbnailMediaId(888L);

        Page<Product> page = new PageImpl<>(List.of(checkoutProduct), PageRequest.of(0, 20), 1);
        when(productRepository.findAllPublishedProductsByIds(eq(List.of(701L)), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(888L)).thenReturn(new NoFileMediaVm(888L, "", "", "", ""));

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 20, List.of(701L));

        assertThat(result.productCheckoutListVms()).hasSize(1);
        assertThat(result.productCheckoutListVms().getFirst().thumbnailUrl()).isEmpty();
    }

    @Test
    void getRelatedProductsStorefront_ShouldFilterUnpublishedRelatedProducts() {
        Product main = buildProduct(800L, true, 5L);
        Product publishedRelated = buildProduct(801L, true, 5L);
        publishedRelated.setThumbnailMediaId(901L);
        Product hiddenRelated = buildProduct(802L, false, 5L);
        hiddenRelated.setPublished(false);
        hiddenRelated.setThumbnailMediaId(902L);

        ProductRelated r1 = ProductRelated.builder().product(main).relatedProduct(publishedRelated).build();
        ProductRelated r2 = ProductRelated.builder().product(main).relatedProduct(hiddenRelated).build();
        Page<ProductRelated> relatedPage = new PageImpl<>(List.of(r1, r2), PageRequest.of(0, 5), 2);

        when(productRepository.findById(800L)).thenReturn(Optional.of(main));
        when(productRelatedRepository.findAllByProduct(eq(main), any(Pageable.class))).thenReturn(relatedPage);
        when(mediaService.getMedia(anyLong()))
            .thenAnswer(invocation -> {
                Long mediaId = invocation.getArgument(0);
                return new NoFileMediaVm(mediaId, "", "", "", "http://img/" + mediaId);
            });

        var result = productService.getRelatedProductsStorefront(800L, 0, 5);

        assertThat(result.productContent()).hasSize(1);
        assertThat(result.productContent().getFirst().id()).isEqualTo(801L);
    }

    @Test
    void getProductsWithFilter_ShouldTrimAndLowercaseInputAndMapPage() {
        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 5), 1);
        when(productRepository.getProductsWithFilter("laptop", "Brand A", PageRequest.of(0, 5))).thenReturn(page);

        var result = productService.getProductsWithFilter(0, 5, "  LaPToP ", "Brand A  ");

        assertThat(result.productContent()).hasSize(1);
        assertThat(result.productContent().getFirst().id()).isEqualTo(1L);
    }

    @Test
    void getProductsByBrand_WhenBrandNotFound_ShouldThrowNotFoundException() {
        when(brandRepository.findBySlug("missing-brand")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductsByBrand("missing-brand"));
    }

    @Test
    void getProductsByBrand_WhenBrandExists_ShouldReturnThumbnails() {
        Brand brand = new Brand();
        brand.setId(11L);
        brand.setName("Brand");

        Product p = buildProduct(900L, true, 10L);
        p.setThumbnailMediaId(600L);

        when(brandRepository.findBySlug("brand-slug")).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(p));
        when(mediaService.getMedia(600L)).thenReturn(new NoFileMediaVm(600L, "", "", "", "http://img/600"));

        var result = productService.getProductsByBrand("brand-slug");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().thumbnailUrl()).isEqualTo("http://img/600");
    }

    @Test
    void getProductsFromCategory_ShouldMapPagedProducts() {
        Category category = new Category();
        category.setId(12L);
        category.setName("Phones");
        category.setSlug("phones");

        Product p = buildProduct(901L, true, 10L);
        p.setThumbnailMediaId(601L);
        ProductCategory productCategory = ProductCategory.builder().product(p).category(category).build();
        Page<ProductCategory> page = new PageImpl<>(List.of(productCategory), PageRequest.of(0, 2), 1);

        when(categoryRepository.findBySlug("phones")).thenReturn(Optional.of(category));
        when(productCategoryRepository.findAllByCategory(PageRequest.of(0, 2), category)).thenReturn(page);
        when(mediaService.getMedia(601L)).thenReturn(new NoFileMediaVm(601L, "", "", "", "http://img/601"));

        ProductListGetFromCategoryVm result = productService.getProductsFromCategory(0, 2, "phones");

        assertThat(result.productContent()).hasSize(1);
        assertThat(result.productContent().getFirst().thumbnailUrl()).isEqualTo("http://img/601");
    }

    @Test
    void getFeaturedProductsById_WhenMediaEmptyAndHasParent_ShouldUseParentThumbnail() {
        Product parent = buildProduct(910L, true, 5L);
        parent.setThumbnailMediaId(710L);
        Product child = buildProduct(911L, true, 5L);
        child.setParent(parent);
        child.setThumbnailMediaId(711L);

        when(productRepository.findAllByIdIn(List.of(911L))).thenReturn(List.of(child));
        when(productRepository.findById(910L)).thenReturn(Optional.of(parent));
        when(mediaService.getMedia(711L)).thenReturn(new NoFileMediaVm(711L, "", "", "", ""));
        when(mediaService.getMedia(710L)).thenReturn(new NoFileMediaVm(710L, "", "", "", "http://img/710"));

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(911L));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().thumbnailUrl()).isEqualTo("http://img/710");
    }

    @Test
    void getListFeaturedProducts_ShouldReturnFeaturePage() {
        Product featured = buildProduct(920L, true, 3L);
        featured.setThumbnailMediaId(720L);
        Page<Product> page = new PageImpl<>(List.of(featured), PageRequest.of(0, 3), 1);

        when(productRepository.getFeaturedProduct(PageRequest.of(0, 3))).thenReturn(page);
        when(mediaService.getMedia(720L)).thenReturn(new NoFileMediaVm(720L, "", "", "", "http://img/720"));

        var result = productService.getListFeaturedProducts(0, 3);

        assertThat(result.productList()).hasSize(1);
        assertThat(result.productList().getFirst().thumbnailUrl()).isEqualTo("http://img/720");
    }

    @Test
    void getProductDetail_ShouldMapAttributesGroupsAndImages() {
        Product detail = buildProduct(930L, true, 5L);
        detail.setShortDescription("short");
        detail.setDescription("desc");
        detail.setSpecification("spec");
        detail.setAllowedToOrder(true);
        detail.setPublished(true);
        detail.setFeatured(true);
        detail.setHasOptions(true);
        detail.setThumbnailMediaId(730L);

        ProductImage image = new ProductImage();
        image.setImageId(731L);
        detail.setProductImages(List.of(image));

        Category c = new Category();
        c.setName("Category A");
        ProductCategory pc = ProductCategory.builder().product(detail).category(c).build();
        detail.setProductCategories(List.of(pc));

        ProductAttributeGroup group = new ProductAttributeGroup();
        group.setId(1L);
        group.setName("General");

        ProductAttribute attr = ProductAttribute.builder().name("Color").productAttributeGroup(group).build();
        ProductAttributeValue attrValue = new ProductAttributeValue();
        attrValue.setProduct(detail);
        attrValue.setProductAttribute(attr);
        attrValue.setValue("Black");
        detail.setAttributeValues(List.of(attrValue));

        when(productRepository.findBySlugAndIsPublishedTrue("detail-slug")).thenReturn(Optional.of(detail));
        when(mediaService.getMedia(730L)).thenReturn(new NoFileMediaVm(730L, "", "", "", "http://img/730"));
        when(mediaService.getMedia(731L)).thenReturn(new NoFileMediaVm(731L, "", "", "", "http://img/731"));

        var result = productService.getProductDetail("detail-slug");

        assertThat(result.id()).isEqualTo(930L);
        assertThat(result.thumbnailMediaUrl()).isEqualTo("http://img/730");
        assertThat(result.productImageMediaUrls()).containsExactly("http://img/731");
        assertThat(result.productAttributeGroups()).hasSize(1);
        assertThat(result.productAttributeGroups().getFirst().name()).isEqualTo("General");
    }

    @Test
    void deleteProduct_WhenVariantHasOptionCombinations_ShouldDeleteCombinations() {
        Product parent = buildProduct(940L, true, 5L);
        Product child = buildProduct(941L, true, 5L);
        child.setParent(parent);

        ProductOption option = new ProductOption();
        option.setId(1L);
        ProductOptionCombination combination = new ProductOptionCombination();
        combination.setProduct(child);
        combination.setProductOption(option);
        combination.setValue("v");

        when(productRepository.findById(941L)).thenReturn(Optional.of(child));
        when(productOptionCombinationRepository.findAllByProduct(child)).thenReturn(List.of(combination));

        productService.deleteProduct(941L);

        assertThat(child.isPublished()).isFalse();
        verify(productOptionCombinationRepository).deleteAll(List.of(combination));
        verify(productRepository).save(child);
    }

    @Test
    void getProductsByMultiQuery_ShouldMapPagedThumbnails() {
        Product p = buildProduct(950L, true, 5L);
        p.setThumbnailMediaId(750L);
        Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 5), 1);

        when(productRepository.findByProductNameAndCategorySlugAndPriceBetween("iphone", "phones", 10.0, 100.0,
            PageRequest.of(0, 5))).thenReturn(page);
        when(mediaService.getMedia(750L)).thenReturn(new NoFileMediaVm(750L, "", "", "", "http://img/750"));

        var result = productService.getProductsByMultiQuery(0, 5, " iPhone ", "phones ", 10.0, 100.0);

        assertThat(result.productContent()).hasSize(1);
        assertThat(result.productContent().getFirst().thumbnailUrl()).isEqualTo("http://img/750");
    }

    @Test
    void getProductVariationsByParentId_WhenHasOptionsTrue_ShouldMapOnlyPublishedVariations() {
        Product parent = buildProduct(960L, true, 5L);
        parent.setHasOptions(true);

        Product publishedVar = buildProduct(961L, true, 5L);
        publishedVar.setPublished(true);
        publishedVar.setThumbnailMediaId(761L);
        ProductImage image = new ProductImage();
        image.setImageId(762L);
        publishedVar.setProductImages(List.of(image));

        Product hiddenVar = buildProduct(962L, true, 5L);
        hiddenVar.setPublished(false);
        hiddenVar.setProductImages(List.of());

        parent.setProducts(List.of(publishedVar, hiddenVar));

        ProductOption option = new ProductOption();
        option.setId(2L);
        ProductOptionCombination poc = new ProductOptionCombination();
        poc.setProduct(publishedVar);
        poc.setProductOption(option);
        poc.setValue("Blue");

        when(productRepository.findById(960L)).thenReturn(Optional.of(parent));
        when(productOptionCombinationRepository.findAllByProduct(publishedVar)).thenReturn(List.of(poc));
        when(mediaService.getMedia(761L)).thenReturn(new NoFileMediaVm(761L, "", "", "", "http://img/761"));
        when(mediaService.getMedia(762L)).thenReturn(new NoFileMediaVm(762L, "", "", "", "http://img/762"));

        var result = productService.getProductVariationsByParentId(960L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().options()).containsEntry(2L, "Blue");
        assertThat(result.getFirst().productImages()).hasSize(1);
    }

    @Test
    void exportProducts_ShouldMapExportRows() {
        Product exportProduct = buildProduct(970L, true, 1L);
        exportProduct.setShortDescription("short");
        exportProduct.setDescription("desc");
        exportProduct.setSpecification("spec");
        exportProduct.setSku("SKU");
        exportProduct.setGtin("GTIN");
        exportProduct.setMetaTitle("meta-title");
        exportProduct.setMetaKeyword("meta-key");
        exportProduct.setMetaDescription("meta-desc");

        when(productRepository.getExportingProducts("p", "B")).thenReturn(List.of(exportProduct));

        var result = productService.exportProducts(" P ", " B ");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(970L);
        assertThat(result.getFirst().brandName()).isEqualTo("Brand");
    }

    private Product buildProduct(Long id, boolean stockTrackingEnabled, Long stockQuantity) {
        Product p = new Product();
        p.setId(id);
        p.setName("P-" + id);
        p.setSlug("p-" + id);
        p.setPrice(100.0);
        p.setPublished(true);
        p.setVisibleIndividually(true);
        p.setAllowedToOrder(true);
        p.setFeatured(false);
        p.setStockTrackingEnabled(stockTrackingEnabled);
        p.setStockQuantity(stockQuantity);
        p.setTaxClassId(1L);

        com.yas.product.model.Brand brand = new com.yas.product.model.Brand();
        brand.setId(1L);
        brand.setName("Brand");
        p.setBrand(brand);
        return p;
    }
}
