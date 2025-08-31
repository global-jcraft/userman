-- 1. Feature Catalog table - Master catalog of all features
CREATE TABLE IF NOT EXISTS huddey_core.feature_catalog (
    id BIGSERIAL PRIMARY KEY,
    feature_name VARCHAR(255) NOT NULL UNIQUE,
    feature_category VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    description TEXT,
    feature_type VARCHAR(50) DEFAULT 'string', -- string, number, boolean, array
    default_value TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
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
CREATE INDEX idx_product_feature_catalog_product ON huddey_core.product_feature_catalog(product_id);
CREATE INDEX idx_product_feature_catalog_feature ON huddey_core.product_feature_catalog(feature_id);
CREATE INDEX idx_product_feature_catalog_enabled ON huddey_core.product_feature_catalog(is_enabled);

-- Insert all features into catalog
INSERT INTO huddey_core.feature_catalog (feature_name, feature_category, display_name, feature_type) VALUES
-- Socials features
('Social networks', 'Socials', 'Social Networks', 'number'),
('Scheduled Posts Per Month', 'Socials', 'Scheduled Posts Per Month', 'number'),
('Calendar Reminders', 'Socials', 'Calendar Reminders', 'boolean'),
('Content Scheduling', 'Socials', 'Content Scheduling', 'string'),
('Extra Cost Per Platform', 'Socials', 'Extra Cost Per Platform', 'number'),

-- Analytics features
('Basic Analytics', 'Analytics', 'Basic Analytics', 'array'),
('Advanced Analytics', 'Analytics', 'Advanced Analytics', 'array'),
('Advanced Predictive Analytics', 'Analytics', 'Advanced Predictive Analytics', 'array'),

-- Creative Academy features
('Creative Academy Access', 'CreativeAcademy', 'Creative Academy Access', 'string'),

-- AI features
('Content Ideas Weekly', 'AI', 'Content Ideas Weekly', 'number'),
('Hashtags Weekly', 'AI', 'Hashtags Weekly', 'number'),
('Content Ideas Per Month', 'AI', 'Content Ideas Per Month', 'number'),
('Hashtags Per Month', 'AI', 'Hashtags Per Month', 'number'),
('Best Posting Time', 'AI', 'Best Posting Time Analysis', 'string'),
('Content Intelligence Ideas Per Month', 'AI', 'Content Intelligence Ideas Per Month', 'number'),
('Content Intelligence Basic Hashtags Per Post', 'AI', 'Basic Hashtags Per Post', 'number'),
('Content Intelligence Caption Versions Per Post', 'AI', 'Caption Versions Per Post', 'number'),
('Audience Optimisation', 'AI', 'Audience Optimisation', 'array'),
('Creative Academy Enhancement', 'AI', 'Creative Academy Enhancement', 'array'),
('Advanced Automation', 'AI', 'Advanced Automation', 'array'),
('Predictive Features', 'AI', 'Predictive Features', 'array'),
('Unlimited Content Intelligence', 'AI', 'Unlimited Content Intelligence', 'array'),
('Team AI Features', 'AI', 'Team AI Features', 'array'),

-- TrendPulse features
('TrendPulse Access', 'TrendPulse', 'TrendPulse Access', 'string'),

-- Integration features
('Integrations Available', 'Integrations', 'Available Integrations', 'array'),

-- Support features
('support_level', 'Support', 'Support Level', 'string'),

-- AdsHub features
('AdsHub Lite Platforms', 'AdsHubLite', 'AdsHub Lite Platforms', 'array'),
('AdsHub Lite Monthly Spend Cap', 'AdsHubLite', 'Monthly Spend Cap', 'number'),
('AdsHub Pro Features', 'AdsHubPro', 'AdsHub Pro Features', 'array'),
('AdsHub Enterprise Features', 'AdsHubEnterprise', 'AdsHub Enterprise Features', 'array'),

-- Collaboration features
('Collaboration Workflow Features', 'CollaborationWorkflow', 'Collaboration Workflow Features', 'array'),
('Collaboration Monetization Access', 'CollaborationMonetization', 'Collaboration Monetization Access', 'array'),
('Advanced Collaboration Features', 'AdvancedCollaboration', 'Advanced Collaboration Features', 'array'),

-- Reporting features
('White Label Reporting Features', 'ReportingWhiteLabel', 'White Label Reporting Features', 'array'),
('Reporting Delivery Features', 'ReportingDelivery', 'Reporting Delivery Features', 'array'),

-- Security features
('Security Access Features', 'SecurityAccess', 'Security Access Features', 'array'),

-- Onboarding features
('Onboarding Support Features', 'OnboardingSupport', 'Onboarding Support Features', 'array'),

-- Storage features
('Included Storage', 'Storage', 'Included Storage', 'string'),
('Max File Size', 'Storage', 'Max File Size', 'string'),
('Retention Period', 'Storage', 'Retention Period', 'string'),
('Storage Add-on', 'Storage', 'Storage Add-on', 'string'),

-- Creative Academy Teams features
('Creative Academy Teams Features', 'CreativeAcademyTeams', 'Creative Academy Teams Features', 'array'),

-- Premium Agency AI features
('Premium Agency AI Features', 'PremiumAgencyAI', 'Premium Agency AI Features', 'array'),

-- Extras features
('Premium Trials', 'Extras', 'Premium Trials', 'string');