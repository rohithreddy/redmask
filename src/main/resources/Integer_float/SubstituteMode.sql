(ColName text, TableName text, value anyelement default 0.0)
RETURNS anyelement AS $$
BEGIN
    EXECUTE format('SELECT mode() WITHIN GROUP (ORDER BY %s) AS modal_value FROM %s',ColName,TableName) INTO value;
    RETURN value;
END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;