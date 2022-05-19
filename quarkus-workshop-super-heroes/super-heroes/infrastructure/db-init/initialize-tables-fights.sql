DROP TABLE IF EXISTS Fight;
DROP SEQUENCE IF EXISTS hibernate_sequence;

CREATE SEQUENCE hibernate_sequence START 1 INCREMENT 1;

create table Fight
(
    id            int8 not null,
    fightDate     timestamp,
    loserLevel    int4 not null,
    loserName     varchar(255),
    loserPicture  varchar(255),
    loserTeam     varchar(255),
    winnerLevel   int4 not null,
    winnerName    varchar(255),
    winnerPicture varchar(255),
    winnerTeam    varchar(255),
    primary key (id)
);
