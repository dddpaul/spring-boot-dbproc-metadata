package com.github.dddpaul.dbproc_metadata;

import com.github.dddpaul.IpTables;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Requires running Oracle server
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class ApplicationTest extends Assert {

    @Autowired
    private DataSource ds;

    @Autowired
    private DaoImpl dao;

    @Before
    public void setUp() throws Exception {
        try (Connection con = ds.getConnection();
             PreparedStatement ps = con.prepareStatement("CREATE OR REPLACE FUNCTION get_number_99 RETURN NUMBER AS BEGIN RETURN 99; END;")) {
            ps.executeUpdate();
        }
    }

    @Test
    public void testGetNumber99WhenDatabaseIsUnavailable() throws IOException, InterruptedException, SQLException {
        // Validate connection explicitly
        try (Connection con = ds.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM DUAL")) {
            ps.executeQuery();
        }

        // Connection is validated already, so function must be executed straight ahead.
        // But is's the first time function is called, so function metadata must be retrieved.
        boolean socketReadTimeoutReached = false;
        IpTables.drop(IpTables.Chain.OUTPUT, 1521);
        try {
            dao.getNumber99();
        } catch (Exception e) {
            socketReadTimeoutReached = true;
        } finally {
            IpTables.allow(IpTables.Chain.OUTPUT, 1521);
        }
        assertTrue(socketReadTimeoutReached);

        // Function metadata retrieve was failed. And all of the later function executions will throw
        // an exception with nested java.sql.SQLException: ORA-17041: Missing IN or OUT parameter at index:: 1.
        dao.getNumber99();
    }
}
