## Red Mask - Mask Sensitive Data in your Data Warehouse

It is a CLI proxyless administration tool to mask sensitive information in a data warehouse. Unlike commercial solutions, RedMask is proxyless. You only need to run RedMask when you define the rules and access control. After that, you can bring down RedMask.

Currently, RedMask supports Postgres, Redshift and Snowflake.
It is an open source tool, released under Apache 2.0 license, and is free to use.

This tool supports two type of data masking - static and dynamic masking:

#### Static Masking:
In static masking, a new table is created with the selected columns masked. This increases storage costs. This technique is suitable for data sets that do not change often.

#### Dynamic Masking:
In dynamic masking, RedMask  creates a view that masks the desired columns. The view has the same name and columns as the underlying table, but is in a different schema. When a query is executed, the data warehouse picks either the table or the view depending on the search path/default schema.

#### Architecture:

Unlike commercial solutions, RedMask is proxyless. You only need to run RedMask when you define the rules and access control. After that, you can bring down RedMask.

RedMask does not intercept your queries or the data. Instead, it relies on standard database techniques to create alternative views with masked data. You can create these views manually as well, RedMask just makes it easier by providing an intuitive user interface.

#### Application Changes:

RedMask requires a database to store temporary data. [TODO] RedMask will use a sqlite database to store this data, but you can configure it to use another database. If you lose this database, RedMask can rebuild it by reading the data warehouse.

Applications connecting to the data warehouse have to make 2 small changes:
1.  Queries accessing tables with sensitive data must refer to tables without the schema name.
    Wrong: `select * from sales.customers` 
    Correct: `select * from customers`
    This restriction does not apply to tables that do not have sensitive data.
2.  Optionally, you need to run an initial sql statement when you create a connection to the data warehouse. This initial sql statement sets the default schema / search path to resolve table names without a schema identifier.
 
##### If you cannot make these application changes, RedMask is not the right solution for your use case.

#### Runtime Performance Impact:

- With static masking, a copy of the table is created with the desired columns masked. This increases the storage requirements.
- With dynamic masking, a view wraps the underlying table. This approach has a negligible impact on performance, unless the masked column is part of a join condition. In general, we recommend joining on ids that do not require to be masked. If you cannot join on an id field, there are some ways to reduce the performance impact of masking.

#### Administrator Workflow:
1. Construct a json masking configuration file. Within a masking configuration, the administrator can choose specific columns that need to be masked. For each column that needs to be masked, it will allow the administrator to configure the masking rules like `STRING_MASKING`, `Random_Integer_Within_Range` etc.
```
{
  "host": "localhost",
  "port": <port_number>,
  "user": <dev_user_name>,
  "database": <database_name>,
  "dbType": <database-type>,
  "rules": [
    {
      "table": <table_name>,
      "columns": [
        {
          "name": <column_name>,
          "mask_type": <masking_type>,
          "mask_params": {
            <Key>: <value>,
            <Key>: <value>,
            <Key>: <value>
          }
        },
        {
          "name": <column_name>,
          "mask_type": <masking_type>,
          "mask_params": {
            <Key>: <value>,
            <Key>: <value>,
            <Key>: <value>
          }
        }
      ]
    }
  ]
}
```

2. Set the following parameters as system environment variables.
```
DB_SUPER_USER = <super_username>  
DB_SUPER_USER_PASSWORD = <super_user_password>  
DB_USER = <dev_user_name>  
DB_USER_PASSWORD = <dev_user_password>
```
3. Administrator will launch RedMask via CLI using below command
```
./redmask -f=/<path_to_josn_file>/masking_config.jso -r=false
```
where:
- `-f`or `--configFilePath` is complete file path of json containing masking configurations.
- `-r` or `--dryRun` when true, this will just generates sql file with required queries. It will not make any changes to DB. It indicates dry run mode.

After running the command, the RedMask will create appropriate schemas/views/permissions in the underlying data warehouse.

##### Snowflake
The Snowflake data warehouse supports role based access control, therefore the role name is to be provided under the user field in the configuration file as follows.
```
{
  "host": "localhost",
  "port": <port_number>,
  "user": <dev_role>,
  "database": <database_name>,
  "dbType": <database-type>,
  "rules": [
    {
      "table": <table_name>,
      "columns": [
        {
          "name": <column_name>,
          "mask_type": <masking_type>,
          "mask_params": {
            <Key>: <value>,
            <Key>: <value>,
            <Key>: <value>
          }
        }
      ]
    }
  ]
}
```

### Masking Functions
All masking functions are required to have the following json configuration
```
{
  "name": <column_name>,
  "mask_type": <masking_type>,
  "mask_params": {
    <Key>: <value>,
    <Key>: <value>,
    <Key>: <value>
  }
}
```

**Mandatory Arguments**
- **name:** The column name to be masked with the mentioned masking type
- **mask_type:** The mask to be used to mask the data
- **mask_params:** JSON for additional parameter. Leave Empty to use default parameters

#### Common Masking Types
The following types are supported on all supported data warehouses.

