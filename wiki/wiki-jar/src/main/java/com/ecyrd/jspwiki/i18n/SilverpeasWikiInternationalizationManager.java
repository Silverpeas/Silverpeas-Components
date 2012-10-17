/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ecyrd.jspwiki.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ecyrd.jspwiki.WikiEngine;
import com.silverpeas.util.FileUtil;

/**
 * Manages all internationalization in JSPWiki.
 * @author Ludovic Bertin
 */
public class SilverpeasWikiInternationalizationManager extends InternationalizationManager {

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
   * @param engine To which engine this belongs to
   */
  public SilverpeasWikiInternationalizationManager(WikiEngine engine) {
    super(engine);
  }

  /**
   * Returns a String from the CORE_BUNDLE using English as the default locale.
   * @param key Key to find
   * @return The English string
   * @throws MissingResourceException If there is no such key
   */
  public String get(String key) throws MissingResourceException {
    return get(CORE_BUNDLE, null, key);
  }

  /**
   * Finds a resource bundle.
   * @param bundle The ResourceBundle to find. Must exist.
   * @param locale The Locale to use. Set to null to get the default locale.
   * @return A localized string
   * @throws MissingResourceException If the key cannot be located at all, even from the default
   * locale.
   */
  public ResourceBundle getBundle(String bundle, Locale locale)
      throws MissingResourceException {
    ResourceBundle b = FileUtil.loadBundle(bundle, getLocale());
    return b;
  }

  /**
   * If you are too lazy to open your own bundle, use this method to get a string simply from a
   * bundle.
   * @param bundle Which bundle the string is in
   * @param locale Locale to use - null for default
   * @param key Which key to use.
   * @return A localized string (or from the default language, if not found)
   * @throws MissingResourceException If the key cannot be located at all, even from the default
   * locale.
   */
  public String get(String bundle, Locale locale, String key)
      throws MissingResourceException {
    return getBundle(bundle, null).getString(key);
  }

}
