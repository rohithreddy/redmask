(colname integer, sizes integer)
RETURNS bigint stable AS $$
import random
return random.randint(10**(sizes-1),10**sizes-1)
$$ LANGUAGE plpythonu;
