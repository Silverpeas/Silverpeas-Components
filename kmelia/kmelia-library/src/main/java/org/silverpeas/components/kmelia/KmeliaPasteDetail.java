package org.silverpeas.components.kmelia;

import org.silverpeas.core.node.model.NodePK;

/**
 * @author Nicolas Eysseric
 */
public class KmeliaPasteDetail {

  private String userId;
  private final NodePK toPK;
  private NodePK fromPK;
  private String targetValidatorIds;
  private String status;

  public KmeliaPasteDetail(final NodePK toNode) {
    toPK = toNode;
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

  public NodePK getFromPK() {
    return fromPK;
  }

  public void setFromPK(final NodePK fromPK) {
    this.fromPK = fromPK;
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