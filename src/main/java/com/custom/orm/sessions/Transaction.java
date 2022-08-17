package com.custom.orm.sessions;

import com.custom.orm.util.PropertiesReader;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;


public class Transaction {

    //  Database credentials
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

    private static Connection connection;

    // starts new connection
    public void begin() {
        System.out.println("Starting connection...");
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            connection.setAutoCommit(false);
            System.out.println("Connection was successfully started: " + connection);
        } catch (SQLException e) {
            e.printStackTrace();
            connection = null;
        }
    }

    // closing connection after saving applied changes
    public void commit() {
        System.out.println("Committing connection...");
        try {
            connection.commit();
            System.out.println("Connection was successfully committed: " + connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void rollback() {
        System.out.println("Rollbacking connection");
        try {
            connection.rollback();
            System.out.println("Connection was successfully rollbacked: " + connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // closing connection without saving applied changes
    public void close() {
        System.out.println("Closing connection");
        try {
            connection.close();
            System.out.println("Connection was successfully closed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
