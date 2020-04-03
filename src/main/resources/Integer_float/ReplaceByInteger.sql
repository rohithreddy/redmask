CREATE OR REPLACE FUNCTION redmask.replaceby(ColName integer, ReplacementNumber integer  default 0)
RETURNS integer AS $$
BEGIN
    RETURN  ReplacementNumber;
END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;