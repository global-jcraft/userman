CREATE TABLE huddey_core.plan_inheritance (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES huddey_core.products(id),
    inherits_from_product_id BIGINT NOT NULL REFERENCES huddey_core.products(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, inherits_from_product_id)
);

-- Seed inheritance data
DO $$
BEGIN
    INSERT INTO huddey_core.plan_inheritance (product_id, inherits_from_product_id)
    VALUES 
    -- STARTER inherits FREE
    ((SELECT id FROM huddey_core.products WHERE name = 'Starter Plan'), 
     (SELECT id FROM huddey_core.products WHERE name = 'Free Plan')),
     
    -- PRO inherits STARTER
    ((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), 
     (SELECT id FROM huddey_core.products WHERE name = 'Starter Plan')),
     
    -- ELITE inherits PRO
    ((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), 
     (SELECT id FROM huddey_core.products WHERE name = 'Pro Plan')),
     
    -- TEAMS inherits ELITE (All Teams plans inherit from Elite base features effectively, or from a specific Teams Base if we had one. 
    -- The request says: TEAMS = ELITE + Upgrades. We should map ALL Teams variants to inherit from ELITE for now, 
    -- OR picking one Representative Teams plan if the logic is strictly linear. 
    -- Looking at the file: "TEAMS PLAN = ELITE + UPGRADES + NEW". 
    -- But there are multiple Teams products (2-10 seats, 11-20, etc). 
    -- If they all share the same features, they should all inherit from ELITE.
    
    -- Teams 2-10
    ((SELECT id FROM huddey_core.products WHERE name = 'Teams Plan 2-10 seats'), 
     (SELECT id FROM huddey_core.products WHERE name = 'Elite Plan')),
    -- Teams 11-20
    ((SELECT id FROM huddey_core.products WHERE name = 'Teams Plan 11-20 seats'), 
     (SELECT id FROM huddey_core.products WHERE name = 'Elite Plan')),
    -- Teams 21-30
    ((SELECT id FROM huddey_core.products WHERE name = 'Teams Plan 21-30 seats'), 
     (SELECT id FROM huddey_core.products WHERE name = 'Elite Plan')),
    -- Teams 31-50
    ((SELECT id FROM huddey_core.products WHERE name = 'Teams Plan 31-50 seats'), 
     (SELECT id FROM huddey_core.products WHERE name = 'Elite Plan')),
     
    -- TEAMS PRO inherits TEAMS
    -- Teams Pro 2-10 inherits Teams 2-10 (conceptually) OR just inherits generic Teams features? 
    -- The model is linear: Free -> Starter -> Pro -> Elite -> Teams -> Teams Pro.
    -- So Teams Pro 2-10 should inherit from Teams 2-10.
    
    ((SELECT id FROM huddey_core.products WHERE name = 'Teams Pro Plan 2-10 seats'), 
     (SELECT id FROM huddey_core.products WHERE name = 'Teams Plan 2-10 seats')),
     
    ((SELECT id FROM huddey_core.products WHERE name = 'Teams Pro Plan 11-20 seats'), 
     (SELECT id FROM huddey_core.products WHERE name = 'Teams Plan 11-20 seats')),
     
    ((SELECT id FROM huddey_core.products WHERE name = 'Teams Pro Plan 21-30 seats'), 
     (SELECT id FROM huddey_core.products WHERE name = 'Teams Plan 21-30 seats')),
     
    ((SELECT id FROM huddey_core.products WHERE name = 'Teams Pro Plan 31-50 seats'), 
     (SELECT id FROM huddey_core.products WHERE name = 'Teams Plan 31-50 seats'));

END $$;
