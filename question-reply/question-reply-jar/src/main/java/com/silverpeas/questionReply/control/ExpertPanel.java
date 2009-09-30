/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.silverpeas.questionReply.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelProvider;
import com.stratelia.silverpeas.genericPanel.PanelSearchEdit;
import com.stratelia.silverpeas.genericPanel.PanelSearchToken;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

public class ExpertPanel extends PanelProvider {
  protected static final int FILTER_LASTNAME = 0;
  protected static final int FILTER_FIRSTNAME = 1;

  protected static final int COL_LASTNAME = 0;
  protected static final int COL_FIRSTNAME = 1;

  protected OrganizationController m_oc = new OrganizationController();

  protected Hashtable m_AllUserDetail = new Hashtable();

  public ExpertPanel(String language, Collection allExperts) {
    initAll(language, allExperts);
  }

  public void initAll(String language, Collection allExperts) {
    String[] filters = new String[2];

    setAllExperts(allExperts);

    // Set the language
    m_Language = language;

    ResourceLocator message = GeneralPropertiesManager
        .getGeneralMultilang(m_Language);

    // Set the resource locator for columns header
    m_rs = new ResourceLocator(
        "com.silverpeas.questionReply.multilang.questionReplyBundle",
        m_Language);

    // Set the Page name
    m_PageName = m_rs.getString("questionReply.experts");
    m_PageSubTitle = m_rs.getString("questionReply.experts");

    // Set column headers
    m_ColumnsHeader = new String[2];
    m_ColumnsHeader[COL_LASTNAME] = message.getString("GML.lastName");
    m_ColumnsHeader[COL_FIRSTNAME] = message.getString("GML.firstName");

    // Build search tokens
    m_SearchTokens = new PanelSearchToken[2];

    m_SearchTokens[FILTER_LASTNAME] = new PanelSearchEdit(0, message
        .getString("GML.lastName"), "");
    m_SearchTokens[FILTER_FIRSTNAME] = new PanelSearchEdit(1, message
        .getString("GML.firstName"), "");

    // Set filters and get Ids
    filters[FILTER_FIRSTNAME] = "";
    filters[FILTER_LASTNAME] = "";
    refresh(filters);
  }

  public void refresh(String[] filters) {
    ArrayList ids = new ArrayList();
    boolean keepit;
    Iterator it = m_AllUserDetail.values().iterator();
    UserDetail user;

    while (it.hasNext()) {
      user = (UserDetail) it.next();
      keepit = true;
      if ((filters[FILTER_FIRSTNAME] != null)
          && (filters[FILTER_FIRSTNAME].length() > 0)) {
        if ((user.getFirstName() == null)
            || (filters[FILTER_FIRSTNAME].length() > user.getFirstName()
                .length())
            || (!user.getFirstName().substring(0,
                filters[FILTER_FIRSTNAME].length()).equalsIgnoreCase(
                filters[FILTER_FIRSTNAME]))) {
          keepit = false;
        }
      }

      if ((filters[FILTER_LASTNAME] != null)
          && (filters[FILTER_LASTNAME].length() > 0)) {
        if ((user.getLastName() == null)
            || (filters[FILTER_LASTNAME].length() > user.getLastName().length())
            || (!user.getLastName().substring(0,
                filters[FILTER_LASTNAME].length()).equalsIgnoreCase(
                filters[FILTER_LASTNAME]))) {
          keepit = false;
        }
      }
      if (keepit) {
        ids.add(user.getId());
      }
    }
    m_Ids = (String[]) ids.toArray(new String[0]);

    // Set search tokens values
    ((PanelSearchEdit) m_SearchTokens[FILTER_FIRSTNAME]).m_Text = getSureString(filters[FILTER_FIRSTNAME]);
    ((PanelSearchEdit) m_SearchTokens[FILTER_LASTNAME]).m_Text = getSureString(filters[FILTER_LASTNAME]);
    verifIndexes();
  }

  public void setAllExperts(Collection allExperts) {
    if (allExperts != null) {
      Iterator itUser = allExperts.iterator();
      UserDetail user;

      m_AllUserDetail.clear();
      while (itUser.hasNext()) {
        user = (UserDetail) itUser.next();
        if (user != null) {
          m_AllUserDetail.put(user.getId(), user);
        }
      }
    }
  }

  public PanelLine getElementInfos(String id) {
    UserDetail theUser = (UserDetail) m_AllUserDetail.get(id);
    String[] theValues;
    PanelLine valret = null;

    SilverTrace.info("questionReply", "ExpertPanel.getElementInfos()",
        "root.GEN_MSG_PARAM_VALUE", "id=" + id);
    theValues = new String[2];
    theValues[COL_LASTNAME] = theUser.getLastName();
    theValues[COL_FIRSTNAME] = theUser.getFirstName();
    valret = new PanelLine(theUser.getId(), theValues, false);
    return valret;
  }
}
