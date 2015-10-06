/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.whitePages.filters;

import com.silverpeas.whitePages.control.CardManager;
import com.silverpeas.whitePages.model.Card;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdministrationServiceProvider;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import org.silverpeas.util.ResourceLocator;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
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
  /**
   * Configuration du filtre, permettant de récupérer les paramètres.
   */
  FilterConfig config = null;

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
      SilverTrace.warn("whitePages", "LoginFilter.doFilter", "root.MSG_GEN_SESSION_TIMEOUT",
          "NewSessionId=" + session.getId());
      RequestDispatcher dispatcher = request.getRequestDispatcher(
          ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout"));
      dispatcher.forward(request, response);
    }

    /*
     * Retrieves all instances of WhitePages for which user is Reader.
     */
    else {
      String userId = mainSessionCtrl.getUserId();
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
        SilverTrace.warn("whitePages", "LoginFilter.doFilter", "root.EX_IGNORED", e);
      }

      chain.doFilter(request, response);
    }
  }

  public FilterConfig getFilterConfig() {
    return config;
  }

  public void setFilterConfig(FilterConfig arg0) {
  }

  public void init(FilterConfig arg0) {
  }

  public void destroy() {

  }
}
