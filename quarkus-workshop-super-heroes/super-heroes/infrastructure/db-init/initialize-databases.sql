CREATE USER super WITH PASSWORD 'super';

CREATE USER superman WITH PASSWORD 'superman';
CREATE DATABASE heroes_database;
GRANT ALL PRIVILEGES ON DATABASE heroes_database TO superman;
GRANT ALL PRIVILEGES ON DATABASE heroes_database TO super;

CREATE USER superbad WITH PASSWORD 'superbad';
CREATE DATABASE villains_database;
GRANT ALL PRIVILEGES ON DATABASE villains_database TO superbad;
GRANT ALL PRIVILEGES ON DATABASE villains_database TO super;

CREATE USER superfight WITH PASSWORD 'superfight';
CREATE DATABASE fights_database;
GRANT ALL PRIVILEGES ON DATABASE fights_database TO superfight;
GRANT ALL PRIVILEGES ON DATABASE fights_database TO super;

