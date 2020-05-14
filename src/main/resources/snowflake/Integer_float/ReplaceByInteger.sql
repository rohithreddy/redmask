(C float , REPLACEMENT float)
RETURNS float
LANGUAGE JAVASCRIPT
AS $$
return Math.floor(REPLACEMENT)
$$
;