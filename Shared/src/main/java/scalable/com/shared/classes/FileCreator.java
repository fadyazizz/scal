package scalable.com.shared.classes;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class FileCreator {
    private static final String SAMPLE_IMAGE_NAME = "download.jpeg";
    private static final String SAMPLE_IMAGE_TYPE = "image/png";

    public static JSONObject generateImageJson() throws IOException {
        InputStream is = FileCreator.class.getClassLoader().getResourceAsStream(SAMPLE_IMAGE_NAME);
        if (is != null) {
            String data = org.apache.commons.codec.binary.Base64.encodeBase64String(is.readAllBytes());
            return new JSONObject().put("data", data).put("type", SAMPLE_IMAGE_TYPE);
        } else
            return null;
    }
}
