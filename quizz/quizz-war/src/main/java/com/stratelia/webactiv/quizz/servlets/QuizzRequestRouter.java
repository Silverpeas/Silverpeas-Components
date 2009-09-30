/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.quizz.servlets;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.quizz.control.QuizzSessionController;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/*
 * CVS Informations
 * 
 * $Id: QuizzRequestRouter.java,v 1.6 2008/10/15 07:40:55 neysseri Exp $
 * 
 * $Log: QuizzRequestRouter.java,v $
 * Revision 1.6  2008/10/15 07:40:55  neysseri
 * Accès via permalien permettait de répondre à un quizz auquel l'utilisateur avait déjà répondu
 *
 * Revision 1.5  2007/07/04 15:30:04  sfariello
 * Ajout Permalien sur les quizz
 *
 * Revision 1.4  2005/09/30 14:20:14  neysseri
 * Centralisation de la gestion des dates
 *
 * Revision 1.3  2002/12/18 15:14:25  scotte
 * Correction erreurs portlet : Index out of bounds qd un publieur accede a un quizz en portlet,
 * PB de forward du component/space ID en portlet
 *
 * Revision 1.2  2002/12/02 12:43:12  neysseri
 * Quizz In PDC merging
 *
 * Revision 1.1.1.1.16.1  2002/11/29 15:06:53  pbialevich
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:48:01  nchaix
 * no message
 *
 * Revision 1.4  2002/05/17 15:09:55  nchaix
 * Merge de la branche bug001 sur la branche principale
 *
 * Revision 1.3.4.1  2002/04/25 06:52:57  santonio
 * portlétisation
 *
 * Revision 1.3  2002/01/22 17:24:17  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 * Centralisation de getFlag du Router renommé getUserRoleLevel dans SC
 * 
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class QuizzRequestRouter extends ComponentRequestRouter {

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for quizz, returns "quizz"
   */
  public String getSessionControlBeanName() {
    return "quizz";
  }

  /**
   * Method declaration
   * 
   * 
   * @param mainSessionCtrl
   * @param componentContext
   * 
   * @return
   * 
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    ComponentSessionController component = (ComponentSessionController) new QuizzSessionController(
        mainSessionCtrl, componentContext);

    return component;
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @param request
   *          The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   *         "/quizz/jsp/quizz.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    SilverTrace.info("Quizz", "QuizzRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", function);
    QuizzSessionController quizzSC = (QuizzSessionController) componentSC;
    String destination = "";

    try {
      boolean profileError = false;
      if (function.startsWith("Main")) {
        // the flag is the best user's profile
        String flag = componentSC.getUserRoleLevel();
        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = "quizzAdmin.jsp";
        } else {
          destination = "quizzUser.jsp";
        }
      } else if (function.startsWith("portlet")) {
        String flag = componentSC.getUserRoleLevel();
        if ("publisher".equals(flag) || "admin".equals(flag))
          destination = "quizzPortlet.jsp";
        else
          destination = "quizzUserPortlet.jsp";
      } else if (function.startsWith("quizzCreator")) {
        String flag = componentSC.getUserRoleLevel();

        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = "quizzCreator.jsp";
        } else {
          profileError = true;
        }
      } else if (function.startsWith("searchResult")) {
        String flag = componentSC.getUserRoleLevel();
        String id = request.getParameter("Id");

        SilverTrace.info("Quizz", "QuizzRequestRouter.getDestination()", "",
            "id = " + id);

        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = "quizzQuestionsNew.jsp?Action=ViewQuizz&QuizzId=" + id;
        } else {
          if (quizzSC.isParticipationAllowed(id))
            destination = "quizzQuestionsNew.jsp?Action=ViewCurrentQuestions&QuizzId="
                + id;
          else
            destination = "quizzResultUser.jsp";
        }
      } else {
        destination = function;
      }

      if (profileError) {
        String sessionTimeout = GeneralPropertiesManager
            .getGeneralResourceLocator().getString("sessionTimeout");

        destination = sessionTimeout;
      } else {
        destination = "/quizz/jsp/" + destination;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpage.jsp";
    }

    return destination;
  }

}
