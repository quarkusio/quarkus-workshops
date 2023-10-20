DROP TABLE IF EXISTS Villain;
DROP SEQUENCE IF EXISTS villain_seq;

CREATE SEQUENCE villain_seq START 1 INCREMENT 50;

create table Villain
(
    id        int8 not null,
    level     int4 not null,
    name      varchar(50) not null,
    otherName varchar(255),
    picture   varchar(255),
    powers    TEXT,
    primary key (id)
);
