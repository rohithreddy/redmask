(C text , PATTERN character, P float, S float)
RETURNS string
LANGUAGE JAVASCRIPT
AS $$
    var strlength=C.length;
    if((strlength-P-S)>0) {
        return (C.slice(0,P)+C.slice(P,strlength-S).replace(/[0-9]/g,PATTERN)+C.slice(strlength-S,));
     }
     return C;
$$
;