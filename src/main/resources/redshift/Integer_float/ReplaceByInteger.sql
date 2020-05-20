(ColName integer, ReplacementNumber integer)
RETURNS integer stable  AS $$
return ReplacementNumber;
$$ LANGUAGE plpythonu;