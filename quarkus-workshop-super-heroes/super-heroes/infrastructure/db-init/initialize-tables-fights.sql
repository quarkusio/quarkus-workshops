DROP TABLE IF EXISTS Fight;
DROP SEQUENCE IF EXISTS fight_seq;

CREATE SEQUENCE fight_seq START 1 INCREMENT 50;

create table Fight
(
    id            int8 not null,
    fightDate     timestamp,
    loserLevel    int4 not null,
    loserName     varchar(255),
    loserPicture  varchar(255),
    loserTeam     varchar(255),
    loserPowers   TEXT,
    winnerLevel   int4 not null,
    winnerName    varchar(255),
    winnerPicture varchar(255),
    winnerTeam    varchar(255),
    winnerPowers  TEXT,
    primary key (id)
);
