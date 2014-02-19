DELIMITER /
DROP PROCEDURE IF EXISTS upsert_player/
DROP PROCEDURE IF EXISTS insert_statistic/
DROP PROCEDURE IF EXISTS insert_match/
DROP PROCEDURE IF EXISTS upsert_wave_summary/
DROP PROCEDURE IF EXISTS upsert_player/
DROP PROCEDURE IF EXISTS insert_statistic/
DROP PROCEDURE IF EXISTS insert_match/
DROP PROCEDURE IF EXISTS upsert_wave_summary/
DROP PROCEDURE IF EXISTS upsert_player/
DROP PROCEDURE IF EXISTS insert_statistic/
DROP PROCEDURE IF EXISTS insert_match/
DROP PROCEDURE IF EXISTS upsert_wave_summary/
DROP PROCEDURE IF EXISTS upsert_player/
DROP PROCEDURE IF EXISTS insert_statistic/
DROP PROCEDURE IF EXISTS insert_match/
DROP PROCEDURE IF EXISTS upsert_wave_summary/
DELIMITER ;
ALTER TABLE `match` DROP FOREIGN KEY FKmatch682879;
ALTER TABLE `match` DROP FOREIGN KEY FKmatch598647;
ALTER TABLE player_session DROP FOREIGN KEY FKplayer_ses609271;
ALTER TABLE wave_statistic  DROP FOREIGN KEY FKwave_stati821020;
ALTER TABLE player_session DROP FOREIGN KEY FKplayer_ses18539;
ALTER TABLE player_statistic DROP FOREIGN KEY FKplayer_sta234666;
ALTER TABLE player_statistic DROP FOREIGN KEY FKplayer_sta440784;
ALTER TABLE `match` DROP FOREIGN KEY FKmatch593517;
ALTER TABLE wave_statistic  DROP FOREIGN KEY FKwave_stati133088;
ALTER TABLE wave_summary DROP FOREIGN KEY FKwave_summa456733;
ALTER TABLE statistic DROP FOREIGN KEY FKstatistic966535;
ALTER TABLE wave_statistic  DROP FOREIGN KEY FKwave_stati573684;
ALTER TABLE wave_summary_perk DROP FOREIGN KEY FKwave_summa738015;
ALTER TABLE wave_summary_perk DROP FOREIGN KEY FKwave_summa968756;
DROP TABLE IF EXISTS setting;
DROP TABLE IF EXISTS map;
DROP TABLE IF EXISTS `match`;
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
  id         smallint NOT NULL AUTO_INCREMENT comment 'Unique id for each game setting', 
  difficulty varchar(15) NOT NULL comment 'Difficulty setting of the game', 
  length     varchar(10) NOT NULL comment 'Length of the game', 
  PRIMARY KEY (id)) comment='Stores all used game settings';
CREATE TABLE map (
  id   smallint NOT NULL AUTO_INCREMENT comment 'Id of each map', 
  name varchar(64) NOT NULL comment 'Name of the map', 
  PRIMARY KEY (id), 
  UNIQUE INDEX (name)) comment='Stores all played maps';
CREATE TABLE `match` (
  id         varchar(36) NOT NULL comment 'Unique id for the match', 
  setting_id smallint NOT NULL comment 'Id of the match''s game settings', 
  map_id     smallint NOT NULL comment 'Id of the map that was played', 
  server_id  smallint NOT NULL, 
  wave       smallint comment 'Wave reached', 
  result     smallint comment 'Result of the match: 1=win, -1=loss', 
  timestamp  timestamp NULL comment 'Date and time the match ended', 
  duration   int comment 'How long the match lasted', 
  PRIMARY KEY (id)) comment='Contains data on each match that was played';
CREATE TABLE player (
  id     varchar(20) NOT NULL comment 'Player''s steamid64 value', 
  name   varchar(64) comment 'Steam community name', 
  avatar varchar(255) comment 'Steam community avatar URL', 
  PRIMARY KEY (id), 
  UNIQUE INDEX (id)) comment='Contains all the players who have played on the server';
