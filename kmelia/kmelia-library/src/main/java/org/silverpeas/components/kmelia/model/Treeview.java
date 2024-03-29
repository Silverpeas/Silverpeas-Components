/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.model;

import java.util.List;

import org.silverpeas.core.node.model.NodeDetail;

public class Treeview {

  private String path = null;
  private List<NodeDetail> tree = null;
  private int nbAliases = 0;
  private String componentId = null;

  public Treeview(String path, List<NodeDetail> tree, String componentId) {
    this.path = path;
    this.tree = tree;
    this.componentId = componentId;
  }

  public String getComponentId() {
    return componentId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public List<NodeDetail> getTree() {
    return tree;
  }

  public void setTree(List<NodeDetail> tree) {
    this.tree = tree;
  }

  public int getNbAliases() {
    return nbAliases;
  }

  public void setNbAliases(int nbAliases) {
    this.nbAliases = nbAliases;
  }

}
