import org.json.JSONObject;
import scalable.com.shared.App;

public class TestApp extends AppClassForTesting {

    public String appName="User";
    JSONObject expectedResponse;
    @Override
    protected String getAppName() {
        return appName;
    }

    @Override
    public JSONObject getExpectedResponse() {
        return expectedResponse;
    }

    public void setExpectedResponse(JSONObject expectedResponse){
           this.expectedResponse=expectedResponse;
    }


}
