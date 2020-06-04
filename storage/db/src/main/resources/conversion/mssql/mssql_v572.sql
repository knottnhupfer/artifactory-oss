IF EXISTS (SELECT * FROM sys.indexes
WHERE name='node_props_node_prop_value_idx' AND object_id = OBJECT_ID('node_props'))
  BEGIN
    DROP INDEX node_props_node_prop_value_idx ON node_props
  END;


IF EXISTS (SELECT * FROM sys.indexes
WHERE name='node_props_prop_key_value_idx' AND object_id = OBJECT_ID('node_props'))
  BEGIN
    DROP INDEX node_props_prop_key_value_idx ON node_props
  END;

IF NOT EXISTS (SELECT * FROM sys.indexes
WHERE name='node_props_node_id_idx' AND object_id = OBJECT_ID('node_props'))
  BEGIN
    CREATE INDEX node_props_node_id_idx ON node_props (node_id)
  END;

IF NOT EXISTS (SELECT * FROM sys.indexes
WHERE name='node_props_prop_key_idx' AND object_id = OBJECT_ID('node_props'))
  BEGIN
    CREATE INDEX node_props_prop_key_idx ON node_props (prop_key)
  END;

IF NOT EXISTS (SELECT * FROM sys.indexes
WHERE name='node_props_prop_value_idx' AND object_id = OBJECT_ID('node_props'))
  BEGIN
    CREATE INDEX node_props_prop_value_idx ON node_props (prop_value)
  END;