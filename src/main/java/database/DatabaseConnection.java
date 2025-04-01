package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// AI Generated - a few changes made by the team

public class DatabaseConnection {
        private static final String URL =
            "jdbc:postgresql://db.tlvtutujpyclacwydynx.supabase.co:5432/postgres?user=postgres&password=!!LunchTeam4";
    // ";


    //TODO check if code matches needs - is dummy code for now, so we can start working.
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            System.out.println("Verbindung zu Supabase erfolgreich!");
        } catch (SQLException e) {
            System.err.println("Fehler bei der Verbindung: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Fehlercode: " + e.getErrorCode());
            System.err.println("Stacktrace:");
            e.printStackTrace();
        }
        return conn;
    }

    public static void main(String[] args) {
        connect();
    }
}