CREATE TABLE player_session (
  id              int NOT NULL AUTO_INCREMENT comment 'Unique id for each player''s session', 
  player_id       varchar(20) NOT NULL comment 'Player''s id', 
  match_id        varchar(36) NOT NULL comment 'Id of the match the player was a part of', 
  wave            smallint NOT NULL comment 'Wave the match was on when the player ended his session', 
  timestamp       timestamp NOT NULL comment 'Date and time the player ended his session', 
  duration        int NOT NULL comment 'How long the player''s session lasted', 
  disconnected    bit(1) NOT NULL comment 'True if the player left before the match ended', 
  finale_played   bit(1) NOT NULL comment 'True if the player participated in the patriarch battle', 
  finale_survived bit(1) NOT NULL comment 'True if the player survived the patriarch battle', 
  PRIMARY KEY (id)) comment='Store data on each match a player joined';
CREATE TABLE wave_statistic  (
  wave_summary_id int NOT NULL comment 'Wave summary that this entry provides more details for', 
  statistic_id    smallint NOT NULL comment 'Id of the statistic', 
  perk_id         smallint NOT NULL comment 'Id of the perk this statistic describes', 
  value           int NOT NULL comment='Statistics that are grouped on a wave by wave basis';
CREATE TABLE player_statistic (
  statistic_id      smallint NOT NULL comment 'Id of the statistic', 
  player_session_id int NOT NULL comment 'Player''s session id the statistic is for', 
  value             int NOT NULL comment='Stores the player statistics for each session';
CREATE TABLE statistic (
  id          smallint NOT NULL AUTO_INCREMENT, 
  category_id smallint NOT NULL comment 'Id of the category the statistic belongs to', 
  name        varchar(32) NOT NULL comment 'Name of the statistic', 
  PRIMARY KEY (id)) comment='Statistics that are tracked by the database';
CREATE TABLE server (
  id      smallint NOT NULL AUTO_INCREMENT comment 'Server id', 
  address varchar(15) NOT NULL comment 'Server address', 
  port    smallint NOT NULL comment 'Server port', 
  PRIMARY KEY (id)) comment='Set of servers that a match has been played on';
CREATE TABLE wave_summary (
  id       int NOT NULL AUTO_INCREMENT comment 'Unique ID for the wave summary', 
  match_id varchar(36) NOT NULL comment 'ID of the match the summary is describing', 
  wave     smallint NOT NULL comment 'Wave the entry is describing', 
  survived bit(1) comment 'True if the team survived the wave', 
  duration int comment 'How long the wave took to complete', 
  PRIMARY KEY (id)) comment='Summary of the results of the specific wave';
CREATE TABLE category (
  id   smallint NOT NULL AUTO_INCREMENT comment 'Id of the category', 
  name varchar(32) NOT NULL UNIQUE comment 'Name of the category', 
  PRIMARY KEY (id)) comment='Categories that each statistic may fall under';
CREATE TABLE wave_summary_perk (
  wave_summary_id int NOT NULL comment 'Wave summary that this entry provides perk information for', 
  perk_id         smallint NOT NULL comment 'Id of the perk', 
  count           int NOT NULL comment 'Stores the perk counts for each wave';
ALTER TABLE `match` ADD INDEX FKmatch682879 (setting_id), ADD CONSTRAINT FKmatch682879 FOREIGN KEY (setting_id) REFERENCES setting (id);
ALTER TABLE `match` ADD INDEX FKmatch598647 (map_id), ADD CONSTRAINT FKmatch598647 FOREIGN KEY (map_id) REFERENCES map (id);
ALTER TABLE player_session ADD INDEX FKplayer_ses609271 (player_id), ADD CONSTRAINT FKplayer_ses609271 FOREIGN KEY (player_id) REFERENCES player (id);
ALTER TABLE wave_statistic  ADD INDEX FKwave_stati821020 (statistic_id), ADD CONSTRAINT FKwave_stati821020 FOREIGN KEY (statistic_id) REFERENCES statistic (id);
ALTER TABLE player_session ADD INDEX FKplayer_ses18539 (match_id), ADD CONSTRAINT FKplayer_ses18539 FOREIGN KEY (match_id) REFERENCES `match` (id);
ALTER TABLE player_statistic ADD INDEX FKplayer_sta234666 (player_session_id), ADD CONSTRAINT FKplayer_sta234666 FOREIGN KEY (player_session_id) REFERENCES player_session (id);
ALTER TABLE player_statistic ADD INDEX FKplayer_sta440784 (statistic_id), ADD CONSTRAINT FKplayer_sta440784 FOREIGN KEY (statistic_id) REFERENCES statistic (id);
ALTER TABLE `match` ADD INDEX FKmatch593517 (server_id), ADD CONSTRAINT FKmatch593517 FOREIGN KEY (server_id) REFERENCES server (id);
ALTER TABLE wave_statistic  ADD INDEX FKwave_stati133088 (perk_id), ADD CONSTRAINT FKwave_stati133088 FOREIGN KEY (perk_id) REFERENCES statistic (id);
ALTER TABLE wave_summary ADD INDEX FKwave_summa456733 (match_id), ADD CONSTRAINT FKwave_summa456733 FOREIGN KEY (match_id) REFERENCES `match` (id);
ALTER TABLE statistic ADD INDEX FKstatistic966535 (category_id), ADD CONSTRAINT FKstatistic966535 FOREIGN KEY (category_id) REFERENCES category (id);
ALTER TABLE wave_statistic  ADD INDEX FKwave_stati573684 (wave_summary_id), ADD CONSTRAINT FKwave_stati573684 FOREIGN KEY (wave_summary_id) REFERENCES wave_summary (id);
ALTER TABLE wave_summary_perk ADD INDEX FKwave_summa738015 (wave_summary_id), ADD CONSTRAINT FKwave_summa738015 FOREIGN KEY (wave_summary_id) REFERENCES wave_summary (id);
ALTER TABLE wave_summary_perk ADD INDEX FKwave_summa968756 (perk_id), ADD CONSTRAINT FKwave_summa968756 FOREIGN KEY (perk_id) REFERENCES statistic (id);
CREATE UNIQUE INDEX setting_index 
  ON setting (difficulty, length);
CREATE UNIQUE INDEX player_session_index 
  ON player_session (player_id, timestamp);
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

DELIMITER /
CREATE PROCEDURE upsert_player(new_id varchar(255), new_name varchar(255), new_avatar varchar(255))
BEGIN
    INSERT INTO player(id, name, avatar) values (new_id, new_name, new_avatar) 
        ON DUPLICATE KEY UPDATE name=new_name, avatar=new_avatar;
END/
DELIMITER ;
DELIMITER /
CREATE PROCEDURE insert_statistic(new_category_name varchar(255), new_name varchar(255))
BEGIN
    INSERT IGNORE INTO category(name) VALUES (new_category_name);
    INSERT IGNORE INTO statistic(category_id, name) values (
            (SELECT id FROM category WHERE name=new_category_name), 
            new_name);
END/
DELIMITER ;
DELIMITER /
CREATE PROCEDURE insert_match(match_uuid varchar(36), match_difficulty varchar(15), 
        match_length varchar(10), match_map varchar(64), match_server_address varchar(15), 
        match_server_port smallint)
BEGIN
    INSERT IGNORE INTO setting(difficulty, length) VALUES (match_difficulty, match_length);
    INSERT IGNORE INTO map(name) VALUES (match_map);
    INSERT IGNORE INTO server(address, port) VALUES (match_server_address, match_server_port);
    INSERT INTO `match` (id, setting_id, map_id, server_id) VALUES (match_uuid, 
            (SELECT id FROM setting WHERE difficulty=match_difficulty AND length=match_length),
            (SELECT id FROM map WHERE name=match_map), 
            (SELECT id FROM server WHERE address=match_server_address AND port=match_server_port));
    
END/
DELIMITER ;
DELIMITER /
CREATE PROCEDURE upsert_wave_summary(new_match_id varchar(36), new_wave smallint, was_survived bit, wave_duration int)
BEGIN
    INSERT INTO wave_summary(match_id, wave, survived, duration) VALUES (
            new_match_id, new_wave, was_survived, wave_duration) ON DUPLICATE KEY UPDATE
            survived=was_survived, duration=wave_duration;
END/
DELIMITER ;
