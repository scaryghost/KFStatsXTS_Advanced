DROP FUNCTION IF EXISTS upsert_player(varchar,varchar,varchar);
DROP FUNCTION IF EXISTS insert_statistic(varchar,varchar);
DROP FUNCTION IF EXISTS insert_match(uuid,varchar,varchar,varchar,varchar,int2);
DROP FUNCTION IF EXISTS insert_wave_summary(uuid,int2);
DROP FUNCTION IF EXISTS upsert_wave_summary(uuid,int2,bool,int4);
DROP FUNCTION IF EXISTS insert_setting(varchar,varchar);
DROP FUNCTION IF EXISTS insert_level(varchar);
DROP FUNCTION IF EXISTS upsert_player(varchar,varchar,varchar);
DROP FUNCTION IF EXISTS insert_category(varchar);
DROP FUNCTION IF EXISTS insert_statistic(varchar,varchar);
ALTER TABLE match DROP CONSTRAINT FKmatch682879;
ALTER TABLE match DROP CONSTRAINT FKmatch598647;
ALTER TABLE player_session DROP CONSTRAINT FKplayer_ses609271;
ALTER TABLE wave_statistic  DROP CONSTRAINT FKwave_stati821020;
ALTER TABLE player_session DROP CONSTRAINT FKplayer_ses18539;
ALTER TABLE player_statistic DROP CONSTRAINT FKplayer_sta234666;
ALTER TABLE player_statistic DROP CONSTRAINT FKplayer_sta440784;
ALTER TABLE match DROP CONSTRAINT FKmatch593517;
ALTER TABLE wave_statistic  DROP CONSTRAINT FKwave_stati133088;
ALTER TABLE wave_summary DROP CONSTRAINT FKwave_summa456733;
ALTER TABLE wave_statistic  DROP CONSTRAINT FKwave_stati573684;
ALTER TABLE statistic DROP CONSTRAINT FKstatistic966535;
DROP TABLE IF EXISTS setting CASCADE;
DROP TABLE IF EXISTS map CASCADE;
DROP TABLE IF EXISTS match CASCADE;
DROP TABLE IF EXISTS player CASCADE;
DROP TABLE IF EXISTS player_session CASCADE;
DROP TABLE IF EXISTS wave_statistic  CASCADE;
DROP TABLE IF EXISTS player_statistic CASCADE;
DROP TABLE IF EXISTS statistic CASCADE;
DROP TABLE IF EXISTS server CASCADE;
DROP TABLE IF EXISTS wave_summary CASCADE;
DROP TABLE IF EXISTS category CASCADE;
CREATE TABLE setting (
  id          SERIAL NOT NULL, 
  difficulty varchar(15) NOT NULL, 
  length     varchar(10) NOT NULL, 
  PRIMARY KEY (id));
COMMENT ON TABLE setting IS 'Stores all used game settings';
COMMENT ON COLUMN setting.id IS 'Unique id for each game setting';
COMMENT ON COLUMN setting.difficulty IS 'Difficulty setting of the game';
COMMENT ON COLUMN setting.length IS 'Length of the game';
CREATE TABLE map (
  id    SERIAL NOT NULL, 
  name varchar(64) NOT NULL, 
  PRIMARY KEY (id));
COMMENT ON TABLE map IS 'Stores all played levels';
COMMENT ON COLUMN map.id IS 'Id of each level';
COMMENT ON COLUMN map.name IS 'Name of the level';
CREATE TABLE match (
  id         uuid NOT NULL, 
  setting_id int2 NOT NULL, 
  map_id     int4 NOT NULL, 
  server_id  int2 NOT NULL, 
  wave       int2, 
  result     int2, 
  timestamp  timestamp, 
  duration   int4, 
  PRIMARY KEY (id));
COMMENT ON TABLE match IS 'Contains data on each match that was played';
COMMENT ON COLUMN match.id IS 'Unique id for the match';
COMMENT ON COLUMN match.setting_id IS 'Id of the match''s game settings';
COMMENT ON COLUMN match.map_id IS 'Id of the level that was played';
COMMENT ON COLUMN match.wave IS 'Wave reached';
COMMENT ON COLUMN match.result IS 'Result of the match: 1=win, -1=loss';
COMMENT ON COLUMN match.timestamp IS 'Date and time the match ended';
COMMENT ON COLUMN match.duration IS 'How long the match lasted';
CREATE TABLE player (
  id     varchar(20) NOT NULL, 
  name   varchar(64), 
  avatar varchar(255), 
  PRIMARY KEY (id));
