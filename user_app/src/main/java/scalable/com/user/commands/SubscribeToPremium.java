package scalable.com.user.commands;

import org.json.JSONObject;
import scalable.com.exceptions.ValidationException;
import scalable.com.shared.classes.JWTHandler;
import scalable.com.shared.classes.PostgresConnection;
import scalable.com.shared.classes.Responder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SubscribeToPremium extends UserCommand{
    @Override
    public String getCommandName() {
        return "SubscribeToPremium";
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

            preparedStatement = connection.prepareStatement("select id from users where id=?", ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

            preparedStatement.setInt(1, Integer.parseInt(this.tokenPayload.getString("id")));


            result = preparedStatement.executeQuery();


            result.last();

            if (result.getRow() < 1) {
                preparedStatement.close();
                return Responder.makeErrorResponse("this account was deleted", 404);
            }
            String procCall = "call subscribeToPremium(?)";

            preparedStatement = connection.prepareStatement(procCall);
            preparedStatement.setInt(1, Integer.parseInt(this.tokenPayload.getString("id")));
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e);
            return  Responder.makeErrorResponse("Something went wrong",400);
        } finally {

            PostgresConnection.disconnect(result, preparedStatement, connection);
        }
        Map<String, String> claims = new HashMap<String, String>();
        claims.put("id", this.tokenPayload.getString("id"));
        claims.put("isArtist",""+this.tokenPayload.getBoolean("isArtist"));
        claims.put("isPremium",""+true);

        String token = JWTHandler.generateToken(claims);
        JSONObject resp=new JSONObject();
        resp.put("newToken",token);
        return Responder.makeDataResponse(resp);
    }

    @Override
    public String getRestAPIMethod() {
        return "POST";
    }

    @Override
    public boolean isAuthNeeded() {
        return true;
    }

    @Override
    public void validateAttributeTypes() throws ValidationException {

    }
}
