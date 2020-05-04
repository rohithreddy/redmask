(ColName float, ReplacementNumber float default 0.0)
RETURNS float AS $$
BEGIN
    RETURN  ReplacementNumber;
END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;