SET search_path = map, public;

ALTER TABLE imported_plans
ADD COLUMN map_version_id UUID NOT NULL,
ADD CONSTRAINT fk_imported_plan_map_version 
    FOREIGN KEY (map_version_id) 
    REFERENCES map_versions(id) 
    ON DELETE CASCADE;

CREATE INDEX idx_imported_plan_map_version_id ON imported_plans(map_version_id);