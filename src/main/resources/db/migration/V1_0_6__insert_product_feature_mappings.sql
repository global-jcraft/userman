-- Insert all products
INSERT INTO huddey_core.products (stripe_product_id, name, description, active) VALUES
('prod_free', 'Free Plan', 'Ideal for beginners and hobbyist creators', true),
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
('price_free_monthly', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_free'), 'FREE', 0.00, 'eur', 'month', true),
('price_free_yearly', (SELECT id FROM huddey_core.products WHERE stripe_product_id = 'prod_free'), 'FREE', 0.00, 'eur', 'year', true);
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


-- Free Plan Features
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
((SELECT id FROM huddey_core.products WHERE name = 'Free Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Social networks'), '1'),
((SELECT id FROM huddey_core.products WHERE name = 'Free Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Scheduled Posts Per Month'), '10'),
((SELECT id FROM huddey_core.products WHERE name = 'Free Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Calendar Reminders'), 'true'),
((SELECT id FROM huddey_core.products WHERE name = 'Free Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Basic Analytics'), 'likes,reach'),
((SELECT id FROM huddey_core.products WHERE name = 'Free Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Creative Academy Access'), 'Not included'),
((SELECT id FROM huddey_core.products WHERE name = 'Free Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Ideas Weekly'), '3'),
((SELECT id FROM huddey_core.products WHERE name = 'Free Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Hashtags Weekly'), '2'),
((SELECT id FROM huddey_core.products WHERE name = 'Free Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Premium Trials'), '3 days rotating premium trials with limitations');

-- Starter Plan Features
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
((SELECT id FROM huddey_core.products WHERE name = 'Starter Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Social networks'), '3'),
((SELECT id FROM huddey_core.products WHERE name = 'Starter Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Scheduling'), 'Unlimited content scheduling and publishing'),
((SELECT id FROM huddey_core.products WHERE name = 'Starter Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Basic Analytics'), 'engagement,views,likes,engagement trends'),
((SELECT id FROM huddey_core.products WHERE name = 'Starter Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Creative Academy Access'), 'Group consultations only'),
((SELECT id FROM huddey_core.products WHERE name = 'Starter Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Ideas Per Month'), '10'),
((SELECT id FROM huddey_core.products WHERE name = 'Starter Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Hashtags Per Month'), '5'),
((SELECT id FROM huddey_core.products WHERE name = 'Starter Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Best Posting Time'), 'weekly analysis'),
((SELECT id FROM huddey_core.products WHERE name = 'Starter Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'TrendPulse Access'), 'Not included');

-- Pro Plan Features
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Social networks'), '7'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Extra Cost Per Platform'), '5'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Advanced Analytics'), 'audience demographics,peak performance times,weekly/monthly performance analytics'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Creative Academy Access'), 'Group consultations,1-on-1 consultations with experienced creators'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Ideas Per Month'), '30'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Hashtags Per Month'), '15'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Intelligence Ideas Per Month'), '10'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Intelligence Basic Hashtags Per Post'), '5'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Intelligence Caption Versions Per Post'), '1'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Audience Optimisation'), 'Best posting times (weekly),Competitor insights (monthly),Manual A/B testing (2 variants)'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Creative Academy Enhancement'), 'AI mentor matching (basic),Learning templates,Text-only summaries'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Advanced Automation'), 'Auto-respond to 50 comments/DMs,Brand mention alerts (daily digest),Basic spam detection'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Predictive Features'), 'Weekly performance predictions,Monthly growth forecasts,3 trending alerts/week'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'TrendPulse Access'), 'Industry trend insights,Competitor trend insights'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Integrations Available'), 'Figma view mode,Google Docs,Google Sheets'),
((SELECT id FROM huddey_core.products WHERE name = 'Pro Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'support_level'), 'Priority support');

-- Elite Plan Features
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value) VALUES
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Social networks'), 'Unlimited'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Advanced Predictive Analytics'), 'demographics,trends,weekly/monthly reports'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Creative Academy Access'), '1-on-1 consultations with established creators,Exclusive sessions with top-tier creators via partnerships,Ability to set own consultation rates,Monetization through Academy'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Content Ideas Per Month'), '50'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Hashtags Per Month'), '30'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Unlimited Content Intelligence'), 'Unlimited ideas with trend analysis,Advanced hashtags,Multiple captions,Auto content calendars'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Audience Optimisation'), 'Real-time posting recs,Deep competitor analysis,Unlimited A/B testing,Sentiment analysis'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Creative Academy Enhancement'), 'AI mentor matching (advanced),Learning paths,Auto transcription/summaries,Course recommendations'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Advanced Automation'), 'Unlimited comment/DM responses,Real-time monitoring,Advanced moderation,Crisis alerts'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Predictive Features'), 'Real-time predictions,Advanced forecasts with revenue,Instant trending alerts,Market analysis'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'TrendPulse Access'), 'Full access,Real-time predictions,Daily updates'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Collaboration Monetization Access'), 'Apply for exclusive collabs with famous creators,Sponsored Content Marketplace'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'Integrations Available'), 'Google,Dropbox,Miro,Figma'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'AdsHub Lite Platforms'), 'Facebook,Instagram,Google Ads,YouTube'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'AdsHub Lite Monthly Spend Cap'), '1000'),
((SELECT id FROM huddey_core.products WHERE name = 'Elite Plan'), (SELECT id FROM huddey_core.feature_catalog WHERE feature_name = 'support_level'), 'Priority+ support');

