package com.custom.orm.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

    public static Properties getProperties(String fileName) throws IOException {

        try (InputStream input = PropertiesReader.class.getClassLoader()
                .getResourceAsStream(fileName)) {

            Properties prop = new Properties();

            if (input == null) {
                throw new IOException("Sorry, unable to find " + fileName);
            }

            prop.load(input);
            return prop;

        } catch (IOException ex) {
            throw new IOException("Sorry, there were some problems with loading Properties from  " + fileName);
        }
    }
}
