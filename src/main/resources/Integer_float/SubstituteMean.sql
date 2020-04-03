CREATE OR REPLACE FUNCTION redmask.substitute_mean(ColName text, TableName text, value anyelement default 0.0)
RETURNS anyelement AS $$
BEGIN
    EXECUTE format('SELECT avg(%s) from %s',ColName,TableName) INTO value;
    RETURN value;
END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;
