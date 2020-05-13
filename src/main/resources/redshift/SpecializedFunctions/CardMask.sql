(ColName text, masktype text, separator text, arg1 integer, arg2 integer)
RETURNS text stable AS $$
def anonymizenumbers(colname,pattern,prefix,suffix):
  import re
  strlength = len(colname)
  if (strlength-prefix-suffix) >= 0:
    return colname[:prefix]+re.sub(r"[0-9]",pattern,colname[prefix:(strlength-suffix)])+colname[strlength-suffix:]
  return colname
arg1_new = arg1+((arg1/4)*len(separator))
arg2_new = arg2+((arg2/4)*len(separator))
if masktype == 'last' :
  return anonymizenumbers(ColName,'x',0,arg1_new)
elif masktype == 'first':
  return anonymizenumbers(ColName,'x',arg1_new,0)
elif masktype == 'firstnlast':
  return anonymizenumbers(ColName,'x',arg1_new,arg2_new)
else:
  return ColName
$$ LANGUAGE plpythonu;
