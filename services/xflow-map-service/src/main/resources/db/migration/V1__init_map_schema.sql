SET search_path = map, public;

CREATE TABLE zone_types (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    code                VARCHAR(30) NOT NULL,
    label               VARCHAR(50) NOT NULL,
    color               VARCHAR(7) NOT NULL,
    is_system           BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, code)
);

CREATE TABLE map_versions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    status              VARCHAR(10) NOT NULL DEFAULT 'DRAFT'
                          CHECK (status IN ('DRAFT', 'PUBLISHED')),
    sequence_number     INTEGER NOT NULL,
    published_at        TIMESTAMPTZ,
    published_by        UUID,
    change_summary      TEXT,
    urgency             VARCHAR(10) NOT NULL DEFAULT 'NORMAL'
                          CHECK (urgency IN ('NORMAL', 'IMMEDIATE')),
    affected_layers     TEXT[] NOT NULL DEFAULT '{}',
    coexistence_until   TIMESTAMPTZ,
    UNIQUE (tenant_id, sequence_number)
);

CREATE TABLE imported_plans (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    file_url            TEXT NOT NULL,
    file_size           BIGINT NOT NULL,
    original_file_name  VARCHAR(255) NOT NULL,
    file_type           VARCHAR(10) NOT NULL CHECK (file_type IN ('PNG','JPG','PDF')),
    opacity_default     SMALLINT NOT NULL DEFAULT 60 CHECK (opacity_default BETWEEN 0 AND 100),
    calibration_status  VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                          CHECK (calibration_status IN ('PENDING','CALIBRATED','FAILED')),
    uploaded_by         UUID NOT NULL,
    uploaded_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE calibration_points (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id             UUID NOT NULL REFERENCES imported_plans(id) ON DELETE CASCADE,
    pixel_x             DOUBLE PRECISION NOT NULL,
    pixel_y             DOUBLE PRECISION NOT NULL,
    latitude            DOUBLE PRECISION NOT NULL,
    longitude           DOUBLE PRECISION NOT NULL,
    residual_error      DOUBLE PRECISION,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE zones (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    map_version_id      UUID NOT NULL REFERENCES map_versions(id) ON DELETE CASCADE,
    zone_type_id        UUID NOT NULL REFERENCES zone_types(id),
    name                VARCHAR(100) NOT NULL,
    capacity_max        INTEGER NOT NULL CHECK (capacity_max > 0),
    geom                GEOMETRY(POLYGON, 4326) NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE obstacles (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    map_version_id      UUID NOT NULL REFERENCES map_versions(id) ON DELETE CASCADE,
    geom                GEOMETRY(LINESTRING, 4326) NOT NULL,
    created_by          UUID NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE graph_nodes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    map_version_id      UUID NOT NULL REFERENCES map_versions(id) ON DELETE CASCADE,
    geom                GEOMETRY(POINT, 4326) NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE graph_edges (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    map_version_id      UUID NOT NULL REFERENCES map_versions(id) ON DELETE CASCADE,
    source_node_id      UUID NOT NULL REFERENCES graph_nodes(id),
    target_node_id      UUID NOT NULL REFERENCES graph_nodes(id),
    geom                GEOMETRY(LINESTRING, 4326) NOT NULL,
    weight_base         DOUBLE PRECISION NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_zones_geom          ON zones USING GIST (geom);
CREATE INDEX idx_obstacles_geom      ON obstacles USING GIST (geom);
CREATE INDEX idx_graph_nodes_geom    ON graph_nodes USING GIST (geom);
CREATE INDEX idx_graph_edges_geom    ON graph_edges USING GIST (geom);

CREATE INDEX idx_zones_version       ON zones (map_version_id);
CREATE INDEX idx_zones_type          ON zones (zone_type_id);
CREATE INDEX idx_obstacles_version   ON obstacles (map_version_id);
CREATE INDEX idx_nodes_version       ON graph_nodes (map_version_id);
CREATE INDEX idx_edges_version       ON graph_edges (map_version_id);
CREATE INDEX idx_calib_points_plan   ON calibration_points (plan_id);
CREATE INDEX idx_edges_source        ON graph_edges (source_node_id);
CREATE INDEX idx_edges_target        ON graph_edges (target_node_id);
CREATE INDEX idx_map_versions_tenant ON map_versions (tenant_id);
CREATE INDEX idx_plans_tenant        ON imported_plans (tenant_id);
CREATE INDEX idx_zone_types_tenant   ON zone_types (tenant_id);