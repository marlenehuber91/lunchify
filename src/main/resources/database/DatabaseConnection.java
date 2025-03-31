package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// AI Generated - a few changes made by the team

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://db.tlvtutujpyclacwydynx.supabase.co:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "!!LunchTeam4";


    //TODO check if code matches needs - is dummy code for now, so we can start working.
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Verbindung zu Supabase erfolgreich!");
        } catch (SQLException e) {
            System.out.println("Fehler bei der Verbindung: " + e.getMessage());
        }
        return conn;
    }

    public static void main(String[] args) {
        connect();
    }
}
