package backend.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

//stub iF for testing only
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ConnectionProvider {
        Connection getConnection() throws SQLException;
}


