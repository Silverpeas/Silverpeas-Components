package com.silverpeas.crm.control;

import com.silverpeas.crm.implementation.CrmDataManager;
import com.silverpeas.crm.model.CrmDataInterface;

/**
 * Cette classe est reponsable de la fabrication des services.
 */
public class ServiceFactory {

  public ServiceFactory() {
  }

  public static CrmDataInterface getCrmData() {
    return new CrmDataManager();
  }

}
