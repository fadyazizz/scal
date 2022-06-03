package scalable.com.user.commands;

import scalable.com.exceptions.ValidationException;
import scalable.com.shared.classes.MinIo;
import scalable.com.shared.classes.PostgresConnection;
import scalable.com.shared.classes.Responder;
import scalable.com.user.UserApp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditProfilePicture extends UserCommand{
    @Override
    public String getCommandName() {
        return "EditProfilePicture";
    }

    @Override
    public String execute() {
        if (files.length() != 1)
            return Responder.makeErrorResponse("Only one profile image allowed per upload, Check Form-Data Files!", 400);
     



    
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        String photoUrl;
        try {

        String procCall = "select id from users where id=?";
        connection = PostgresConnection.getDataSource().getConnection();
        connection.setAutoCommit(true);
        preparedStatement = connection.prepareStatement(procCall, ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        preparedStatement.setInt(1, Integer.parseInt(this.tokenPayload.getString("id")));
        ResultSet resultSet=preparedStatement.executeQuery();

        if(!resultSet.next()) {
            return  Responder.makeErrorResponse("account not found !",404);
        }
        String id=resultSet.getString("id");
        preparedStatement.close();
        
        try {
            photoUrl = MinIo.uploadObject(UserApp.MinioBucketName, id, files.getJSONObject("image"));
            if (photoUrl.isEmpty())
                return Responder.makeErrorResponse("Error Occurred While Uploading Your Image!", 404);
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 400);
        }
        procCall = "call edit_account(?,?,?)";

        preparedStatement = connection.prepareStatement(procCall);
        preparedStatement.setInt(1, Integer.parseInt(this.tokenPayload.getString("id")));
        preparedStatement.setString(2,null);
        preparedStatement.setString(3,photoUrl);
        preparedStatement.executeUpdate();
        
    } catch (SQLException e) {
          
        return  Responder.makeErrorResponse(e.getMessage(),400);
    } finally {

        PostgresConnection.disconnect(null, preparedStatement, connection);
    }

        return Responder.makeMsgResponse(String.format("Profile Picture uploaded successfully. You can find at %s", photoUrl));
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
