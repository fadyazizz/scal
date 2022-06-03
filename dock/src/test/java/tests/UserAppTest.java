package tests;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import shared.testsHelper.TestHelper;
import user.UserApp;


import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserAppTest {
    
    static String username = "fady"+ new Date().getTime();
    static String password = "12345678";
    static String token="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6IjE1IiwiaXNBcnRpc3QiOiJ0cnVlIn0.7Ku_-aMNMxhD9T_3uiEEOeBGnkqFFyZrsNM2VU6Ng_s";
    static String firstname="7amo";
    static String lastname="bikaaaa";
    static boolean isArtist=false;
    static String email=username+"@gmail.com";
   
    @BeforeClass
    public static void createSetup() throws IOException, ClassNotFoundException, TimeoutException {
        System.out.println("in create setup");
        //app that is being tested

        UserApp app=new UserApp();
        //starting the app
        app.start();
        TestHelper.appBeingTested=app;
      

    }


    @Test
    public void T01_SignUpCreatesAnEntryInDB() {


      
        


        JSONObject response = Requester.callSignUpCommand(username, email, password,firstname,lastname,false);
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));
    }

//    @Test
//    public void T02_SignUpSameUser() {
//
//
//        String email = username + "@gmail.com";
//        String birthdate = "1997-12-14";
//
//
//        JSONObject response = Requester.signUp(username, email, password, birthdate);
//        int statusCode = response.getInt("statusCode");
//        assertTrue(statusCode >= 500);
//
//        String msg = response.getString("msg");
//        assertTrue(msg.contains("duplicate key"));
//
//    }

//    @Test
//    public void T03_SignUpMissingParam() {
//
//
//        String email = username + "@gmail.com";
//        String birthdate = null;
//
//
//        JSONObject response = Requester.signUp(username, email, password, birthdate);
//        int statusCode = response.getInt("statusCode");
//        assertTrue(statusCode >= 400 && statusCode <= 500);
//
//        String msg = response.getString("msg");
//        assertTrue(msg.contains("missing") && msg.contains("birthdate"));
//
//    }

//    @Test
//    public void T04_SignUpIncorrectlyFormattedDate() {
//
//
//        String email = username + "@gmail.com";
//        String birthdate = "14-12-1997";
//
//
//        JSONObject response = Requester.signUp(username, email, password, birthdate);
//        int statusCode = response.getInt("statusCode");
//        assertTrue(statusCode >= 400 && statusCode <= 500);
//
//        String msg = response.getString("msg");
//        assertTrue(msg.contains("birthdate must be of type SQL_DATE"));
//
//    }

    @Test
    public void T05_GetUserWithoutLogin() {

        JSONObject response = Requester.callViewMyProfileCommand();
        System.out.println(response);
        int statusCode = response.getInt("statusCode");
        String msg = response.getString("msg");
        assertTrue(statusCode == 401 || statusCode == 403);
        assertTrue(msg.contains("Unauthorized"));
    }

    @Test
    public void T06_Login() {


        JSONObject response = Requester.login(username, password);
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("Login Successful!", response.getString("msg"));
        assertTrue(response.has("token"));
        token = response.getString("token");
       //Requester.authenticationParams = AuthParamsHandler.decodeToken(token);
    }

    @Test
    public void T07_viewMyProfile() {

        JSONObject response = Requester.callViewMyProfileCommand();
        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = response.getJSONObject("data");
        assertEquals(username, data.getString("username"));
       // assertTrue(Auth.verifyHash(password, data.getString("password")));

    }


    @Test
    public void T08_updatePassword() {

        String newPassword = "123456";

        JSONObject response = Requester.updatePassword(password, newPassword);
        assertEquals(200, response.getInt("statusCode"));

        assertEquals("Account Updated Successfully!", response.getString("msg"));

        JSONObject user = Requester.callViewMyProfileCommand().getJSONObject("data");
        //assertTrue(Auth.verifyHash(newPassword, user.getString("password")));
    }

    @Test
    public void T09_updatePasswordBack() {
        JSONObject response = Requester.updatePassword("123456", password);
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("Account Updated Successfully!", response.getString("msg"));
    }

    @Test
    public void T10_newPasswordCannotMatchPreviousPassword() {
        JSONObject response = Requester.updatePassword(password, password);
        int statusCode = response.getInt("statusCode");
        assertTrue(statusCode >= 400);
        assertTrue(response.getString("msg").contains("cannot match"));
    }

    @Test
    public void T11_updateProfilePicture() {

        JSONObject response = null;
        try {
            response = Requester.updateProfilePicture();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertEquals(200, response.getInt("statusCode"));
        String msg = response.getString("msg");
        assertTrue(msg.startsWith("Profile Picture uploaded successfully"));
       // assertTrue(msg.contains(Utilities.formatUUID(userId)));
    }

    @Test
    public void T12_viewAnotherProfile() {

        JSONObject response = Requester.viewAnotherProfile(username);
        JSONObject user = Requester.callViewMyProfileCommand().getJSONObject("data");
        assertEquals(200, response.getInt("statusCode"));
        assertEquals(username, user.getString("username"));
        //assertTrue(!Utilities.isDevelopmentMode() || user.has("photoUrl"));
    }

    @Test
    public void T13_deleteProfilePicture() {

        //JSONObject response = Requester.deleteProfilePicture();
//        JSONObject user = Requester.getUser().getJSONObject("data");
//        assertEquals(200, response.getInt("statusCode"));
//        assertEquals("Profile Picture deleted successfully", response.getString("msg"));
//        assertFalse(user.has("photoUrl"));
    }


    @Test
    public void T14_deleteAccount() {


        JSONObject response = Requester.deleteAccount(password);
        assertEquals(200, response.getInt("statusCode"));

        assertEquals("Account Deleted Successfully!", response.getString("msg"));

        JSONObject getUserResponse = Requester.callViewMyProfileCommand();
        assertEquals("User not found!", getUserResponse.getString("msg"));
    }

//    @Test
//    public void T15_signUpAfterDeleteWillFailWithSameUsername() {
//
//        String email = username + "@gmail.com";
//        String birthdate = "1997-12-14";
//        JSONObject response = Requester.signUp(username, email, password, birthdate);
//        String msg = response.getString("msg");
//        assertEquals("This username is already in use, please try another one.", msg);
//        assertEquals(200, response.getInt("statusCode"));
//        JSONObject getUserResponse = Requester.getUser();
//        assertEquals("User not found!", getUserResponse.getString("msg"));
//    }

    @AfterClass
    public static void deleteFromPostgres() {
      
    }
}
