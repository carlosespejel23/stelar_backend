-- Add created_by column to in_app_notifications table
ALTER TABLE in_app_notifications ADD COLUMN IF NOT EXISTS created_by UUID;