##### 1. STRING_MASKING

It masks the string column with a user-defined pattern. Additionally you can mention the number of letter to be left unmasked from the start and the end of  the string.

**Supported datatypes:** text,varchar, character varying    
**Result datatype:** text  

**Additional Paramters**  
- **pattern:** The pattern by which you would want to mask the text column. Default value is \*
- **show_first:** The number of characters you don’t want to mask from the beginning. The default value is *0*.
- **show_last:** The number of characters you don’t want to mask from the end. The default value is *0*.  

     

##### 2. RANDOM_INTEGER_WITHIN_RANGE

It masks a integer column by replacing the original value with a random generated value between the specified min and max values. 

**Supported datatypes:** int,integer, short    
**Result datatype:** int  

**Additional Paramters**  
- **min:** The minimum allowed value for the random integer. Default value is *0*.
- **max:** The maximum allowed value for the random integer. Default value is *10*.

---

##### 3. RANDOM_INTEGER_FIXED_WIDTH  

It masks a integer column by replacing the original value with a random generated value having digits equal to the specified size. 

**Supported datatypes:** int,integer, bigint,short    
**Result datatype:** Bigint

**Additional Paramters**  
- **size:** An integer type value representing the number of digits there should be in the randomly generated number. The default value is *2*.
  
##### 4. INTEGER_FIXED_VALUE

It masks an integer type column by the fixed number passed as the value parameter.

**Supported datatypes:** int,integer, short   
**Result datatype:** integer  

**Additional Paramters**  
- **value:** The integer type value the column is to be replaced by. The default value is *0*.  



##### 5. FLOAT_FIXED_VALUE

It masks a float type column by the fixed number passed as the value parameter.

**Supported datatypes:** float
**Result datatype:** float  

**Additional Paramters**  
- **value:** The integer type value the column is to be replaced by. The default value is *0.00*.   


##### 6. EMAIL_SHOW_DOMAIN

It masks the username part of the Email ID, while displaying the domain part.

**Supported datatypes:** text,varchar, character varying    
**Result datatype:** text  


##### 7. EMAIL_MASK_ALPHANUMERIC

It masks the all alphanumeric characters in the Email ID.

**Supported datatypes:** text,varchar, character varying    
**Result datatype:** text  


##### 8. EMAIL_SHOW_FIRST_CHARACTER_DOMAIN

It masks the username part of the Email ID, while displaying the first character and the domain.

**Supported datatypes:** text,varchar, character varying    
**Result datatype:** text  


##### 9. EMAIL_SHOW_FIRST_CHARACTERS

It masks entire email except the starting N characters specified in the show_first argument. 

**Supported datatypes:** text,varchar, character varying    
**Result datatype:** text  

**Additional Paramters**  
- **show_first:** The number of characters you want to display. default value is *0*.



##### 10. CREDIT_CARD_SHOW_FIRST

It mask column containing credit/debit card data, while showing the first few characters as passed in the show_first argument.

**Supported datatypes:** text,varchar, character varying    
**Result datatype:** text  

**Additional Paramters**  
- **separator:** String/character used to separate the card number into group of digits.
- **show_first:** The number of digits to be left unmasked from the start.


##### 11. CREDIT_CARD_SHOW_LAST

It mask column containing credit/debit card data, while showing the last few characters as passed in the show_last argument.

**Supported datatypes:** text,varchar, character varying    
**Result datatype:** text  

**Additional Paramters**  
- **separator:** String/character used to separate the card number into group of digits.
- **show_last:** The number of digits to be left unmasked from the last.


##### 12. CREDIT_CARD_SHOW_FIRST_LAST

It mask column containing credit/debit card data, while showing the first and last few characters as passed in the show_first and show_last argument.

**Supported datatypes:** text,varchar, character varying    
**Result datatype:** text  

**Additional Paramters**  
- **separator:** String/character used to separate the card number into group of digits.
- **show_first:** The number of digits to be left unmasked from the start.
- **show_last:** The number of digits to be left unmasked from the last.

#### Postgres Masking Types
The following types are supported only on the Postgres

##### 1. INTEGER_RANGE

It mask the integer column and convert into a int4range type with a range equal to the step parameter. 

**Supported datatypes:** int, integer   
**Result datatype:** int4range  

**Additional Paramters**  
- **step:** The integer type value denoting the range width. the default value is *10*.


##### 2. BIGINT_RANGE

It mask the integer column and convert into a int8range type with a range equal to the step parameter. 

**Supported datatypes:** int, integer   
**Result datatype:** int8range  

**Additional Paramters**  
- **step:** The integer type value denoting the range width. the default value is *10*.


##### 3. NUMERIC_RANGE

It mask the integer column and convert into a numrange type with a range equal to the step parameter. 

**Supported datatypes:** int, integer   
**Result datatype:** numrange  

**Additional Paramters**  
- **step:** The integer type value denoting the range width. the default value is *10*.



#### Todo:
- Add document for masking various functions.
- Troubleshooting
- Provide some exmaple for masked data.
