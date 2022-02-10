/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.kmelia;

import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.admin.component.model.PasteDetailFromToPK;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.node.model.NodePK;

import java.util.HashMap;

public class KmeliaCopyDetail extends PasteDetailFromToPK<NodePK, NodePK> {

  public static final String PUBLICATION_HEADER = PasteDetail.OPTION_PREFIX + "PublicationHeader";
  public static final String PUBLICATION_CONTENT = PasteDetail.OPTION_PREFIX + "PublicationContent";
  public static final String PUBLICATION_FILES = PasteDetail.OPTION_PREFIX + "PublicationFiles";
  public static final String PUBLICATION_PDC = PasteDetail.OPTION_PREFIX + "PublicationPDC";
  public static final String NODE_RIGHTS = PasteDetail.OPTION_PREFIX + "NodeRights";
  public static final String ADMINISTRATIVE_OPERATION =
      PasteDetail.OPTION_PREFIX + "AdministrativeOperation";

  private String publicationTargetValidatorIds;
  private String publicationStatus;

  public KmeliaCopyDetail(String userId) {
    super(userId);
  }

  public KmeliaCopyDetail(PasteDetail pasteDetail) {
    setOptions(
        pasteDetail.getOptions() != null ? new HashMap<>(pasteDetail.getOptions()) :
            null);
    setUserId(pasteDetail.getUserId());
    if (pasteDetail instanceof KmeliaCopyDetail) {
      KmeliaCopyDetail copyDetail = (KmeliaCopyDetail) pasteDetail;
      setPublicationStatus(copyDetail.getPublicationStatus());
      setPublicationTargetValidatorIds(copyDetail.getPublicationValidatorIds());
      setFromComponentId(copyDetail.getFromComponentId());
    }
  }

  public static KmeliaCopyDetail fromPasteDetail(KmeliaPasteDetail pasteDetail) {
    KmeliaCopyDetail copyDetail = new KmeliaCopyDetail(pasteDetail.getUserId());
    copyDetail.setToNodePK(pasteDetail.getToPK());
    copyDetail.setPublicationStatus(pasteDetail.getStatus());
    copyDetail.setPublicationTargetValidatorIds(pasteDetail.getTargetValidatorIds());
    return copyDetail;
  }

  public void setFromNodePK(NodePK fromNodePK) {
    setFromPK(fromNodePK);
    setFromComponentId(fromNodePK.getInstanceId());
  }

  public NodePK getFromNodePK() {
    return getFromPK();
  }

  public void setToNodePK(NodePK toNodePK) {
    setToPK(toNodePK);
  }

  public NodePK getToNodePK() {
    return getToPK();
  }

  public String getPublicationValidatorIds() {
    return publicationTargetValidatorIds;
  }

  public void setPublicationTargetValidatorIds(final String ids) {
    this.publicationTargetValidatorIds = ids;
  }

  public String getPublicationStatus() {
    return publicationStatus;
  }

  public void setPublicationStatus(final String publicationStatus) {
    this.publicationStatus = publicationStatus;
  }

  public boolean isPublicationHeaderMustBeCopied() {
    return getOptions() == null || isMustBeCopied(PUBLICATION_HEADER) ||
        isPublicationContentMustBeCopied() || isPublicationFilesMustBeCopied();
  }

  public boolean isPublicationContentMustBeCopied() {
    return isMustBeCopied(PUBLICATION_CONTENT);
  }

  public boolean isPublicationFilesMustBeCopied() {
    return isMustBeCopied(PUBLICATION_FILES);
  }

  public boolean isPublicationPositionsMustBeCopied() {
    return isMustBeCopied(PUBLICATION_PDC);
  }

  public boolean isNodeRightsMustBeCopied() {
    return isMustBeCopied(NODE_RIGHTS);
  }

  private boolean isMustBeCopied(String optionName) {
    return getOptions() == null || StringUtil.getBooleanValue(getOptions().get(optionName));
  }

  public boolean isAdministrativeOperation() {
    return getOptions() != null &&
        StringUtil.getBooleanValue(getOptions().get(ADMINISTRATIVE_OPERATION));
  }
}
