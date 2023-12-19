/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.whitepages.filters;

import org.silverpeas.components.whitepages.control.CardManager;
import org.silverpeas.components.whitepages.model.Card;
import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Ce filtre a pour effet de contrôler que l'utilisateur courant n'a pas une fiche à remplir dans
 * une instance de whitePages. Si c'est le cas, 2 attributs sont mis en sessions :
 * <ul>
 * <li>RedirectToComponentId : avec le componentId de l'instance pour que le mecanisme de
 * redirection le renvoie sur le composant</li>
 * <li>FicheNonRemplie : avec le componentId de l'instance pour que le filtre mappé sur tous les
 * routers des composants puisse intercepter au besoin et renvoyer sur la fiche.</li>
 * </ul>
 * @author Ludovic Bertin
 */
public class LoginFilter implements Filter {

  public static final String ATTRIBUTE_FORCE_CARD_CREATION = "forceCardCreation";
  public static final String ATTRIBUTE_COMPONENT_ID = "RedirectToComponentId";


  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpSession session = ((HttpServletRequest) request).getSession(true);

    /*
     * Retrieve main session controller
     */
    MainSessionController mainSessionCtrl = (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

    /*
     * If no main session controller, forward user to timeout page
     */
    if (mainSessionCtrl == null) {
      SilverLogger.getLogger(this).warn("Session timeout. New session id: " + session.getId());
      RequestDispatcher dispatcher = request.getRequestDispatcher(
          ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout"));
      dispatcher.forward(request, response);
    }

    /*
     * Retrieves all instances of WhitePages for which user is Reader.
     */
    else {
      String userId = mainSessionCtrl.getUserId();
      retrievesAllWhitePagesInstances(session, userId);

      chain.doFilter(request, response);
    }
  }

  private void retrievesAllWhitePagesInstances(final HttpSession session, final String userId) {
    try {
      CompoSpace[] availableInstances =
          AdministrationServiceProvider.getAdminService().getCompoForUser(userId, "whitePages");

      for (final CompoSpace availableInstance : availableInstances) {
        String instanceId = availableInstance.getComponentId();

        /* Retrieve component */
        ComponentInst instance =
            AdministrationServiceProvider.getAdminService().getComponentInst(instanceId);

        /* Is user is administrator for that instance */
        boolean userIsAdmin = false;
        String[] activeProfiles =
            AdministrationServiceProvider.getAdminService().getCurrentProfiles(userId, instance);
        for (final String activeProfile : activeProfiles) {
          if ("admin".equals(activeProfile)) {
            userIsAdmin = true;
            break;
          }
        }

        /* Is forcedCardFilling parameter turned on */
        String forcedCardFilling = AdministrationServiceProvider.getAdminService()
            .getComponentParameterValue(instanceId, "isForcedCardFilling");
        boolean isForcedCardFilling =
            ((forcedCardFilling != null) && (forcedCardFilling.equals("yes")));

        /*
         * Redirect user if and only if user is no admin and forcedCardFilling parameter turned on
         */
        if (isForcedCardFilling && !userIsAdmin) {
          CardManager cardManager = CardManager.getInstance();
          Card userCard = cardManager.getUserCard(userId, instanceId);
          if ((userCard == null) || (!cardManager.isPublicationClassifiedOnPDC(userCard))) {
            session.setAttribute(ATTRIBUTE_COMPONENT_ID, instanceId);
            session.setAttribute(ATTRIBUTE_FORCE_CARD_CREATION, instanceId);
            break;
          }
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
  }
}
