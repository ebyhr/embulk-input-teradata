package org.embulk.input;

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
        @ConfigDefault("5433")
        public int getPort();

        @Config("user")
        public String getUser();

        @Config("password")
        @ConfigDefault("\"\"")
        public String getPassword();

        @Config("database")
        public String getDatabase();

        @Config("schema")
        @ConfigDefault("\"public\"")
        public String getSchema();

        @Config("resource_pool")
        @ConfigDefault("null")
        public Optional<String> getResourcePool();
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

        String url = String.format("jdbc:Teradata://%s:%d/%s",
                t.getHost(), t.getPort(), t.getDatabase());

        Properties props = new Properties();
        props.setProperty("user", t.getUser());
        props.setProperty("password", t.getPassword());
        props.setProperty("loginTimeout", String.valueOf(t.getConnectTimeout())); // seconds
        props.setProperty("socketTimeout", String.valueOf(t.getSocketTimeout())); // seconds

        // Enable keepalive based on tcp_keepalive_time, tcp_keepalive_intvl and tcp_keepalive_probes kernel parameters.
        // Socket options TCP_KEEPCNT, TCP_KEEPIDLE, and TCP_KEEPINTVL are not configurable.
        props.setProperty("tcpKeepAlive", "true");

        // if (t.getSsl()) {
        //     // TODO add ssl_verify (boolean) option to allow users to verify certification.
        //     //      see embulk-input-ftp for SSL implementation.
        //     props.setProperty("ssl", "true");
        //     props.setProperty("sslfactory", "org.Teradata.ssl.NonValidatingFactory");  // disable server-side validation
        // }
        // // setting ssl=false enables SSL. See org.Teradata.core.v3.openConnectionImpl.

        props.putAll(t.getOptions());

        Connection con = driver.connect(url, props);

        try {
            if (t.getResourcePool().isPresent()) {
                Statement stmt = con.createStatement();
                String escapedResourcePool = t.getResourcePool().get().replaceAll("'", "''");
                String sql = String.format("SET SESSION RESOURCE_POOL = '%s'", escapedResourcePool);
                logger.info("SQL: " + sql);
                stmt.execute(sql);
            }

            TeradataInputConnection c = new TeradataInputConnection(con, t.getSchema());
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
