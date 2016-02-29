/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.media;

import org.silverpeas.util.i18n.I18NHelper;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ehugonnet
 */
public class ExifProperty {

  private int property;
  private Map<String, String> labels;

  public ExifProperty(int property) {
    this.property = property;
    this.labels = new HashMap<>();
  }

  /**
   * @return the property
   */
  public int getProperty() {
    return property;
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return labels.get(I18NHelper.defaultLanguage);
  }

  /**
   * @return the label
   */
  public String getLabel(String lang) {
    return labels.get(lang);
  }

  /**
   * @param label the label to set
   */
  public void setLabel(String lang, String label) {
    this.labels.put(lang, label);
  }
}
