/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.questionReply.control;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelProvider;
import com.stratelia.silverpeas.genericPanel.PanelSearchEdit;
import com.stratelia.silverpeas.genericPanel.PanelSearchToken;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExpertPanel extends PanelProvider {
  protected static final int FILTER_LASTNAME = 0;
  protected static final int FILTER_FIRSTNAME = 1;

  protected static final int COL_LASTNAME = 0;
  protected static final int COL_FIRSTNAME = 1;

  protected static final OrganizationController organizationController = new OrganizationController();

  protected Map<String, UserDetail> allUserDetails = new HashMap<String, UserDetail>();

  public ExpertPanel(String language, Collection<UserDetail> allExperts) {
    initAll(language, allExperts);
  }

  public void initAll(String lang, Collection<UserDetail> allExperts) {
    String[] filters = new String[2];
    setAllExperts(allExperts);
    // Set the language
    this.language = lang;
    ResourceLocator message = GeneralPropertiesManager.getGeneralMultilang(language);
    // Set the resource locator for columns header
    resourceLocator = new ResourceLocator("com.silverpeas.questionReply.multilang.questionReplyBundle",
        language);
    // Set the Page name
    pageName = resourceLocator.getString("questionReply.experts");
    pageSubTitle = resourceLocator.getString("questionReply.experts");
    // Set column headers
    columnHeaders = new String[2];
    columnHeaders[COL_LASTNAME] = message.getString("GML.lastName");
    columnHeaders[COL_FIRSTNAME] = message.getString("GML.firstName");
    // Build search tokens
    searchTokens = new PanelSearchToken[2];
    searchTokens[FILTER_LASTNAME] = new PanelSearchEdit(0, message.getString("GML.lastName"), "");
    searchTokens[FILTER_FIRSTNAME] = new PanelSearchEdit(1, message.getString("GML.firstName"), "");
    // Set filters and get Ids
    filters[FILTER_FIRSTNAME] = "";
    filters[FILTER_LASTNAME] = "";
    refresh(filters);
  }

  public void refresh(String[] filters) {
    List<String> currentIds = new ArrayList<String>();
    for (UserDetail user : allUserDetails.values()) {
      boolean keepit = true;
      if (StringUtil.isDefined(filters[FILTER_FIRSTNAME])) {
        if ((user.getFirstName() == null)
            || (filters[FILTER_FIRSTNAME].length() > user.getFirstName().length())
            || (!user.getFirstName().substring(0,
            filters[FILTER_FIRSTNAME].length()).equalsIgnoreCase(
            filters[FILTER_FIRSTNAME]))) {
          keepit = false;
        }
      }

      if (StringUtil.isDefined(filters[FILTER_LASTNAME])) {
        keepit = !(user.getLastName() == null || filters[FILTER_LASTNAME].length() > user.getLastName().length()
            || !user.getLastName().substring(0, filters[FILTER_LASTNAME].length()).equalsIgnoreCase(
            filters[FILTER_LASTNAME]));
      }
      if (keepit) {
        currentIds.add(user.getId());
      }
    }
    ids = currentIds.toArray(new String[currentIds.size()]);

    // Set search tokens values
    ((PanelSearchEdit) searchTokens[FILTER_FIRSTNAME]).m_Text = getSureString(filters[FILTER_FIRSTNAME]);
    ((PanelSearchEdit) searchTokens[FILTER_LASTNAME]).m_Text = getSureString(filters[FILTER_LASTNAME]);
    verifIndexes();
  }

  public void setAllExperts(Collection<UserDetail> allExperts) {
    if (allExperts != null) {
      allUserDetails.clear();
      for (UserDetail user : allExperts) {
        if (user != null) {
          allUserDetails.put(user.getId(), user);
        }
      }
    }
  }

  public PanelLine getElementInfos(String id) {
    UserDetail theUser = allUserDetails.get(id);
    SilverTrace.info("questionReply", "ExpertPanel.getElementInfos()",
        "root.GEN_MSG_PARAM_VALUE", "id=" + id);
    String[] theValues = new String[2];
    theValues[COL_LASTNAME] = theUser.getLastName();
    theValues[COL_FIRSTNAME] = theUser.getFirstName();
    return new PanelLine(theUser.getId(), theValues, false);
  }
}
