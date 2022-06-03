package scalable.com.music.commands;

import scalable.com.exceptions.ValidationException;
import scalable.com.shared.classes.Responder;

public class TestCommand extends MusicCommand{
    String fady;
    @Override
    public String getCommandName() {
        return "Test";
    }

    @Override
    public String execute() {

             System.out.println("executing test commands");
             System.out.println(this.origRequest);

        return Responder.makeMsgResponse("ya walaaaaaaaaaa");
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
            this.fady=this.body.getString("fady");
        }

        catch (Exception e){
            throw new ValidationException("attributes data types are wrong: "+e.getMessage());
        }
    }
}
