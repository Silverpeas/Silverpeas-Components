package com.silverpeas.formsonline.model;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * @author Nicolas Eysseric
 */
public class FormPK extends WAPrimaryKey {

  public FormPK(String id, String componentId) {
    super(id, componentId);
  }

  public FormPK(int id, String componentId) {
    super(Integer.toString(id), componentId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof FormPK)) {
      return false;
    }
    FormPK other = (FormPK) obj;
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
