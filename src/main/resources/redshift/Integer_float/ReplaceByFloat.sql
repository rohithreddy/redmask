(ColName float, ReplacementNumber float)
RETURNS float  stable AS $$
return ReplacementNumber;
$$ LANGUAGE plpythonu;