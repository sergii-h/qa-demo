package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertyReader {
    private static PropertyReader instance;

    private final Properties properties;

    private PropertyReader() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                System.getProperty("properties.file.name", "test.properties")
        );

        properties = new Properties();

        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new InternalError(e);
        }

        for (String propertyName : properties.stringPropertyNames()) {
            String systemPropertyValue = System.getProperty(propertyName);

            if (systemPropertyValue != null) {
                properties.setProperty(propertyName, systemPropertyValue);
            }
        }
    }

    public static synchronized PropertyReader getInstance() {
        if (instance == null) {
            instance = new PropertyReader();
        }

        return instance;
    }

    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }
}
