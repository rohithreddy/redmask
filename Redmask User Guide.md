<div align="center">

# Redmask User Document

20th April 2020

`Version 0.1`

**Prepared For:**

Redmask user 

<img width="350em" src="https://mma.prnewswire.com/media/1015155/HashedIn_Logo.jpg?p=publish" alt="HashedIn Logo">
</div>

---

# Red Mask


## Mask Sensitive Data in your Data Warehouse

RedMask is a CLI proxyless administration tool to mask sensitive information in a data warehouse. Administrators can mask data using a variety of techniques. Users directly connect to the underlying data warehouse and run queries. They either see masked data or the real data depending on their access permissions.  \
 \
Currently, RedMask supports Postgres, Redshift and Snowflake. It is an open source tool, released under Apache 2.0 license, and is free to use.

RedMask supports both **static** and **dynamic** masking. In static masking, a new table is created with the selected columns masked. This increases storage costs. This technique is suitable for data sets that do not change often.

In dynamic masking, RedMask creates a view that masks the desired columns. The view has the same name and columns as the underlying table, but is in a different schema. When a query is executed, the data warehouse picks either the table or the view depending on the search path/default schema. 

**Architecture**

Unlike commercial solutions, RedMask is proxyless. You only need to run RedMask when you define the rules and access control. After that, you can bring down RedMask.

RedMask does not intercept your queries or the data. Instead, it relies on standard database techniques to create alternative views with masked data. You can create these views manually as well, RedMask just makes it easier by providing an intuitive user interface.

**Application Changes**

RedMask requires a database to store temporary data. [TODO] RedMask will use a sqlite database to store this data, but you can configure it to use another database. If you lose this database, RedMask can rebuild it by reading the data warehouse. 

Applications connecting to the data warehouse have to make 2 small changes:



1. Queries accessing tables with sensitive data must refer to tables without the schema name.  \
Wrong:  ```select * from sales.customers``` \
Correct: ```select * from customers```.  \
This restriction does not apply to tables that do not have sensitive data.
2. Optionally, you need to run an initial sql statement when you create a connection to the data warehouse. This initial sql statement sets the default schema / search path to resolve table names without a schema identifier. 

If you cannot make these application changes, RedMask is not the right solution for your use case.

**Runtime Performance Impact**

With static masking, a copy of the table is created with the desired columns masked. This increases the storage requirements.

With dynamic masking, a view wraps the underlying table. This approach has a negligible impact on performance, unless the masked column is part of a join condition. In general, we recommend joining on ids that do not require to be masked. If you cannot join on an id field, there are some ways to reduce the performance impact of masking.

**Administrator Workflow**

1. Construct a json masking configuration file. Within a masking configuration, the administrator can choose specific columns that need to be masked. For each column that needs to be masked, it will allow the administrator to configure the masking rules (redaction / email masking, substitution, etc)


```
{
  "host": "localhost",
  "port": <port_number>,
  "user": <dev_user_name>,
  "database": <database_name>,
  “dbtype”: <database_type>,
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


3. Administrator will launch RedMask via CLI using below command \

```
./redmask -f=/<path_to_josn_file>/masking_config.jso -r=false -m=dynamic
```


4. Next, the administrator can choose the users for which a masking configuration is to be enabled. **Only 1 mapping configuration per user.**
5. Once the administrator saves, the RedMask will create appropriate schemas/views/permissions in the underlying data warehouse. 
6. [TODO]Optionally, RedMask will write the configuration to a local sqlite database, so that next time it is possible to edit the masking rules.

**Applying Masking Rules to Data Warehouse**

Let’s assume a database user “developer” that needs to write queries. 

Let’s take 3 tables:



*   `public.customers`: name, postal_code, and dob (date of birth) need to be masked
*   `public.orders`: amount needs to be masked
*   `public.cities`: Does not need to be masked

Now, RedMask will do the following for the customers table:



*   First, revoke access to customer table for the user developer
*   Create a new schema called “developer”
*   Create a new view developer.customers, with the following definition: \

```
CREATE VIEW developer.customers as 
SELECT id, mask(name) as name, mask(postal_code) as postal_code, mask(dob) as dob, city, is_premium, joining_date
FROM public.customers
```

*   Notice that the name of the view is the name of the table. The columns are also in the exact same order as the underlying table.
*   The function mask(..) is something that we will write. There will be several variants of this function based on the configuration the user provided.
*   When a developer runs a query like `SELECT id, name, dob from customers`, because the first schema is $user, the view will be picked up instead of the table.
*   If the developer tries the query `SELECT  id, name, dob from public.customers`, they would get a permission error, because we revoked direct access to the table.
*   The above step is repeated for every table that has masked data. \


# RedMask Masking Functions


# Sample Table Used:


```
select * from customer;
     name     |         email         | age |    dob     | interest |        card         |  mobileno  | balance 
