CREATE OR REPLACE FUNCTION redmask.generate_string(columname text, size integer)
returns text stable AS $$
import random
import string
str=""
return (str.join(random.sample(string.digits+string.ascii_letters,size)))
$$ language plpythonu;

