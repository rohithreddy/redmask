## Red Mask - Mask Sensitive Data in your Data Warehouse

It is a CLI proxyless administration tool to mask sensitive information in a data warehouse. Unlike commercial solutions, RedMask is proxyless. You only need to run RedMask when you define the rules and access control. After that, you can bring down RedMask.

Currently, RedMask supports Postgres. Support for snowflake and Redshift will be added soon.
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

#### Todo:
- Add document for masking various functions.
- Troubleshooting
- Provide some exmaple for masked data.