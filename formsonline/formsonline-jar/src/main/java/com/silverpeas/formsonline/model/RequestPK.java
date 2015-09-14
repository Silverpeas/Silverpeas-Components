package com.silverpeas.formsonline.model;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * @author Nicolas Eysseric
 */
public class RequestPK extends WAPrimaryKey {

  public RequestPK(String id, String componentId) {
    super(id, componentId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof RequestPK)) {
      return false;
    }
    RequestPK other = (RequestPK) obj;
    if (id == null) {
      return other.id == null;
    }
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

}
