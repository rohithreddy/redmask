(ColName text , pattern character default 'x', prefix integer default 0, Suffix integer default 0)
RETURNS text AS $$
DECLARE
    strlength integer;
BEGIN
    strlength:=length(ColName);
    IF prefix>0 then
        return substring(ColName FROM 1 FOR prefix )
          ||regexp_replace(substring(ColName FROM prefix+1 for (strlength-prefix-Suffix)),'[0-9]',pattern,'g')
          ||substring(ColName FROM (strlength-Suffix+1) FOR Suffix );
     END IF;
     return regexp_replace(substring(ColName FROM prefix+1 for (strlength-prefix-Suffix)),'[0-9]',pattern,'g')
          ||substring(ColName FROM (strlength-Suffix+1) FOR Suffix );
END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;