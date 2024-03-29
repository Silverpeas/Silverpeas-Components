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

import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This object contains elements which are displayed in a kmelia Topic
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class TopicDetail extends Object implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  /** A NodeDetail collection representing the path from leaf to root */
  private Collection<NodeDetail> path;

  /** the informations of the Topic are in this object */
  private NodeDetail nodeDetail;

  /** A NodeDetail collection representing the path from leaf to root */
  private Collection<KmeliaPublication> publications;

  /**
   * Construct an empty TopicDetail
   * @since 1.0
   */
  public TopicDetail() {
    init(null, null, null);
  }

  /**
   * Create a new TopicDetail
   * @since 1.0
   */
  public TopicDetail(Collection<NodeDetail> path, NodeDetail nodeDetail,
      Collection<KmeliaPublication> kmeliaPublication) {
    init(path, nodeDetail, kmeliaPublication);
  }

  /**
   * Create a new TopicDetail
   * @since 1.0
   */
  private void init(Collection<NodeDetail> path, NodeDetail nodeDetail,
      Collection<KmeliaPublication> kmeliaPublications) {
    this.path = path;
    this.nodeDetail = nodeDetail;
    this.publications = kmeliaPublications;
  }

  /**
   * Get the path
   * @return the path
   * @since 1.0
   */
  public Collection<NodeDetail> getPath() {
    return this.path;
  }

  /**
   * Get the Topic nodePK
   * @return the Topic nodePK
   * @see NodePK
   * @since 1.0
   */
  public NodePK getNodePK() {
    return this.nodeDetail.getNodePK();
  }

  /**
   * Get the detail of this topic
   * @return the detail of this topic
   * @see NodeDetail
   * @since 1.0
   */
  public NodeDetail getNodeDetail() {
    return this.nodeDetail;
  }

  /**
   * Get a PublicationDetail collection containing all the publications in this topic
   * @return a PublicationDetail collection containing all the publications in this topic
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @see java.util.Collection
   * @since 1.0
   */
  public Collection<KmeliaPublication> getKmeliaPublications() {
    return this.publications;
  }

  /**
   * Set the path
   * @param path a NodeDetail Collection
   * @since 1.0
   */
  public void setPath(Collection<NodeDetail> path) {
    this.path = path;
  }

  /**
   * Set the detail of this topic
   * @param nd the topic NodeDetail
   * @since 1.0
   */
  public void setNodeDetail(NodeDetail nd) {
    this.nodeDetail = nd;
  }

  /**
   * Set the publication details of each publication containing in this topic
   * @param pd a PublicationDetail Collection
   * @since 1.0
   */
  public void setPublicationDetails(Collection<KmeliaPublication> pd) {
    this.publications = pd;
  }

  public List<KmeliaPublication> getValidPublications() {
    return getValidPublications(null);
  }

  public List<KmeliaPublication> getValidPublications(PublicationPK pubPKToExclude) {
    List<KmeliaPublication> validPublications = new ArrayList<>();
    for (KmeliaPublication aPublication : publications) {
      PublicationDetail detail = aPublication.getDetail();
      if (detail.getStatus() != null && detail.getStatus().equals("Valid")) {
        if (pubPKToExclude == null)
          validPublications.add(aPublication);
        else {
          if (!detail.getPK().equals(pubPKToExclude))
            validPublications.add(aPublication);
        }
      }
    }
    return validPublications;
  }
}