package com.silverpeas.crm.model;

import java.io.Serializable;

import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Class declaration
 * @author
 */
public class CrmPK extends WAPrimaryKey implements Serializable {
  
  private static final long serialVersionUID = 135542061633931139L;
  
  protected static OrganizationController organizationController = new OrganizationController();

  public CrmPK(String id, String componentId) {
    super(id);
    ComponentInst component = organizationController.getComponentInst(componentId);
    setComponentName(componentId);
    setSpace(component.getDomainFatherId());
  }

  /**
   * Method declaration
   * @param other
   * @return
   * @see
   */
  public boolean equals(Object other) {
    if (!(other instanceof CrmPK)) {
      return false;
    }
    return id.equals(((CrmPK) other).getId());
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String toString() {
    return "(id = " + getId() + " )";
  }
  
}
