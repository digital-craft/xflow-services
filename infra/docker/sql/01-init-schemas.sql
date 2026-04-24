-- docker/sql/01-init-schemas.sql

DO $$
DECLARE
    -- We recover the variables injected by Docker
    app TEXT := current_setting('custom.app');
    default_password TEXT := current_setting('custom.db_password');

    -- List of services (diagrams)
    services TEXT[] := ARRAY['auth', 'map', 'tracking', 'routing', 'link_notif'];
    service_name TEXT;
    full_user_name TEXT;
    user_password TEXT;
BEGIN
    RAISE NOTICE 'App name: %', app;
    RAISE NOTICE 'Default password: %', default_password;
    FOREACH service_name IN ARRAY services
    LOOP
        -- 1. Creating the schema
        EXECUTE format('CREATE SCHEMA IF NOT EXISTS %I', service_name);

        -- 2. Construction of username (ex: auth_app)
        full_user_name := service_name || '_' || app;
        user_password := service_name || '_' || default_password;

        -- 3. Creation of user if nonexistent
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = full_user_name) THEN
        EXECUTE format('CREATE USER %I WITH ENCRYPTED PASSWORD %L', full_user_name, user_password);
        END IF;

        -- 4. Assignment of rights and isolation
        EXECUTE format('GRANT ALL PRIVILEGES ON SCHEMA %I TO %I', service_name, full_user_name);
        EXECUTE format('ALTER ROLE %I SET search_path TO %I', full_user_name, service_name);

        RAISE NOTICE 'Service % configured with user %', service_name, full_user_name;
    END LOOP;
END
$$;