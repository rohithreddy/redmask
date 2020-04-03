CREATE OR REPLACE FUNCTION redmask.anonymize(ColName text , Pattern character  default 'x',UntouchedPrefixLength integer default 0,UntouchedSuffixLength integer default 0)
RETURNS text AS $$
DECLARE
    strlength integer;
BEGIN
    strlength:=length(ColName);
    IF strlength< (UntouchedPrefixLength+UntouchedSuffixLength) then
        return substring(ColName FROM 1 FOR (strlength/2) )
          ||repeat(Pattern,4)
          ||substring(ColName FROM (strlength-(strlength/2)+1) FOR (strlength/2) );
    END IF;
    return substring(ColName FROM 1 FOR UntouchedPrefixLength)
      || repeat(Pattern,(strlength-UntouchedPrefixLength-UntouchedSuffixLength))
      || substring(ColName FROM (strlength-UntouchedSuffixLength+1) FOR UntouchedSuffixLength);

END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;