-- Teams Plan Features (all seat variations)
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'White-label portal (custom branding),Shared content calendar (org-wide),Approval workflows (draft → review → publish),Team asset library,Slack/MS Teams integration,Notion wiki,Airtable dashboards,Miro workshops'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Plan%' AND p.name NOT LIKE '%Pro%' AND fc.feature_name = 'Collaboration Workflow Features';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'Custom branded reports,Automated PDF/Excel reports,Cross-account performance dashboards'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Plan%' AND p.name NOT LIKE '%Pro%' AND fc.feature_name = 'White Label Reporting Features';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'Google Workspace SSO,Dropbox Business,Audit logs + API access,Enterprise compliance (CCPA, SOC2, GDPR)'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Plan%' AND p.name NOT LIKE '%Pro%' AND fc.feature_name = 'Security Access Features';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'Dedicated onboarding,Priority email + chat'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Plan%' AND p.name NOT LIKE '%Pro%' AND fc.feature_name = 'Onboarding Support Features';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'Content Generation at scale (batch),Trend Forecasting (cross-client),Competitor Benchmarking,Team Collaboration Insights'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Plan%' AND p.name NOT LIKE '%Pro%' AND fc.feature_name = 'Team AI Features';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'Multi-client ad management (FB + Google),Cross-platform analytics (FB, Google, YouTube),Team ad workflows,White-label reports,Unlimited spend'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Plan%' AND p.name NOT LIKE '%Pro%' AND fc.feature_name = 'AdsHub Pro Features';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, '2 TB'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Plan%' AND p.name NOT LIKE '%Pro%' AND fc.feature_name = 'Included Storage';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, '10 GB'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Plan%' AND p.name NOT LIKE '%Pro%' AND fc.feature_name = 'Max File Size';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, '90 days'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Plan%' AND p.name NOT LIKE '%Pro%' AND fc.feature_name = 'Retention Period';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, '+1 TB @ €40/mo'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Plan%' AND p.name NOT LIKE '%Pro%' AND fc.feature_name = 'Storage Add-on';

-- Teams Pro Plan Features (all seat variations)
INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'Huddey built-in chat,Content Collaboration Rooms (real-time co-editing),Client Co-Creation Mode,Advanced approval chains,Knowledge Base & Playbooks,Kanban task boards,Cross-team collaboration (multi-departments)'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Advanced Collaboration Features';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'ROI Tracking,Agency Analytics Rollup,White-label portals with custom domains,Branded PDF/Excel exports'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Reporting Delivery Features';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'Team Training Hub,Role-based learning paths,Performance dashboards,Guest Expert Sessions (monthly live)'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Creative Academy Teams Features';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'AI Campaign Brainstorming,AI Sentiment Analysis,AI Campaign Forecasting,AI Budget Reallocation,AI Media Planning'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Premium Agency AI Features';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'Platforms: FB, Google, TikTok, LinkedIn,Client approval workflows,Cross-channel attribution (organic + paid ROI),AI-driven campaign optimization'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'AdsHub Enterprise Features';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, 'SLA-backed 24/7 support,Dedicated account manager,Beta feature access'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'support_level';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, '10 TB'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Included Storage';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, '100 GB'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Max File Size';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, '120 days'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Retention Period';

INSERT INTO huddey_core.product_feature_catalog (product_id, feature_id, feature_value)
SELECT p.id, fc.id, '+5 TB @ €150/mo'
FROM huddey_core.products p, huddey_core.feature_catalog fc 
WHERE p.name LIKE 'Teams Pro Plan%' AND fc.feature_name = 'Storage Add-on';