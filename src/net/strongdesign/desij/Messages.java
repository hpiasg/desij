/*
 * Created on 15.12.2004
 *
 */
package net.strongdesign.desij;

import java.util.*;


/**
 * 
 * @author Mark Schaefer 
 */
public class Messages {
    private static final String BUNDLE_NAME = "net.strongdesign.stg.messages";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(
        String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}