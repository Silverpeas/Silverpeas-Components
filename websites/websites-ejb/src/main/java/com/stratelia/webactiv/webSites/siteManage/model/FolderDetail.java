/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.stratelia.webactiv.webSites.siteManage.model;

import java.util.Collection;

import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

public class FolderDetail extends Object implements java.io.Serializable {

  /** A NodeDetail collection representing the path from leaf to root */
  private Collection path;

  /** the informations of the Topic are in this object */
  private NodeDetail nodeDetail;

  /** A NodeDetail collection representing the path from leaf to root */
  private Collection publicationDetails;

  /**
   * A int collection which contains the number of publication containing under
   * each sub topics of the topics
   */
  private Collection nbPubByTopic;

  public FolderDetail() {
    init(null, null, null, null);
  }

  public FolderDetail(Collection path, NodeDetail nodeDetail,
      Collection pubDetails, Collection nbPubByTopic) {
    init(path, nodeDetail, pubDetails, nbPubByTopic);
  }

  private void init(Collection path, NodeDetail nodeDetail,
      Collection pubDetails, Collection nbPubByTopic) {
    this.path = path;
    this.nodeDetail = nodeDetail;
    this.publicationDetails = pubDetails;
    this.nbPubByTopic = nbPubByTopic;
  }

  public Collection getPath() {
    return this.path;
  }

  public NodePK getNodePK() {
    return this.nodeDetail.getNodePK();
  }

  public NodeDetail getNodeDetail() {
    return this.nodeDetail;
  }

  public Collection getPublicationDetails() {
    return this.publicationDetails;
  }

  public Collection getNbPubByTopic() {
    return this.nbPubByTopic;
  }

  public void setPath(Collection path) {
    this.path = path;
  }

  public void setNodeDetail(NodeDetail nd) {
    this.nodeDetail = nd;
  }

  public void setPublicationDetails(Collection pd) {
    this.publicationDetails = pd;
  }

  public void setNbPubByTopic(Collection nbPubByTopic) {
    this.nbPubByTopic = nbPubByTopic;
  }
}