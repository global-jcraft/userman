ALTER TABLE huddey_core.user_subscriptions 
ADD COLUMN pending_plan VARCHAR(50),
ADD COLUMN pending_plan_effective_date TIMESTAMP WITH TIME ZONE;