package com.stratelia.silverpeas.pdcPeas.servlets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.whitePages.control.CardManager;
import com.silverpeas.whitePages.model.Card;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchUserWrapperSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A simple wrapper for the userpanel.
 */
public class PdcSearchUserWrapper extends ComponentRequestRouter {
  /**
   * Returns a new session controller
   */
  public ComponentSessionController createComponentSessionController(
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
      ComponentSessionController componentSC, HttpServletRequest request) {
    SilverTrace.info("pdcPeas", "PdcSearchUserWrapper.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", " Function=" + function);

    PdcSearchUserWrapperSessionController pdcSearchUserWrapperScc = (PdcSearchUserWrapperSessionController) componentSC;

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

        List users = pdcSearchUserWrapperScc.getSelectedUsers();
        if (users != null) {
          GlobalSilverContent gsc = null;
          StringBuffer ids = new StringBuffer("");
          StringBuffer names = new StringBuffer("");
          String userCardId = null;
          String userId = null;
          CardManager cardM = CardManager.getInstance();
          Card card = null;

          for (int i = 0; i < users.size(); i++) {
            gsc = (GlobalSilverContent) users.get(i);
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
        }

        else {
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
