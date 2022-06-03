package com.databaseHelper;



import org.apache.ibatis.jdbc.ScriptRunner;
import shared.App;
import shared.classes.PostgresConnection;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {

    public static void createSchema(App app) throws SQLException, FileNotFoundException {

              Connection connection = PostgresConnection.getDataSource().getConnection();
              //connection.

              ScriptRunner sr = new ScriptRunner(connection);
              String[] tables=new String[]{"userTable","blockedTable"};
             for(int i=0;i< tables.length;i++) {
                 InputStream inputStream = app.getClass().getClassLoader().getResourceAsStream("sql/"+tables[i]+".sql");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                
                 sr.runScript(reader);
             }
             
          }

          public static void createProcs(App app) throws IOException, SQLException {
              Connection connection = PostgresConnection.getDataSource().getConnection();
             

              ScriptRunner sr = new ScriptRunner(connection);
              String[] tables=new String[]{"blockUser","DeleteAccount","subscribeToPremium","unsubscribeToPremium","updateAccount","userRegister"};
              for(int i=0;i< tables.length;i++) {
                      String statement="";
                  InputStream inputStream = app.getClass().getClassLoader().getResourceAsStream("sql/"+tables[i]+".sql");
                  BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                      String strCurrentLine;

                      while ((strCurrentLine = reader.readLine()) != null) {
                          statement=statement+strCurrentLine+"\n";

                      }
                  Statement s=connection.createStatement();
                      s.execute(statement);

                  }

                
                  //sr.runScript(reader);
              

              

          }
    

    
}
