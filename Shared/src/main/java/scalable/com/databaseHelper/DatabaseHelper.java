package scalable.com.databaseHelper;

import org.apache.ibatis.jdbc.ScriptRunner;
import scalable.com.shared.classes.PostgresConnection;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {

    public static void createSchema() throws SQLException, FileNotFoundException {

              Connection connection = PostgresConnection.getDataSource().getConnection();
              //connection.

              ScriptRunner sr = new ScriptRunner(connection);
              String[] tables=new String[]{"userTable","blockedTable"};
             for(int i=0;i< tables.length;i++) {
                 Reader reader = new BufferedReader(new FileReader(getAbsolutePath(tables[i])));
                 sr.runScript(reader);
             }
             
          }

          public static void createProcs() throws IOException, SQLException {
              Connection connection = PostgresConnection.getDataSource().getConnection();
             

              ScriptRunner sr = new ScriptRunner(connection);
              String[] tables=new String[]{"blockUser","DeleteAccount","subscribeToPremium","unsubscribeToPremium","updateAccount","userRegister"};
              for(int i=0;i< tables.length;i++) {
                      String statement="";
                  BufferedReader reader = new BufferedReader(new FileReader(getAbsolutePath(tables[i])));
                      String strCurrentLine;

                      while ((strCurrentLine = reader.readLine()) != null) {
                          statement=statement+strCurrentLine+"\n";

                      }
                  Statement s=connection.createStatement();
                      s.execute(statement);

                  }

                
                  //sr.runScript(reader);
              

              

          }
          public static String getAbsolutePath(String fileName){
              ClassLoader classLoader = DatabaseHelper.class.getClassLoader();

              File file = new File(classLoader.getResource("sql/" + fileName + ".sql").getFile());
              //System.out.println(file.getAbsolutePath());
              return file.getAbsolutePath();
          }

    
}
