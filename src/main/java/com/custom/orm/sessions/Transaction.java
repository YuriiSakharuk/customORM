package com.custom.orm.sessions;

import com.custom.orm.util.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;


public class Transaction {

    private final Logger log = LoggerFactory.getLogger(Transaction.class);


    private static final Properties properties;

    static {
        try {
            properties = PropertiesReader.getProperties("app.properties");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String DB_URL = properties.getProperty("db.url");
    private static final String USER = properties.getProperty("db.username");
    private static final String PASS = properties.getProperty("db.password");

    private Connection connection = null;

    // starts new connection
    public void begin() {
        log.info("Starting connection...");
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            connection.setAutoCommit(false);
            log.info("Connection was successfully started: " + connection);
        } catch (SQLException e) {
            e.printStackTrace();
            // custom exception throw
        }
    }

    // closing connection after saving applied changes
    public void commit() {
        log.info("Committing connection...");
        try {
            connection.commit();
            log.info("Connection was successfully committed: " + connection + "\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void rollback() {
        log.info("Rollbacking connection");
        try {
            connection.rollback();
            log.info("Connection was successfully rollbacked: " + connection + "\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // closing connection without saving applied changes
    public void close() {
        log.info("Closing connection");
        try {
            connection.close();
            log.info("Connection was successfully closed\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
