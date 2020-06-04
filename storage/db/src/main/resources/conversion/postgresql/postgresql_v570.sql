CREATE INDEX CONCURRENTLY node_props_node_prop_value_idx ON node_props(node_id, prop_key, substr(prop_value, 1, 2400));
CREATE INDEX node_props_prop_key_value_idx ON node_props (prop_key, substr(prop_value, 1, 2400) varchar_pattern_ops);
DROP INDEX node_props_node_id_idx;
DROP INDEX node_props_prop_value_idx;
DROP INDEX node_props_prop_key_idx;