package scalable.com.user;

import org.apache.ibatis.jdbc.ScriptRunner;
import scalable.com.databaseHelper.DatabaseHelper;
import scalable.com.shared.App;
import scalable.com.shared.classes.Arango;
import scalable.com.shared.classes.ClassManager;
import scalable.com.shared.classes.PostgresConnection;

import javax.swing.plaf.synth.SynthEditorPaneUI;
import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class UserApp extends App {
    
    public static String MinioBucketName="user-app-photosbucket";
        public static void main(String[] args) throws TimeoutException, IOException, ClassNotFoundException, SQLException{


            UserApp app=new UserApp();
            app.start();
            
           //TODO comment this line after development
            createSchema(app);
            createProcs(app);


        }
      
     
    @Override
    protected String getAppName() {
        return "User";
    }

    public static void createSchema(App app) throws SQLException, FileNotFoundException {

        Connection connection = PostgresConnection.getDataSource().getConnection();
        //connection.

        ScriptRunner sr = new ScriptRunner(connection);
        String[] tables=new String[]{"userTable","blockedTable"};
        for(int i=0;i< tables.length;i++) {
            InputStream inputStream = UserApp.class.getClassLoader().getResourceAsStream("sql/"+tables[i]+".sql");
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
            InputStream inputStream = UserApp.class.getClassLoader().getResourceAsStream("sql/"+tables[i]+".sql");
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
