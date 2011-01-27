/*
 * Created on 15.12.2004
 *
 */
package net.strongdesign.stg;

import java.util.*;


/**
 * 
 * @author Mark Sch�er 
 */
public class Messages {
    private static final String BUNDLE_NAME = "stg.messages";

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