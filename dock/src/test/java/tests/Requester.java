package tests;

import org.json.JSONObject;
import shared.testsHelper.TestHelper;
import user.commands.*;


import java.io.IOException;

public class Requester {
    Requester() {
    
    }

    //static JSONObject authenticationParams = AuthParamsHandler.getUnauthorizedAuthParams();

    private static JSONObject makeRequest(JSONObject body, String methodType, JSONObject uriParams) {
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", methodType);
        request.put("uriParams", uriParams);
       
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
        JSONObject request = makeRequest(body, "POST", new JSONObject());
        SignUp signUp=new SignUp();
        return new JSONObject(TestHelper.execute(signUp,request));
    }

    public static JSONObject login(String username, String password) {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);
        JSONObject request = makeRequest(body, "POST", new JSONObject());
        return new JSONObject(new Login().execute(request));
    }

    public static JSONObject callViewMyProfileCommand() {
        JSONObject request = makeRequest(new JSONObject(), "GET", new JSONObject());
        TestHelper.attachTokenPayLoad(UserAppTest.token,request);
        ViewMyProfile viewMyProfile=new ViewMyProfile();
        return new JSONObject(TestHelper.execute(viewMyProfile,request));
    }

    public static JSONObject viewAnotherProfile(String username) {
        JSONObject uriParams = new JSONObject().put("username", username);
        JSONObject request = makeRequest(null, "GET", uriParams);
        return new JSONObject(new ViewAnotherUserProfile().execute(request));
    }

    public static JSONObject updatePassword(String oldPassword, String newPassword) {
        JSONObject body = new JSONObject();
        body.put("oldPassword", oldPassword);
        body.put("newPassword", newPassword);
        JSONObject request = makeRequest(body, "PUT", new JSONObject());
        return new JSONObject(new ChangePassword().execute(request));
    }

    static public JSONObject updateProfilePicture() throws IOException {
        JSONObject request = makeRequest(new JSONObject(), "PUT", new JSONObject());
        //JSONObject files = new JSONObject().put("image", org.sab.minio.FileSimulation.generateImageJson());
        //request.put("files", files);
        return new JSONObject(new EditProfilePicture().execute(request));
    }

  

    public static JSONObject deleteAccount(String password) {
        JSONObject body = new JSONObject();
        body.put("password", password);

        JSONObject request = makeRequest(body, "DELETE", new JSONObject());
        return new JSONObject(new DeleteAccount().execute(request));
    }


}
