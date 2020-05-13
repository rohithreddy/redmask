(ColName integer, ReplacementNumber integer  default 0)
RETURNS integer stable  AS $$
return ReplacementNumber;
$$ LANGUAGE plpythonu;