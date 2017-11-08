import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Test1 {

    static {
        java.security.Security.setProperty ("networkaddress.cache.ttl" , "60");   
    }
   
        public static void main(String[] argv) {

            System.out.println("-------- Oracle JDBC Connection Testing ------");
    
            try {

                Class.forName("oracle.jdbc.driver.OracleDriver");

            } catch (ClassNotFoundException e) {

                System.out.println("Where is your Oracle JDBC Driver?");
                e.printStackTrace();
                return;

            }

            System.out.println("Oracle JDBC Driver Registered!");

            Connection connection = null;  

            try {
                long startTime = System.currentTimeMillis();
                //connection = DriverManager.getConnection("jdbc:oracle:thin:@172.24.180.22:1521:GDFMS", "gdfs", "fsdg%1228");
                connection = DriverManager.getConnection("jdbc:oracle:thin:@(description=(address=(host=172.24.180.22)(protocol=tcp)(port=1521))(connect_data=(service_name=GDFMS)(server=DEDICATED)))", "gdfs", "fsdg%1228");
                
                
                System.out.println("1====>"+(System.currentTimeMillis() - startTime));

            } catch (SQLException e) {

                System.out.println("Connection Failed! Check output console");
                e.printStackTrace();
                return;

            }

            if (connection != null) {
                System.out.println("You made it, take control your database now!");
            } else {
                System.out.println("Failed to make connection!");
            }
        }

        
    }

