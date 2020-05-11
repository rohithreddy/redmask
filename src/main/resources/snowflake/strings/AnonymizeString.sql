(C text , PATTERN character, P float, S float)
RETURNS string
LANGUAGE JAVASCRIPT
AS $$
    var strlength=C.length;
    if((strlength-P-S)>0) {
        return (C.slice(0,P)+C.slice(P,-S).replace(/./g,PATTERN)+C.slice(-S,));
     }
     return "world";
$$
;