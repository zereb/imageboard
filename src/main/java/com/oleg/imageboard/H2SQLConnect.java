package com.oleg.imageboard;

import org.h2.tools.RunScript;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.h2.tools.Server.createTcpServer;

public class H2SQLConnect {

    private final String DB_URL = "jdbc:h2:" + Server.DB_NAME;
    private final String JDBC_DRIVER = "org.h2.Driver";


    public H2SQLConnect(){
        try {
            org.h2.tools.Server server = createTcpServer("-tcpAllowOthers").start();

            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        try (Connection connection = getConnection();
            InputStream is = getClass().getResourceAsStream("/init.sql")
        ){
            RunScript.execute(connection, new InputStreamReader(is));
            System.out.println("Connected to db");
        }catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, Server.LOGIN, Server.PASSWORD);
    }
}
