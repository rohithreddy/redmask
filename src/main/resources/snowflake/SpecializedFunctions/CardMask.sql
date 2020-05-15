(
    COLNAME text,
    MASKTYPE text,
    SEPARATOR text,
    ARG1 float,
    ARG2 float)
RETURNS text
LANGUAGE JAVASCRIPT
AS $$
function anonymize_numbers(colname,pattern,prefix,suffix)
{
var strlength=colname.length
if((strlength-prefix-suffix)>0) {
return (colname.slice(0,prefix)+colname.slice(prefix,strlength-suffix).replace(/[0-9]/g,pattern)+colname.slice(strlength-suffix,))
}
return colname
}
var arg1_new = ARG1+(Math.floor(ARG1/4)*SEPARATOR.length)
var arg2_new = ARG2+(Math.floor(ARG2/4)*SEPARATOR.length)
switch(MASKTYPE){
case  'last':
return anonymize_numbers(COLNAME,'x',0,arg1_new)
case 'first':
return anonymize_numbers(COLNAME,'x',arg1_new,0)
case 'firstnlast':
return anonymize_numbers(COLNAME,'x',arg1_new,arg2_new)
default:
return COLNAME
}
$$
;
