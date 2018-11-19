/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.te4.bookshop.utilities;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Daniel
 */
public class ConnectionFactory {

    //normally use config-file
    private static String database = "production";

    /**
     *
     * @return production or tests depending of state
     */
    public static String getDatabase() {
        return database;
    }

    /**
     * <p>Set database to production or test-state with 'production' or 'test'</p>
     * @param database
     */
    public static void setDatabase(String database) {
        ConnectionFactory.database = database;
    }

    /**
     * <h2>Make Connection</h2>
     * <p>Method that returns a Connection to specific database</p>
     * <p>Depending of the params you could access test- or productiondatabase</p>
     * @param enviroment can be production or test
     * @return com.mysql.jdbc.Connection
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection make(String enviroment) throws ClassNotFoundException, SQLException{
        //normally use config-file
        String username = "root";
        String password = "";
        String url = "jdbc:mysql://localhost/";
        if (enviroment.equals("production")) { //for normal use
            url = "jdbc:mysql://localhost/bookshop";
        } else if (enviroment.equals("test")) { //during tests
            url = "jdbc:mysql://localhost/bookshop_test";
        }
        Class.forName("com.mysql.jdbc.Driver");
        return (Connection) DriverManager.getConnection(url, username, password);
    }
}
