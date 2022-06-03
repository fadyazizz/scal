package scalable.com.user.commands;

import scalable.com.exceptions.ValidationException;
import scalable.com.shared.classes.ByCryptHelper;
import scalable.com.shared.classes.MinIo;
import scalable.com.shared.classes.PostgresConnection;
import scalable.com.shared.classes.Responder;
import scalable.com.user.UserApp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteAccount extends UserCommand {
    @Override
    public String getCommandName() {
        return "DeleteAccount";
    }

    @Override
    public String execute() {

        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {

            String procCall = "select id,profile_photo from users where id=?";
            connection = PostgresConnection.getDataSource().getConnection();
            connection.setAutoCommit(true);
            preparedStatement = connection.prepareStatement(procCall,ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            preparedStatement.setInt(1, Integer.parseInt(this.tokenPayload.getString("id")));
            ResultSet resultSet=preparedStatement.executeQuery();

            if(!resultSet.next()) {
                return  Responder.makeErrorResponse("account already deleted",404);
            }
            String id=resultSet.getString("id");
            String photoUrl=resultSet.getString("profile_photo");
            preparedStatement.close();
            procCall = "call deleteAccount(?)";

            preparedStatement = connection.prepareStatement(procCall);
            preparedStatement.setInt(1, Integer.parseInt(this.tokenPayload.getString("id")));
            preparedStatement.executeUpdate();
            
            if (photoUrl!=null) {
                try {
                    String publicId = id;
                    if (!MinIo.deleteObject(UserApp.MinioBucketName, publicId))
                        return Responder.makeErrorResponse("Error Occurred While Deleting Your Image!", 404);
                } catch (Exception e) {
                    return Responder.makeErrorResponse(e.getMessage(), 400);
                }
            }

        } catch (SQLException e) {
            
            return  Responder.makeErrorResponse(e.getMessage(),400);
        } finally {

            PostgresConnection.disconnect(null, preparedStatement, connection);
        }


        return Responder.makeMsgResponse("Account deleted successfully");

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
