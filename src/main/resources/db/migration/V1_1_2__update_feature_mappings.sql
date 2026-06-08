-- V1_1_2__update_feature_mappings.sql
-- Implements strict Additive Feature Matrix by determining effective features for each plan
-- Rewritten to use pure SQL (no PL/pgSQL blocks)

-- 1. Clear Existing Mappings
DELETE FROM huddey_core.product_feature_catalog;

-- ==========================================
-- 2. INSERT FEATURE MAPPINGS
-- ==========================================

-- ========================
-- A. FREE PLAN
-- ========================
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, f.id, v.val
FROM huddey_core.products p
CROSS JOIN (VALUES 
    ('Social networks', '1'),
    ('Scheduled Posts Per Month', '10'),
    ('Content Scheduling', '10 posts per month'),
    ('Calendar Reminders', 'true'),
    ('Analytics_Likes', 'true'),
    ('Analytics_Reach', 'true'),
    ('Content Ideas Weekly', '3'),
    ('Hashtags Weekly', '2'),
    ('Included Storage', '1 GB'),
    ('Premium Trials', '3 days rotating'),
    ('support_level', 'Community forum')
) AS v(name, val)
JOIN huddey_core.feature_catalog f ON f.feature_name = v.name
WHERE p.name = 'Free Plan';


-- ========================
-- B. STARTER PLAN
-- ========================
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, f.id, v.val
FROM huddey_core.products p
CROSS JOIN (VALUES 
    ('Social networks', '3'),
    ('Scheduled Posts Per Month', 'Unlimited'),
    ('Content Scheduling', 'Unlimited'),
    ('Content Ideas Per Month', '10'),
    ('Hashtags Per Month', '5'),
    ('Included Storage', '5 GB'),
    ('support_level', 'Email (48h)'),
    ('Calendar Reminders', 'true'),
    ('Analytics_Likes', 'true'),
    ('Analytics_Reach', 'true'),
    ('Premium Trials', '3 days rotating'),
    ('Analytics_Engagement', 'true'),
    ('Analytics_Views', 'true'),
    ('Analytics_TrendImpression', 'true'),
    ('Best Posting Time', 'Weekly analysis'),
    ('Creative Academy Access', 'Group consultations')
) AS v(name, val)
JOIN huddey_core.feature_catalog f ON f.feature_name = v.name
WHERE p.name = 'Starter Plan';


-- ========================
-- C. PRO PLAN
-- ========================
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, f.id, v.val
FROM huddey_core.products p
CROSS JOIN (VALUES 
    ('Social networks', '7'),
    ('Content Ideas Per Month', '30'),
    ('Hashtags Per Month', '15'),
    ('Creative Academy Access', 'Group + 1-on-1 (1/mo)'),
    ('support_level', 'Priority (24h)'),
    ('Included Storage', '25 GB'),
    ('Scheduled Posts Per Month', 'Unlimited'),
    ('Content Scheduling', 'Unlimited'),
    ('Calendar Reminders', 'true'),
    ('Analytics_Likes', 'true'),
    ('Analytics_Reach', 'true'),
    ('Analytics_Engagement', 'true'),
    ('Analytics_Views', 'true'),
    ('Analytics_TrendImpression', 'true'),
    ('Best Posting Time', 'Weekly analysis'),
    ('Extra Cost Per Platform', '5'),
    ('Analytics_Demographics', 'true'),
    ('Analytics_PeakTime', 'true'),
    ('Analytics_Performance', 'true'),
    ('AA_AutoRespond_50', 'true'),
    ('TrendPulse Access', 'Industry & competitor insights'),
    ('AO_ABTest', 'true'),
    ('PF_WeeklyPred', 'true'),
    ('PF_GrowthForecast', 'true'),
    ('PF_Trending3', 'true'),
    ('AA_BrandAlerts', 'true'),
    ('AA_Spam', 'true'),
    ('Int_FigmaView', 'true'),
    ('Int_GoogleDocs', 'true'),
    ('Int_GoogleSheets', 'true'),
    ('CAE_MentorMatch_Basic', 'true'),
    ('CAE_Templates', 'true'),
    ('CAE_Summaries', 'true')
) AS v(name, val)
JOIN huddey_core.feature_catalog f ON f.feature_name = v.name
WHERE p.name = 'Pro Plan';


