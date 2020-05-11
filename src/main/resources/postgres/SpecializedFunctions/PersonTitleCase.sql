CREATE OR REPLACE FUNCTION redmask.titlecase(Given_string text)
RETURNS text as $$
BEGIN
 return (Select regexp_replace(initcap(Given_string),'[a-z]','x','g'));
END;
$$
LANGUAGE plpgsql VOLATILE SECURITY INVOKER;
