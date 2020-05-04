(colname integer, sizes integer DEFAULT 10)
RETURNS BIGINT AS $$
    SELECT CAST(redmask.random_int_between(colname::integer, (10^(sizes-1))::integer, ((10^sizes)-1)::integer ) AS BIGINT);
$$
LANGUAGE SQL VOLATILE SECURITY INVOKER;
