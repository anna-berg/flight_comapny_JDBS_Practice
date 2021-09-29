package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtil {

    public static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

//    возвращает значение пропертис по ключу (по username or password возвр. их значение)
    public static String get (String key){
        return PROPERTIES.getProperty(key);
    }

    private static void loadProperties (){
        try (var inputStream = PropertiesUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
            PROPERTIES.load(inputStream);
        } catch (IOException e) {
            //пробрасываем рантайм искл. для того что бы
            // приложение упало если не смогло прочитать апликейшен файл
            throw new RuntimeException(e);
        }
    }

    private PropertiesUtil(){
    }
}
