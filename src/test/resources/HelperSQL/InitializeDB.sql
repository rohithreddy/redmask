DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS cashier;
CREATE TABLE IF NOT EXISTS customer(
  name text,
  email text,
  age integer,
  DOB date,
  interest float,
  card text
);
insert into customer VALUES ('User Alpha','useralpha@email.com',1,'2019-07-26',3.5,'1234-5679-8723-8789');
insert into customer VALUES ('User Beta','userbeta@email.com',2,'2019-06-25',6.4,'1234-5679-3478-6872');
insert into customer VALUES ('User Charlie','usercharlie@email.com',3,'2019-05-24',7.6,'1234-1048-1224-7389');
insert into customer VALUES ('User Delta','userdelta@email.com',4,'2019-04-23',3.5,'1234-5679-3247-7234');
insert into customer VALUES ('User Echo','userecho@email.com',5,'2019-03-22',2.9,'1234-5679-7892-0934');
insert into customer VALUES ('User Foxtrot','userfoxtrot@email.com',5,'2019-02-21',10.25,'1234-4783-4234-7923');

CREATE TABLE IF NOT EXISTS cashier(
  name text,
  sales numeric(15,2),
  aadhaarNo bigint
);

insert into cashier VALUES ('Lucas Scott',1232.34,859734858437);
insert into cashier VALUES ('Brooke Davis',47368.64,347892774389);
insert into cashier VALUES ('Peyton Sawyer',34794.70,134974732856);
