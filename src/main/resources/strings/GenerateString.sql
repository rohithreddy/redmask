CREATE OR REPLACE FUNCTION generate_string(ColName text, length integer)
RETURNS text
AS $$
  SELECT array_to_string(
    array(
        select substr('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789',
                      ((random()*(36-1)+1)::integer)
                      ,1)
        from generate_series(1,length)
    ),''
  );
$$
LANGUAGE SQL VOLATILE SECURITY INVOKER;
