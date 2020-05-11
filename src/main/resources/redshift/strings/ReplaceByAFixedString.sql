CREATE OR REPLACE FUNCTION redmask.replaceby(ColName text, ReplacementString text)
RETURNS text stable AS $$
    return  ReplacementString;
$$ language plpythonu;
