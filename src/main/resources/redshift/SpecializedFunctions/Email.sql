(ColName text, masktype text, arg1)
RETURNS text stable AS $$
import re
usernametext,domaintext = ColName.split('@')
if masktype == 'domain':
  return re.sub(r'.','*',usernametext)+'@'+domaintext;
if masktype ==  'firstN':
  return ColName[:arg1]+re.sub(r'.','*',ColName[arg1:]);
if masktype == 'firstndomain':
  return usernametext[:1]+re.sub(r'.','*',usernametext[1:])+'@'+domaintext;
if masktype ==  'nonspecialcharacter':
  return re.sub(r'[a-zA-Z0-9]','*',ColName);
else:
  return Colname;
$$ LANGUAGE plpythonu;
