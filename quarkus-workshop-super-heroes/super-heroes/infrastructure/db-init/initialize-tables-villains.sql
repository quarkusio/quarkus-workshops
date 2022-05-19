DROP TABLE IF EXISTS Villain;
DROP SEQUENCE IF EXISTS hibernate_sequence;

CREATE SEQUENCE hibernate_sequence START 1 INCREMENT 1;

create table Villain
(
    id        int8 not null,
    level     int4 not null,
    name      varchar(255),
    otherName varchar(255),
    picture   varchar(255),
    powers    TEXT,
    primary key (id)
);
