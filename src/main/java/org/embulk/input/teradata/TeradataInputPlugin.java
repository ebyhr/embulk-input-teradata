package org.embulk.input.teradata;

import com.google.common.base.Optional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.input.jdbc.AbstractJdbcInputPlugin;
import org.embulk.input.teradata.TeradataInputConnection;

import org.embulk.spi.Exec;
import org.slf4j.Logger;

public class TeradataInputPlugin
        extends AbstractJdbcInputPlugin
{
    private final Logger logger = Exec.getLogger(TeradataInputPlugin.class);
    private static final Driver driver = new com.teradata.jdbc.TeraDriver();

    public interface TeradataPluginTask
            extends PluginTask
    {
        @Config("host")
        public String getHost();

        @Config("port")
        @ConfigDefault("1025")
        public int getPort();

        @Config("user")
        public String getUser();

        @Config("password")
        @ConfigDefault("\"\"")
        public String getPassword();

        @Config("database")
        public String getDatabase();

    }

    @Override
    protected Class<? extends PluginTask> getTaskClass()
    {
        return TeradataPluginTask.class;
    }

    @Override
    protected TeradataInputConnection newConnection(PluginTask task) throws SQLException
    {
        TeradataPluginTask t = (TeradataPluginTask) task;

        String url = String.format("jdbc:teradata://%s",t.getHost());

        Properties props = new Properties();
	props.setProperty("database", t.getDatabase());
        props.setProperty("user", t.getUser());
        props.setProperty("password", t.getPassword());

        props.putAll(t.getOptions());

        Connection con = driver.connect(url, props);

        try {
            Statement stmt = con.createStatement();
            String sql = String.format("select * from dbc.tables;");
            logger.info("SQL: " + sql);
            stmt.execute(sql);

            TeradataInputConnection c = new TeradataInputConnection(con);
            con = null;
            return c;
        }
        finally {
            if (con != null) {
                con.close();
            }
        }
    }
}
