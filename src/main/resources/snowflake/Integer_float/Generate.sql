(COLNAME float, SIZE float)
returns float
LANGUAGE JAVASCRIPT
AS $$
  var max = 10**SIZE
  var min = 10**(SIZE-1)
  return Math.floor(Math.random() * (max - min)) + min;
$$
;
