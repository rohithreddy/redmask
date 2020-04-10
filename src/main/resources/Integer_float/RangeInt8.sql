(
  val BIGINT,
  step BIGINT DEFAULT 10
)
RETURNS INT8RANGE
AS $$
SELECT int8range(
    val / step * step,
    ((val / step)+1) * step
  );
$$
LANGUAGE SQL IMMUTABLE SECURITY INVOKER;
