/**
    Document        kfsxts_advanced_db_mysql.sql
    Author          etsai (scary ghost)
    Website         https://github.com/scaryghost/KFStatsXTS_Advanced
    Description     SQL to generate the MySQL advanced database for KFStatsX.  Uncomment the 
                    below section if you need to drop the tables to reset the db
 */

/*
DROP PROCEDURE IF EXISTS insert_setting;
DROP PROCEDURE IF EXISTS insert_level;
DROP PROCEDURE IF EXISTS upsert_player;
DROP PROCEDURE IF EXISTS insert_category;
DROP PROCEDURE IF EXISTS insert_statistic;
DROP PROCEDURE IF EXISTS insert_setting;
DROP PROCEDURE IF EXISTS insert_level;
DROP PROCEDURE IF EXISTS upsert_player;
DROP PROCEDURE IF EXISTS insert_category;
DROP PROCEDURE IF EXISTS insert_statistic;
DROP PROCEDURE IF EXISTS insert_setting;
DROP PROCEDURE IF EXISTS insert_level;
DROP PROCEDURE IF EXISTS upsert_player;
DROP PROCEDURE IF EXISTS insert_category;
DROP PROCEDURE IF EXISTS insert_statistic;
DROP PROCEDURE IF EXISTS insert_setting;
DROP PROCEDURE IF EXISTS insert_level;
DROP PROCEDURE IF EXISTS upsert_player;
DROP PROCEDURE IF EXISTS insert_category;
DROP PROCEDURE IF EXISTS insert_statistic;
ALTER TABLE `match` DROP FOREIGN KEY FKmatch682879;
ALTER TABLE `match` DROP FOREIGN KEY FKmatch90667;
ALTER TABLE player_session DROP FOREIGN KEY FKplayer_ses609271;
ALTER TABLE wave_statistic DROP FOREIGN KEY FKwave_stati295633;
ALTER TABLE player_session DROP FOREIGN KEY FKplayer_ses18539;
ALTER TABLE statistic DROP FOREIGN KEY FKstatistic966535;
ALTER TABLE wave_statistic DROP FOREIGN KEY FKwave_stati448226;
ALTER TABLE player_statistic DROP FOREIGN KEY FKplayer_sta234666;
ALTER TABLE player_statistic DROP FOREIGN KEY FKplayer_sta440784;
DROP TABLE IF EXISTS setting;
DROP TABLE IF EXISTS level;
DROP TABLE IF EXISTS `match`;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS player_session;
DROP TABLE IF EXISTS wave_statistic;
DROP TABLE IF EXISTS player_statistic;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS statistic;
*/

CREATE TABLE setting (
  id         smallint(5) NOT NULL AUTO_INCREMENT comment 'Unique id for each game setting', 
  difficulty varchar(15) NOT NULL comment 'Difficulty setting of the game', 
  length     varchar(10) NOT NULL comment 'Length of the game', 
  PRIMARY KEY (id)) comment='Stores all used game settings';
CREATE TABLE level (
  id   int(10) NOT NULL AUTO_INCREMENT comment 'Id of each level', 
  name varchar(64) NOT NULL comment 'Name of the level', 
  PRIMARY KEY (id), 
  UNIQUE INDEX (name)) comment='Stores all played levels';
CREATE TABLE `match` (
  id         varchar(36) NOT NULL comment 'Unique id for the match', 
  setting_id smallint(5) NOT NULL comment 'Id of the match''s game settings', 
  level_id   int(10) NOT NULL comment 'Id of the level that was played', 
  wave       smallint(6) comment 'Wave reached', 
  result     smallint(6) comment 'Result of the match: 1=win, -1=loss', 
  timestamp  timestamp NULL comment 'Date and time the match ended', 
  duration   int(11) comment 'How long the match lasted', 
  PRIMARY KEY (id)) comment='Contains data on each match that was played';
CREATE TABLE player (
  id     varchar(20) NOT NULL comment 'Player''s steamid64 value', 
  name   varchar(64) comment 'Steam community name', 
  avatar varchar(255) comment 'Steam community avatar URL', 
  PRIMARY KEY (id), 
  UNIQUE INDEX (id)) comment='Contains all the players who have played on the server';
CREATE TABLE player_session (
  id              int(32) NOT NULL AUTO_INCREMENT comment 'Unique id for each player''s session', 
  player_id       varchar(20) NOT NULL comment 'Player''s id', 
  match_id        varchar(36) NOT NULL comment 'Id of the match the player was a part of', 
  wave            smallint(5) NOT NULL comment 'Wave the match was on when the player ended his session', 
  timestamp       timestamp NOT NULL comment 'Date and time the player ended his session', 
  duration        int(16) NOT NULL comment 'How long the player''s session lasted', 
  disconnected    bit(1) NOT NULL comment 'Stores whether or not the player ended the session before the match ended: 1=disconnected, 0=stayed the whole match', 
  finale_played   bit(1) NOT NULL comment 'Stored whether or not the final wave was played: 1=finale played, 0=finale not reached', 
  finale_survived bit(1) NOT NULL comment 'Stores whether or not the player survived the finale: 1=survived, 0=died', 
  PRIMARY KEY (id)) comment='Store data on each match a player joined';
