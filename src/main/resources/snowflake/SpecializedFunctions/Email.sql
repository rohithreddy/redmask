(
    COLNAME text,
    MASKTYPE text,
    ARG1 float)
RETURNS text
LANGUAGE JAVASCRIPT
AS $$
    var [username,domain] = COLNAME.split('@');
    switch(MASKTYPE){
case 'domain':
return username.replace(/./g,'*')+'@'+domain;
case 'firstN':
return COLNAME.slice(0,ARG1)+COLNAME.slice(ARG1,).replace(/./g,'*');
case 'firstndomain':
return username[0]+username.slice(1,).replace(/./g,'*')+'@'+domain;
case 'nonspecialcharacter':
return COLNAME.replace(/[a-zA-Z0-9]/g,'*');
default:
return COLNAME;
}
$$
;

