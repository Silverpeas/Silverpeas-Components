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

package com.stratelia.webactiv.quickinfo.servlets;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.wysiwyg.WysiwygException;
import org.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.quickinfo.control.QuickInfoSessionController;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationSelection;

import javax.ejb.CreateException;
import javax.servlet.http.HttpServletRequest;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

public class QuickInfoRequestRouter extends ComponentRequestRouter<QuickInfoSessionController> {
  private static final long serialVersionUID = 2256481728385587395L;

  @Override
  public String getSessionControlBeanName() {
    return "quickinfo";
  }

  public QuickInfoSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new QuickInfoSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param quickInfo The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, QuickInfoSessionController quickInfo,
      HttpServletRequest request) {
    String destination = null;
    String flag = getFlag(quickInfo.getUserRoles());
    if (flag == null) {
      return null;
    }

    try {
      if (function.startsWith("Main") || function.startsWith("quickInfoUser")
          || function.startsWith("quickInfoPublisher")) {
        Collection<PublicationDetail> infos = null;
        if ("publisher".equals(flag)) {
          infos = quickInfo.getQuickInfos();
          request.setAttribute("infos", infos);
          destination = "/quickinfo/jsp/quickInfoPublisher.jsp";
        } else if ("admin".equals(flag)) {
          infos = quickInfo.getQuickInfos();
          request.setAttribute("infos", infos);
          request.setAttribute("isAdmin", "true");
          destination = "/quickinfo/jsp/quickInfoPublisher.jsp";
        } else {
          infos = quickInfo.getVisibleQuickInfos();
          if (infos == null) {
            infos = new ArrayList<PublicationDetail>();
          }
          Iterator<PublicationDetail> iterator = infos.iterator();
          request.setAttribute("infos", iterator);
          destination = "/quickinfo/jsp/quickInfoUser.jsp";
        }
      } else if (function.startsWith("portlet")) {
        Collection<PublicationDetail> infos = null;
        infos = quickInfo.getVisibleQuickInfos();
        if (infos == null) {
          infos = new ArrayList<PublicationDetail>();
        }
        Iterator<PublicationDetail> iterator = infos.iterator();
        request.setAttribute("infos", iterator);
        destination = "/quickinfo/jsp/portlet.jsp";
      } else if (function.startsWith("quickInfoEdit") || function.startsWith("searchResult")) {
        if ("publisher".equals(flag) || "admin".equals(flag)) {
          String action = request.getParameter("Action");
          PublicationDetail quickInfoDetail = null;
          if (action == null) {
            if (!function.startsWith("searchResult")) {
              action = "Add";
            } else {
              action = "Edit";
            }
          }

          if ("Edit".equals(action)) {
            String id = request.getParameter("Id");
            request.setAttribute("Id", id);
            quickInfo.setPageId(QuickInfoSessionController.PAGE_HEADER);

            quickInfoDetail = quickInfo.getDetail(id);

            request.setAttribute("info", quickInfoDetail);
            destination = "/quickinfo/jsp/quickInfoEdit.jsp";

          } else if ("Add".equals(action)) {
            request.setAttribute("info", null);
            destination = "/quickinfo/jsp/quickInfoEdit.jsp";
          } else if ("ReallyRemove".equals(action)) {
            String id = request.getParameter("Id");
            quickInfo.remove(id);
            destination = getDestination("Main", quickInfo, request);
          } else if ("ReallyAdd".equals(action) || "ReallyUpdate".equals(action)) {
            createOrUpdateQuickInfo(quickInfo, request, action);
            destination = getDestination("quickInfoPublisher", quickInfo, request);
          }
        } else if ("user".equals(flag)) {
          destination = getDestination("quickInfoUser", quickInfo, request);
        } else {
          destination =
              GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
        }
      } else if (function.startsWith("multicopy")) {
        try {
          String paramName, Id;
          PublicationDetail pubDetail;
          PublicationSelection pubSelect;
          @SuppressWarnings("unchecked")
          Enumeration<String> parameters = request.getParameterNames();
          while (parameters.hasMoreElements()) {
            paramName = (String) parameters.nextElement();
            if (paramName.startsWith("selectItem")) {
              Id = request.getParameter(paramName);
              if (Id != null) {
                pubDetail = quickInfo.getDetail(Id);
                pubSelect = new PublicationSelection(pubDetail);
                quickInfo.addClipboardSelection((ClipboardSelection) pubSelect);
              }
            }
          }
        } catch (Exception e) {
          try {
            quickInfo.setClipboardError("copyError", e);
            // SilverTrace : mettre un warning
          } catch (Exception ee) {
            SilverTrace.error("quickinfo", "QuickInfoRequestRouter.getDestination()",
                "quickinfo.NO_DATATOCOPY", ee);
          }
        }
        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD, null, null)
            + "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.startsWith("copy")) {
        try {
          PublicationDetail pubDetail = quickInfo.getDetail(request.getParameter("Id"));
          PublicationSelection pubSelect = new PublicationSelection(pubDetail);
          quickInfo.addClipboardSelection((ClipboardSelection) pubSelect);
        } catch (Exception e) {
          try {
            quickInfo.setClipboardError("copyError", e);
            // SilverTrace : mettre un warning
          } catch (Exception ee) {
            SilverTrace.error("quickinfo", "QuickInfoRequestRouter.getDestination()",
                "quickinfo.NO_DATATOCOPY", ee);
          }
        }
        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD, null, null)
            + "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.startsWith("paste")) {
        try {
          SilverTrace.debug("quickinfo", "QuickInfoRequestRouter.getDestination()", "clipboard '"
              + quickInfo.getClipboardName() + "' count=" + quickInfo.getClipboardCount());
          Collection<ClipboardSelection> clipObjects = quickInfo.getClipboardSelectedObjects();
          Iterator<ClipboardSelection> clipObjectIterator = clipObjects.iterator();
          while (clipObjectIterator.hasNext()) {
            ClipboardSelection clipObject = clipObjectIterator.next();
            if ((clipObject != null)
                && (clipObject
                    .isDataFlavorSupported(PublicationSelection.PublicationDetailFlavor))) {
              PublicationDetail pubDetail;
              pubDetail = (PublicationDetail) clipObject
                  .getTransferData(PublicationSelection.PublicationDetailFlavor);

              String description = WysiwygController.load(pubDetail.getPK().getInstanceId(), pubDetail.getPK().getId(), null);

              quickInfo.add(pubDetail.getName(), description, pubDetail.getBeginDate(),
                  pubDetail.getEndDate(), null);
            }
          }
          quickInfo.clipboardPasteDone();
        } catch (Exception e) {
          quickInfo.setClipboardError("pasteError", e);
          SilverTrace.error("quickinfo", "QuickInfoRequestRouter.getDestination()",
              "quickinfo.PASTE_ERROR", e);
        }
        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD, null, null) + "Idle.jsp";
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

  /**
   * This method retrieve all the request parameters before creating or updating a quick info
   * @param quickInfo the QuickInfoSessionController
   * @param request the HttpServletRequest
   * @param action a string representation of an action
   * @throws ParseException
   * @throws RemoteException
   * @throws CreateException
   * @throws WysiwygException
   */
  private void createOrUpdateQuickInfo(QuickInfoSessionController quickInfo,
      HttpServletRequest request, String action) throws ParseException, RemoteException,
      CreateException, WysiwygException {
    String name = request.getParameter("Name");
    String description = request.getParameter("Description");
    if (description == null) {
      description = "";
    }
    Date beginDate = null;
    String beginString = request.getParameter("BeginDate");

    if (StringUtil.isDefined(beginString) && beginString.trim().length() > 0) {
      beginDate = DateUtil.stringToDate(beginString, quickInfo.getLanguage());
    }

    Date endDate = null;
    String endString = request.getParameter("EndDate");
    if (StringUtil.isDefined(endString) && endString.trim().length() > 0) {
      endDate = DateUtil.stringToDate(endString, quickInfo.getLanguage());
    }

    if ("ReallyAdd".equals(action)) {
      String positions = request.getParameter("Positions");
      quickInfo.add(name, description, beginDate, endDate, positions);
    } else {
      String id = request.getParameter("Id");
      quickInfo.update(id, name, description, beginDate, endDate);
    }
  }

  public String getFlag(String[] profiles) {
    String flag = "user";
    for (int i = 0; i < profiles.length; i++) {
      // if admin, return it, we won't find a better profile
      if (profiles[i].equals("admin")) {
        return profiles[i];
      }
      if (profiles[i].equals("publisher")) {
        flag = profiles[i];
      }
    }
    return flag;
  }
}