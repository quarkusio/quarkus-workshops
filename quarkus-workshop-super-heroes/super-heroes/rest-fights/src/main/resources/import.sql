ALTER SEQUENCE fight_seq RESTART WITH 50;

INSERT INTO fight(id, fightDate, winnerName, winnerLevel, winnerPicture, winnerPowers, loserName, loserLevel, loserPicture, loserPowers, winnerTeam, loserTeam)
VALUES (nextval('fight_seq'), current_timestamp,
        'Chewbacca', 5, 'https://www.superherodb.com/pictures2/portraits/10/050/10466.jpg', 'Agility, Longevity, Marksmanship, Natural Weapons, Stealth, Super Strength, Weapons Master',
        'Buuccolo', 3, 'https://www.superherodb.com/pictures2/portraits/10/050/15355.jpg', 'Accelerated Healing, Adaptation, Agility, Flight, Immortality, Intelligence, Invulnerability, Reflexes, Self-Sustenance, Size Changing, Spatial Awareness, Stamina, Stealth, Super Breath, Super Speed, Super Strength, Teleportation',
        'heroes', 'villains');
INSERT INTO fight(id, fightDate, winnerName, winnerLevel, winnerPicture, winnerPowers, loserName, loserLevel, loserPicture, loserPowers, winnerTeam, loserTeam)
VALUES (nextval('fight_seq'), current_timestamp,
        'Galadriel', 10, 'https://www.superherodb.com/pictures2/portraits/10/050/11796.jpg', 'Danger Sense, Immortality, Intelligence, Invisibility, Magic, Precognition, Telekinesis, Telepathy',
        'Darth Vader', 8, 'https://www.superherodb.com/pictures2/portraits/10/050/10444.jpg', 'Accelerated Healing, Agility, Astral Projection, Cloaking, Danger Sense, Durability, Electrokinesis, Energy Blasts, Enhanced Hearing, Enhanced Senses, Force Fields, Hypnokinesis, Illusions, Intelligence, Jump, Light Control, Marksmanship, Precognition, Psionic Powers, Reflexes, Stealth, Super Speed, Telekinesis, Telepathy, The Force, Weapons Master',
        'heroes', 'villains');
INSERT INTO fight(id, fightDate, winnerName, winnerLevel, winnerPicture, winnerPowers, loserName, loserLevel, loserPicture, loserPowers, winnerTeam, loserTeam)
VALUES (nextval('fight_seq'), current_timestamp,
        'Annihilus', 23, 'https://www.superherodb.com/pictures2/portraits/10/050/1307.jpg', 'Agility, Durability, Flight, Reflexes, Stamina, Super Speed, Super Strength',
        'Shikamaru', 1, 'https://www.superherodb.com/pictures2/portraits/10/050/11742.jpg', 'Adaptation, Agility, Element Control, Fire Control, Intelligence, Jump, Marksmanship, Possession, Reflexes, Shapeshifting, Stamina, Stealth, Telekinesis, Wallcrawling, Weapon-based Powers, Weapons Master',
        'villains', 'heroes');
