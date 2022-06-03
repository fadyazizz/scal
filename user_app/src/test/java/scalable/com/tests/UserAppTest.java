package scalable.com.tests;

import com.arangodb.entity.BaseDocument;
import com.fasterxml.jackson.core.JsonParser;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import scalable.com.shared.App;
import scalable.com.shared.testsHelper.TestHelper;
import scalable.com.user.UserApp;


import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserAppTest {
    
    static String username = "fady"+ new Date().getTime();
    static String password = "12345678";
    static String token;
    static String firstname="7amo";
    static String lastname="bikaaaa";
    static boolean isArtist=false;
    static String email=username+"@gmail.com";
    static String id;
    static String photo_url;
   
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
        assertEquals("successfully registered", response.getString("msg"));
    }

    @Test
    public void T02_SignUpSameUser() {


        
        


        JSONObject response = Requester.callSignUpCommand(username, email, password,firstname,lastname,isArtist);
        int statusCode = response.getInt("statusCode");
        assertTrue(statusCode == 406);

        String msg = response.getString("msg");
        assertTrue(msg.contains("username already exists"));

    }

    @Test
    public void T03_SignUpMissingParam() {




        JSONObject response = Requester.callSignUpCommand(username, email, password, firstname,null,false );
        int statusCode = response.getInt("statusCode");
        assertTrue(statusCode >= 400 && statusCode <= 500);

        String msg = response.getString("msg");
        assertTrue(msg.contains("missing") && msg.contains("lastname"));

    }

    @Test
    public void T04_Login_with_Wrong_Pass() {


        JSONObject response = Requester.login(email, password+"wrong");
        assertTrue( response.getInt("statusCode")!=200);



        assertEquals("wrong username or password", response.getString("msg"));



    }
    @Test
    public void T05_Login() {


        JSONObject response = Requester.login(email, password);

        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));
        assertTrue(response.has("data"));
        response=(JSONObject)response.get("data");
       assertEquals(response.getString("first_name"),firstname);
       assertEquals(response.getString("last_name"),lastname);
       assertEquals(response.getString("username"),username);
      

       
        assertTrue(response.has("token"));
        token = response.getString("token");
        id=response.getString("id");
        
    }
 



     @Test
     public void T06_ChangePasswordWithWrongOldPassword(){
        JSONObject response=Requester.updatePassword(password+"wrong","newPassword");

         assertEquals(response.getString("msg"),"old password is wrong");
            
     }
    @Test
    public void T07_ChangePassword(){
        String newPassword="newPassword";
        JSONObject response=Requester.updatePassword(password,newPassword);

        assertEquals(200, response.getInt("statusCode"));
        assertEquals(response.getString("msg"),"password changed successfully");
        password=newPassword;
        

    }



    @Test
    public void T07_viewMyProfile() {

        JSONObject response = Requester.callViewMyProfileCommand();
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = response.getJSONObject("data");
        assertEquals(username, data.getString("username"));
        assertEquals(firstname, data.getString("first_name"));
        assertEquals(lastname, data.getString("last_name"));
        assertEquals(email, data.getString("email"));


    }
    @Test
    public void T08_viewAnotherUserProfile() {

        JSONObject response = Requester.callViewAnotherProfile(username);
        
        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = response.getJSONObject("data");
        assertEquals(username, data.getString("username"));
        assertEquals(firstname, data.getString("first_name"));
        assertEquals(lastname, data.getString("last_name"));
        assertEquals(email, data.getString("email"));


    }
    @Test
    public void T09_addprofilePhoto() {

        JSONObject response = null;
        try {
            response = Requester.updateProfilePicture();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(200, response.getInt("statusCode"));
       assertTrue(response.getString("msg").contains("Profile Picture uploaded successfully. You can find at"));
       photo_url=response.getString("msg").split("at ")[1];

    }

    @Test
    public void T10_viewMyProfileWithPicture() {

        JSONObject response = Requester.callViewMyProfileCommand();
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = response.getJSONObject("data");
        assertEquals(username, data.getString("username"));
        assertEquals(firstname, data.getString("first_name"));
        assertEquals(lastname, data.getString("last_name"));
        assertEquals(email, data.getString("email"));
        assertEquals(photo_url,data.getString("profile_photo"));


    }







    @Test
    public void T11_editProfile() {

        JSONObject response = null;
        try {
            response = Requester.updateProfilePicture();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(200, response.getInt("statusCode"));
        assertTrue(response.getString("msg").contains("Profile Picture uploaded successfully. You can find at"));
        photo_url=response.getString("msg").split("at ")[1];
    }
    @Test
    public void T12_subscribeToPremium() {
       JSONObject response = Requester.subscribeToPremium();


        assertEquals(200, response.getInt("statusCode"));
        assertTrue(response.has("data"));

        assertTrue(response.getJSONObject("data").has("newToken"));
        String newToken=response.getJSONObject("data").getString("newToken");
        token=newToken;
        
      
    }







    @Test
    public void T13_deleteAccount() {


        JSONObject response = Requester.deleteAccount(password);
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));

        assertEquals("Account deleted successfully", response.getString("msg"));

        JSONObject getUserResponse = Requester.callViewMyProfileCommand();
        assertEquals("account not found", getUserResponse.getString("msg"));
    }

    @Test
    public void T14_deleteAlreadyDeletedAccount() {


        JSONObject response = Requester.deleteAccount(password);
        assertEquals(404, response.getInt("statusCode"));

        assertEquals("account already deleted", response.getString("msg"));

    }
    



    @AfterClass
    public static void deleteFromPostgres() {
      
    }
}
