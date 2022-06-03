package user;



import com.databaseHelper.DatabaseHelper;
import shared.App;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

public class UserApp extends App {
    
    public static String MinioBucketName="user-app-photosbucket";
        public static void main(String[] args) throws TimeoutException, IOException, ClassNotFoundException, SQLException{


            UserApp app=new UserApp();
            app.start();
            
           //TODO comment this line after development
            DatabaseHelper.createSchema();
            DatabaseHelper.createProcs();


        }
      
     
    @Override
    protected String getAppName() {
        return "User";
    }

 

}
