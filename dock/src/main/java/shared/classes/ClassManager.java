package shared.classes;



import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ClassManager {
    private final ClassRegistry classRegistry = ClassRegistry.getInstance();
    private final Map<String, String> commandMap = new ConcurrentHashMap<>();
    public  Map<String, Properties> validationMap=new ConcurrentHashMap<>();

    private static Properties readPropertiesFile(String path) throws IOException {
        final InputStream inputStream = ClassManager.class.getClassLoader().getResourceAsStream(path);
        if(inputStream==null){
            return null;
        }
        final Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }

    public void init() throws IOException, ClassNotFoundException {
        loadCommandMap();
        
        loadCommandClasses();
        System.out.println("loading validations");
        loadValidations();

       
    }
    private void loadValidations() throws ClassNotFoundException {

        for (Map.Entry<String,String> entry : commandMap.entrySet()){
            final Class<?> commandClass = this.getCommand(entry.getKey());
            // creating an instance of the command class

            try {
                final Command commandInstance = (Command) commandClass.getDeclaredConstructor().newInstance();
                String name=(String)commandClass.getMethod("getCommandName").invoke(commandInstance, null);
                Properties commandProperties = readPropertiesFile(name+ ".properties");
                if (commandProperties==null){
                    continue;
                }
                validationMap.put(name,commandProperties);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
    public void addValidationAttributes(String commandName,String[] validationAttributes){
        Properties properties=new Properties();
        if (!validationAttributes[0].equals("")) {
            for (int i = 0; i < validationAttributes.length; i++) {
                properties.put(validationAttributes[i], validationAttributes[i]);
            }
        }
        validationMap.put(commandName,properties);
    }
    private void loadCommandMap() throws IOException {
        final Properties commandProperties = readPropertiesFile(ServiceConstants.COMMAND_MAP_FILENAME);
        for (final String key : commandProperties.stringPropertyNames()) {
            commandMap.put(key, commandProperties.getProperty(key));
        }
    }

    private void loadCommandClasses() throws ClassNotFoundException {
        boolean someClassWereNotFound = false;
        for (final String className : commandMap.values()) {
            try {
                classRegistry.addClassByName(className);
            } catch (ClassNotFoundException e) {
                someClassWereNotFound = true;
            }
        }

        if (someClassWereNotFound) {
            throw new ClassNotFoundException("Some classes were not found.");
        }
    }

    public Class<?> getCommand(String functionName) throws ClassNotFoundException {
     
        final String className = commandMap.get(functionName);
        if(className == null) {
            throw new ClassNotFoundException("Class not found in command map.");
        }
        return classRegistry.getClass(className);
    }


   // funtionName is className
    public void addCommand(String className, String commandPath, byte[] b) {
        commandMap.put(className, commandPath);
        classRegistry.addClassByBytes(commandPath, b);
    }

    public void updateCommand(String className, String commandPath, byte[] b) {
        addCommand(className, commandPath, b);
    }

    public void deleteCommand(String className,String commandName) {
        final String temp = commandMap.get(className);
        System.out.println(temp);
        commandMap.remove(className);
        System.out.println(1);
        classRegistry.removeClass(temp);
        System.out.println(2);
        validationMap.remove(commandName);
        System.out.println(3);
    }

}
