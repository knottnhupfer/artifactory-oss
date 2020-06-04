CREATE INDEX node_props_node_prop_value_idx ON node_props(node_id, prop_key, prop_value(255));
CREATE INDEX node_props_prop_key_value_idx ON node_props (prop_key, prop_value(255));
ALTER TABLE node_props DROP INDEX node_props_node_id_idx;
ALTER TABLE node_props DROP INDEX node_props_prop_key_idx;
ALTER TABLE node_props DROP INDEX node_props_prop_value_idx;