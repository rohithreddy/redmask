CREATE OR REPLACE FUNCTION redmask.anonymize_letters(ColName text , Pattern character  default 'x',Prefix integer default 0,Suffix integer default 0)
RETURNS text AS $$
DECLARE
    strlength integer;
BEGIN
    strlength:=length(ColName);
    IF Prefix>0 then
        return substring(ColName FROM 1 FOR Prefix )
          ||regexp_replace(substring(ColName FROM Prefix+1 for (strlength-Prefix-Suffix)),'[a-z A-z]',Pattern,'g')
          ||substring(ColName FROM (strlength-Suffix+1) FOR Suffix );
     END IF;
     return regexp_replace(substring(ColName FROM Prefix+1 for (strlength-Prefix-Suffix)),'[a-z A-Z]',Pattern,'g')
          ||substring(ColName FROM (strlength-Suffix+1) FOR Suffix );
END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;