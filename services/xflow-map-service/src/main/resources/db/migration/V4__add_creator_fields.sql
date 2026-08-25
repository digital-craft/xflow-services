SET search_path = map, public;

ALTER TABLE zones
    ADD COLUMN created_by UUID NOT NULL,
    ADD COLUMN updated_by UUID NOT NULL;

ALTER TABLE zone_types
    ADD COLUMN created_by UUID NOT NULL,
    ADD COLUMN updated_by UUID NOT NULL;

ALTER TABLE map_versions
    ADD COLUMN created_by UUID NOT NULL,
    ADD COLUMN updated_by UUID NOT NULL;

ALTER TABLE imported_plans
    DROP COLUMN uploaded_by,
    DROP COLUMN uploaded_at,
    ADD COLUMN created_by UUID NOT NULL,
    ADD COLUMN updated_by UUID NOT NULL;

ALTER TABLE calibration_points
    ADD COLUMN created_by UUID NOT NULL,
    ADD COLUMN updated_by UUID NOT NULL;

ALTER TABLE obstacles
    ADD COLUMN updated_by UUID NOT NULL;

ALTER TABLE graph_nodes
    ADD COLUMN created_by UUID NOT NULL,
    ADD COLUMN updated_by UUID NOT NULL;

ALTER TABLE graph_edges
    ADD COLUMN created_by UUID NOT NULL,
    ADD COLUMN updated_by UUID NOT NULL;
