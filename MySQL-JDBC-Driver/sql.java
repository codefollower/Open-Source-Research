create user test identified by test
default tablespace users
temporary tablespace temp;

grant connect,resource to test;

create table pet(
  name varchar2(50) not null,
  age number
);
