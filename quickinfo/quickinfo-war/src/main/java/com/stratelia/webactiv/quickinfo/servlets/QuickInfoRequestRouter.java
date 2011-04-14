/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.stratelia.webactiv.quickinfo.servlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.clipboard.control.ejb.ClipboardBm;
import com.stratelia.webactiv.quickinfo.control.QuickInfoSessionController;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationSelection;
import com.silverpeas.util.clipboard.ClipboardSelection;

public class QuickInfoRequestRouter extends ComponentRequestRouter {
  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "quickinfo";
  }

  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    ComponentSessionController component =
        (ComponentSessionController) new QuickInfoSessionController(
            mainSessionCtrl, componentContext);
    return component;
  }

  private void setGlobalInfo(QuickInfoSessionController quickInfo,
      HttpServletRequest request) {
    ResourceLocator settings = quickInfo.getSettings();
    ResourceLocator messages = quickInfo.getMessage();

    request.setAttribute("settings", settings);
    request.setAttribute("messages", messages);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = null;

    QuickInfoSessionController quickInfo = (QuickInfoSessionController) componentSC;

    String flag = getFlag(quickInfo.getUserRoles());
    if (flag == null)
      return null;

    try {
      if ((function.startsWith("Main"))
          || (function.startsWith("quickInfoUser"))
          || (function.startsWith("quickInfoPublisher"))) {
        Collection infos = null;
        setGlobalInfo(quickInfo, request);
        if (flag.equals("publisher")) {
          infos = quickInfo.getQuickInfos();
          request.setAttribute("infos", infos);
          destination = "/quickinfo/jsp/quickInfoPublisher.jsp";
        } else if (flag.equals("admin")) {
          infos = quickInfo.getQuickInfos();
          request.setAttribute("infos", infos);
          request.setAttribute("isAdmin", "true");
          destination = "/quickinfo/jsp/quickInfoPublisher.jsp";
        } else {
          infos = quickInfo.getVisibleQuickInfos();
          if (infos == null)
            infos = new ArrayList();
          Iterator iterator = infos.iterator();
          request.setAttribute("infos", iterator);
          destination = "/quickinfo/jsp/quickInfoUser.jsp";
        }
      } else if (function.startsWith("portlet")) {
        Collection infos = null;
        infos = quickInfo.getVisibleQuickInfos();
        if (infos == null)
          infos = new ArrayList();
        Iterator iterator = infos.iterator();
        request.setAttribute("infos", iterator);
        setGlobalInfo(quickInfo, request);
        destination = "/quickinfo/jsp/portlet.jsp";
      } else if (function.startsWith("quickInfoEdit")
          || function.startsWith("searchResult")) {
        if (flag.equals("publisher") || flag.equals("admin")) {
          String action = request.getParameter("Action");
          PublicationDetail quickInfoDetail = null;
          if (action == null) {
            if (!function.startsWith("searchResult")) {
              action = "Add";
            } else {
              action = "Edit";
            }
          }

          if (action.equals("Edit")) {
            setGlobalInfo(quickInfo, request);
            String id = request.getParameter("Id");
            request.setAttribute("Id", id);
            quickInfo.setPageId(QuickInfoSessionController.PAGE_HEADER);

            quickInfoDetail = quickInfo.getDetail(id);

            request.setAttribute("info", quickInfoDetail);
            destination = "/quickinfo/jsp/quickInfoEdit.jsp";

          } else if (action.equals("changePage")) {
            setGlobalInfo(quickInfo, request);
            String id = request.getParameter("Id");
            request.setAttribute("Id", id);
            quickInfoDetail = quickInfo.getDetail(id);
            request.setAttribute("info", quickInfoDetail);

            String strPageId = request.getParameter("page");
            int pageId = QuickInfoSessionController.PAGE_HEADER;
            if (strPageId != null && !strPageId.equals("")) {
              pageId = Integer.parseInt(strPageId);
            }
            if (!quickInfo.isPdcUsed()) {
              pageId = QuickInfoSessionController.PAGE_HEADER;
            }
            quickInfo.setPageId(pageId);
            if (pageId == QuickInfoSessionController.PAGE_HEADER) {
              destination = "/quickinfo/jsp/quickInfoEdit.jsp";
            } else {
              destination = "/quickinfo/jsp/pdcPositions.jsp";
            }

          } else if (action.equals("Add")) {
            setGlobalInfo(quickInfo, request);
            request.setAttribute("info", null);
            destination = "/quickinfo/jsp/quickInfoEdit.jsp";
          } else if (action.equals("ReallyRemove")) {
            String id = request.getParameter("Id");
            quickInfo.remove(id);
            destination = getDestination("Main", componentSC, request);
          } else if ((action.equals("ReallyAdd"))
              || (action.equals("ReallyUpdate"))) {
            String name = request.getParameter("Name");
            String description = request.getParameter("Description");
            if (description == null)
              description = "";
            Date beginDate = null;
            String beginString = request.getParameter("BeginDate");

            if (beginString.trim().length() > 0)
              beginDate = DateUtil.stringToDate(beginString, quickInfo
                  .getLanguage());

            Date endDate = null;
            String endString = request.getParameter("EndDate");
            if (endString.trim().length() > 0)
              endDate = DateUtil.stringToDate(endString, quickInfo
                  .getLanguage());

            if (action.equals("ReallyAdd")) {
              quickInfo.add(name, description, beginDate, endDate);
            } else {
              String id = request.getParameter("Id");
              quickInfo.update(id, name, description, beginDate, endDate);
            }
            destination = getDestination("quickInfoPublisher", componentSC,
                request);
          }
        } else if (flag.equals("user")) {
          destination = getDestination("quickInfoUser", componentSC, request);
        } else {
          destination = GeneralPropertiesManager.getGeneralResourceLocator()
              .getString("sessionTimeout");
        }
      } else if (function.startsWith("multicopy")) {
        try {
          String paramName, Id;
          PublicationDetail pubDetail;
          PublicationSelection pubSelect;
          Enumeration parameters = request.getParameterNames();
          while (parameters.hasMoreElements()) {
            paramName = (String) parameters.nextElement();
            if (paramName.startsWith("selectItem")) {
              Id = request.getParameter(paramName);
              if (Id != null) {
                pubDetail = ((QuickInfoSessionController) componentSC)
                    .getDetail(Id);
                pubSelect = new PublicationSelection(pubDetail);
                componentSC.addClipboardSelection((ClipboardSelection) pubSelect);
              }
            }
          }
        } catch (Exception e) {
          try {
            componentSC.setClipboardError("copyError", e);
            // SilverTrace : mettre un warning
          } catch (Exception ee) {
            SilverTrace.error("quickinfo",
                "QuickInfoRequestRouter.getDestination()",
                "quickinfo.NO_DATATOCOPY", ee);
          }
        }
        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD)
            + "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.startsWith("copy")) {
        try {
          PublicationDetail pubDetail = ((QuickInfoSessionController) componentSC)
              .getDetail(request.getParameter("Id"));
          PublicationSelection pubSelect = new PublicationSelection(pubDetail);
          componentSC.addClipboardSelection((ClipboardSelection) pubSelect);
        } catch (Exception e) {
          try {
            componentSC.setClipboardError("copyError", e);
            // SilverTrace : mettre un warning
          } catch (Exception ee) {
            SilverTrace.error("quickinfo",
                "QuickInfoRequestRouter.getDestination()",
                "quickinfo.NO_DATATOCOPY", ee);
          }
        }
        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD)
            + "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.startsWith("paste")) {
        try {
          SilverTrace.debug("quickinfo",
              "QuickInfoRequestRouter.getDestination()", "clipboard '"
              + componentSC.getClipboardName() + "' count="
              + componentSC.getClipboardCount());
          Collection clipObjects = componentSC.getClipboardSelectedObjects();
          Iterator clipObjectIterator = clipObjects.iterator();
          while (clipObjectIterator.hasNext()) {
            ClipboardSelection clipObject = (ClipboardSelection) clipObjectIterator
                .next();
            if ((clipObject != null)
                && (clipObject
                .isDataFlavorSupported(PublicationSelection.PublicationDetailFlavor))) {
              PublicationDetail pubDetail;
              pubDetail = (PublicationDetail) clipObject
                  .getTransferData(PublicationSelection.PublicationDetailFlavor);

              String description = WysiwygController.load(pubDetail.getPK()
                  .getInstanceId(), pubDetail.getPK().getId(), null);

              ((QuickInfoSessionController) componentSC).add(pubDetail
                  .getName(), description, pubDetail.getBeginDate(), pubDetail
                  .getEndDate());
            }
          }
          componentSC.clipboardPasteDone();
        } catch (Exception e) {
          componentSC.setClipboardError("pasteError", e);
          SilverTrace.error("quickinfo",
              "QuickInfoRequestRouter.getDestination()",
              "quickinfo.PASTE_ERROR", e);
        }
        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD) + "Idle.jsp";
      } else
        destination = "/quickinfo/jsp/" + function;
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpage.jsp";
    }
    SilverTrace.info("quickinfo", "QuickInfoRequestRooter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "destination" + destination);
    return destination;
  }

  public String getFlag(String[] profiles) {
    String flag = "user";
    for (int i = 0; i < profiles.length; i++) {
      // if admin, return it, we won't find a better profile
      if (profiles[i].equals("admin"))
        return profiles[i];
      if (profiles[i].equals("publisher"))
        flag = profiles[i];
    }
    return flag;
  }

}