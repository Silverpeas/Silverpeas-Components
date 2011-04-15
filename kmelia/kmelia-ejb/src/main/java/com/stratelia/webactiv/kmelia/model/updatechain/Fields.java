/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

public class Fields {
  private String title;
  private String libelle;

  private FieldUpdateChainDescriptor name;
  private FieldUpdateChainDescriptor description;
  private FieldUpdateChainDescriptor keywords;
  private FieldUpdateChainDescriptor tree;

  private String helper;

  private String[] topics;

  public FieldUpdateChainDescriptor getName() {
    return name;
  }

  public void setName(FieldUpdateChainDescriptor name) {
    this.name = name;
  }

  public FieldUpdateChainDescriptor getDescription() {
    return description;
  }

  public void setDescription(FieldUpdateChainDescriptor description) {
    this.description = description;
  }

  public FieldUpdateChainDescriptor getKeywords() {
    return keywords;
  }

  public void setKeywords(FieldUpdateChainDescriptor keywords) {
    this.keywords = keywords;
  }

  public FieldUpdateChainDescriptor getTree() {
    return tree;
  }

  public void setTree(FieldUpdateChainDescriptor tree) {
    this.tree = tree;
  }

  public String[] getTopics() {
    return topics;
  }

  public void setTopics(String[] topics) {
    this.topics = topics;
  }

  public String getHelper() {
    return helper;
  }

  public void setHelper(String helper) {
    this.helper = helper;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }

}