-- ========================
-- D. ELITE PLAN
-- ========================
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, f.id, v.val
FROM huddey_core.products p
CROSS JOIN (VALUES 
    ('Social networks', 'Unlimited'),
    ('Content Ideas Per Month', '50+'),
    ('Hashtags Per Month', '30+'),
    ('Content Intelligence Caption Versions Per Post', 'Multiple'),
    ('AO_UnlimitedAB', 'true'),
    ('AA_AutoRespond_Unl', 'true'),
    ('TrendPulse Access', 'Real-time + Daily updates'),
    ('PF_InstantAlert', 'true'),
    ('AA_RealTime', 'true'),
    ('AA_AdvModeration', 'true'),
    ('PF_RealTimePred', 'true'),
    ('PF_AdvForecast', 'true'),
    ('Creative Academy Access', 'Unlimited 1:1 + Exclusive'),
    ('CAE_MentorMatch_Adv', 'true'),
    ('CAE_Paths', 'true'),
    ('CAE_AutoTrans', 'true'),
    ('support_level', 'Priority+ (12h)'),
    ('Included Storage', '100 GB'),
    ('Int_Google', 'true'),
    ('Int_Dropbox', 'true'),
    ('Int_Miro', 'true'),
    ('Int_Figma', 'true'),
    ('Scheduled Posts Per Month', 'Unlimited'),
    ('Content Scheduling', 'Unlimited'),
    ('Calendar Reminders', 'true'),
    ('Analytics_Likes', 'true'),
    ('Analytics_Reach', 'true'),
    ('Analytics_Engagement', 'true'),
    ('Analytics_Views', 'true'),
    ('Analytics_TrendImpression', 'true'),
    ('Best Posting Time', 'Weekly analysis'),
    ('Analytics_Demographics', 'true'),
    ('Analytics_PeakTime', 'true'),
    ('Analytics_Performance', 'true'),
    ('Int_GoogleDocs', 'true'),
    ('Int_GoogleSheets', 'true'),
    ('CAE_Templates', 'true'),
    ('UCI_Calendars', 'true'),
    ('AO_Sentiment', 'true'),
    ('CAE_Recs', 'true'),
    ('AA_Crisis', 'true'),
    ('PF_Market', 'true'),
    ('AdsHub Lite Monthly Spend Cap', '1000'),
    ('Ads_FB', 'true'),
    ('Ads_Insta', 'true'),
    ('Ads_Google', 'true'),
    ('Collab_Exclusive', 'true'),
    ('Collab_Marketplace', 'true')
) AS v(name, val)
JOIN huddey_core.feature_catalog f ON f.feature_name = v.name
WHERE p.name = 'Elite Plan';


-- ========================
-- E. TEAMS PLANS (All Variants)
-- ========================
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, f.id, v.val
FROM huddey_core.products p
CROSS JOIN (VALUES 
    ('AdsPro_MultiClient', 'true'),
    ('AdsPro_Workflows', 'true'),
    ('AdsPro_CrossPlat', 'true'),
    ('AdsPro_Reports', 'true'),
    ('AdsPro_UnlSpend', 'true'),
    ('support_level', 'Priority (12h) + Onboarding'),
    ('Included Storage', '2 TB'),
    ('Social networks', 'Unlimited'),
    ('Content Ideas Per Month', '50+'),
    ('Hashtags Per Month', '30+'),
    ('Content Intelligence Caption Versions Per Post', 'Multiple'),
    ('AO_UnlimitedAB', 'true'),
    ('AA_AutoRespond_Unl', 'true'),
    ('TrendPulse Access', 'Real-time + Daily updates'),
    ('PF_InstantAlert', 'true'),
    ('AA_RealTime', 'true'),
    ('AA_AdvModeration', 'true'),
    ('PF_RealTimePred', 'true'),
    ('PF_AdvForecast', 'true'),
    ('Creative Academy Access', 'Unlimited 1:1 + Exclusive'),
    ('CAE_MentorMatch_Adv', 'true'),
    ('CAE_Paths', 'true'),
    ('CAE_AutoTrans', 'true'),
    ('Int_Google', 'true'),
    ('Int_Dropbox', 'true'),
    ('Int_Miro', 'true'),
    ('Int_Figma', 'true'),
    ('Scheduled Posts Per Month', 'Unlimited'),
    ('Content Scheduling', 'Unlimited'),
    ('Calendar Reminders', 'true'),
    ('Analytics_Likes', 'true'),
    ('Analytics_Reach', 'true'),
    ('Analytics_Engagement', 'true'),
    ('Analytics_Views', 'true'),
    ('Analytics_TrendImpression', 'true'),
    ('Best Posting Time', 'Weekly analysis'),
    ('Analytics_Demographics', 'true'),
    ('Analytics_PeakTime', 'true'),
    ('Analytics_Performance', 'true'),
    ('Int_GoogleDocs', 'true'),
    ('Int_GoogleSheets', 'true'),
    ('CAE_Templates', 'true'),
    ('UCI_Calendars', 'true'),
    ('AO_Sentiment', 'true'),
    ('CAE_Recs', 'true'),
    ('AA_Crisis', 'true'),
    ('PF_Market', 'true'),
    ('Collab_Exclusive', 'true'),
    ('Collab_Marketplace', 'true'),
    ('Collab_WhiteLabel', 'true'),
    ('Collab_Calendar', 'true'),
    ('Collab_Approval', 'true'),
    ('Collab_Library', 'true'),
    ('Collab_Slack', 'true'),
    ('Collab_Notion', 'true'),
    ('Collab_Airtable', 'true'),
    ('Collab_Miro', 'true'),
    ('Rep_Custom', 'true'),
    ('Rep_Automated', 'true'),
    ('Rep_CrossAccount', 'true'),
    ('Sec_SSO', 'true'),
    ('Sec_Dropbox', 'true'),
    ('Sec_Audit', 'true'),
    ('TAI_Batch', 'true'),
    ('TAI_Forecast', 'true'),
    ('TAI_Bench', 'true'),
    ('TAI_Insights', 'true')
) AS v(name, val)
JOIN huddey_core.feature_catalog f ON f.feature_name = v.name
WHERE p.name LIKE 'Teams Plan%';


