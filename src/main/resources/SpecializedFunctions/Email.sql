(
    ColName text,
    masktype text default 'domain',
    arg1 integer default 4)
RETURNS text AS $$
DECLARE
    username_text text := regexp_replace(Colname, '@.*', '');
    domain_text text := regexp_replace(Colname, '.*@', '');
BEGIN
    CASE masktype
    when 'domain' then
     return (Select regexp_replace(username_text,'.','x','g')||'@'||domain_text);
    when 'firstN' then
     return redmask.anonymize(Colname,'x',arg1);
    when 'firstndomain' then
     return (Select substring(username_text from 1 for 1)||regexp_replace(substring (username_text from 2 for length(username_text)),'.','x','g')||'@'||domain_text);
    when 'nonspecialcharacter' then
        return regexp_replace(Colname,'[a-zA-Z0-9]','x','g');
    else
     Return Colname;
    End Case;
END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;
