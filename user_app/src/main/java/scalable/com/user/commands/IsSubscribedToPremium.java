package scalable.com.user.commands;

import org.json.JSONObject;
import scalable.com.exceptions.ValidationException;
import scalable.com.shared.classes.ByCryptHelper;
import scalable.com.shared.classes.JWTHandler;
import scalable.com.shared.classes.PostgresConnection;
import scalable.com.shared.classes.Responder;

import javax.validation.constraints.NotBlank;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class IsSubscribedToPremium extends UserCommand {

   
    @Override
    public String getCommandName() {
        return "IsSubscribedToPremium";
    }

    @Override
    public String execute() {

        Connection connection = null;
        ResultSet result=null;
        PreparedStatement preparedStatement=null;
        
        JSONObject response=new JSONObject();
        
        try {

            
            connection = PostgresConnection.getDataSource().getConnection();
            connection.setAutoCommit(true);

            preparedStatement=connection.prepareStatement("select ispremium from users where id=?", ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

            preparedStatement.setInt(1,Integer.parseInt(this.tokenPayload.getString("id")));

            
            result=preparedStatement.executeQuery();
            
            result.last();
            if(result.getRow()>=1) {
                boolean isSub=result.getBoolean("ispremium");
                response.put("isSubscribed",isSub);

            }
            else{
                    return Responder.makeErrorResponse("account not found",406);
                }
            

        } catch (SQLException e) {
            System.out.println(e.toString());
            return  Responder.makeErrorResponse("Something went wrong",400);
        } finally {

            PostgresConnection.disconnect(result, preparedStatement, connection);
        }
        return Responder.makeDataResponse(response);
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
