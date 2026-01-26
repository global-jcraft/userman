-- 1. Feature Catalog table - Master catalog of all features
CREATE TABLE IF NOT EXISTS huddey_core.feature_catalog (
    id BIGSERIAL PRIMARY KEY,
    feature_name VARCHAR(255) NOT NULL UNIQUE,
    feature_category VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    description TEXT,
    feature_type VARCHAR(50) DEFAULT 'string', -- string, number, boolean, group
    default_value TEXT,
    parent_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feature_catalog_parent FOREIGN KEY (parent_id) REFERENCES huddey_core.feature_catalog(id) ON DELETE CASCADE
);

-- 2. Product Feature Catalog table - Junction table linking products to features
CREATE TABLE IF NOT EXISTS huddey_core.product_feature_catalog (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    feature_id BIGINT NOT NULL,
    feature_value TEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_feature_catalog_product FOREIGN KEY (product_id) REFERENCES huddey_core.products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_feature_catalog_feature FOREIGN KEY (feature_id) REFERENCES huddey_core.feature_catalog(id) ON DELETE CASCADE,
    UNIQUE(product_id, feature_id)
);

-- Create indexes
CREATE INDEX idx_feature_catalog_category ON huddey_core.feature_catalog(feature_category);
CREATE INDEX idx_feature_catalog_active ON huddey_core.feature_catalog(is_active);
CREATE INDEX idx_feature_catalog_parent ON huddey_core.feature_catalog(parent_id);
CREATE INDEX idx_product_feature_catalog_product ON huddey_core.product_feature_catalog(product_id);
CREATE INDEX idx_product_feature_catalog_feature ON huddey_core.product_feature_catalog(feature_id);
CREATE INDEX idx_product_feature_catalog_enabled ON huddey_core.product_feature_catalog(is_enabled);

-- 3. Populate Feature Catalog with Hierarchy
DO $$
DECLARE
    -- Feature Group IDs
    v_socials_root BIGINT;
    v_analytics_root BIGINT;
    v_creative_academy_root BIGINT;
    v_ai_root BIGINT;
    v_trendpulse_root BIGINT;
    v_integrations_root BIGINT;
    v_support_root BIGINT;
    v_adshub_root BIGINT;
    v_collab_root BIGINT;
    v_reporting_root BIGINT;
    v_security_root BIGINT;
    v_onboarding_root BIGINT;
    v_storage_root BIGINT;
    v_extras_root BIGINT;

    -- Sub-Group IDs
    v_basic_analytics BIGINT;
    v_advanced_analytics BIGINT;
    v_adv_pred_analytics BIGINT;
    v_audience_opt BIGINT;
    v_ca_enhance BIGINT;
    v_adv_auto BIGINT;
    v_predictive BIGINT;
    v_ul_content_intel BIGINT;
    v_team_ai BIGINT;
    v_adshub_lite BIGINT;
    v_adshub_pro BIGINT;
    v_adshub_ent BIGINT;
    v_collab_workflow BIGINT;
    v_collab_monet BIGINT;
    v_adv_collab BIGINT;
    v_report_white BIGINT;
    v_report_delivery BIGINT;
    v_ca_teams BIGINT;
    v_premium_agency BIGINT;
    v_int_avail BIGINT;
    
