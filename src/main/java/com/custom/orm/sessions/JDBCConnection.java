package com.custom.orm.sessions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCConnection {

    private static Connection connection = null;

    public static Connection getConnection (String url, String login, String password) throws SQLException {
        return connection = DriverManager.getConnection(url, login, password);
    }

    public static void closeConnection() throws SQLException {
        connection.close();
    }


}
