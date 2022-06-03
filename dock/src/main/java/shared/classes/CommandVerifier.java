package shared.classes;

import com.exceptions.ValidationException;
import org.json.JSONObject;


import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class  CommandVerifier extends Command {


    protected JSONObject body, uriParams, authenticationParams, files,tokenPayload;

    
    protected  final String IS_AUTHENTICATED = "isAuthenticated";
    public  final String GOOD_REQUEST_BODY = "goodRequest";
    protected JSONObject origRequest;

    @Override
    public final String execute(JSONObject request) {
        System.out.println("executing ..."+this.getCommandName());
        origRequest = request;
        //setting this objects uriParams
        uriParams = request.getJSONObject("uriParams");
        //setting this command object's method type
        String methodType = getRestAPIMethod();
        //check the request made has the correct restfull api method
        if (!methodType.toUpperCase().equals(request.getString("methodType"))) {
            System.out.println("wrong method type");
            return Responder.makeErrorResponse(String.format("%s expects a %s Request!", getClass().getSimpleName(), methodType), 500);
        }
         //get the body of this request if found
        body = request.has("body") ? request.getJSONObject("body") : new JSONObject();

        authenticationParams = request.has("authenticationParams") ? request.getJSONObject("authenticationParams") : new JSONObject();
        files = request.has("files") ? request.getJSONObject("files") : new JSONObject();

        //did the server authenticate this request?

        if (isAuthNeeded() && !request.getBoolean(IS_AUTHENTICATED)) {
            System.out.println("not authenticated!");
            return Responder.makeErrorResponse("Unauthorized action! Please Login!", 401);
        }
               try {
                 
                  this.verifyBody();
               }
               catch(Exception e){
                   return Responder.makeErrorResponse(e.getMessage(), 400);
               }
        if (isAuthNeeded()){
            tokenPayload=request.getJSONObject("tokenPayload");
        }

        return execute();
    }
    
    public abstract String execute();
   // public abstract void validateAttributesNumber() throws IOException, ValidationException;

   
    public void validateAttributesNumber() throws IOException, ValidationException {
        System.out.println("validating");
        try {
           if(this.validationProperties==null){
               throw new NullPointerException();
           }
             Properties prop=this.validationProperties;
            for (Map.Entry<Object, Object> e : prop.entrySet()) {
                System.out.println("\"" +e.getValue()+"\"");
                if (!body.has( (String) e.getValue() )) {

                    throw new ValidationException("Attribute: "+e.toString()+" is missing");
                }
            }
        }
        catch (NullPointerException e){
           
            throw new ValidationException("It seems you forgot to create "+this.getCommandName()+".properties file");
        }

     




    }
    public boolean verifyBody() throws ValidationException, IOException {
        this.validateAttributesNumber();
         this.validateAttributeTypes();
        this.validateAnnotations();
        return true;
    }
    public void validateAnnotations() throws ValidationException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(this);
        if(!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(cv -> cv.getMessage())
                    .collect(Collectors.joining(", "));
            System.out.println(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }

    public abstract String getRestAPIMethod();
    public abstract boolean isAuthNeeded();
    public abstract void validateAttributeTypes() throws ValidationException;

}
