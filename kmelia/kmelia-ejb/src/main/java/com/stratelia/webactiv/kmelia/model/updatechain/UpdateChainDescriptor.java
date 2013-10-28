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

import java.util.ArrayList;
import java.util.List;

public class UpdateChainDescriptor {

  private String title;
  private String libelle;

  private String helper;

  private List<FieldUpdateChainDescriptor> fields = new ArrayList<FieldUpdateChainDescriptor>();

  public UpdateChainDescriptor(String title, List<FieldUpdateChainDescriptor> fields) {
    this.title = title;
    this.fields = fields;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<FieldUpdateChainDescriptor> getFields() {
    return fields;
  }

  public void setFields(List<FieldUpdateChainDescriptor> fields) {
    this.fields = fields;
  }

  public void add(FieldUpdateChainDescriptor field) {
    fields.add(field);
  }

  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }

  public String getHelper() {
    return helper;
  }

  public void setHelper(String helper) {
    this.helper = helper;
  }

}