BEGIN
    -- ==========================================
    -- A. CREATE ROOT GROUPS
    -- ==========================================

    -- Socials
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Socials_Root', 'Socials', 'Socials', 'group', NULL) RETURNING id INTO v_socials_root;
    
    -- Analytics
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Analytics_Root', 'Analytics', 'Analytics', 'group', NULL) RETURNING id INTO v_analytics_root;
    
    -- Creative Academy
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('CreativeAcademy_Root', 'CreativeAcademy', 'Creative Academy', 'group', NULL) RETURNING id INTO v_creative_academy_root;

    -- AI
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('AI_Root', 'AI', 'AI', 'group', NULL) RETURNING id INTO v_ai_root;

    -- TrendPulse
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('TrendPulse_Root', 'TrendPulse', 'TrendPulse', 'group', NULL) RETURNING id INTO v_trendpulse_root;

    -- Integrations
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Integrations_Root', 'Integrations', 'Integrations', 'group', NULL) RETURNING id INTO v_integrations_root;

    -- Support
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Support_Root', 'Support', 'Support', 'group', NULL) RETURNING id INTO v_support_root;

    -- Storage
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Storage_Root', 'Storage', 'Storage', 'group', NULL) RETURNING id INTO v_storage_root;

    -- Extras
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Extras_Root', 'Extras', 'Extras', 'group', NULL) RETURNING id INTO v_extras_root;

    -- AdsHub
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('AdsHub_Root', 'AdsHub', 'AdsHub', 'group', NULL) RETURNING id INTO v_adshub_root;

    -- Collaboration
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Collaboration_Root', 'Collaboration', 'Collaboration', 'group', NULL) RETURNING id INTO v_collab_root;

    -- Reporting
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Reporting_Root', 'Reporting', 'Reporting', 'group', NULL) RETURNING id INTO v_reporting_root;

    -- Security
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Security_Root', 'Security', 'Security', 'group', NULL) RETURNING id INTO v_security_root;
    
    -- Onboarding
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('OnboardingSupport_Root', 'OnboardingSupport', 'Onboarding', 'group', NULL) RETURNING id INTO v_onboarding_root;


    -- ==========================================
    -- B. POPULATE SUB-FEATURES
    -- ==========================================

    -- Socials Children
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES
    ('Social networks', 'Socials', 'Social Networks', 'number', v_socials_root),
    ('Scheduled Posts Per Month', 'Socials', 'Scheduled Posts Per Month', 'number', v_socials_root),
    ('Calendar Reminders', 'Socials', 'Calendar Reminders', 'boolean', v_socials_root),
    ('Content Scheduling', 'Socials', 'Content Scheduling', 'string', v_socials_root),
    ('Extra Cost Per Platform', 'Socials', 'Extra Cost Per Platform', 'number', v_socials_root);

    -- Analytics Groups
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Basic Analytics', 'Analytics', 'Basic Analytics', 'group', v_analytics_root) RETURNING id INTO v_basic_analytics;
    
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Advanced Analytics', 'Analytics', 'Advanced Analytics', 'group', v_analytics_root) RETURNING id INTO v_advanced_analytics;
    
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Advanced Predictive Analytics', 'Analytics', 'Advanced Predictive Analytics', 'group', v_analytics_root) RETURNING id INTO v_adv_pred_analytics;

    -- Analytics Children
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES 
    ('Analytics_Likes', 'Analytics', 'Likes', 'boolean', v_basic_analytics),
    ('Analytics_Reach', 'Analytics', 'Reach', 'boolean', v_basic_analytics),
    ('Analytics_Engagement', 'Analytics', 'Engagement', 'boolean', v_basic_analytics),
    ('Analytics_Views', 'Analytics', 'Views', 'boolean', v_basic_analytics),
    ('Analytics_TrendImpression', 'Analytics', 'Engagement Trends', 'boolean', v_basic_analytics),

    ('Analytics_Demographics', 'Analytics', 'Audience Demographics', 'boolean', v_advanced_analytics),
    ('Analytics_PeakTime', 'Analytics', 'Peak Performance Times', 'boolean', v_advanced_analytics),
    ('Analytics_Performance', 'Analytics', 'Weekly/Monthly Performance Analytics', 'boolean', v_advanced_analytics),

    ('Pred_Demographics', 'Analytics', 'Predictive Demographics', 'boolean', v_adv_pred_analytics),
    ('Pred_Trends', 'Analytics', 'Predictive Trends', 'boolean', v_adv_pred_analytics),
    ('Pred_Reports', 'Analytics', 'Predictive Weekly/Monthly Reports', 'boolean', v_adv_pred_analytics);
    
    -- Creative Academy
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Creative Academy Access', 'CreativeAcademy', 'Creative Academy Access', 'string', v_creative_academy_root);
    
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Creative Academy Teams Features', 'CreativeAcademyTeams', 'Creative Academy Teams Features', 'group', v_creative_academy_root) RETURNING id INTO v_ca_teams;

    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES
    ('CAT_Training', 'CreativeAcademyTeams', 'Team Training Hub', 'boolean', v_ca_teams),
    ('CAT_Roles', 'CreativeAcademyTeams', 'Role-based Learning Paths', 'boolean', v_ca_teams),
    ('CAT_Dash', 'CreativeAcademyTeams', 'Performance Dashboards', 'boolean', v_ca_teams),
    ('CAT_Guest', 'CreativeAcademyTeams', 'Guest Expert Sessions', 'boolean', v_ca_teams);

    -- AI Direct Children
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES
    ('Content Ideas Weekly', 'AI', 'Content Ideas Weekly', 'number', v_ai_root),
    ('Hashtags Weekly', 'AI', 'Hashtags Weekly', 'number', v_ai_root),
    ('Content Ideas Per Month', 'AI', 'Content Ideas Per Month', 'number', v_ai_root),
    ('Hashtags Per Month', 'AI', 'Hashtags Per Month', 'number', v_ai_root),
    ('Best Posting Time', 'AI', 'Best Posting Time Analysis', 'string', v_ai_root),
    ('Content Intelligence Ideas Per Month', 'AI', 'Content Intelligence Ideas Per Month', 'number', v_ai_root),
    ('Content Intelligence Basic Hashtags Per Post', 'AI', 'Basic Hashtags Per Post', 'number', v_ai_root),
    ('Content Intelligence Caption Versions Per Post', 'AI', 'Caption Versions Per Post', 'number', v_ai_root);
    
    -- AI Groups
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Audience Optimisation', 'AI', 'Audience Optimisation', 'group', v_ai_root) RETURNING id INTO v_audience_opt;

    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Creative Academy Enhancement', 'AI', 'Creative Academy Enhancement', 'group', v_ai_root) RETURNING id INTO v_ca_enhance;

    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Advanced Automation', 'AI', 'Advanced Automation', 'group', v_ai_root) RETURNING id INTO v_adv_auto;

    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Predictive Features', 'AI', 'Predictive Features', 'group', v_ai_root) RETURNING id INTO v_predictive;

    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Unlimited Content Intelligence', 'AI', 'Unlimited Content Intelligence', 'group', v_ai_root) RETURNING id INTO v_ul_content_intel;
    
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Team AI Features', 'AI', 'Team AI Features', 'group', v_ai_root) RETURNING id INTO v_team_ai;
    
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Premium Agency AI Features', 'PremiumAgencyAI', 'Premium Agency AI Features', 'group', v_ai_root) RETURNING id INTO v_premium_agency;

    -- AI Group Children
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES 
    ('AO_BestPosting', 'AI', 'Best Posting Times', 'boolean', v_audience_opt),
    ('AO_Competitor', 'AI', 'Competitor Insights', 'boolean', v_audience_opt),
    ('AO_ABTest', 'AI', 'A/B Testing', 'boolean', v_audience_opt),
    ('AO_RealTime', 'AI', 'Real-time Posting Recs', 'boolean', v_audience_opt),
    ('AO_Sentiment', 'AI', 'Sentiment Analysis', 'boolean', v_audience_opt),
    ('AO_DeepCompetitor', 'AI', 'Deep Competitor Analysis', 'boolean', v_audience_opt),
    ('AO_UnlimitedAB', 'AI', 'Unlimited A/B Testing', 'boolean', v_audience_opt),

    ('CAE_MentorMatch_Basic', 'AI', 'AI Mentor Matching (Basic)', 'boolean', v_ca_enhance),
    ('CAE_MentorMatch_Adv', 'AI', 'AI Mentor Matching (Advanced)', 'boolean', v_ca_enhance),
    ('CAE_Templates', 'AI', 'Learning Templates', 'boolean', v_ca_enhance),
    ('CAE_Summaries', 'AI', 'Text-only Summaries', 'boolean', v_ca_enhance),
    ('CAE_Paths', 'AI', 'Learning Paths', 'boolean', v_ca_enhance),
    ('CAE_AutoTrans', 'AI', 'Auto Transcription/Summaries', 'boolean', v_ca_enhance),
    ('CAE_Recs', 'AI', 'Course Recommendations', 'boolean', v_ca_enhance),

    ('AA_AutoRespond_50', 'AI', 'Auto-respond to 50 comments/DMs', 'boolean', v_adv_auto),
    ('AA_AutoRespond_Unl', 'AI', 'Unlimited comment/DM responses', 'boolean', v_adv_auto),
    ('AA_BrandAlerts', 'AI', 'Brand Mention Alerts', 'boolean', v_adv_auto),
    ('AA_Spam', 'AI', 'Spam Detection', 'boolean', v_adv_auto),
    ('AA_RealTime', 'AI', 'Real-time Monitoring', 'boolean', v_adv_auto),
    ('AA_AdvModeration', 'AI', 'Advanced Moderation', 'boolean', v_adv_auto),
    ('AA_Crisis', 'AI', 'Crisis Alerts', 'boolean', v_adv_auto),

    ('PF_WeeklyPred', 'AI', 'Weekly Performance Predictions', 'boolean', v_predictive),
    ('PF_RealTimePred', 'AI', 'Real-time Predictions', 'boolean', v_predictive),
    ('PF_GrowthForecast', 'AI', 'Monthly Growth Forecasts', 'boolean', v_predictive),
    ('PF_AdvForecast', 'AI', 'Advanced Forecasts with Revenue', 'boolean', v_predictive),
    ('PF_Trending3', 'AI', '3 Trending Alerts/Week', 'boolean', v_predictive),
    ('PF_Market', 'AI', 'Market Analysis', 'boolean', v_predictive),
    ('PF_InstantAlert', 'AI', 'Instant Trending Alerts', 'boolean', v_predictive),
    
    ('UCI_Ideas', 'AI', 'Unlimited Ideas with Trend Analysis', 'boolean', v_ul_content_intel),
    ('UCI_Hashtags', 'AI', 'Advanced Hashtags', 'boolean', v_ul_content_intel),
    ('UCI_Captions', 'AI', 'Multiple Captions', 'boolean', v_ul_content_intel),
    ('UCI_Calendars', 'AI', 'Auto Content Calendars', 'boolean', v_ul_content_intel),
    
    ('TAI_Batch', 'AI', 'Content Generation at Scale (Batch)', 'boolean', v_team_ai),
    ('TAI_Forecast', 'AI', 'Trend Forecasting (Cross-client)', 'boolean', v_team_ai),
    ('TAI_Bench', 'AI', 'Competitor Benchmarking', 'boolean', v_team_ai),
    ('TAI_Insights', 'AI', 'Team Collaboration Insights', 'boolean', v_team_ai),
    
    ('PAI_Brainstorm', 'PremiumAgencyAI', 'AI Campaign Brainstorming', 'boolean', v_premium_agency),
    ('PAI_Sentiment', 'PremiumAgencyAI', 'AI Sentiment Analysis', 'boolean', v_premium_agency),
    ('PAI_Forecast', 'PremiumAgencyAI', 'AI Campaign Forecasting', 'boolean', v_premium_agency),
    ('PAI_Budget', 'PremiumAgencyAI', 'AI Budget Reallocation', 'boolean', v_premium_agency),
    ('PAI_Media', 'PremiumAgencyAI', 'AI Media Planning', 'boolean', v_premium_agency);

    -- TrendPulse
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('TrendPulse Access', 'TrendPulse', 'TrendPulse Access', 'string', v_trendpulse_root);
    
    -- Integrations
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Integrations Available', 'Integrations', 'Available Integrations', 'group', v_integrations_root) RETURNING id INTO v_int_avail;

    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES
    ('Int_FigmaView', 'Integrations', 'Figma View Mode', 'boolean', v_int_avail),
    ('Int_GoogleDocs', 'Integrations', 'Google Docs', 'boolean', v_int_avail),
    ('Int_GoogleSheets', 'Integrations', 'Google Sheets', 'boolean', v_int_avail),
    ('Int_Google', 'Integrations', 'Google', 'boolean', v_int_avail),
    ('Int_Dropbox', 'Integrations', 'Dropbox', 'boolean', v_int_avail),
    ('Int_Miro', 'Integrations', 'Miro', 'boolean', v_int_avail),
    ('Int_Figma', 'Integrations', 'Figma (Full)', 'boolean', v_int_avail);

    -- Support
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('support_level', 'Support', 'Support Level', 'string', v_support_root);
    
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('Beta Feature Access', 'Support', 'Beta Feature Access', 'boolean', v_support_root);

    -- AdsHub (Root already created)
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('AdsHub Lite Platforms', 'AdsHubLite', 'AdsHub Lite Platforms', 'group', v_adshub_root) RETURNING id INTO v_adshub_lite;
    
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id)
    VALUES ('AdsHub Lite Monthly Spend Cap', 'AdsHubLite', 'Monthly Spend Cap', 'number', v_adshub_root);

    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('AdsHub Pro Features', 'AdsHubPro', 'AdsHub Pro Features', 'group', v_adshub_root) RETURNING id INTO v_adshub_pro;

    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('AdsHub Enterprise Features', 'AdsHubEnterprise', 'AdsHub Enterprise Features', 'group', v_adshub_root) RETURNING id INTO v_adshub_ent;
    
    -- AdsHub Children
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES
    ('Ads_FB', 'AdsHubLite', 'Facebook', 'boolean', v_adshub_lite),
    ('Ads_Insta', 'AdsHubLite', 'Instagram', 'boolean', v_adshub_lite),
    ('Ads_Google', 'AdsHubLite', 'Google Ads', 'boolean', v_adshub_lite),
    ('Ads_YT', 'AdsHubLite', 'YouTube', 'boolean', v_adshub_lite),
    ('Ads_TikTok', 'AdsHubLite', 'TikTok', 'boolean', v_adshub_lite),
    ('Ads_LinkedIn', 'AdsHubLite', 'LinkedIn', 'boolean', v_adshub_lite),
    
    ('AdsPro_MultiClient', 'AdsHubPro', 'Multi-client Ad Management', 'boolean', v_adshub_pro),
    ('AdsPro_CrossPlat', 'AdsHubPro', 'Cross-platform Analytics', 'boolean', v_adshub_pro),
    ('AdsPro_Workflows', 'AdsHubPro', 'Team Ad Workflows', 'boolean', v_adshub_pro),
    ('AdsPro_Reports', 'AdsHubPro', 'White-label Reports', 'boolean', v_adshub_pro),
    ('AdsPro_UnlSpend', 'AdsHubPro', 'Unlimited Spend', 'boolean', v_adshub_pro),
    
    ('AdsEnt_ClientApproval', 'AdsHubEnterprise', 'Client Approval Workflows', 'boolean', v_adshub_ent),
    ('AdsEnt_Attribution', 'AdsHubEnterprise', 'Cross-channel Attribution', 'boolean', v_adshub_ent),
    ('AdsEnt_Optimization', 'AdsHubEnterprise', 'AI-driven Optimization', 'boolean', v_adshub_ent);
    
    -- Collaboration
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Collaboration Workflow Features', 'CollaborationWorkflow', 'Collaboration Workflow Features', 'group', v_collab_root) RETURNING id INTO v_collab_workflow;

    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Collaboration Monetization Access', 'CollaborationMonetization', 'Collaboration Monetization Access', 'group', v_collab_root) RETURNING id INTO v_collab_monet;

    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Advanced Collaboration Features', 'AdvancedCollaboration', 'Advanced Collaboration Features', 'group', v_collab_root) RETURNING id INTO v_adv_collab;
    
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES
    ('Collab_WhiteLabel', 'CollaborationWorkflow', 'White-label Portal', 'boolean', v_collab_workflow),
    ('Collab_Calendar', 'CollaborationWorkflow', 'Shared Content Calendar', 'boolean', v_collab_workflow),
    ('Collab_Approval', 'CollaborationWorkflow', 'Approval Workflows', 'boolean', v_collab_workflow),
    ('Collab_Library', 'CollaborationWorkflow', 'Team Asset Library', 'boolean', v_collab_workflow),
    ('Collab_Slack', 'CollaborationWorkflow', 'Slack/MS Teams Integration', 'boolean', v_collab_workflow),
    ('Collab_Notion', 'CollaborationWorkflow', 'Notion Wiki', 'boolean', v_collab_workflow),
    ('Collab_Airtable', 'CollaborationWorkflow', 'Airtable Dashboards', 'boolean', v_collab_workflow),
    ('Collab_Miro', 'CollaborationWorkflow', 'Miro Workshops', 'boolean', v_collab_workflow),

    ('Collab_Exclusive', 'CollaborationMonetization', 'Apply for Exclusive Collabs', 'boolean', v_collab_monet),
    ('Collab_Marketplace', 'CollaborationMonetization', 'Sponsored Content Marketplace', 'boolean', v_collab_monet),

    ('AdvCollab_Chat', 'AdvancedCollaboration', 'Huddey Built-in Chat', 'boolean', v_adv_collab),
    ('AdvCollab_Rooms', 'AdvancedCollaboration', 'Content Collaboration Rooms', 'boolean', v_adv_collab),
    ('AdvCollab_CoCreate', 'AdvancedCollaboration', 'Client Co-Creation Mode', 'boolean', v_adv_collab),
    ('AdvCollab_AdvApproval', 'AdvancedCollaboration', 'Advanced Approval Chains', 'boolean', v_adv_collab),
    ('AdvCollab_Knowledge', 'AdvancedCollaboration', 'Knowledge Base & Playbooks', 'boolean', v_adv_collab),
    ('AdvCollab_Kanban', 'AdvancedCollaboration', 'Kanban Task Boards', 'boolean', v_adv_collab),
    ('AdvCollab_CrossTeam', 'AdvancedCollaboration', 'Cross-team Collaboration', 'boolean', v_adv_collab);

    -- Reporting
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('White Label Reporting Features', 'ReportingWhiteLabel', 'White Label Reporting Features', 'group', v_reporting_root) RETURNING id INTO v_report_white;
    
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Reporting Delivery Features', 'ReportingDelivery', 'Reporting Delivery Features', 'group', v_reporting_root) RETURNING id INTO v_report_delivery;
    
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES
    ('Rep_Custom', 'ReportingWhiteLabel', 'Custom Branded Reports', 'boolean', v_report_white),
    ('Rep_Automated', 'ReportingWhiteLabel', 'Automated PDF/Excel Reports', 'boolean', v_report_white),
    ('Rep_CrossAccount', 'ReportingWhiteLabel', 'Cross-account Dashboards', 'boolean', v_report_white),

    ('Rep_ROI', 'ReportingDelivery', 'ROI Tracking', 'boolean', v_report_delivery),
    ('Rep_Agency', 'ReportingDelivery', 'Agency Analytics Rollup', 'boolean', v_report_delivery),
    ('Rep_WhitePortals', 'ReportingDelivery', 'White-label Portals', 'boolean', v_report_delivery),
    ('Rep_BrandedExports', 'ReportingDelivery', 'Branded Exports', 'boolean', v_report_delivery);

    -- Security
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Security Access Features', 'SecurityAccess', 'Security Access Features', 'group', v_security_root);
    
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES
    ('Sec_SSO', 'SecurityAccess', 'Google Workspace SSO', 'boolean', (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Security Access Features')),
    ('Sec_Dropbox', 'SecurityAccess', 'Dropbox Business', 'boolean', (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Security Access Features')),
    ('Sec_Audit', 'SecurityAccess', 'Audit Logs + API Access', 'boolean', (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Security Access Features')),
    ('Sec_Compliance', 'SecurityAccess', 'Enterprise Compliance', 'boolean', (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Security Access Features'));

    -- Onboarding
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) 
    VALUES ('Onboarding Support Features', 'OnboardingSupport', 'Onboarding Support Features', 'group', v_onboarding_root);

    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES
    ('Onb_Dedicated', 'OnboardingSupport', 'Dedicated Onboarding', 'boolean', (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Onboarding Support Features')),
    ('Onb_Priority', 'OnboardingSupport', 'Priority Email + Chat', 'boolean', (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Onboarding Support Features'));

    -- Storage Children
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES
    ('Included Storage', 'Storage', 'Included Storage', 'string', v_storage_root),
    ('Max File Size', 'Storage', 'Max File Size', 'string', v_storage_root),
    ('Retention Period', 'Storage', 'Retention Period', 'string', v_storage_root),
    ('Storage Add-on', 'Storage', 'Storage Add-on', 'string', v_storage_root);

    -- Extras Children
    INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type, parent_id) VALUES
    ('Premium Trials', 'Extras', 'Premium Trials', 'string', v_extras_root);

END $$;