-- V1_1_3__create_effective_features_view.sql
-- 1. Clean up redundant mappings from product_feature_catalog
-- Deletes rows where a plan inherits the exact same feature value and status from a parent plan.
WITH RECURSIVE product_hierarchy AS (
    -- Anchor: Direct parent
    SELECT 
        pi.product_id AS child_id,
        pi.inherits_from_product_id AS parent_id,
        1 AS depth
    FROM huddey_core.plan_inheritance pi
    
    UNION ALL
    
    -- Recursive: Ancestors
    SELECT 
        ph.child_id,
        pi.inherits_from_product_id AS parent_id,
        ph.depth + 1 AS depth
    FROM product_hierarchy ph
    JOIN huddey_core.plan_inheritance pi ON ph.parent_id = pi.product_id
),
parent_features AS (
    -- Get features from parent products, ranking them so the closest parent comes first
    SELECT 
        ph.child_id AS product_id,
        pfc.feature_id,
        pfc.feature_value,
        pfc.is_enabled,
        ROW_NUMBER() OVER (
            PARTITION BY ph.child_id, pfc.feature_id 
            ORDER BY ph.depth ASC
        ) AS rn
    FROM product_hierarchy ph
    JOIN huddey_core.product_feature_catalog pfc ON ph.parent_id = pfc.product_id
)
DELETE FROM huddey_core.product_feature_catalog pfc
USING parent_features pf
WHERE pfc.product_id = pf.product_id
  AND pfc.feature_id = pf.feature_id
  AND pf.rn = 1 -- closest parent
  AND pfc.feature_value = pf.feature_value
  AND pfc.is_enabled = pf.is_enabled;

-- 2. Create recursive CTE view to resolve inherited subscription features dynamically
CREATE OR REPLACE VIEW huddey_core.effective_product_features AS
WITH RECURSIVE product_hierarchy AS (
    -- Anchor: Every product inherits from itself (level 0)
    SELECT 
        id AS target_product_id,
        id AS product_id,
        0 AS level
    FROM huddey_core.products
    
    UNION ALL
    
    -- Recursive step: Go up the inheritance chain
    SELECT 
        ph.target_product_id,
        pi.inherits_from_product_id AS product_id,
        ph.level + 1 AS level
    FROM product_hierarchy ph
    JOIN huddey_core.plan_inheritance pi ON ph.product_id = pi.product_id
),
ranked_features AS (
    -- Retrieve all features from the hierarchy, ranking them so specific plan features override general ones
    SELECT 
        ph.target_product_id AS product_id,
        pfc.feature_id,
        pfc.feature_value,
        pfc.is_enabled,
        ROW_NUMBER() OVER (
            PARTITION BY ph.target_product_id, pfc.feature_id 
            ORDER BY ph.level ASC
        ) AS rn
    FROM product_hierarchy ph
    JOIN huddey_core.product_feature_catalog pfc ON ph.product_id = pfc.product_id
)
SELECT 
    (ROW_NUMBER() OVER ())::BIGINT AS id, -- Unique stable ID for Hibernate/JPA mapping
    product_id,
    feature_id,
    feature_value,
    is_enabled
FROM ranked_features
WHERE rn = 1 AND is_enabled = true;
