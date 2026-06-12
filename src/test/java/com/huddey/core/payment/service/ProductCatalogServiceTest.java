package com.huddey.core.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huddey.core.payment.data.dto.ProductResponse;
import com.huddey.core.payment.data.entity.EffectiveProductFeature;
import com.huddey.core.payment.data.entity.FeatureCatalog;
import com.huddey.core.payment.data.entity.Product;
import com.huddey.core.payment.repository.EffectiveProductFeatureRepository;
import com.huddey.core.payment.repository.ProductCatalogRepository;
import com.stripe.exception.StripeException;

@ExtendWith(MockitoExtension.class)
class ProductCatalogServiceTest {

  @Mock private ProductCatalogRepository productCatalogRepository;
  @Mock private EffectiveProductFeatureRepository effectiveProductFeatureRepository;

  @InjectMocks private ProductCatalogService productCatalogService;

  @Test
  void getAllProducts_shouldRetrieveAndMapActiveProductsAndFeatures() throws StripeException {
    // Arrange
    Product product = new Product();
    product.setId(1L);
    product.setStripeProductId("prod_123");
    product.setName("Pro Plan");
    product.setDescription("Pro description");
    product.setActive(true);
    product.setCreatedAt(OffsetDateTime.now());
    product.setUpdatedAt(OffsetDateTime.now());
    product.setPrices(new ArrayList<>());

    FeatureCatalog feature = new FeatureCatalog();
    feature.setId(10L);
    feature.setFeatureName("Social networks");
    feature.setFeatureCategory("Socials");
    feature.setDisplayName("Social networks");

    EffectiveProductFeature epf = new EffectiveProductFeature();
    epf.setId(100L);
    epf.setProduct(product);
    epf.setFeature(feature);
    epf.setFeatureValue("7");
    epf.setIsEnabled(true);

    when(productCatalogRepository.findAllActiveWithPrices()).thenReturn(List.of(product));
    when(effectiveProductFeatureRepository.findAllWithProduct()).thenReturn(List.of(epf));

    // Act
    List<ProductResponse> result = productCatalogService.getAllProducts();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    ProductResponse response = result.getFirst();
    assertEquals("prod_123", response.getStripeProductId());
    assertEquals("Pro Plan", response.getName());
    assertEquals(1, response.getFeatures().size());
    assertEquals("Social networks: 7", response.getFeatures().getFirst().getDisplayValue());

    verify(productCatalogRepository).findAllActiveWithPrices();
    verify(effectiveProductFeatureRepository).findAllWithProduct();
  }
}
