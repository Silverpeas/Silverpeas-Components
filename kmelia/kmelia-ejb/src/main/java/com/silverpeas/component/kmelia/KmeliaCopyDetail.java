package com.silverpeas.component.kmelia;

import com.silverpeas.admin.components.PasteDetail;
import com.silverpeas.admin.components.PasteDetailFromToPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.node.model.NodePK;

import java.util.HashMap;

public class KmeliaCopyDetail extends PasteDetailFromToPK<NodePK, NodePK> {

  public final static String PUBLICATION_HEADER = PasteDetail.OPTION_PREFIX+"PublicationHeader";
  public final static String PUBLICATION_CONTENT = PasteDetail.OPTION_PREFIX+"PublicationContent";
  public final static String PUBLICATION_FILES = PasteDetail.OPTION_PREFIX+"PublicationFiles";
  public final static String PUBLICATION_PDC = PasteDetail.OPTION_PREFIX+"PublicationPDC";
  
  public final static String NODE_RIGHTS = PasteDetail.OPTION_PREFIX+"NodeRights";

  public KmeliaCopyDetail(String userId) {
    super(userId);
  }
  
  public KmeliaCopyDetail(PasteDetail pasteDetail) {
    setOptions(pasteDetail.getOptions());
    setUserId(pasteDetail.getUserId());
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
  
  public void addOption(String key, String value) {
    if (getOptions() == null) {
      super.setOptions(new HashMap<String, String>());
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
}