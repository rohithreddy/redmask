(
    ColName text,
    masktype text default 'last',
    separator text default '',
    arg1 integer default 4,
    arg2 integer default 0 )
RETURNS text AS $$
DECLARE
    arg1_new integer;
    arg2_new integer;
BEGIN
    arg1_new := arg1+((arg1%4)*length(separator));
    arg2_new := arg2+((arg2%4)*length(separator));
    CASE masktype
    when 'last' then
     return redmask.anonymize_numbers(Colname,'x',0,arg1_new);
    when 'first' then
     return redmask.anonymize_numbers(Colname,'x',arg1_new,0);
    when 'firstnlast' then
     return redmask.anonymize_numbers(Colname,'x',arg1_new,arg2_new);
    else
     Return Colname;
    End Case;

END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;
