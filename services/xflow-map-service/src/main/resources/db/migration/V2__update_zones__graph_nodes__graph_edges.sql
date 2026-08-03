SET search_path TO map, public;

ALTER TABLE zones 
  ALTER COLUMN geom TYPE GEOMETRY(MULTIPOLYGON, 4326) 
  USING ST_Multi(geom);

ALTER TABLE graph_nodes 
  ADD COLUMN node_id BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE;

ALTER TABLE graph_edges 
  ADD COLUMN edge_id BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
  ADD COLUMN source BIGINT,
  ADD COLUMN target BIGINT;

DROP INDEX IF EXISTS idx_zones_geom;
DROP INDEX IF EXISTS idx_obstacles_geom;
DROP INDEX IF EXISTS idx_graph_nodes_geom;
DROP INDEX IF EXISTS idx_graph_edges_geom;
DROP INDEX IF EXISTS idx_zones_version;
DROP INDEX IF EXISTS idx_obstacles_version;
DROP INDEX IF EXISTS idx_nodes_version;
DROP INDEX IF EXISTS idx_edges_version;
DROP INDEX IF EXISTS idx_map_versions_status;

CREATE INDEX idx_zones_spatial ON zones USING GIST (geom) WHERE active = true;
CREATE INDEX idx_obstacles_spatial ON obstacles USING GIST (geom) WHERE active = true;
CREATE INDEX idx_graph_nodes_spatial ON graph_nodes USING GIST (geom) WHERE active = true;
CREATE INDEX idx_graph_edges_spatial ON graph_edges USING GIST (geom) WHERE active = true;

CREATE INDEX idx_zones_version_active ON zones (map_version_id, active);
CREATE INDEX idx_obstacles_version_active ON obstacles (map_version_id, active);
CREATE INDEX idx_nodes_version_active ON graph_nodes (map_version_id, active);
CREATE INDEX idx_edges_version_active ON graph_edges (map_version_id, active);
CREATE INDEX idx_map_versions_tenant_status ON map_versions (tenant_id, status);