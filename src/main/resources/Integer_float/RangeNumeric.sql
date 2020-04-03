CREATE OR REPLACE FUNCTION redmask.range_numeric(
  val NUMERIC,
  step INTEGER DEFAULT 10
)
RETURNS NUMRANGE
AS $$
WITH i AS (
  SELECT redmask.range_int4(val::INTEGER,step) as r
)
SELECT numrange(
    lower(i.r)::NUMERIC,
    upper(i.r)::NUMERIC
  )
FROM i
;
$$
LANGUAGE SQL IMMUTABLE SECURITY INVOKER;
