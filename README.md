KFStatsXTS Advanced
===================
The advanced tracking server is a custom data manager for the KFSXTrackingServer application.  It is designed specifically to log V3 of the KFStatsX UDP packets, fully capturing all the data available.  In addition to the extra storage, support for MySQL and PostgreSQL is provided along with SQLite3.

## Database Setup
To configure the KFSXTrackingServer to use the TS Advanced files, you will need to edit the "db" properties in the server.properties file, at the minimum: db.url and db.writer.script.  If you are use MySQL or PostgreSQL, you will need to modify the db.user, db.password, and db.driver properties,

### SQLite3
SQLite3 requires the least amount of changes to the server.properties file, only requiring 3 changes.  As with the main application, a pre-assembled sqlite3 file has been provided, though the schema of the database is provided if you wish to create it yourself.  Once the database is ready, modify the db* properties as follows:

    db.url=jdbc:sqlite:<path to KFStatsXTS_Advanced>/sql/kfsxts_advanced_db.sqlite3
    db.writer.script=<path to KFStatsXTS_Advanced>/data/SQLiteWriter.groovy
    db.reader.script=<path to KFStatsXTS_Advanced>/data/StandardReader.groovy
    
### PostgreSQL
Using PostgreSQL (and MySQL) will require setting up a database.  The schema of the Postgres database is located in the sql folder, named `kfsxts_advanced_db_pgsql.sql`, and can be use setup the database.  Once you have the database setup, you will need to set the db* properties as follows:

    db.url=jdbc:postgresql://<postgres server ip>:<postgres server port>/<kfsx database name>
    db.driver=org.postgresql.Driver
    db.reader.script=<path to KFStatsXTS_Advanced>/data/StandardReader.groovy
    db.writer.script=<path to KFStatsXTS_Advanced>/data/StoredProcedureWriter.groovy
    db.user=<kfsx db username>
    db.password=<kfsx db password>

### MySQL
As with the PostgreSQL database, you will need to configure your MySQL server to have a KFStatsX database with the schema defined in the sql folder named `kfsxts_advanced_db_mysql.sql`.  When that is completed, set the db* properties as follows:

    db.url=jdbc:mysql://<mysql server ip>:<mysql server port>/<kfsx database name>
    db.driver=com.mysql.jdbc.Driver
    db.reader.script=<path to KFStatsXTS_Advanced>/data/StandardReader.groovy
    db.writer.script=<path to KFStatsXTS_Advanced>/data/MySQLWriter.groovy
    db.user=<kfsx db username>
    db.password=<kfsx db password>

## Running KFSXTrackingServer
After configuring the appropriate properties and setting up the database, you can now run the application.  If you are using SQLite as the db, the java command will not need any changes.  People using PostgreSQL or MySQL will need to retrive the approriate jdbc driver and modify the java classpath to include said driver.  You can get the jdbc drivers from respective pages:

    http://jdbc.postgresql.org/download.html
    http://www.mysql.com/products/connector/

When you have the approriate JBDC driver, modify the `java` command to look as follows:

    java -classpath KFSXTrackingServer.jar;lib;<path to jdbc jar> \
    com.github.etsai.kfsxtrackingserver.Main -propertyfile share\etc\server_advanced.properties

## Web Server
The webpages for the advanced database are still a work in progress.  As such, the above examples use StandardReader.groovy, which builds the necessary data for the default KFSXTrackingServer pages.  If you wish to see what they look like in their current state, you can update the `db.reader.script` and `http.root.dir` properties with the following values:

    db.reader.script=<path to KFStatsXTS_Advanced>/data/AdvancedReader.groovy
    http.root.dir=<path to KFStatsXTS_Advanced>/http
