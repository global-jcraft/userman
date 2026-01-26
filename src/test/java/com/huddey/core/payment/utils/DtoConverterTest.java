package com.huddey.core.payment.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.huddey.core.payment.data.dto.ProductFeatureDto;
import com.huddey.core.payment.data.entity.FeatureCatalog;
import com.huddey.core.payment.data.entity.ProductFeatureCatalog;

class DtoConverterTest {

  @Test
  void convertToFeatureDtos_shouldBuildHierarchyCorrectly() {
    // 1. Setup Data

    // Parent Group (e.g. "Analytics") - Not enabled explicitly for product
    FeatureCatalog analyticsGroup = new FeatureCatalog();
    analyticsGroup.setId(100L);
    analyticsGroup.setFeatureName("Analytics_Group");
    analyticsGroup.setFeatureCategory("Analytics");
    analyticsGroup.setDisplayName("Analytics");

    // Child Feature 1 (e.g. "Likes") - Enabled
    FeatureCatalog likesFeature = new FeatureCatalog();
    likesFeature.setId(101L);
    likesFeature.setFeatureName("Analytics_Likes");
    likesFeature.setFeatureCategory("Analytics");
    likesFeature.setDisplayName("Likes");
    likesFeature.setParent(analyticsGroup);

    // Child Feature 2 (e.g. "Reach") - Enabled
    FeatureCatalog reachFeature = new FeatureCatalog();
    reachFeature.setId(102L);
    reachFeature.setFeatureName("Analytics_Reach");
    reachFeature.setFeatureCategory("Analytics");
    reachFeature.setDisplayName("Reach");
    reachFeature.setParent(analyticsGroup);

    // Root Feature (e.g. "Support") - Enabled
    FeatureCatalog supportFeature = new FeatureCatalog();
    supportFeature.setId(200L);
    supportFeature.setFeatureName("Support");
    supportFeature.setFeatureCategory("Support");
    supportFeature.setDisplayName("Priority Support");
    // No parent

    // Create ProductFeatureCatalog entries (the flat list from DB)
    ProductFeatureCatalog pfc1 = new ProductFeatureCatalog();
    pfc1.setFeature(likesFeature);
    pfc1.setFeatureValue("true");

    ProductFeatureCatalog pfc2 = new ProductFeatureCatalog();
    pfc2.setFeature(reachFeature);
    pfc2.setFeatureValue("true");

    ProductFeatureCatalog pfc3 = new ProductFeatureCatalog();
    pfc3.setFeature(supportFeature);
    pfc3.setFeatureValue("true");

    List<ProductFeatureCatalog> inputFeatures = List.of(pfc1, pfc2, pfc3);

    // 2. Execute
    List<ProductFeatureDto> result = DtoConverter.convertToFeatureDtos(inputFeatures);

    // 3. Verify
    assertEquals(2, result.size(), "Should have 2 root items (Analytics Group and Support)");

    // Check Analytics Group (Synthesized Parent)
    ProductFeatureDto analyticsDto =
        result.stream()
            .filter(f -> "Analytics_Group".equals(f.getFeatureName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Analytics Group not found"));

    assertEquals(2, analyticsDto.getSubFeatures().size(), "Analytics should have 2 sub-features");
    assertTrue(
        analyticsDto.getSubFeatures().stream()
            .anyMatch(f -> "Analytics_Likes".equals(f.getFeatureName())));
    assertTrue(
        analyticsDto.getSubFeatures().stream()
            .anyMatch(f -> "Analytics_Reach".equals(f.getFeatureName())));

    // Check Support Feature (Root)
    ProductFeatureDto supportDto =
        result.stream()
            .filter(f -> "Support".equals(f.getFeatureName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Support feature not found"));

    // Leaf node subfeatures is an empty list (Java object state)
    // Serialization (Jackson) will skip it due to @JsonInclude(NON_EMPTY)
    assertEquals(0, supportDto.getSubFeatures().size());
  }
}
