package scalable.com.user.commands;

import org.json.JSONObject;
import scalable.com.exceptions.ValidationException;
import scalable.com.shared.classes.ByCryptHelper;
import scalable.com.shared.classes.PostgresConnection;
import scalable.com.shared.classes.Responder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewMyProfile extends UserCommand {
    @Override
    public String getCommandName() {
        return "ViewMyProfile";
    }

    @Override
    public String execute() {
            JSONObject returnObject=null;
        Connection connection = null;
        ResultSet result=null;
        PreparedStatement preparedStatement=null;
        try {
            connection = PostgresConnection.getDataSource().getConnection();
            connection.setAutoCommit(true);

            preparedStatement = connection.prepareStatement("select first_name,last_name,profile_photo,username,email,isartist,ispremium from users where id=?", ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

            preparedStatement.setInt(1, Integer.parseInt(this.tokenPayload.getString("id")));


            result = preparedStatement.executeQuery();


            result.last();
           
            if (result.getRow() >= 1) {
               returnObject=new JSONObject();
               returnObject.put("username",result.getString("username")) ;
               returnObject.put("first_name",result.getString("first_name"));
               returnObject.put("last_name",result.getString("last_name"));
               returnObject.put("email",result.getString("email"));
                returnObject.put("profile_photo",result.getString("profile_photo"));
               

            }  else{
                return Responder.makeErrorResponse("account not found",404);
            }

        }  catch (SQLException e) {
            System.out.println(e);
            return  Responder.makeErrorResponse("Something went wrong",400);
        } finally {

            PostgresConnection.disconnect(result, preparedStatement, connection);
        }



        return Responder.makeDataResponse(returnObject);
    }

    @Override
    public String getRestAPIMethod() {
        return "GET";
    }

    @Override
    public boolean isAuthNeeded() {
        return true;
    }

    @Override
    public void validateAttributeTypes() throws ValidationException {

    }
}
