package scalable.com.tests;

import org.json.JSONObject;
import scalable.com.shared.testsHelper.TestHelper;
import scalable.com.user.UserApp;
import scalable.com.user.commands.*;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Requester {
    Requester() {
    
    }

    //static JSONObject authenticationParams = AuthParamsHandler.getUnauthorizedAuthParams();

    private static JSONObject makeRequest(JSONObject body, String methodType, JSONObject uriParams,boolean needsTokenPayload) {
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", methodType);
        request.put("uriParams", uriParams);
        
        if(needsTokenPayload) {
            JSONObject tokenPayload=new JSONObject();
            tokenPayload.put("id",UserAppTest.id);
            tokenPayload.put("isArtist",UserAppTest.isArtist);
           
            request.put("tokenPayload",tokenPayload );
            request.put("isAuthenticated",true);
        }
        System.out.println("this is the request: "+request);
        return request;
    }

    public static JSONObject callSignUpCommand(String username, String email, String password,String firstName,String lastName,boolean isArtist) {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("email", email);
        body.put("password", password);
        body.put("firstname",firstName);
        body.put("lastname",lastName);
        body.put("isArtist",isArtist);
        JSONObject request = makeRequest(body, "POST", new JSONObject(),false);
        SignUp signUp=new SignUp();
        return new JSONObject(TestHelper.execute(signUp,request));
    }

    public static JSONObject login(String email, String password) {
        JSONObject body = new JSONObject();
        body.put("email", email);
        body.put("password", password);
        JSONObject request = makeRequest(body, "POST", new JSONObject(),false);
        Login login=new Login();
        return new JSONObject(TestHelper.execute(login,request));
    }

    public static JSONObject callViewMyProfileCommand() {
        JSONObject request = makeRequest(new JSONObject(), "GET", new JSONObject(),true);
        TestHelper.attachTokenPayLoad(UserAppTest.token,request);
        ViewMyProfile viewMyProfile=new ViewMyProfile();
        return new JSONObject(TestHelper.execute(viewMyProfile,request));
    }

    public static JSONObject callViewAnotherProfile(String username) {
        JSONObject uriParams = new JSONObject().put("username", username);
        JSONObject request = makeRequest(null, "GET", uriParams,true);
        ViewAnotherUserProfile viewAnotherUserProfile= new ViewAnotherUserProfile();
        return new JSONObject(TestHelper.execute(viewAnotherUserProfile,request));
    }

    public static JSONObject updatePassword(String oldPassword, String newPassword) {
        JSONObject body = new JSONObject();
        body.put("oldPassword", oldPassword);
        body.put("newPassword", newPassword);
        JSONObject request = makeRequest(body, "POST", new JSONObject(),true);
       ChangePassword changePassword= new ChangePassword();
        return new JSONObject(TestHelper.execute(changePassword,request));
    }

    static public JSONObject updateProfilePicture() throws IOException {
        JSONObject request = makeRequest(new JSONObject(), "POST", new JSONObject(),true);

        JSONObject files = new JSONObject().put("image", scalable.com.shared.classes.FileCreator.generateImageJson());
        request.put("files", files);
        EditProfilePicture editProfilePicture=new EditProfilePicture();
        return new JSONObject(TestHelper.execute(editProfilePicture,request));
    }
    public static JSONObject subscribeToPremium(){
        JSONObject request = makeRequest(new JSONObject(), "POST", new JSONObject(),true);
        SubscribeToPremium subscribeToPremium=new SubscribeToPremium();
        return new JSONObject(TestHelper.execute(subscribeToPremium,request));

    }
  

    public static JSONObject deleteAccount(String password) {
        JSONObject body = new JSONObject();
        body.put("password", password);

        JSONObject request = makeRequest(body, "POST", new JSONObject(),true);
        DeleteAccount deleteAccount=new DeleteAccount();
        return new JSONObject(TestHelper.execute(deleteAccount,request));
    }


}