COMMENT ON TABLE player IS 'Contains all the players who have played on the server';
COMMENT ON COLUMN player.id IS 'Player''s steamid64 value';
COMMENT ON COLUMN player.name IS 'Steam community name';
COMMENT ON COLUMN player.avatar IS 'Steam community avatar URL';
CREATE TABLE player_session (
  id               SERIAL NOT NULL, 
  player_id       varchar(20) NOT NULL, 
  match_id        uuid NOT NULL, 
  wave            int2 NOT NULL, 
  timestamp       timestamp NOT NULL, 
  duration        int4 NOT NULL, 
  disconnected    bool NOT NULL, 
  finale_played   bool NOT NULL, 
  finale_survived bool NOT NULL, 
  PRIMARY KEY (id));
COMMENT ON TABLE player_session IS 'Store data on each match a player joined';
COMMENT ON COLUMN player_session.id IS 'Unique id for each player''s session';
COMMENT ON COLUMN player_session.player_id IS 'Player''s id';
COMMENT ON COLUMN player_session.match_id IS 'Id of the match the player was a part of';
COMMENT ON COLUMN player_session.wave IS 'Wave the match was on when the player ended his session';
COMMENT ON COLUMN player_session.timestamp IS 'Date and time the player ended his session';
COMMENT ON COLUMN player_session.duration IS 'How long the player''s session lasted';
COMMENT ON COLUMN player_session.disconnected IS 'Stores whether or not the player ended the session before the match ended: 1=disconnected, 0=stayed the whole match';
COMMENT ON COLUMN player_session.finale_played IS 'Stored whether or not the final wave was played: 1=finale played, 0=finale not reached';
COMMENT ON COLUMN player_session.finale_survived IS 'Stores whether or not the player survived the finale: 1=survived, 0=died';
CREATE TABLE wave_statistic  (
  wave_summary_id int4 NOT NULL, 
  statistic_id    int2 NOT NULL, 
  perk_id         int2 NOT NULL, 
  value           int4 NOT NULL);
COMMENT ON TABLE wave_statistic  IS 'Statistics that are grouped on a wave by wave basis';
COMMENT ON COLUMN wave_statistic .statistic_id IS 'Id of the statistic';
COMMENT ON COLUMN wave_statistic .value IS 'Value of the data';
CREATE TABLE player_statistic (
  statistic_id      int2 NOT NULL, 
  player_session_id int4 NOT NULL, 
  value             int4 NOT NULL);
COMMENT ON TABLE player_statistic IS 'Stores the player statistics for each session';
COMMENT ON COLUMN player_statistic.statistic_id IS 'Id of the statistic';
COMMENT ON COLUMN player_statistic.player_session_id IS 'Player''s session id the statistic is for';
COMMENT ON COLUMN player_statistic.value IS 'Value of the statistic';
CREATE TABLE statistic (
  id           SERIAL NOT NULL, 
  category_id int2 NOT NULL, 
  name        varchar(32) NOT NULL, 
  PRIMARY KEY (id));
COMMENT ON TABLE statistic IS 'Statistics that are tracked by the database';
COMMENT ON COLUMN statistic.category_id IS 'Id of the category the statistic belongs to';
COMMENT ON COLUMN statistic.name IS 'Name of the statistic';
CREATE TABLE server (
  id       SERIAL NOT NULL, 
  address varchar(15) NOT NULL, 
  port    int2 NOT NULL, 
  PRIMARY KEY (id));
COMMENT ON TABLE server IS 'Set of servers that a match has been played on';
COMMENT ON COLUMN server.id IS 'Server id';
COMMENT ON COLUMN server.address IS 'Server address';
COMMENT ON COLUMN server.port IS 'Server port';
CREATE TABLE wave_summary (
  id         SERIAL NOT NULL, 
  match_id  uuid NOT NULL, 
  wave      int2 NOT NULL, 
  completed bool, 
  duration  int4, 
  PRIMARY KEY (id));
CREATE TABLE category (
  id    SERIAL NOT NULL, 
  name varchar(32) NOT NULL UNIQUE, 
  PRIMARY KEY (id));