--------------+-----------------------+-----+------------+----------+---------------------+------------+-----------------
 User Alpha   | useralpha@email.com   |   1 | 2019-07-26 |      5.4 | 1234-5679-8723-8789 | 6453478658 |        34234.42
 User Beta    | userbeta@email.com    |   2 | 2019-06-25 |      6.4 | 1234-5679-3478-6872 | 4234347654 |       452356.42
 User Charlie | usercharlie@email.com |   3 | 2019-05-24 |      7.6 | 1234-1048-1224-7389 | 4623587654 |       198731.34
 User Delta   | userdelta@email.com   |   4 | 2019-04-23 |      3.5 | 1234-5679-3247-7234 | 9182347462 |          534.46
 User Echo    | userecho@email.com    |   5 | 2019-03-22 |      2.9 | 1234-5679-7892-0934 | 3247089675 |       324423.37
 User Foxtrot | userfoxtrot@email.com |   5 | 2019-02-21 |    10.25 | 1234-4783-4234-7923 | 3478763478 |        36427.37
(6 rows)
```



# 1. String Types


## 1.1. String field

This masking function allows the user to mask a string type column with a user-defined pattern

specified as the _pattern_ parameter. The user can also leave the first and last few characters of

the string as unmasked by specifying the number of unmasked character in _show_first_ and

_show_last_ parameter respectively.

Mandatory parameters:



*   **name**: The name of the integer type column you want to mask.
*   **mask_type**: STRING_MASKING \


Optional Parameters: Map of additional params



*   **pattern**: The pattern by which you would want to mask the text column. Default value is *
*   **show_first**: An integer type value representing the number of characters you don’t want to mask from the beginning. The default value is 0.
*   **show_last**: An integer type value representing the number of characters you don’t want to mask from the end. The default value is 0.

Sample JSON Configuration


```
"rules": [
    {
      "table": <table_name>,
      "columns": [
        {
          "name": <column_name>,
          "mask_type": <masking_type>,
          "mask_params": {
            "pattern": <masking_pattern>,
            "show_first": <int_value_of_show_first>,
            "show_last": <int_value_of_show_last>
          }
        }
      ]
    }
  ]
```


Sample JSON Configuration for above dataset.


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "name",
          "mask_type": "STRING_MASKING",
          "mask_params": {
            "pattern": "*",
            "show_first": 2,
            "show_last": 2
          }
        }
      ]
    }
  ]
```


Masked output for name column:

```
  name     
--------------
 Us******ha
 Us*****ta
 Us********ie
 Us******ta
 Us*****ho
 Us********ot
(6 rows)
```



# 2. Numerical Types


## 2.1 Integer

Range:  -2147483648 to +2147483647


#### 2.1.1.  Random_Integer_Within_Range

This masking function allows the user to mask an integer column with a randomly generated integer between the specified minimum and maximum values.

Mandatory parameters:



*   **name**: The name of the integer type column you want to mask.
*   **mask_type**: RANDOM_INTEGER_WITHIN_RANGE

Optional Parameters



*   **min**: An integer type value representing the minimum value of the random integer.
*   **max**: An integer type value representing the maximum value of the random integer.

 

Sample JSON Configuration


```
"rules":[
      {
         "table": <table_name>,
         "columns":[
            {
               "name": <column_name>,
               "maskType": <masking_type>,
	         "maskParams":
                 {
                    "min": <int_value_of_min>,
                    "max": <int_value_of_max>
		     }
            }
	
         ]
      }
   ]
```


Sample JSON Configuration for above dataset


```
"rules":[
      {
         "table":"customer",
         "columns":[
            {
               "name":"age",
               "maskType":"RANDOM_INTEGER_WITHIN_RANGE",
	         "maskParams":
                 {
                    "min": 10,
                    "max": 20
		     }
            }
	
         ]
      }
   ]
```


Masked Output for age column:


```
age 
-----
  11
  19
  18
  14
  18
  16
(6 rows)
```



#### 2.1.2. Random_Integer_Fixed_Width

This masking function generates a random number having a fixed number of digits as passed as the size parameter. 

 

Mandatory parameters:



