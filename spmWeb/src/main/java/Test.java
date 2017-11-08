import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

public class Test {
  /*  static {
        java.security.Security.setProperty ("networkaddress.cache.ttl" , "0");   
    }
   */
    
    @Autowired
    public static void main(String[] args) throws SQLException, IOException, PropertyVetoException {
        
        
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            long startTime = System.currentTimeMillis();
             
            System.out.println("0====>"+(System.currentTimeMillis() ));
            connection = DataSource1.getInstance().getConnection();
            System.out.println("1====>"+(System.currentTimeMillis() - startTime));
            statement = connection.createStatement();
            
            resultSet = statement.executeQuery("select * from st_comm_cd where rownum=1");
            while (resultSet.next()) {

                String userid = resultSet.getString("comm_cd");
               
 
                System.out.println("userid : " + userid);
              
 
            }
            //System.out.println("length ==> "+resultSet.getFetchSize());
            
            System.out.println("2====>"+(System.currentTimeMillis() - startTime));
          
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) try { resultSet.close(); } catch (SQLException e) {e.printStackTrace();}
            if (statement != null) try { statement.close(); } catch (SQLException e) {e.printStackTrace();}
            if (connection != null) try { connection.close(); } catch (SQLException e) {e.printStackTrace();}
        }

    }
        
        
        
    

}
