package org.embulk.input.teradata;

import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.embulk.spi.Exec;
import org.embulk.input.jdbc.JdbcInputConnection;

public class TeradataInputConnection
        extends JdbcInputConnection
{
    private final Logger logger = Exec.getLogger(TeradataInputConnection.class);

    public TeradataInputConnection(Connection connection)
            throws SQLException
    {
        super(connection, null);
    }
}
