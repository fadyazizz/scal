package scalable.com.shared.controllers;


public interface DatabaseClient {
    void createPool(int maxConnections);
    void destroyPool() throws ClientDoesNotExistException;
    void setMaxConnections(int maxConnections) throws ClientDoesNotExistException;
    int getMaxConnectionCount();
    String getName();
}
