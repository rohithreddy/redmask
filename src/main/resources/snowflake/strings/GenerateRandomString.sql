CREATE OR REPLACE FUNCTION redmask.generate_string(COLNAME text, STRLENGTH integer)
RETURNS string
LANGUAGE JAVASCRIPT
AS $$
  var result           = '';
  var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  var charactersLength = characters.length;
  for ( var i = 0; i < STRLENGTH; i++ ) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength));
  }
  return result;
$$
;
