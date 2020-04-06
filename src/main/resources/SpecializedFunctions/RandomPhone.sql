CREATE OR REPLACE FUNCTION redmask.random_phone( 
          phone_prefix TEXT DEFAULT '0' 
        ) 
        RETURNS TEXT AS $$ 
        BEGIN 
          RETURN (SELECT  phone_prefix 
                  || CAST(redmask.random_int_between(100000000,999999999) AS TEXT) 
                  AS \"phone\"); 
        END 
        $$ LANGUAGE plpgsql;