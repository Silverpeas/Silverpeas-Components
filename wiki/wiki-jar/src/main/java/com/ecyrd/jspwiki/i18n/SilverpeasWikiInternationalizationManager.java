package com.ecyrd.jspwiki.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ecyrd.jspwiki.WikiEngine;

/**
 * Manages all internationalization in JSPWiki.
 * 
 * @author Ludovic Bertin
 */
public class SilverpeasWikiInternationalizationManager extends
		InternationalizationManager {
	
    private static ThreadLocal preferredLocale = new ThreadLocal();

    public static void setPreferredLanguage(String newLanguage) {
        Locale locale = new Locale(newLanguage);
        preferredLocale.set(locale);
    }
    
    public static Locale getLocale() {
        return (Locale) preferredLocale.get();
    }

    /**
     * Constructs a new InternationalizationManager.
     * 
     * @param engine
     *            To which engine this belongs to
     */
    public SilverpeasWikiInternationalizationManager(WikiEngine engine)
    {
    	super(engine);
    }

    /**
     * Returns a String from the CORE_BUNDLE using English as the default
     * locale.
     * 
     * @param key
     *            Key to find
     * @return The English string
     * @throws MissingResourceException
     *             If there is no such key
     */
    public String get(String key) throws MissingResourceException
    {
        return get(CORE_BUNDLE, null, key);
    }

    /**
     * Finds a resource bundle.
     * 
     * @param bundle
     *            The ResourceBundle to find. Must exist.
     * @param locale
     *            The Locale to use. Set to null to get the default locale.
     * @return A localized string
     * @throws MissingResourceException
     *             If the key cannot be located at all, even from the default
     *             locale.
     */
    public ResourceBundle getBundle(String bundle, Locale locale) throws MissingResourceException
    {
        ResourceBundle b = ResourceBundle.getBundle(bundle, getLocale());

        return b;
    }

    /**
     * If you are too lazy to open your own bundle, use this method to get a
     * string simply from a bundle.
     * 
     * @param bundle
     *            Which bundle the string is in
     * @param locale
     *            Locale to use - null for default
     * @param key
     *            Which key to use.
     * @return A localized string (or from the default language, if not found)
     * @throws MissingResourceException
     *             If the key cannot be located at all, even from the default
     *             locale.
     */
    public String get(String bundle, Locale locale, String key) throws MissingResourceException
    {
        return getBundle(bundle, null).getString(key);
    }

}
