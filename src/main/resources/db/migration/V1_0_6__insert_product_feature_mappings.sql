-- Insert all products
INSERT INTO huddey_core.products (stripe_product_id, name, description, active) VALUES
('prod_TAENr8KnyzLi8I', 'Free Plan', 'Ideal for beginners and hobbyist creators', true),
('prod_SulaUk9xDSjvEX', 'Starter Plan', 'Perfect for growing creators', true),
('prod_SulaIh9gvfvSRN', 'Pro Plan', 'Advanced features for professional creators', true),
('prod_SulawnaPRdY6xG', 'Elite Plan', 'Ultimate plan for elite creators', true),
('prod_Sulayyf6qf6sEH', 'Teams Plan 2-10 seats', 'Team collaboration for 2-10 members', true),
('prod_Sulapoa2aXyAxi', 'Teams Plan 11-20 seats', 'Team collaboration for 11-20 members', true),
('prod_SulaeqrhOF1eh7', 'Teams Plan 21-30 seats', 'Team collaboration for 21-30 members', true),
('prod_Sula7iopS6mVjn', 'Teams Plan 31-50 seats', 'Team collaboration for 31-50 members', true),
('prod_SulaseY63WPgq9', 'Teams Pro Plan 2-10 seats', 'Advanced team features for 2-10 members', true),
('prod_SulaeOc7dYgYAh', 'Teams Pro Plan 11-20 seats', 'Advanced team features for 11-20 members', true),
('prod_SulaXHipbLtCMM', 'Teams Pro Plan 21-30 seats', 'Advanced team features for 21-30 members', true),
('prod_SuladTSJgv69nt', 'Teams Pro Plan 31-50 seats', 'Advanced team features for 31-50 members', true);

