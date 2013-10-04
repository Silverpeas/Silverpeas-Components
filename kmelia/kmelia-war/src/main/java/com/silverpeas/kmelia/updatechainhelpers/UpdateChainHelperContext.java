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

package com.silverpeas.kmelia.updatechainhelpers;

import java.util.List;

import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.model.updatechain.FieldUpdateChainDescriptor;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class UpdateChainHelperContext {

  private PublicationDetail pubDetail;
  private String[] topics;
  private List<NodeDetail> allTopics;
  private FieldUpdateChainDescriptor descriptor;
  private KmeliaSessionController kmeliaScc;

  public KmeliaSessionController getKmeliaScc() {
    return kmeliaScc;
  }

  public UpdateChainHelperContext() {

  }

  public UpdateChainHelperContext(PublicationDetail pubDetail, KmeliaSessionController kmeliaScc) {
    this.pubDetail = pubDetail;
    this.kmeliaScc = kmeliaScc;
  }

  public UpdateChainHelperContext(PublicationDetail pubDetail) {
    this.pubDetail = pubDetail;
  }

  public PublicationDetail getPubDetail() {
    return pubDetail;
  }

  public void setPubDetail(PublicationDetail pubDetail) {
    this.pubDetail = pubDetail;
  }

  public String[] getTopics() {
    return topics;
  }

  public void setTopics(String[] topics) {
    this.topics = topics;
  }

  public FieldUpdateChainDescriptor getDescriptor() {
    return descriptor;
  }

  public void setDescriptor(FieldUpdateChainDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  public List<NodeDetail> getAllTopics() {
    return allTopics;
  }

  public void setAllTopics(List<NodeDetail> allTopics) {
    this.allTopics = allTopics;
  }

}