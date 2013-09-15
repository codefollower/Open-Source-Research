psql test

psql postgres

create table pet(
  id serial,
  name varchar(50) not null,
  age int
);

insert into pet(name,age) values('name1', 1);
insert into pet(name,age) values('name2', 2);
select * from pet;

CREATE TABLE weather (
    city            varchar(80),
    temp_lo         int,           -- low temperature
    temp_hi         int,           -- high temperature
    prcp            real,          -- precipitation
    date            date
);

CREATE TABLE test (
    ts            timestamp 
);
