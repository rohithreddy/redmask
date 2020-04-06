CREATE OR REPLACE FUNCTION redmask.partdate(ColName timestamp without time zone, date_category text default 'day')
RETURNS timestamp AS $$
DECLARE
    categoryval integer;
BEGIN
    CASE date_category
    when 'day' then
     EXECUTE format('select extract(day from timestamp %L)',ColName) into categoryval;
    when 'month' then
     EXECUTE format('select extract(month from timestamp %L)',ColName) into categoryval;
    when 'year' then
     EXECUTE format('select extract(year from timestamp %L)',ColName) into categoryval;
    when 'hour'then
     EXECUTE format('select extract(hour from timestamp %L)',ColName) into categoryval;
    when 'minute' then
     EXECUTE format('select extract(minute from timestamp %L)',ColName) into categoryval;
    else
     Return Colname;
    End CAse;

    CASE date_category
    when 'day' then
     RETURN make_timestamp(2000,1,categoryval::integer,1,0,0);
    when 'month' then
     RETURN make_timestamp(2000,categoryval::integer,1,1,0,0);
    when 'year' then
        RETURN make_timestamp(categoryval::integer,1,1,0,0,0);
    when 'hour' then
        RETURN make_timestamp(2000,1,1,categoryval::integer,0,0);
    else
        RETURN make_timestamp(2000,1,1,0,categoryval::integer,0);
     End Case;
END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;
