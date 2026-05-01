ALTER TABLE auth.users 
    ADD COLUMN failed_attempts INTEGER DEFAULT 0,
    ADD COLUMN locked_until    TIMESTAMP WITH TIME ZONE;