COMMENT ON TABLE category IS 'Categories that each statistic may fall under';
COMMENT ON COLUMN category.id IS 'Id of the category';
COMMENT ON COLUMN category.name IS 'Name of the category';
ALTER TABLE match ADD CONSTRAINT FKmatch682879 FOREIGN KEY (setting_id) REFERENCES setting (id);
ALTER TABLE match ADD CONSTRAINT FKmatch598647 FOREIGN KEY (map_id) REFERENCES map (id);
ALTER TABLE player_session ADD CONSTRAINT FKplayer_ses609271 FOREIGN KEY (player_id) REFERENCES player (id);
ALTER TABLE wave_statistic  ADD CONSTRAINT FKwave_stati821020 FOREIGN KEY (statistic_id) REFERENCES statistic (id);
ALTER TABLE player_session ADD CONSTRAINT FKplayer_ses18539 FOREIGN KEY (match_id) REFERENCES match (id);
ALTER TABLE player_statistic ADD CONSTRAINT FKplayer_sta234666 FOREIGN KEY (player_session_id) REFERENCES player_session (id);
ALTER TABLE player_statistic ADD CONSTRAINT FKplayer_sta440784 FOREIGN KEY (statistic_id) REFERENCES statistic (id);
ALTER TABLE match ADD CONSTRAINT FKmatch593517 FOREIGN KEY (server_id) REFERENCES server (id);
ALTER TABLE wave_statistic  ADD CONSTRAINT FKwave_stati133088 FOREIGN KEY (perk_id) REFERENCES statistic (id);
ALTER TABLE wave_summary ADD CONSTRAINT FKwave_summa456733 FOREIGN KEY (match_id) REFERENCES match (id);
ALTER TABLE wave_statistic  ADD CONSTRAINT FKwave_stati573684 FOREIGN KEY (wave_summary_id) REFERENCES wave_summary (id);
ALTER TABLE statistic ADD CONSTRAINT FKstatistic966535 FOREIGN KEY (category_id) REFERENCES category (id);
CREATE UNIQUE INDEX setting_index 
  ON setting (difficulty, length);
CREATE UNIQUE INDEX map_name 
  ON map (name);
CREATE UNIQUE INDEX player_id 
  ON player (id);
CREATE UNIQUE INDEX player_session_index 
  ON player_session (player_id, match_id, timestamp);
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
CREATE FUNCTION upsert_player(new_id varchar(20), new_name varchar(64), new_avatar varchar(255)) RETURNS VOID AS $$
BEGIN
    LOOP
        UPDATE player SET name=new_name,avatar=new_avatar WHERE id = new_id;
        IF found THEN
            RETURN;
        END IF;
        BEGIN
            INSERT INTO player VALUES (new_id, new_name, new_avatar);
            RETURN;
        EXCEPTION WHEN unique_violation THEN
        END;
    END LOOP;
END;
$$
LANGUAGE plpgsql;
CREATE FUNCTION insert_statistic(new_category_name varchar(32), new_name varchar(32)) RETURNS VOID AS $$
DECLARE
new_category_id int2;
BEGIN
    INSERT INTO category(name) 
    SELECT new_name WHERE NOT EXISTS 
    (SELECT 1 FROM category c WHERE c.name=new_category_name);

    SELECT category.id into new_category_id from category where name=new_category_name;
    INSERT INTO statistic(category_id, name) 
    SELECT new_category_id,new_name WHERE NOT EXISTS 
    (SELECT 1 FROM statistic s WHERE s.category_id=new_category_id AND s.name=new_name);
END; $$
LANGUAGE plpgsql;
CREATE FUNCTION insert_match(match_uuid uuid, match_difficulty varchar(15), match_length varchar(10), 
        match_map varchar(64), match_server_address varchar(15), match_server_port int2) RETURNS VOID AS $$
BEGIN
    INSERT INTO setting(difficulty,length) 
    SELECT match_difficulty, match_length WHERE NOT EXISTS 
    (SELECT 1 FROM setting s WHERE s.difficulty=match_difficulty AND s.length=match_length);

    INSERT INTO map(name) 
    SELECT match_map
    WHERE NOT EXISTS (SELECT 1 FROM map m WHERE m.name=match_map);

    INSERT INTO server(address,port) 
    SELECT match_server_address, match_server_port WHERE NOT EXISTS 
    (SELECT 1 FROM server s WHERE s.address=match_server_address AND s.port=match_server_port);
    
    INSERT INTO match VALUES (match_uuid, (select id from setting where difficulty=match_difficulty and length=match_length)
            , (select id from map where name=match_map), 
            (select id from server where address=match_server_address AND port=match_server_port));
    
END; $$
LANGUAGE plpgsql;
CREATE FUNCTION insert_wave_summary(new_match_id uuid, new_wave int2) RETURNS VOID AS $$
BEGIN
    INSERT INTO wave_summary(match_id,wave) 
    SELECT new_match_id, new_wave WHERE NOT EXISTS 
    (SELECT 1 FROM wave_summary ws WHERE ws.match_id=new_match_id AND ws.wave=new_wave);
END;
$$
LANGUAGE plpgsql;
CREATE FUNCTION upsert_wave_summary(new_match_id uuid, new_wave int2, was_completed bool, wave_duration int4) RETURNS VOID AS $$
BEGIN
    LOOP
        UPDATE wave_summary SET completed=was_completed, duration=wave_duration WHERE match_id=new_match_id AND wave=new_wave;
        IF found THEN
            RETURN;
        END IF;
        BEGIN
            INSERT INTO wave_summary(match_id, wave) VALUES (new_match_id, new_wave);
            RETURN;
        EXCEPTION WHEN unique_violation THEN
        END;
    END LOOP;
END;
$$
LANGUAGE plpgsql;