-- ========================
-- F. TEAMS PRO PLANS (All Variants)
-- ========================
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, f.id, v.val
FROM huddey_core.products p
CROSS JOIN (VALUES 
    ('AdsEnt_ClientApproval', 'true'),
    ('AdsEnt_Attribution', 'true'),
    ('AdsEnt_Optimization', 'true'),
    ('AdvCollab_AdvApproval', 'true'),
    ('support_level', '24/7 SLA Backed'),
    ('Included Storage', '10 TB'),
    ('Max File Size', '100 GB'),
    ('Retention Period', '120 days'),
    ('Storage Add-on', '+5TB @ €150/mo'),
    ('Social networks', 'Unlimited'),
    ('Content Ideas Per Month', '50+'),
    ('Hashtags Per Month', '30+'),
    ('Content Intelligence Caption Versions Per Post', 'Multiple'),
    ('Creative Academy Access', 'Unlimited 1:1 + Exclusive'),
    ('TrendPulse Access', 'Real-time + Daily updates'),
    ('Scheduled Posts Per Month', 'Unlimited'),
    ('Content Scheduling', 'Unlimited'),
    ('Calendar Reminders', 'true'),
    ('Analytics_Likes', 'true'),
    ('Analytics_Reach', 'true'),
    ('Analytics_Engagement', 'true'),
    ('Analytics_Views', 'true'),
    ('Analytics_TrendImpression', 'true'),
    ('Best Posting Time', 'Weekly analysis'),
    ('Analytics_Demographics', 'true'),
    ('Analytics_PeakTime', 'true'),
    ('Analytics_Performance', 'true'),
    ('Int_GoogleDocs', 'true'),
    ('Int_GoogleSheets', 'true'),
    ('CAE_Templates', 'true'),
    ('UCI_Calendars', 'true'),
    ('AO_Sentiment', 'true'),
    ('CAE_Recs', 'true'),
    ('AA_Crisis', 'true'),
    ('PF_Market', 'true'),
    ('Collab_Exclusive', 'true'),
    ('Collab_Marketplace', 'true'),
    ('Collab_WhiteLabel', 'true'),
    ('Collab_Calendar', 'true'),
    ('Collab_Library', 'true'),
    ('Collab_Slack', 'true'),
    ('Collab_Notion', 'true'),
    ('Collab_Airtable', 'true'),
    ('Collab_Miro', 'true'),
    ('Rep_Custom', 'true'),
    ('Rep_Automated', 'true'),
    ('Rep_CrossAccount', 'true'),
    ('Sec_SSO', 'true'),
    ('Sec_Dropbox', 'true'),
    ('Sec_Audit', 'true'),
    ('TAI_Batch', 'true'),
    ('TAI_Forecast', 'true'),
    ('TAI_Bench', 'true'),
    ('TAI_Insights', 'true'),
    ('AdsPro_MultiClient', 'true'),
    ('AdsPro_Workflows', 'true'),
    ('AdsPro_CrossPlat', 'true'),
    ('AdsPro_Reports', 'true'),
    ('AdsPro_UnlSpend', 'true'),
    ('AdvCollab_Chat', 'true'),
    ('AdvCollab_Rooms', 'true'),
    ('AdvCollab_CoCreate', 'true'),
    ('AdvCollab_Knowledge', 'true'),
    ('AdvCollab_Kanban', 'true'),
    ('AdvCollab_CrossTeam', 'true'),
    ('Rep_ROI', 'true'),
    ('Rep_Agency', 'true'),
    ('Rep_WhitePortals', 'true'),
    ('Rep_BrandedExports', 'true'),
    ('CAT_Training', 'true'),
    ('CAT_Roles', 'true'),
    ('CAT_Dash', 'true'),
    ('CAT_Guest', 'true'),
    ('PAI_Brainstorm', 'true'),
    ('PAI_Sentiment', 'true'),
    ('PAI_Forecast', 'true'),
    ('PAI_Budget', 'true'),
    ('PAI_Media', 'true'),
    ('Beta Feature Access', 'true')
) AS v(name, val)
JOIN huddey_core.feature_catalog f ON f.feature_name = v.name
WHERE p.name LIKE 'Teams Pro Plan%';
