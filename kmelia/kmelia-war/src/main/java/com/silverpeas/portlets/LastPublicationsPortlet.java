package com.silverpeas.portlets;

import java.io.IOException;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.kmelia.KmeliaTransversal;

public class LastPublicationsPortlet extends GenericPortlet implements
    FormNames {

  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    PortletSession session = request.getPortletSession();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session
        .getAttribute("SilverSessionController",
            PortletSession.APPLICATION_SCOPE);

    String spaceId = (String) session.getAttribute(
        "Silverpeas_Portlet_SpaceId", PortletSession.APPLICATION_SCOPE);

    PortletPreferences pref = request.getPreferences();
    int nbPublis = Integer.parseInt(pref.getValue("nbPublis", "5"));

    KmeliaTransversal kmeliaTransversal = new KmeliaTransversal(
        m_MainSessionCtrl);
    List publications = kmeliaTransversal.getPublications(spaceId, nbPublis);

    request.setAttribute("Publications", publications);

    include(request, response, "portlet.jsp");
  }

  public void doEdit(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "edit.jsp");
  }

  /** Include "help" JSP. */
  public void doHelp(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "help.jsp");
  }

  /** Include a page. */
  private void include(RenderRequest request, RenderResponse response,
      String pageName) throws PortletException {
    response.setContentType(request.getResponseContentType());
    if (!StringUtil.isDefined(pageName)) {
      // assert
      throw new NullPointerException("null or empty page name");
    }
    try {
      PortletRequestDispatcher dispatcher = getPortletContext()
          .getRequestDispatcher("/portlets/jsp/lastPublications/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }

  /*
   * Process Action.
   */
  public void processAction(ActionRequest request, ActionResponse response)
      throws PortletException {
    if (request.getParameter(SUBMIT_FINISHED) != null) {
      //
      // handle "finished" button on edit page
      // return to view mode
      //
      processEditFinishedAction(request, response);
    } else if (request.getParameter(SUBMIT_CANCEL) != null) {
      //
      // handle "cancel" button on edit page
      // return to view mode
      //
      processEditCancelAction(request, response);
    }
  }

  /*
   * Process the "cancel" action for the edit page.
   */
  private void processEditCancelAction(ActionRequest request,
      ActionResponse response) throws PortletException {
    response.setPortletMode(PortletMode.VIEW);
  }

  /*
   * Process the "finished" action for the edit page. Set the "url" to the value
   * specified in the edit page.
   */
  private void processEditFinishedAction(ActionRequest request,
      ActionResponse response) throws PortletException {
    String nbPublis = request.getParameter(TEXTBOX_NB_ITEMS);
    String displayDescription = request.getParameter("displayDescription");

    // Check if it is a number
    try {
      int nb = Integer.parseInt(nbPublis);

      if (nb < 0 || nb > 30)
        throw new NumberFormatException();

      // store preference
      PortletPreferences pref = request.getPreferences();
      try {
        pref.setValue("nbPublis", nbPublis);
        pref.setValue("displayDescription", displayDescription);
        pref.store();
      } catch (ValidatorException ve) {
        getPortletContext().log("could not set nbPublis", ve);
        throw new PortletException("IFramePortlet.processEditFinishedAction",
            ve);
      } catch (IOException ioe) {
        getPortletContext().log("could not set nbPublis", ioe);
        throw new PortletException("IFramePortlet.prcoessEditFinishedAction",
            ioe);
      }
      response.setPortletMode(PortletMode.VIEW);

    } catch (NumberFormatException e) {
      response.setRenderParameter(ERROR_BAD_VALUE, "true");
      response.setPortletMode(PortletMode.EDIT);
    }
  }
}
