package backend.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

//stub iF for testing only
public interface ConnectionProvider {
        Connection getConnection() throws SQLException;
}


