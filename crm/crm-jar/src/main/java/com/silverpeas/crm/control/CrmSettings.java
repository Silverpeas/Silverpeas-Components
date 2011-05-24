package com.silverpeas.crm.control;

import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.ResourceLocator;

public class CrmSettings extends SilverpeasSettings {
  static private String[] m_Function = null;
  static private String[] m_Event_State = null;
  static private String[] m_Media = null;

  static {
    ResourceLocator rs = new ResourceLocator("com.silverpeas.crm.multilang.crmBundle", "");
    m_Function = CrmSettings.readStringArray(rs, "crm.", ".function", -1);
    m_Event_State = CrmSettings.readStringArray(rs, "crm.", ".event_state", -1);
    m_Media = CrmSettings.readStringArray(rs, "crm.", ".media", -1);
  }

  static public String[] getFunction() {
    return m_Function;
  }

  static public String[] getEventState() {
    return m_Event_State;
  }

  static public String[] getMedia() {
    return m_Media;
  }
}
