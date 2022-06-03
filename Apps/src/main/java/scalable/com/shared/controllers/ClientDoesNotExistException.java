package scalable.com.shared.controllers;

public class ClientDoesNotExistException extends Exception{
    public ClientDoesNotExistException(String m) {
        super(m);
    }

}
