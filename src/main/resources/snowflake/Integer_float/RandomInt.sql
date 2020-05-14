(COLNAME float, MIN float, MAX float)
RETURNS float
LANGUAGE JAVASCRIPT
AS $$
  return Math.floor(Math.random() * (MAX - MIN)) + MIN;
$$
;

