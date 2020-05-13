(ColName integer, int_start integer, int_stop integer)
RETURNS integer stable AS $$
import random
return random.randint(int_start,int_stop)
$$ LANGUAGE plpythonu;