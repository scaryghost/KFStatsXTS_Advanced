PRAGMA foreign_keys = ON;
DROP TABLE IF EXISTS setting;
DROP TABLE IF EXISTS map;
DROP TABLE IF EXISTS match;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS player_session;
DROP TABLE IF EXISTS wave_statistic ;
DROP TABLE IF EXISTS player_statistic;
DROP TABLE IF EXISTS statistic;
DROP TABLE IF EXISTS server;
DROP TABLE IF EXISTS wave_summary;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS wave_summary_perk;
CREATE TABLE setting (
  id          INTEGER NOT NULL PRIMARY KEY, 
  difficulty varchar(15) NOT NULL, 
  length     varchar(10) NOT NULL);
CREATE TABLE map (
  id    INTEGER NOT NULL PRIMARY KEY, 
  name varchar(64) NOT NULL);
CREATE TABLE match (
  id         varchar NOT NULL, 
  setting_id integer NOT NULL, 
  map_id     integer NOT NULL, 
  server_id  integer NOT NULL, 
  wave       integer, 
  result     integer, 
  time_end   timestamp, 
  duration   integer, 
  PRIMARY KEY (id), 
  FOREIGN KEY(setting_id) REFERENCES setting(id), 
  FOREIGN KEY(map_id) REFERENCES map(id), 
  FOREIGN KEY(server_id) REFERENCES server(id));
CREATE TABLE player (
  id     varchar(20) NOT NULL, 
  name   varchar(64), 
  avatar varchar(255), 
  PRIMARY KEY (id));
CREATE TABLE player_session (
  id               INTEGER NOT NULL PRIMARY KEY, 
  player_id       varchar(20) NOT NULL, 
  match_id        varchar NOT NULL, 
  wave            integer NOT NULL, 
  time_end        timestamp NOT NULL, 
  duration        integer NOT NULL, 
  disconnected    integer NOT NULL, 
  finale_played   integer NOT NULL, 
  finale_survived integer NOT NULL, 
  FOREIGN KEY(player_id) REFERENCES player(id), 
  FOREIGN KEY(match_id) REFERENCES match(id));
CREATE TABLE wave_statistic  (
  wave_summary_id integer NOT NULL, 
  statistic_id    integer NOT NULL, 
  perk_id         integer NOT NULL, 
  value           integer NOT NULL, 
  FOREIGN KEY(statistic_id) REFERENCES statistic(id), 
  FOREIGN KEY(perk_id) REFERENCES statistic(id), 
  FOREIGN KEY(wave_summary_id) REFERENCES wave_summary(id));
CREATE TABLE player_statistic (
  statistic_id      integer NOT NULL, 
  player_session_id integer NOT NULL, 
  value             integer NOT NULL, 
  FOREIGN KEY(player_session_id) REFERENCES player_session(id), 
  FOREIGN KEY(statistic_id) REFERENCES statistic(id));
CREATE TABLE statistic (
  id           INTEGER NOT NULL PRIMARY KEY, 
  category_id integer NOT NULL, 
  name        varchar(32) NOT NULL, 
  FOREIGN KEY(category_id) REFERENCES category(id));
CREATE TABLE server (
  id       INTEGER NOT NULL PRIMARY KEY, 
  address varchar(15) NOT NULL, 
  port    integer);
CREATE TABLE wave_summary (
  id        INTEGER NOT NULL PRIMARY KEY, 
  match_id varchar NOT NULL, 
  wave     integer NOT NULL, 
  survived integer, 
  duration integer, 
  time_end timestamp, 
  FOREIGN KEY(match_id) REFERENCES match(id));
CREATE TABLE category (
  id    INTEGER NOT NULL PRIMARY KEY, 
  name varchar(32) NOT NULL UNIQUE);
CREATE TABLE wave_summary_perk (
  wave_summary_id integer NOT NULL, 
  perk_id         integer NOT NULL, 
  count           integer, 
  FOREIGN KEY(wave_summary_id) REFERENCES wave_summary(id), 
  FOREIGN KEY(perk_id) REFERENCES statistic(id));
CREATE UNIQUE INDEX setting_index 
  ON setting (difficulty, length);
CREATE UNIQUE INDEX map_name 
  ON map (name);
CREATE UNIQUE INDEX player_id 
  ON player (id);
CREATE UNIQUE INDEX player_session_index 
  ON player_session (player_id, time_end);
CREATE UNIQUE INDEX wave_statistic_index 
  ON wave_statistic  (statistic_id, perk_id, wave_summary_id);
CREATE UNIQUE INDEX player_statistic_index 
  ON player_statistic (statistic_id, player_session_id);
CREATE UNIQUE INDEX statistic_index 
  ON statistic (category_id, name);
CREATE UNIQUE INDEX server_index 
  ON server (address, port);
CREATE UNIQUE INDEX wave_summary_index 
  ON wave_summary (match_id, wave);
CREATE UNIQUE INDEX wave_summary_perk_index 
  ON wave_summary_perk (wave_summary_id, perk_id);
