DROP TABLE IF EXISTS Hero;
DROP SEQUENCE IF EXISTS hero_seq;

CREATE SEQUENCE hero_seq START 1 INCREMENT 50;

create table Hero
(
    id        int8 not null,
    level     int4 not null,
    name      varchar(50) not null,
    otherName varchar(255),
    picture   varchar(255),
    powers    TEXT,
    primary key (id)
);
