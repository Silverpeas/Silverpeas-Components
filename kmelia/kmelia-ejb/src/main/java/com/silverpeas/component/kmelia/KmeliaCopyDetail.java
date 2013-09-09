package com.silverpeas.component.kmelia;

import java.util.HashMap;

import com.silverpeas.admin.components.PasteDetail;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.node.model.NodePK;

public class KmeliaCopyDetail extends PasteDetail {
  
  public final static String PUBLICATION_HEADER = PasteDetail.OPTION_PREFIX+"PublicationHeader";
  public final static String PUBLICATION_CONTENT = PasteDetail.OPTION_PREFIX+"PublicationContent";
  public final static String PUBLICATION_FILES = PasteDetail.OPTION_PREFIX+"PublicationFiles";
  public final static String PUBLICATION_PDC = PasteDetail.OPTION_PREFIX+"PublicationPDC";
  
  public final static String NODE_RIGHTS = PasteDetail.OPTION_PREFIX+"NodeRights";
  
  private NodePK fromNodePK;
  private NodePK toNodePK;

  public KmeliaCopyDetail(String userId) {
    super(userId);
  }
  
  public KmeliaCopyDetail(PasteDetail pasteDetail) {
    setOptions(pasteDetail.getOptions());
    setUserId(pasteDetail.getUserId());
  }

  public void setFromNodePK(NodePK fromNodePK) {
    this.fromNodePK = fromNodePK;
  }

  public NodePK getFromNodePK() {
    return fromNodePK;
  }

  public void setToNodePK(NodePK toNodePK) {
    this.toNodePK = toNodePK;
  }

  public NodePK getToNodePK() {
    return toNodePK;
  }
  
  public void addOption(String key, String value) {
    if (getOptions() == null) {
      super.setOptions(new HashMap<String, String>());
    }
    getOptions().put(key, value);
  }
  
  public boolean isPublicationHeaderMustBeCopied() {
    if (getOptions() == null) {
      return true;
    }
    return isMustBeCopied(PUBLICATION_HEADER) ||
        isPublicationContentMustBeCopied() ||
        isPublicationFilesMustBeCopied();
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
    if (getOptions() == null) {
      return false;
    }
    return StringUtil.getBooleanValue(getOptions().get(NODE_RIGHTS));
  }
  
  private boolean isMustBeCopied(String optionName) {
    if (getOptions() == null) {
      return true;
    }
    return StringUtil.getBooleanValue(getOptions().get(optionName));
  }
  

}