-- Insert Free Plan prices
INSERT INTO huddey_core.product_prices (stripe_price_id, product_id, plan_id, unit_amount, currency, recurring_interval, active) VALUES
('price_1SDtutCsPWgUOsg2TEJg6VVE', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_TAENr8KnyzLi8I'), 'HUDDEY_FREE', 0.00, 'eur', 'month', true);
-- Insert Starter Plan prices
INSERT INTO huddey_core.product_prices (stripe_price_id, product_id, plan_id, unit_amount, currency, recurring_interval, active) VALUES
('price_1Ryw3LCsPWgUOsg20ShZ4uLn', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaUk9xDSjvEX'), 'STARTER_MONTHLY', 15.00, 'eur', 'month', true),
('price_1Ryw3MCsPWgUOsg21bcOiCkR', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaUk9xDSjvEX'), 'STARTER_YEARLY', 153.00, 'eur', 'year', true);
-- Insert Pro Plan prices
INSERT INTO huddey_core.product_prices (stripe_price_id, product_id, plan_id, unit_amount, currency, recurring_interval, active) VALUES
('price_1Ryw3MCsPWgUOsg2PuITEfyW', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaIh9gvfvSRN'), 'PRO_MONTHLY',39.00, 'eur', 'month', true),
('price_1Ryw3NCsPWgUOsg214wC4gpo', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaIh9gvfvSRN'), 'PRO_YEARLY', 374.00, 'eur', 'year', true);
-- Insert Elite Plan prices
INSERT INTO huddey_core.product_prices (stripe_price_id, product_id, plan_id, unit_amount, currency, recurring_interval, active) VALUES
('price_1Ryw3NCsPWgUOsg2lgsmS3p1', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulawnaPRdY6xG'), 'ELITE_MONTHLY',99.00, 'eur', 'month', true),
('price_1Ryw3OCsPWgUOsg2UXvFZdJS', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulawnaPRdY6xG'), 'ELITE_YEARLY',891.00, 'eur', 'year', true);
-- Insert Teams Plan prices
INSERT INTO huddey_core.product_prices (stripe_price_id, product_id, plan_id, unit_amount, currency, recurring_interval, active) VALUES
('price_1Ryw3OCsPWgUOsg2TESdXzdA', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_Sulayyf6qf6sEH'), 'TEAM_MONTHLY_10',249.00, 'eur', 'month', true),
('price_1Ryw3PCsPWgUOsg27rbRJzOr', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_Sulayyf6qf6sEH'), 'TEAM_YEARLY_10',2539.00, 'eur', 'year', true),
('price_1Ryw3PCsPWgUOsg2NlBBcYfp', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_Sulapoa2aXyAxi'), 'TEAM_MONTHLY_20',429.00, 'eur', 'month', true),
('price_1Ryw3QCsPWgUOsg2OObTQ13i', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_Sulapoa2aXyAxi'), 'TEAM_YEARLY_20',4375.00, 'eur', 'year', true),
('price_1Ryw3QCsPWgUOsg2QKEghbhv', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaeqrhOF1eh7'), 'TEAM_MONTHLY_30',599.00, 'eur', 'month', true),
('price_1Ryw3RCsPWgUOsg2uYDc6YRD', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaeqrhOF1eh7'), 'TEAM_YEARLY_30',6109.00, 'eur', 'year', true),
('price_1Ryw3RCsPWgUOsg2TZ2ZN0wg', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_Sula7iopS6mVjn'), 'TEAM_MONTHLY_50',899.00, 'eur', 'month', true),
('price_1Ryw3SCsPWgUOsg2FwapBiQI', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_Sula7iopS6mVjn'), 'TEAM_YEARLY_50',9169.00, 'eur', 'year', true);
-- Insert Teams Pro Plan prices
INSERT INTO huddey_core.product_prices (stripe_price_id, product_id, plan_id, unit_amount, currency, recurring_interval, active) VALUES
('price_1Ryw3SCsPWgUOsg25mrojs1g', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaseY63WPgq9'), 'TEAM_PRO_MONTHLY_10', 425.00, 'eur', 'month', true),
('price_1Ryw3SCsPWgUOsg2lE3tsuIq', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaseY63WPgq9'), 'TEAM_PRO_YEARLY_10',4233.00, 'eur', 'year', true),
('price_1Ryw3TCsPWgUOsg2haaW4Gfs', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaeOc7dYgYAh'), 'TEAM_PRO_MONTHLY_20',645.00, 'eur', 'month', true),
('price_1Ryw3TCsPWgUOsg2jwKWDPR9', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaeOc7dYgYAh'), 'TEAM_PRO_YEARLY_20',6424.00, 'eur', 'year', true),
('price_1Ryw3UCsPWgUOsg2pCXs99GV', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaXHipbLtCMM'), 'TEAM_PRO_MONTHLY_30',845.00, 'eur', 'month', true),
('price_1Ryw3UCsPWgUOsg2Gn8ZqToD', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SulaXHipbLtCMM'), 'TEAM_PRO_YEARLY_30',8416.00, 'eur', 'year', true),
('price_1Ryw3VCsPWgUOsg2WGKsNinm', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SuladTSJgv69nt'), 'TEAM_PRO_MONTHLY_50',1185.00, 'eur', 'month', true),
('price_1Ryw3VCsPWgUOsg2efo5hQZ9', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_SuladTSJgv69nt'), 'TEAM_PRO_YEARLY_50',11802.00, 'eur', 'year', true);


-- ==========================================
-- ENABLE SUBFEATURES FOR PRODUCTS
-- ==========================================

DO $$
DECLARE
    v_free_id BIGINT;
    v_starter_id BIGINT;
    v_pro_id BIGINT;
    v_elite_id BIGINT;
BEGIN
    SELECT id INTO v_free_id FROM huddey_core.products WHERE name = 'Free Plan';
    SELECT id INTO v_starter_id FROM huddey_core.products WHERE name = 'Starter Plan';
    SELECT id INTO v_pro_id FROM huddey_core.products WHERE name = 'Pro Plan';
    SELECT id INTO v_elite_id FROM huddey_core.products WHERE name = 'Elite Plan';

-- 1. Free Plan
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
    (v_free_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Social networks'), '1'),
    (v_free_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Scheduled Posts Per Month'), '10'),
    (v_free_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Calendar Reminders'), 'true'),
    (v_free_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Creative Academy Access'), 'Not included'),
    (v_free_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Ideas Weekly'), '3'),
    (v_free_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Hashtags Weekly'), '2'),
    (v_free_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Premium Trials'), '3 days rotating premium trials with limitations');
    
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
    (v_free_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Analytics_Likes'), 'true'),
    (v_free_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Analytics_Reach'), 'true');
    
-- 2. Starter Plan
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Social networks'), '3'),
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Scheduling'), 'Unlimited content scheduling and publishing'),
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Creative Academy Access'), 'Group consultations only'),
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Ideas Per Month'), '10'),
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Hashtags Per Month'), '5'),
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Best Posting Time'), 'weekly analysis'),
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'TrendPulse Access'), 'Not included');

    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Analytics_Likes'), 'true'),
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Analytics_Reach'), 'true'),
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Analytics_Engagement'), 'true'),
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Analytics_Views'), 'true'),
    (v_starter_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Analytics_TrendImpression'), 'true');

-- 3. Pro Plan
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Social networks'), '7'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Extra Cost Per Platform'), '5'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Creative Academy Access'), 'Group consultations,1-on-1 consultations with experienced creators'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Ideas Per Month'), '30'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Hashtags Per Month'), '15'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Intelligence Ideas Per Month'), '10'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Intelligence Basic Hashtags Per Post'), '5'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Intelligence Caption Versions Per Post'), '1'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'TrendPulse Access'), 'Industry trend insights,Competitor trend insights'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'support_level'), 'Priority support');

    -- Subfeatures for Pro
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Analytics_Demographics'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Analytics_PeakTime'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Analytics_Performance'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AO_BestPosting'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AO_Competitor'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AO_ABTest'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='CAE_MentorMatch_Basic'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='CAE_Templates'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='CAE_Summaries'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AA_AutoRespond_50'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AA_BrandAlerts'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AA_Spam'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='PF_WeeklyPred'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='PF_GrowthForecast'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='PF_Trending3'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Int_FigmaView'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Int_GoogleDocs'), 'true'),
    (v_pro_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Int_GoogleSheets'), 'true');
    
-- 4. Elite Plan
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Social networks'), 'Unlimited'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Creative Academy Access'), '1-on-1 consultations with established creators,Exclusive sessions with top-tier creators via partnerships,Ability to set own consultation rates,Monetization through Academy'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Ideas Per Month'), '50'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Hashtags Per Month'), '30'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'TrendPulse Access'), 'Full access,Real-time predictions,Daily updates'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'AdsHub Lite Monthly Spend Cap'), '1000'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'support_level'), 'Priority+ support');

    -- Subfeatures for Elite
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Pred_Demographics'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Pred_Trends'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Pred_Reports'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='UCI_Ideas'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='UCI_Hashtags'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='UCI_Captions'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='UCI_Calendars'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AO_RealTime'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AO_DeepCompetitor'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AO_UnlimitedAB'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AO_Sentiment'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='CAE_MentorMatch_Adv'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='CAE_Paths'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='CAE_AutoTrans'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='CAE_Recs'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AA_AutoRespond_Unl'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AA_RealTime'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AA_AdvModeration'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='AA_Crisis'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='PF_RealTimePred'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='PF_AdvForecast'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='PF_InstantAlert'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='PF_Market'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Collab_Exclusive'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Collab_Marketplace'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Int_Google'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Int_Dropbox'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Int_Miro'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Int_Figma'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Ads_FB'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Ads_Insta'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Ads_Google'), 'true'),
    (v_elite_id, (SELECT id FROM huddey_core.feature_catalog WHERE feature_name='Ads_YT'), 'true');
    
-- 5. Teams Plans
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
    SELECT p.id, fc.id, 'true'
    FROM huddey_core.products p, huddey_core.feature_catalog fc 
    WHERE p.name LIKE 'Teams Plan%' 
    AND fc.feature_name IN (
        'Collab_WhiteLabel', 'Collab_Calendar', 'Collab_Approval', 'Collab_Library', 
        'Collab_Slack', 'Collab_Notion', 'Collab_Airtable', 'Collab_Miro',
        'Rep_Custom', 'Rep_Automated', 'Rep_CrossAccount',
        'Sec_SSO', 'Sec_Dropbox', 'Sec_Audit', 'Sec_Compliance',
        'Onb_Dedicated', 'Onb_Priority',
        'TAI_Batch', 'TAI_Forecast', 'TAI_Bench', 'TAI_Insights',
        'AdsPro_MultiClient', 'AdsPro_CrossPlat', 'AdsPro_Workflows', 'AdsPro_Reports', 'AdsPro_UnlSpend'
    );
     
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
    SELECT p.id, fc.id, '2 TB' FROM huddey_core.products p, huddey_core.feature_catalog fc WHERE p.name LIKE 'Teams Plan%' AND fc.feature_name = 'Included Storage';
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
    SELECT p.id, fc.id, '10 GB' FROM huddey_core.products p, huddey_core.feature_catalog fc WHERE p.name LIKE 'Teams Plan%' AND fc.feature_name = 'Max File Size';
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
    SELECT p.id, fc.id, '90 days' FROM huddey_core.products p, huddey_core.feature_catalog fc WHERE p.name LIKE 'Teams Plan%' AND fc.feature_name = 'Retention Period';
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
    SELECT p.id, fc.id, '+1 TB @ €40/mo' FROM huddey_core.products p, huddey_core.feature_catalog fc WHERE p.name LIKE 'Teams Plan%' AND fc.feature_name = 'Storage Add-on';

-- 6. Teams Pro Plans
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
    SELECT p.id, fc.id, 'true'
    FROM huddey_core.products p, huddey_core.feature_catalog fc 
    WHERE p.name LIKE 'Teams Pro Plan%' 
    AND fc.feature_name IN (
        'AdvCollab_Chat', 'AdvCollab_Rooms', 'AdvCollab_CoCreate', 'AdvCollab_AdvApproval', 
        'AdvCollab_Knowledge', 'AdvCollab_Kanban', 'AdvCollab_CrossTeam',
        'Rep_ROI', 'Rep_Agency', 'Rep_WhitePortals', 'Rep_BrandedExports',
        'CAT_Training', 'CAT_Roles', 'CAT_Dash', 'CAT_Guest',
        'PAI_Brainstorm', 'PAI_Sentiment', 'PAI_Forecast', 'PAI_Budget', 'PAI_Media',
        'AdsEnt_ClientApproval', 'AdsEnt_Attribution', 'AdsEnt_Optimization',
        'Beta Feature Access'
    );
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
    SELECT p.id, fc.id, 'SLA-backed 24/7 support' FROM huddey_core.products p, huddey_core.feature_catalog fc WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'support_level';
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
    SELECT p.id, fc.id, '10 TB' FROM huddey_core.products p, huddey_core.feature_catalog fc WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Included Storage';
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
    SELECT p.id, fc.id, '100 GB' FROM huddey_core.products p, huddey_core.feature_catalog fc WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Max File Size';
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
    SELECT p.id, fc.id, '120 days' FROM huddey_core.products p, huddey_core.feature_catalog fc WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Retention Period';
    INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
    SELECT p.id, fc.id, '+5 TB @ €150/mo' FROM huddey_core.products p, huddey_core.feature_catalog fc WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Storage Add-on';

END $$;