CREATE TABLE wave_statistic (
  statistic_id smallint(16) NOT NULL comment 'Id of the statistic', 
  match_id     varchar(36) NOT NULL comment 'Id of the match this statistic corresponds to', 
  wave         smallint(5) NOT NULL comment 'Wave the data corresponds to', 
  value        mediumint(24) NOT NULL comment 'Value of the data') comment='Statistics that are grouped on a wave by wave basis';
CREATE TABLE player_statistic (
  statistic_id      smallint(16) NOT NULL comment 'Id of the statistic', 
  player_session_id int(32) NOT NULL comment 'Player''s session id the statistic is for', 
  value             mediumint(24) NOT NULL comment 'Value of the statistic') comment='Stores the player statistics for each session';
CREATE TABLE category (
  id   smallint(5) NOT NULL AUTO_INCREMENT comment 'Id of the category', 
  name varchar(32) NOT NULL UNIQUE comment 'Name of the category', 
  PRIMARY KEY (id)) comment='Categories that each statistic may fall under';
CREATE TABLE statistic (
  id          smallint(16) NOT NULL AUTO_INCREMENT, 
  category_id smallint(5) NOT NULL comment 'Id of the category the statistic belongs to', 
  name        varchar(32) NOT NULL comment 'Name of the statistic', 
  PRIMARY KEY (id)) comment='Statistics that are tracked by the database';
ALTER TABLE `match` ADD INDEX FKmatch682879 (setting_id), ADD CONSTRAINT FKmatch682879 FOREIGN KEY (setting_id) REFERENCES setting (id);
ALTER TABLE `match` ADD INDEX FKmatch90667 (level_id), ADD CONSTRAINT FKmatch90667 FOREIGN KEY (level_id) REFERENCES level (id);
ALTER TABLE player_session ADD INDEX FKplayer_ses609271 (player_id), ADD CONSTRAINT FKplayer_ses609271 FOREIGN KEY (player_id) REFERENCES player (id);
ALTER TABLE wave_statistic ADD INDEX FKwave_stati295633 (statistic_id), ADD CONSTRAINT FKwave_stati295633 FOREIGN KEY (statistic_id) REFERENCES statistic (id);
ALTER TABLE player_session ADD INDEX FKplayer_ses18539 (match_id), ADD CONSTRAINT FKplayer_ses18539 FOREIGN KEY (match_id) REFERENCES `match` (id);
ALTER TABLE statistic ADD INDEX FKstatistic966535 (category_id), ADD CONSTRAINT FKstatistic966535 FOREIGN KEY (category_id) REFERENCES category (id);
ALTER TABLE wave_statistic ADD INDEX FKwave_stati448226 (match_id), ADD CONSTRAINT FKwave_stati448226 FOREIGN KEY (match_id) REFERENCES `match` (id);
ALTER TABLE player_statistic ADD INDEX FKplayer_sta234666 (player_session_id), ADD CONSTRAINT FKplayer_sta234666 FOREIGN KEY (player_session_id) REFERENCES player_session (id);
ALTER TABLE player_statistic ADD INDEX FKplayer_sta440784 (statistic_id), ADD CONSTRAINT FKplayer_sta440784 FOREIGN KEY (statistic_id) REFERENCES statistic (id);
CREATE UNIQUE INDEX setting_index 
  ON setting (difficulty, length);
CREATE UNIQUE INDEX player_session_index 
  ON player_session (player_id, match_id, timestamp);
CREATE UNIQUE INDEX wave_statistic_index 
  ON wave_statistic (match_id, statistic_id, wave);
CREATE UNIQUE INDEX player_statistic_index 
  ON player_statistic (statistic_id, player_session_id);
CREATE UNIQUE INDEX statistic_index 
  ON statistic (category_id, name);
DELIMITER /
CREATE PROCEDURE insert_setting(new_difficulty varchar(255), new_length varchar(255)) 
BEGIN
    INSERT IGNORE INTO setting(difficulty, length) values(new_difficulty, new_length);
END/
DELIMITER ;
DELIMITER /
CREATE PROCEDURE insert_level(new_name varchar(255))
BEGIN
    INSERT IGNORE INTO level(name) values (new_name);
END/
DELIMITER ;
DELIMITER /
CREATE PROCEDURE upsert_player(new_id varchar(255), new_name varchar(255), new_avatar varchar(255))
BEGIN
    INSERT INTO player(id, name, avatar) values (new_id, new_name, new_avatar) 
        ON DUPLICATE KEY UPDATE name=new_name, avatar=new_avatar;
END/
DELIMITER ;
DELIMITER /
CREATE PROCEDURE insert_category(new_name varchar(255))
BEGIN
    INSERT IGNORE INTO category(name) values(new_name);
END/
DELIMITER ;
DELIMITER /
CREATE PROCEDURE insert_statistic(new_category_name varchar(255), new_name varchar(255))
BEGIN
DECLARE new_category_id smallint(8);
call insert_category(new_category_name);
SELECT category.id into new_category_id from category where name=new_category_name;
INSERT IGNORE INTO statistic(category_id, name) values (new_category_id, new_name);
END/
DELIMITER ;