*   **name**: The name of the integer type column you want to mask.
*   **mask_type**:  RANDOM_INTEGER_FIXED_WIDTH

Optional parameter



*   **size**: An integer type value representing the number of digits there should be in the randomly generated number. The default value is 2.

Sample JSON Configuration:


```
"rules": [
    {
      "table": <table_name>,
      "columns": [
        {
          "name": <column_name>,
          "maskType": <masking_type>,
          "maskParams": {
            "size": <int_value_of_size>
          }
        }
     ]
    }
  ]
```


Sample JSON configuration for above dataset


```
"rules": [
    {
      "table": customer,
      "columns": [
        {
          "name": "age",
          "maskType": "RANDOM_INTEGER_FIXED_WIDTH",
          "maskParams": {
            "size": 2
          }
        }
     ]
    }
  ]
```


Masked Output for age column:


```
 age 
-----
  14
  59
  77
  52
  83
  55
(6 rows)
```



#### 2.1.3. Integer_Fixed_Value

This masking function mask a integer type column by the fixed number passed as the value parameter or the default value 0.

Mandatory parameters:



*   **name**: The name of the integer type column you want to mask.
*   **mask_type**: INTEGER_FIXED_VALUE

Optional parameters:



*   **value**: The integer type value the column is to be replaced by. The default value is 0.

Sample JSON Configuration:


```
"rules": [
    {
      "table": <table_name>,
      "columns": [
        {
          "name": <column_name>,
          "maskType": <masking_type>,
          "maskParams": {
            "value": <int_value_of_value>
          }
        }
     ]
    }
  ]
```


  SampleJSON Configuration for above dataset:


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "age",
          "maskType": "INTEGER_FIXED_VALUE",
          "maskParams": {
            "value": 6
          }
        }
      ]
    }
  ]
```


	

Masked Output for age column:


```
age 
-----
   6
   6
   6
   6
   6
   6
(6 rows)
```



#### 2.1.4. Integer_Range

This masking function converts a column of type integer into a range of integer,  with the range equal to the step parameter.

Mandatory parameters:



*   **name**: The name of the integer type column you want to mask.
*   **mask_type**: INTEGER_RANGE

Optional parameters:



*   **step**: The integer type value denoting the range width. the default value is 10.

  Sample JSON Configuration:


```
"rules": [
    {
      "table": <table_name>,
      "columns": [
        {
          "name": <column_name>,
          "maskType": <masking_type>,
          "maskParams": {
            "step": <int_value_of_step>
          }
        }
     ]
    }
  ]
```


Sample JSON Configuration for above dataset:


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "age",
          "maskType": "INTEGER_RANGE",
          "maskParams": {
            "step": 2
          }
        }
      ]
    }
  ]
```


Masked Output for age column


```
 age  
-------
 [0,2)
 [2,4)
 [2,4)
 [4,6)
 [4,6)
 [4,6)
(6 rows)
```



## 2.2 Bigint

Range:  -9223372036854775808 to +9223372036854775807


#### 2.2.1. Bigint_Range

This masking function converts a column of type bigint into a range of bigint, with the range equal to the step parameter.

Mandatory parameters:



*   **name**: The name of the integer type column you want to mask.
*   **mask_type**: BIGINT_RANGE

Optional parameters:



*   **step**: The bigint type value denoting the range width. The default is 10.

Sample JSON Configuration:


```
"rules": [
    {
      "table": <table_name>,
      "columns": [
        {
          "name": <column_name>,
          "maskType": <masking_type>,
          "maskParams": {
            "step": <int_value_of_step>
          }
        }
     ]
    }
  ]
```


Sample JSON Configuration for above dataet:


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "mobileno",
          "maskType": "BIGINT_RANGE",
          "maskParams": {
            "step": 2000000000
          }
        }
      ]
    }
  ]
```


Masked Output for mobileno column


```
mobileno         
--------------------------
 [6000000000,7000000000)
 [4000000000,5000000000)
 [4000000000,5000000000)
 [9000000000,10000000000)
 [3000000000,4000000000)
 [3000000000,4000000000)
(6 rows)
```



## 2.3 Numeric

Range:  User-defined range 


#### 2.3.1. Numeric_Range

This masking function converts a column of type numeric into a range of numeric, with the range equal to the step parameter.

Mandatory parameters:



*   **name**: The name of the integer type column you want to mask.
*   **mask_type**: NUMERIC_RANGE

Optional parameters:



*   **step**: The integer type value denoting the range width.

 

Sample JSON Configuration:


```
"rules": [
    {
      "table": <table_name>,
      "columns": [
        {
          "name": <column_name>,
          "maskType": <masking_type>,
          "maskParams": {
            "step": <int_value_of_step>
          }
        }
     ]
    }
  ]
