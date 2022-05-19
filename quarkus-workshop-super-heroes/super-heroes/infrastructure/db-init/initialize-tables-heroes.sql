DROP TABLE IF EXISTS Hero;
DROP SEQUENCE IF EXISTS hibernate_sequence;

CREATE SEQUENCE hibernate_sequence START 1 INCREMENT 1;

create table Hero
(
    id        int8        not null,
    level     int4        not null check (level >= 1),
    name      varchar(50) not null,
    otherName varchar(255),
    picture   varchar(255),
    powers    TEXT,
    primary key (id)
);

