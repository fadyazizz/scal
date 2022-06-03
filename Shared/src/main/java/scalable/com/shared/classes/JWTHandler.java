package scalable.com.shared.classes;


import com.auth0.jwt.JWTCreator;





import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JWTHandler {
    public static final String IS_AUTHENTICATED = "isAuthenticated";
    public static final String TOKEN_PAYLOAD = "tokenPayload";


    
    private static final Algorithm ALGORITHM = Algorithm.HMAC256("secret");

    private static final JWTVerifier JWT_VERIFIER = JWT.require(ALGORITHM).build();
    public static JSONObject getUnauthorizedAuthParams() {
        return new JSONObject().put(IS_AUTHENTICATED, false);
    }
    public static JSONObject decodeToken(String token) {
        JSONObject returnJSON=new JSONObject();
        
        JSONObject tokenPayLoad=new JSONObject();
        boolean isAuthenticated;
        try {
            Map<String, Object> claims = verifyAndDecode(token);
            for (Map.Entry<String, Object> entry : claims.entrySet())
                tokenPayLoad.put(entry.getKey(), entry.getValue());
            isAuthenticated = true;
        } catch (JWTVerificationException jwtVerificationException) {
            isAuthenticated = false;
        }
        
        return returnJSON.put(IS_AUTHENTICATED,isAuthenticated).put(TOKEN_PAYLOAD,tokenPayLoad);
    }
    public static Map<String, Object> verifyAndDecode(String token) {
        DecodedJWT decoded = JWT_VERIFIER.verify(token);
        Map<String, Object> claims = new HashMap<>();
        Map<String, Claim> decodedClaims = decoded.getClaims();
        for (String key : decodedClaims.keySet()) {
            if (key.equals("exp") || key.equals("iat")) {
                claims.put(key, decodedClaims.get(key).asDate());
            } else {
                claims.put(key, decodedClaims.get(key).asString());
            }
        }
        return claims;
    }
    public static String generateToken(Map<String, String> claims) {
        return createJwtWithClaims(claims).sign(ALGORITHM);
    }
    private static JWTCreator.Builder createJwtWithClaims(Map<String, String> claims) {
        JWTCreator.Builder builder = JWT.create();
        for (String key : claims.keySet()) {
            builder.withClaim(key, claims.get(key));
        }
        return builder;
    }
}
