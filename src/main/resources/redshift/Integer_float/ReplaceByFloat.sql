(ColName float, ReplacementNumber float default 0.0)
RETURNS float  stable AS $$
return ReplacementNumber;
$$ LANGUAGE plpythonu;