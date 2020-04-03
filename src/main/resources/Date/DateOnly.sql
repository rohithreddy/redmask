CREATE OR REPLACE FUNCTION redmask.partdate(ColName timestamp without time zone, date_category text default 'day')
RETURNS timestamp AS $$
DECLARE
    categoryval integer;
BEGIN
    CASE date_category
    when 'day' then
     EXECUTE format('select extract(day from timestamp %L)',ColName) into categoryval;
    when 'month'
    when 'year'
    when '
    RETURN make_timestamp(2000,1,categoryval::integer,0,0,0);

END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;
