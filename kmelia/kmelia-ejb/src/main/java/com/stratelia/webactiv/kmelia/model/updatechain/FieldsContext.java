/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.webactiv.kmelia.model.updatechain;

public class FieldsContext {

  String currentFieldIndex = "0";
  String language = "fr";
  String componentId = null;
  String userId = null;
  int lastFieldIndex;
  boolean useMandatory = true; // used to modify several objects at the same time.

  public FieldsContext() {
  }

  public FieldsContext(String language, String componentId, String userId) {
    setLanguage(language);
    setComponentId(componentId);
    setUserId(userId);
  }

  public String getCurrentFieldIndex() {
    return currentFieldIndex;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setCurrentFieldIndex(String currentFieldIndex) {
    this.currentFieldIndex = currentFieldIndex;
  }

  public void incCurrentFieldIndex(int increment) {
    int currentFieldIndexInt = 0;
    if (currentFieldIndex != null) {
      currentFieldIndexInt = Integer.parseInt(currentFieldIndex);
    }
    currentFieldIndexInt = currentFieldIndexInt + increment;
    this.currentFieldIndex = Integer.toString(currentFieldIndexInt);
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String string) {
    userId = string;
  }

  public int getLastFieldIndex() {
    return lastFieldIndex;
  }

  public void setLastFieldIndex(int lastFieldIndex) {
    this.lastFieldIndex = lastFieldIndex;
  }

  public boolean useMandatory() {
    return useMandatory;
  }

  public void setUseMandatory(boolean ignoreMandatory) {
    this.useMandatory = ignoreMandatory;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }
}