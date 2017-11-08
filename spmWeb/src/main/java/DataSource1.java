import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

public class DataSource1 {

    private static DataSource1     datasource;
    private BasicDataSource ds;

    DataSource1() {
        
        try {
        ds = new BasicDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setUsername("gdfs");
        ds.setPassword("fsdg%1228");
        ds.setUrl("jdbc:oracle:thin:@172.24.180.22:1521:GDFMS");
       //ds.setUrl("jdbc:oracle:oci:@GDFMS_IDC");
       
        
     // the settings below are optional -- dbcp can work with defaults
        ds.setMinIdle(1);
        ds.setMaxIdle(1);
     
         
        

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static DataSource1 getInstance() throws IOException, SQLException, PropertyVetoException {
        if (datasource == null) {
            datasource = new DataSource1();
            return datasource;
        } else {
            return datasource;
        }
    }

    public Connection getConnection() throws SQLException {
        return this.ds.getConnection();
    }
}
