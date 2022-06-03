package scalable.com.music;

import scalable.com.databaseHelper.DatabaseHelper;
import scalable.com.shared.App;
import scalable.com.shared.classes.Arango;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

public class MusicApp extends App {
    public Arango arangoPool;

    public static void main(String[] args) throws TimeoutException, IOException, ClassNotFoundException, SQLException {


        MusicApp app=new MusicApp();
        //app.dbInit();
        app.start();
        //comment this line after development
        //DatabaseHelper.createSchema();
        //DatabaseHelper.createProcs();


    }


    @Override
    protected String getAppName() {
        return "Music";
    }



}
