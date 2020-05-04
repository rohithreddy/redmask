CREATE OR REPLACE FUNCTION redmask.replaceby(ColName text, ReplacementString text)
RETURNS text AS $$
BEGIN
    RETURN  ReplacementString;
END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;
