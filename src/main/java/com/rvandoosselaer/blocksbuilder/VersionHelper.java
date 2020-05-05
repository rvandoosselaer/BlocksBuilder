package com.rvandoosselaer.blocksbuilder;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads the version.properties file that is generated during the gradle build process and exposes version information.
 *
 * @author rvandoosselaer
 */
@Slf4j
public class VersionHelper {

    private static final String VERSION_FILE = "/version.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = VersionHelper.class.getResourceAsStream(VERSION_FILE)) {
            PROPERTIES.load(in);
        } catch (IOException | NullPointerException e) {
            log.warn("File not found: {}", VERSION_FILE);
        }
    }

    public static String getVersion() {
        return PROPERTIES.getProperty("version", "0.0.unknown");
    }

}
