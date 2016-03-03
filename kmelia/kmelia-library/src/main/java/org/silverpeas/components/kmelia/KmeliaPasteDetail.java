package org.silverpeas.components.kmelia;

import com.stratelia.webactiv.node.model.NodePK;

/**
 * @author Nicolas Eysseric
 */
public class KmeliaPasteDetail {

  private String userId;
  private NodePK toPK;
  private String targetValidatorIds;
  private String status;

  public KmeliaPasteDetail(NodePK pk) {
    toPK = pk;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public NodePK getToPK() {
    return toPK;
  }

  public String getTargetValidatorIds() {
    return targetValidatorIds;
  }

  public void setTargetValidatorIds(String ids) {
    targetValidatorIds = ids;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }
}