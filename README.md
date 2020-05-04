# Red Mask - Mask Sensitive Data in your Data Warehouse

It is a CLI proxyless administration tool to mask sensitive information in a data warehouse. Unlike commercial solutions, RedMask is proxyless. You only need to run RedMask when you define the rules and access control. After that, you can bring down RedMask.

Currently, RedMask supports Postgres. Support for snowflake and Redshift will be added soon.
It is an open source tool, released under Apache 2.0 license, and is free to use.

This tool supports two type of data masking - static and dynamic masking:

##### Static Masking: 
In static masking, a new table is created with the selected columns masked. This increases storage costs. This technique is suitable for data sets that do not change often.

##### Dynamic Masking: 
In dynamic masking, RedMask creates a view that masks the desired columns. The view has the same name and columns as the underlying table, but is in a different schema. When a query is executed, the data warehouse picks either the table or the view depending on the search path/default schema. 

