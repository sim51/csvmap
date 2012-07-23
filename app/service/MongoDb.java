package service;

import play.Logger;
import play.Play;
import play.exceptions.DatabaseException;
import play.exceptions.PlayException;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class MongoDb {

    private static volatile Mongo mongo;

    public static void initialize() throws PlayException {
        if (mongo != null) {
            throw new DatabaseException("MongoDb database is already initialyze");
        }
        String ip = Play.configuration.getProperty("mongodb.ip", "127.0.0.1");
        Integer port = Integer.valueOf(Play.configuration.getProperty("mongodb.port", "27017"));
        try {
            mongo = new Mongo(ip, port.intValue());
        } catch (Exception e) {
            throw new DatabaseException("error on mongo init", e);
        }
    }

    /**
     * Method to retrieve the database into the ThreadLocal.
     * 
     * @return
     */
    public static DB db() {
        if (mongo == null) {
            Logger.debug("Init mongo database");
            initialize();
        }
        Logger.debug("Current thread is " + Thread.currentThread().getName() + " " + Thread.currentThread().getId());
        String databasename = Play.configuration.getProperty("mongodb.db", "play");
        DB db = mongo.getDB(databasename);
        return db;
    }

    /**
     * Methode to destroy all reference.
     */
    public static void destroy() {
        mongo.close();
    }

}