```


Sample JSON Configuration: for above dataset”


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "balance",
          "maskType": "NUMERIC_RANGE",
          "maskParams": {
            "step": 1000
          }
        }
      ]
    }
  ]
```


Masked Output for balance column


```
balance 
-----------------
 [34000,35000)
 [452000,453000)
 [198000,199000)
 [0,1000)
 [324000,325000)
 [36000,37000)
(6 rows)
```



## 2.4 Float

Range:  -9223372036854775808 to +9223372036854775807


#### 2.4.1. Float_Fixed_Value

This masking function mask a float type column by the fixed number passed as the

 * value parameter or the default value of 0.00.

Mandatory parameters:



*   **name**: The name of the integer type column you want to mask.
*   **mask_type**: FLOAT_FIXED_VALUE

Optional Parameters:



*   **value**: The integer type value the column is to be replaced by. The default value is 0.00 .

Sample JSON Configuration:


```
"rules": [
    {
      "table": <table_name>,
      "columns": [
        {
          "name": <column_name>,
          "maskType": <masking_type>,
          "maskParams": {
            "value": <int_value_of_value>
          }
        }
     ]
    }
  ]
```


 

Sample JSON Configuration for above dataset:


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "interest",
          "maskType": "FLOAT_FIXED_VALUE",
          "maskParams": {
            "value": 7.6
          }
        }
      ]
    }
  ]
```


Masked Output for interest column


```
interest 
----------
      7.6
      7.6
      7.6
      7.6
      7.6
      7.6
(6 rows)
```



# 3. Specific Use-Cases


## 3.1. Email

 This masking function is used to mask a column containing email data entered as string.

 This function has the following mask_type:-



*   EMAIL_SHOW_DOMAIN : This shows only the domain part of the email address.
*   EMAIL_SHOW_FIRST_CHARACTER_DOMAIN : This shows the first character and the domain part of the email address.
*   EMAIL_SHOW_FIRST_CHARACTERS : This shows the first few characters equal to number passed in the show_first parameter.
*   EMAIL_MASK_ALPHANUMERIC : This mask all the alphanumeric characters in the email address.

Mandatory parameters:



*   **name**: The name of the integer type column you want to mask.
*   **mask_type**: One of the following &lt; EMAIL_SHOW_DOMAIN, EMAIL_SHOW_FIRST_CHARACTER_DOMAIN, EMAIL_SHOW_FIRST_CHARACTERS, EMAIL_MASK_ALPHANUMERIC>

Optional  Arguments



*   **show_first**: The number of characters you want to display(only needed for_ the “Email_Show_First_Characters” _mask type.

Sample JSON Configuration


```
"rules": [
    {
      "table": <table_name>,
      "columns": [
        {
          "name": <column_name>,
          "maskType": <masking_type>,
          "maskParams": {
            "show_first": <int_value_of_show_first>
          }
        }
     ]
    }
  ]
```


Sample JSON Configuration for Email_Show_Domain on above dataset:


```
"rules": [
   {
      "table": "customer",
      "columns": [
        {
          "name": "email",
          "maskType": "EMAIL_SHOW_DOMAIN",
          "maskParams": {
          }
        }
      ]
   }
  ]
```


Masked Output for Email Column:


```
       email         
-----------------------
 xxxxxxxxx@email.com
 xxxxxxxx@email.com
 xxxxxxxxxxx@email.com
 xxxxxxxxx@email.com
 xxxxxxxx@email.com
 xxxxxxxxxxx@email.com
(6 rows)
```


Sample JSON Configuration for Email_Show_First_Character_Domain on above dataset:


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "email",
          "maskType": "EMAIL_SHOW_FIRST_CHARACTER_DOMAIN",
          "maskParams": {
          }
        }
      ]
    }
  ]
```


Masked Output for Email Column:


```
         email         
-----------------------
 uxxxxxxxx@email.com
 uxxxxxxx@email.com
 uxxxxxxxxxx@email.com
 uxxxxxxxx@email.com
 uxxxxxxx@email.com
 uxxxxxxxxxx@email.com
(6 rows)
```


Sample JSON Configuration for Email_Show_First_Characters


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "email",
          "maskType": "EMAIL_SHOW_FIRST_CHARACTERS",
          "maskParams": {
          "show_first": 7
          }
        }
      ]
    }
  ]
