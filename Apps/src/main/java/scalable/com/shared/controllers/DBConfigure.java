package scalable.com.shared.controllers;


public class DBConfigure {
    private int   connectionTimeOut;
    private DatabaseClient myClient;

    public DBConfigure(int connectionTimeOut)  {
        this.connectionTimeOut=connectionTimeOut;
        myClient = null;
    }

    public DatabaseClient getMyClient() {
        return myClient;
    }

    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public void setMyClient(DatabaseClient myClient) {
        this.myClient = myClient;
    }

    public String getMyClientName()  {
        if(myClient==null)
            return null;
        return  myClient.getName();
    }


}
