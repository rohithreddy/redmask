(colname text,pattern char,prefix integer,suffix integer)
returns text stable AS $$
import re
strlength = len(colname)
if (strlength-prefix-suffix) >= 0:
	return colname[:prefix]+re.sub(r".",pattern,colname[prefix:(-suffix)])+colname[-suffix:]
return colname;
$$ language plpythonu;