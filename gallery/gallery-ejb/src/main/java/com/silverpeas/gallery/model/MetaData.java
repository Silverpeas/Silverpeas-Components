/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.model;

import java.io.UnsupportedEncodingException;
import java.util.Date;

public class MetaData {

  private String property;
  private String label;
  private boolean date = false;
  private String value; // value as String
  private Date dateValue; // value as Date if metadata is a date
  private byte[] data;

  public MetaData() {
  }

  public MetaData(String value) {
    this.value = value;
  }

  public MetaData(byte[] data) {
    this.data = data;
  }

  public String getLabel() {
    return label;
  }

  public MetaData setLabel(String label) {
    this.label = label;
    return this;
  }

  public boolean isDate() {
    return date;
  }

  public MetaData setDate(boolean date) {
    this.date = date;
    return this;
  }

  public String getValue() {
    return value;
  }

  public MetaData setValue(String value) {
    this.value = value;
    return this;
  }

  public String getProperty() {
    return property;
  }

  public MetaData setProperty(String property) {
    this.property = property;
    return this;
  }

  public Date getDateValue() {
    return dateValue;
  }

  public MetaData setDateValue(Date dateValue) {
    this.dateValue = dateValue;
    return this;
  }

  public MetaData setData(byte[] data) {
    this.data = data;
    return this;
  }

  public void convert(String encoding) throws UnsupportedEncodingException {
    if(this.data != null) {
      this.value = new String(data, encoding);
    }
  }

}
