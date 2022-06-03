package scalable.com.user.commands;

import scalable.com.exceptions.ValidationException;
import scalable.com.shared.classes.ByCryptHelper;
import scalable.com.shared.classes.PostgresConnection;
import scalable.com.shared.classes.Responder;

import javax.validation.constraints.NotBlank;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChangePassword extends UserCommand {
    @NotBlank(message = "old password cannot be empty")
    private String oldPassword;
    @NotBlank(message = "new password cannot be empty")
    private String newPassword;


    @Override
    public String getCommandName() {
        return "ChangePassword";
    }

    @Override
    public String execute() {
        Connection connection = null;
        ResultSet result=null;
        PreparedStatement preparedStatement=null;
        try {
            connection = PostgresConnection.getDataSource().getConnection();
            connection.setAutoCommit(true);

            preparedStatement = connection.prepareStatement("select password from users where id=?", ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

            preparedStatement.setInt(1, Integer.parseInt(this.tokenPayload.getString("id")));

            
            result = preparedStatement.executeQuery();
           
           
            result.last();
            System.out.println("abl el if");
            if (result.getRow() >= 1) {
                String passwordHash=result.getString("password");
                System.out.println(passwordHash);
                preparedStatement.close();
                if(!ByCryptHelper.verifyHash(this.oldPassword,passwordHash)) {

                    return Responder.makeErrorResponse("old password is wrong",400);
                }
                else{
                    String procCall = "call edit_account(?,?,?)";

                    preparedStatement = connection.prepareStatement(procCall);
                    preparedStatement.setInt(1, Integer.parseInt(this.tokenPayload.getString("id")));
                    preparedStatement.setString(2,ByCryptHelper.hash(this.newPassword));
                    preparedStatement.setString(3,null);
                    preparedStatement.executeUpdate();
                }
                
            }  else{
                return Responder.makeErrorResponse("account not found",404);
            }

        }  catch (SQLException e) {
            System.out.println(e);
            return  Responder.makeErrorResponse("Something went wrong",400);
        } finally {

            PostgresConnection.disconnect(result, preparedStatement, connection);
        }

        return Responder.makeMsgResponse("password changed successfully");
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

        try {
            this.oldPassword = body.getString("oldPassword");
            this.newPassword = body.getString("newPassword");
        }
    
        catch (Exception e){
        throw new ValidationException("attributes data types are wrong: "+e.getMessage());
    }
    }
}