```


Masked Output for Email Column:


```
        email         
-----------------------
 useralpxxxxxxxxxxxx
 userbetxxxxxxxxxxx
 userchaxxxxxxxxxxxxxx
 userdelxxxxxxxxxxxx
 userechxxxxxxxxxxx
 userfoxxxxxxxxxxxxxxx
(6 rows)
```

Sample JSON Configuration for Email_Mask_Alphanumeric on above dataset:


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "email",
          "maskType": "EMAIL_SHOW_DOMAIN",
          "maskParams": {
          }
        }
      ]
    }
  ]
```


Masked Output for Email Column:


```
        email         
-----------------------
 xxxxxxxxx@xxxxx.xxx
 xxxxxxxx@xxxxx.xxx
 xxxxxxxxxxx@xxxxx.xxx
 xxxxxxxxx@xxxxx.xxx
 xxxxxxxx@xxxxx.xxx
 xxxxxxxxxxx@xxxxx.xxx
(6 rows)
```

## 3.2. Debit/Credit Card

This masking function is used to mask column containing credit/debit card data entered as strings.

This function have the following variations :-



*   CREDIT_CARD_SHOW_FIRST : This show the first few character equal to number passed in the show_first parameter.
*   CREDIT_CARD_SHOW_LAST: This show the last few character equal to number passed in the show_last parameter.
*   CREDIT_CARD_SHOW_FIRST_LAST: This shows the first few characters  and the last few characters equal to the number passed in the show_first and show_last parameter respectively.

Mandatory parameters:



*   **name**: The name of the integer type column you want to mask.
*   **mask_type**: One of the following &lt;CREDIT_CARD_SHOW_FIRST, CREDIT_CARD_SHOW_LAST, CREDIT_CARD_SHOW_FIRST_LAST

Optional parameter



*   **separator**: String/character used to separate the card number in group of 4s..
*   **show_first**: Integer value to depict number of digits to leave from the start.
*   **show_last**: Integer value to depict number of digits to leave from the start

Sample JSON Configuration


```
"rules": [
    {
      "table": <table_name>,
      "columns": [
        {
          "name": <column_name>,
          "mask_type": <masking_type>,
          "mask_params": {
            "separator": <separator_pattern>,
            "show_first": <int_value_of_show_first>,
            "show_last": <int_value_of_show_last>
          }
        }
      ]
    }
  ]
```


Sample JSON configuration for Credit_Card_Show_First on above dataset:


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "card",
          "maskType": "CREDIT_CARD_SHOW_FIRST",
          "maskParams": {
            "separator": "-",
            "show_first": 4
          }
        }
      ]
    }
  ]
```


Masked Output for Card Column:


```
     card         
---------------------
 1234-xxxx-xxxx-xxxx
 1234-xxxx-xxxx-xxxx
 1234-xxxx-xxxx-xxxx
 1234-xxxx-xxxx-xxxx
 1234-xxxx-xxxx-xxxx
 1234-xxxx-xxxx-xxxx
(6 rows)
```

Sample JSON configuration for Credit_Card_Show_Last on above dataset:


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "age",
          "maskType": "CREDIT_CARD_SHOW_LAST",
          "maskParams": {
            "separator": "-",
            "show_last": 4
          }
        }
      ]
    }
  ]
```


Masked Output for Card Column:


```
      card         
---------------------
 xxxx-xxxx-xxxx-8789
 xxxx-xxxx-xxxx-6872
 xxxx-xxxx-xxxx-7389
 xxxx-xxxx-xxxx-7234
 xxxx-xxxx-xxxx-0934
 xxxx-xxxx-xxxx-7923
(6 rows)
```


Sample JSON configuration for Credit_Card_Show_First_Last on above dataset:


```
"rules": [
    {
      "table": "customer",
      "columns": [
        {
          "name": "age",
          "maskType": "CREDIT_CARD_SHOW_FIRST_LAST",
          "maskParams": {
            "separator": "-",
            "show_first": 5,
            "show_last": 3
          }
        }
      ]
    }
  ]
```


Masked Output for Card Column:


```
        card         
---------------------
 1234-5xxx-xxxx-x789
 1234-5xxx-xxxx-x872
 1234-1xxx-xxxx-x389
 1234-5xxx-xxxx-x234
 1234-5xxx-xxxx-x934
 1234-4xxx-xxxx-x923
(6 rows)
```

