package com.silverpeas.component.kmelia;

import com.silverpeas.admin.components.PasteDetail;
import com.silverpeas.admin.components.PasteDetailFromToPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.node.model.NodePK;

import java.util.HashMap;

public class KmeliaCopyDetail extends PasteDetailFromToPK<NodePK, NodePK> {

  public final static String PUBLICATION_HEADER = PasteDetail.OPTION_PREFIX+"PublicationHeader";
  public final static String PUBLICATION_CONTENT = PasteDetail.OPTION_PREFIX+"PublicationContent";
  public final static String PUBLICATION_FILES = PasteDetail.OPTION_PREFIX+"PublicationFiles";
  public final static String PUBLICATION_PDC = PasteDetail.OPTION_PREFIX+"PublicationPDC";
  public final static String NODE_RIGHTS = PasteDetail.OPTION_PREFIX+"NodeRights";
  public final static String ADMINISTRATIVE_OPERATION =
      PasteDetail.OPTION_PREFIX + "AdministrativeOperation";

  private String publicationTargetValidatorIds;
  private String publicationStatus;

  public KmeliaCopyDetail(String userId) {
    super(userId);
  }

  public KmeliaCopyDetail(PasteDetail pasteDetail) {
    setOptions(
        pasteDetail.getOptions() != null ? new HashMap<String, String>(pasteDetail.getOptions()) :
            null);
    setUserId(pasteDetail.getUserId());
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

  public void addOption(String key, String value) {
    if (getOptions() == null) {
      setOptions(new HashMap<String, String>());
    }
    getOptions().put(key, value);
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
    return getOptions() != null && StringUtil.getBooleanValue(getOptions().get(NODE_RIGHTS));
  }
  
  private boolean isMustBeCopied(String optionName) {
    return getOptions() == null || StringUtil.getBooleanValue(getOptions().get(optionName));
  }

  public boolean isAdministrativeOperation() {
    return getOptions() != null &&
        StringUtil.getBooleanValue(getOptions().get(ADMINISTRATIVE_OPERATION));
  }
}