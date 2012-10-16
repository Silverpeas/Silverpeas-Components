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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.pdcPeas.servlets;

import com.silverpeas.whitePages.control.CardManager;
import com.silverpeas.whitePages.model.Card;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchUserWrapperSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * A simple wrapper for the userpanel.
 */
public class PdcSearchUserWrapper
    extends ComponentRequestRouter<PdcSearchUserWrapperSessionController> {

  private static final long serialVersionUID = 3997262296536961121L;

  /**
   * Returns a new session controller
   */
  public PdcSearchUserWrapperSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new PdcSearchUserWrapperSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * Returns the base name for the session controller of this router.
   */
  public String getSessionControlBeanName() {
    return "pdcSearchUserWrapper";
  }

  /**
   * Do the requested function and return the destination url.
   */
  public String getDestination(String function,
      PdcSearchUserWrapperSessionController pdcSearchUserWrapperScc, HttpServletRequest request) {
    SilverTrace.info("pdcPeas", "PdcSearchUserWrapper.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", " Function=" + function);
    try {
      if (function.equals("Open")) {
        pdcSearchUserWrapperScc.setFormName(request.getParameter("formName"));
        pdcSearchUserWrapperScc.setElementId(request.getParameter("elementId"));
        pdcSearchUserWrapperScc.setElementName(request
            .getParameter("elementName"));
        pdcSearchUserWrapperScc.setSelectedUserIds(request
            .getParameter("selectedUsers"));
        pdcSearchUserWrapperScc.initPdcSearchUser(); // selection par défaut
        // d'éléments
        // return
        // "/RpdcSearch/jsp/ToSearchToSelect?ComponentName=expertLocator&ReturnURL=/RpdcSearchUserWrapper/jsp/Close";
        return "/RpdcSearch/jsp/ToSearchToSelect?ComponentName=whitePages&ReturnURL=/RpdcSearchUserWrapper/jsp/Close";
      } else if (function.equals("Close")) {
        pdcSearchUserWrapperScc.getUserSelection();
        request.setAttribute("formName", pdcSearchUserWrapperScc.getFormName());
        request.setAttribute("elementId", pdcSearchUserWrapperScc
            .getElementId());
        request.setAttribute("elementName", pdcSearchUserWrapperScc
            .getElementName());

        List<GlobalSilverContent> users = pdcSearchUserWrapperScc.getSelectedUsers();
        if (users != null) {
          StringBuffer ids = new StringBuffer("");
          StringBuffer names = new StringBuffer("");
          String userCardId = null;
          String userId = null;
          CardManager cardM = CardManager.getInstance();
          Card card = null;

          for (GlobalSilverContent gsc : users) {
            userCardId = gsc.getId();

            card = cardM.getCard(new Long(userCardId).longValue());
            userId = card.getUserId();

            ids.append(userCardId);
            ids.append("-");
            ids.append(userId);
            ids.append(",");

            names.append(gsc.getName());
            names.append(",");
          }

          if (!ids.toString().equals("")) {
            ids = ids.deleteCharAt(ids.length() - 1);
            names = names.deleteCharAt(names.length() - 1);

          }

          request.setAttribute("userIds", ids.toString());
          request.setAttribute("userNames", names.toString());
        } else {
          request.setAttribute("userIds", "");
          request.setAttribute("userNames", "");
        }
        return "/pdcPeas/jsp/closeWrapper.jsp";
      } else {
        return "/RpdcSearch/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
  }
}
