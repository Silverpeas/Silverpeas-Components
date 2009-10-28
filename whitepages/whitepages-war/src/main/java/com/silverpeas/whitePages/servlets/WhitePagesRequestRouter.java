/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
---*/

package com.silverpeas.whitePages.servlets;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.util.StringUtil;
import com.silverpeas.whitePages.control.WhitePagesSessionController;
import com.silverpeas.whitePages.filters.LoginFilter;
import com.silverpeas.whitePages.model.Card;
import com.silverpeas.whitePages.record.UserRecord;
import com.stratelia.silverpeas.containerManager.ContainerContext;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * Expert Locator request rooter.
 */
public class WhitePagesRequestRouter extends ComponentRequestRouter {

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for whitePages, returns "whitePages"
   */
  public String getSessionControlBeanName() {
    SilverTrace.info("whitePages",
        "WhitePagesRequestRouter.getSessionControlBeanName()", "", "");
    return "whitePages";
  }

  /*
   * (non-Javadoc)
   *
   * @seecom.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter#
   * createComponentSessionController
   * (com.stratelia.silverpeas.peasCore.MainSessionController,
   * com.stratelia.silverpeas.peasCore.ComponentContext)
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    SilverTrace.info("whitePages",
        "WhitePagesRequestRouter.createComponentSessionController()", "", "");
    ComponentSessionController component = (ComponentSessionController) new WhitePagesSessionController(
        mainSessionCtrl, componentContext,
        "com.silverpeas.whitePages.multilang.whitePagesBundle",
        "com.silverpeas.whitePages.settings.whitePagesIcons");

    return component;
  }

  /*
   * (non-Javadoc)
   *
   * @seecom.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter#
   * getDestination(java.lang.String,
   * com.stratelia.silverpeas.peasCore.ComponentSessionController,
   * javax.servlet.http.HttpServletRequest)
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    SilverTrace.info("whitePages", "WhitePagesRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", function);
    String destination = "";

    /*
     * Cast to WhitePages Session Controller.
     */
    WhitePagesSessionController scc = (WhitePagesSessionController) componentSC;

    /*
     * Retrieves user highest role and store information in request.
     */
    String flag = scc.getUserRoleLevel();
    request.setAttribute("isAdmin", Boolean.valueOf(flag.equals("admin")));

    try {

      /*
       * Extracts container context.
       */
      resetContainerContext(scc, request);
      resetReturnURL(scc, request);

      /*
       * Default page.
       */
      if (function.startsWith("Main")) {
        request.setAttribute("FirstVisite", "0");
        if (flag.equals("admin") || flag.equals("user")) {
          scc.initCurrentUserCards(); // re init combo des fiches

          /*
           * If user has been redirected to this page, he must create his own
           * card, so user is redirected to his own card creation (excepted if
           * card already exists).
           */
          HttpSession session = request.getSession(true);
          if ((session.getAttribute(LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION) != null)) {
            destination = getDestination("ForceCardCreation", componentSC,
                request);
          }

          /*
           * else redirects to card list
           */
          else {
            destination = getDestination("search", componentSC, request);
          }
        } else
          destination = "/admin/jsp/errorpage.jsp";
      }

      /*
       * User is forced to create his own card and classify it.
       */
      else if (function.startsWith("ForceCardCreation")) {
        if (flag.equals("admin") || flag.equals("user")) {
          scc.initCurrentUserCards(); // re init combo des fiches

          /*
           * 1st case : user card has not been created
           */
          if (!scc.existCard(scc.getUserId())) {
            destination = getDestination("createIdentity", componentSC, request);

          }

          /*
           * 2nd case : user card is not classified
           */
          else if (!scc.isCardClassifiedOnPdc()) {
            request.setAttribute("FirstVisite", "1");
            destination = getDestination("ViewPdcPositions", componentSC,
                request);
          }

          /*
           * Else user should not have been redirected
           */
          else {
            request.getSession(true).removeAttribute(
                LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION);
            destination = getDestination("Main", componentSC, request);
          }
        } else
          destination = "/admin/jsp/errorpage.jsp";
      }

      /*
       * Card list.
       */
      else if (function.equals("search")) {
        request.setAttribute("IsEmailHidden", scc.isEmailHidden());

        if (flag.equals("admin")) {
          request.setAttribute("listCards", scc.getCards());
          destination = "/whitePages/jsp/listCards.jsp";
        } else if (flag.equals("user")) {
          request.setAttribute("listCards", scc.getVisibleCards());
          destination = "/whitePages/jsp/listCardsUser.jsp";
        } else
          destination = "/admin/jsp/errorpage.jsp";
      }

      /*
       * New card (administrator only) : redirects to user panel.
       */
      else if (function.equals("createQuery")) {
        if (flag.equals("admin")) {
          destination = scc.initUserPanel();
        } else
          destination = "/admin/jsp/errorpage.jsp";
      }

      /*
       * Identity creation form.
       */

      else if (function.equals("createIdentity")) {
        UserDetail user = null;

        /*
         * If forceCardCreation flag is set card is current user's one
         */
        HttpSession session = request.getSession(true);
        boolean ownCard = (session
            .getAttribute(LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION) != null);
        if (ownCard) {
          user = scc.getUserDetail();
        }

        /*
         * else if user is administrator, the card is for the user he has just
         * selected...
         */
        else if (flag.equals("admin")) {
          user = scc.getUserDetailSelected();
        }

        /*
         * ...else user create his own card.
         */
        else {
          user = scc.getUserDetail();
        }

        if ((user == null) || (scc.existCard(user.getId()))) {
          request.setAttribute("user", user);
          destination = "/whitePages/jsp/errorSelectUser.jsp";
        } else {
          // création Card à partir d'un UserDetail et set de ViewIdentityForm,
          // currentCard en session
          Card card = scc.createCard(user);
          if (card == null) {
            destination = "/whitePages/jsp/errorIdentity.jsp";
          } else {
            card = scc.setCardRecord();
            Form updateForm = card.readCardUpdateForm();
            PagesContext context = new PagesContext("myForm", "1", scc
                .getLanguage());
            context.setComponentId(card.getInstanceId());
            context.setObjectId(card.getPK().getId());
            context.setUserId(scc.getUserId());

            DataRecord data = card.readCardRecord();

            // set dataRecord vide de CurrentCard et updateForm et viewForm
            request.setAttribute("card", card);
            request.setAttribute("whitePagesCards", scc.getCurrentUserCards());
            request.setAttribute("Form", updateForm);
            request.setAttribute("context", context);
            request.setAttribute("data", data);
            destination = "/whitePages/jsp/createCard.jsp";
          }
        }
      }
      /*
       * Card creation form.
       */
      else if (function.equals("createCard")) {
        Card card = scc.setCardRecord();
        Form updateForm = card.readCardUpdateForm();
        PagesContext context = new PagesContext("myForm", "1", scc
            .getLanguage());
        context.setComponentId(card.getInstanceId());
        context.setObjectId(card.getPK().getId());
        context.setUserId(scc.getUserId());
        DataRecord data = card.readCardRecord();

        // set dataRecord vide de CurrentCard et updateForm et viewForm
        request.setAttribute("card", card);
        request.setAttribute("whitePagesCards", scc.getCurrentUserCards());
        request.setAttribute("Form", updateForm);
        request.setAttribute("context", context);
        request.setAttribute("data", data);
        destination = "/whitePages/jsp/createCard.jsp";
      }

      /*
       * Persistence of new card information.
       */
      else if (function.equals("effectiveCreate")) {
        /*
         * Stores card, identity and data record.
         */
        scc.insertCard();

        /*
         * Updates record object with new values.
         */
        scc.setCardRecord(request);
        scc.saveCard();

        /*
         * If user has been forced to create his own card and done it, removes
         * forced redirection.
         */
        // HttpSession session = request.getSession(true);
        // session.removeAttribute(LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION);

        /*
         * Go back to main page.
         */
        destination = getDestination("Main", componentSC, request);
      }

      else if (function.equals("consultIdentity")) {
        String userCardIdString = request.getParameter("userCardId");
        if (!StringUtil.isDefined(userCardIdString)) {
          // on vient de reverseHide
          userCardIdString = (String) request.getAttribute("userCardId");
        }
        if (!StringUtil.isDefined(userCardIdString)) {
          // on vient de pdc search
          userCardIdString = request.getParameter("documentId");
        }
        if (!StringUtil.isDefined(userCardIdString)) {
          // on vient de search
          userCardIdString = request.getParameter("id");
        }
        long userCardId = new Long(userCardIdString).longValue();
        Card card = scc.getCard(userCardId);

        /*
         * Owners and administrators only can edit user identity.
         */
        if ((flag.equals("admin"))
            || (card != null && card.getUserId().equals(scc.getUserId()))) {

          if (card == null) // user supprimé par appletAdmin
          {
            destination = "/whitePages/jsp/errorIdentity.jsp";
          } else {
            Form userForm = card.readUserForm();
            PagesContext context = new PagesContext("myForm", "0", scc
                .getLanguage());
            context.setComponentId(card.getInstanceId());
            context.setObjectId(card.getPK().getId());
            context.setUserId(scc.getUserId());
            UserRecord data = card.readUserRecord();
            request.setAttribute("contentId", scc.getCurrentCardContentId());

            // retourne un objet Card contenant les infos de la fiche y compris
            // le form et le dataRecord.
            request.setAttribute("card", card);
            request.setAttribute("whitePagesCards", scc.getCurrentUserCards());
            request.setAttribute("Form", userForm);
            request.setAttribute("context", context);
            request.setAttribute("data", data);
            destination = "/whitePages/jsp/consultIdentity.jsp";
          }
        } else if (flag.equals("user")) {
          destination = getDestination("viewIdentity", componentSC, request);
        } else
          destination = "/admin/jsp/errorpage.jsp";
      }

      else if (function.equals("consultCard")) {
        String userCardIdString = request.getParameter("userCardId");
        if (!StringUtil.isDefined(userCardIdString)) {
          // on vient de reverseHide
          userCardIdString = (String) request.getAttribute("userCardId");
        }

        long userCardId = new Long(userCardIdString).longValue();
        Card card = scc.getCard(userCardId);

        /*
         * Owners and administrators only can edit a card.
         */
        if (flag.equals("admin")
            || (card != null && card.getUserId().equals(scc.getUserId()))) {

          if (card == null) {
            destination = "/whitePages/jsp/errorIdentity.jsp";
          } else {
            Form viewForm = card.readCardViewForm();
            PagesContext context = new PagesContext("myForm", "0", scc
                .getLanguage());
            context.setComponentId(card.getInstanceId());
            context.setObjectId(card.getPK().getId());
            context.setUserId(scc.getUserId());
            DataRecord data = card.readCardRecord();

            request.setAttribute("card", card);
            request.setAttribute("whitePagesCards", scc.getCurrentUserCards());
            request.setAttribute("Form", viewForm);
            request.setAttribute("context", context);
            request.setAttribute("data", data);
            request.setAttribute("contentId", scc.getCurrentCardContentId());

            destination = "/whitePages/jsp/consultCard.jsp";
          }
        } else
          destination = "/admin/jsp/errorpage.jsp";
      }

      else if (function.equals("updateCard")) {
        String userCardIdString = request.getParameter("userCardId");

        long userCardId = new Long(userCardIdString).longValue();
        Card card = scc.getCard(userCardId);
        if (card == null) {
          destination = "/whitePages/jsp/errorIdentity.jsp";
        }

        else if ((flag.equals("admin"))
            || (card.getUserId().equals(scc.getUserId()))) {
          // retourne un objet Card contenant les infos de la fiche y compris le
          // form et le dataRecord.
          Form updateForm = card.readCardUpdateForm();
          PagesContext context = new PagesContext("myForm", "0", scc
              .getLanguage());
          context.setComponentId(card.getInstanceId());
          context.setObjectId(card.getPK().getId());
          context.setUserId(scc.getUserId());
          DataRecord data = card.readCardRecord();

          request.setAttribute("card", card);
          request.setAttribute("whitePagesCards", scc.getCurrentUserCards());
          request.setAttribute("Form", updateForm);
          request.setAttribute("context", context);
          request.setAttribute("data", data);
          destination = "/whitePages/jsp/updateCard.jsp";
        } else
          destination = "/admin/jsp/errorpage.jsp";
      }

      else if (function.equals("effectiveUpdate")) {
        scc.updateCardRecord(request);
        scc.saveCard();
        destination = "/whitePages/jsp/closeUpdate.jsp?userCardId="
            + request.getParameter("userCardId");
      }

      else if (function.equals("reverseHide")) {
        if (flag.equals("admin")) {
          String retour = request.getParameter("returnPage"); // consultIdentity
          // ou consultCard
          String userCardIdString = request.getParameter("userCardId");
          ArrayList arrayCards = new ArrayList();
          arrayCards.add(userCardIdString);
          scc.reverseHide(arrayCards);

          request.setAttribute("userCardId", userCardIdString);
          destination = getDestination(retour, componentSC, request);
        } else
          destination = "/admin/jsp/errorpage.jsp";
      }

      else if (function.equals("delete")) {
        if (flag.equals("admin")) {
          String[] checkCards = request.getParameterValues("checkedCard");
          ArrayList listToDelete = new ArrayList();
          if (checkCards != null) {
            for (int i = 0; i < checkCards.length; i++) {
              String userCardId = checkCards[i];
              listToDelete.add(userCardId);
            }
          }
          scc.delete(listToDelete);

          destination = getDestination("search", componentSC, request);
        } else
          destination = "/admin/jsp/errorpage.jsp";
      }

      else if (function.equals("hide")) {
        if (flag.equals("admin")) {
          String[] checkCards = request.getParameterValues("checkedCard");
          ArrayList listToMask = new ArrayList();
          if (checkCards != null) {
            for (int i = 0; i < checkCards.length; i++) {
              String userCardId = checkCards[i];
              listToMask.add(userCardId);
            }
          }
          scc.hide(listToMask);
          destination = getDestination("search", componentSC, request);
        } else
          destination = "/admin/jsp/errorpage.jsp";
      }

      else if (function.equals("unHide")) {
        if (flag.equals("admin")) {
          String[] checkCards = request.getParameterValues("checkedCard");
          ArrayList listToUnMask = new ArrayList();
          if (checkCards != null) {
            for (int i = 0; i < checkCards.length; i++) {
              String userCardId = checkCards[i];
              listToUnMask.add(userCardId);
            }
          }
          scc.unHide(listToUnMask);
          destination = getDestination("search", componentSC, request);
        } else
          destination = "/admin/jsp/errorpage.jsp";
      }

      /*
       * Acces externe via d'autres composants ou Acces interne role User
       */
      else if (function.equals("viewIdentity")) {
        String userCardIdString = request.getParameter("userCardId");
        if (!StringUtil.isDefined(userCardIdString)) {
          // on vient de pdc search
          userCardIdString = request.getParameter("documentId");
        }
        if (!StringUtil.isDefined(userCardIdString)) {
          // on vient de search
          userCardIdString = request.getParameter("id");
        }

        String hostComponentName = request.getParameter("HostComponentName");
        String hostUrl = request.getParameter("HostUrl");
        String hostSpaceName = request.getParameter("HostSpaceName");
        String hostPath = request.getParameter("HostPath");

        if (hostComponentName != null)
          scc.setHostParameters(hostSpaceName, hostComponentName, hostUrl,
              hostPath);
        else {
          String hostParameters[] = scc.getHostParameters();
          if (hostParameters != null) {
            hostComponentName = hostParameters[0];
            hostUrl = hostParameters[1];
            hostSpaceName = hostParameters[2];
            hostPath = hostParameters[3];
          }
        }

        Card card = null;
        // interne rôle user
        if (userCardIdString != null) {
          // retourne un objet Card contenant les infos de la fiche y compris le
          // form et le dataRecord.
          long userCardId = new Long(userCardIdString).longValue();
          card = scc.getCardReadOnly(userCardId);

        }
        // externe
        else {
          String userId = request.getParameter("userId");
          SilverTrace.info("whitePages",
              "WhitePagesRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "userId = " + userId);
          card = scc.getUserCard(userId);
        }
        if (card == null) {
          destination = "/whitePages/jsp/errorIdentity.jsp";
        } else {
          Form userForm = card.readUserForm();
          PagesContext context = new PagesContext("myForm", "0", scc
              .getLanguage());
          context.setComponentId(card.getInstanceId());
          context.setObjectId(card.getPK().getId());
          context.setUserId(scc.getUserId());
          UserRecord data = card.readUserRecord();

          // retourne un objet Card contenant les infos de la fiche y compris le
          // form et le dataRecord.
          request.setAttribute("card", card);
          request.setAttribute("whitePagesCards", scc.getCurrentUserCards());
          request.setAttribute("Form", userForm);
          request.setAttribute("context", context);
          request.setAttribute("data", data);
          request.setAttribute("HostSpaceName", hostSpaceName);
          request.setAttribute("HostPath", hostPath);
          request.setAttribute("IsFicheVisible", scc.isFicheVisible());

          destination = "/whitePages/jsp/viewIdentity.jsp";
        }
      }

      else if (function.equals("viewCard")) {
        String userCardIdString = request.getParameter("userCardId");

        String hostComponentName = null;
        String hostUrl = null;
        String hostSpaceName = null;
        String hostPath = null;
        String hostParameters[] = scc.getHostParameters();
        if (hostParameters != null) {
          hostComponentName = hostParameters[0];
          hostUrl = hostParameters[1];
          hostSpaceName = hostParameters[2];
          hostPath = hostParameters[3];
        }

        long userCardId = new Long(userCardIdString).longValue();
        Card card = scc.getCardReadOnly(userCardId);
        if (card == null) {
          destination = "/whitePages/jsp/errorIdentity.jsp";
        } else {
          Form viewForm = card.readCardViewForm();
          PagesContext context = new PagesContext("myForm", "0", scc
              .getLanguage());
          context.setComponentId(card.getInstanceId());
          context.setObjectId(card.getPK().getId());
          context.setUserId(scc.getUserId());
          DataRecord data = card.readCardRecord();

          // retourne un objet Card contenant les infos de la fiche y compris le
          // form et le dataRecord.
          request.setAttribute("card", card);
          request.setAttribute("whitePagesCards", scc.getCurrentUserCards());
          request.setAttribute("Form", viewForm);
          request.setAttribute("context", context);
          request.setAttribute("data", data);
          request.setAttribute("HostComponentName", hostComponentName);
          request.setAttribute("HostUrl", hostUrl);
          request.setAttribute("HostSpaceName", hostSpaceName);
          request.setAttribute("HostPath", hostPath);

          destination = "/whitePages/jsp/viewCard.jsp";
        }
      }

      else if (function.equals("searchResult") || function.equals("Consult")) // pdc
      // search
      // result
      {
        if (flag.equals("admin")) {
          destination = getDestination("consultIdentity", componentSC, request);
        } else {
          destination = getDestination("viewIdentity", componentSC, request);
        }
      }

      else if (function.equals("ViewPdcPositions")) {
        String userCardId = (String) request.getParameter("userCardId");

        request.setAttribute("UserCardId", userCardId);
        // NEWD DLE
        // request.setAttribute("ReturnURL",
        // "/RwhitePages/"+scc.getSpaceId()+"_"+scc.getComponentId()+"/ViewPdcPositions?userCardId="+userCardId);
        request.setAttribute("ReturnURL", "/RwhitePages/"
            + scc.getComponentId() + "/ViewPdcPositions?userCardId="
            + userCardId);
        // NEWF DLE
        request.setAttribute("SilverContentId", new Integer(scc
            .getSilverObjectId(userCardId)).toString());

        // paramètre pour ouvrir directement la création d'une position du pdc
        String firstVisite = "0";
        if (StringUtil.isDefined((String) request.getAttribute("FirstVisite")))
          firstVisite = (String) request.getAttribute("FirstVisite");
        request.setAttribute("FirstVisite", firstVisite);

        destination = "/whitePages/jsp/pdcPositions.jsp";
      }

      /*
       * Expert notification
       */
      else if (function.equals("NotifyExpert")) {
        String cardId = request.getParameter("cardId");

        /*
         * if no card id defined, redirects to main page.
         */
        if (cardId == null)
          return getDestination("Main", scc, request);

        /*
         * Retrieves notified user card and store it in request
         */
        Card card = scc.getCard(Long.parseLong(cardId));
        String expertName = card.readUserRecord().getUserDetail()
            .getDisplayedName();

        scc.setNotifiedUserCard(card);
        request.setAttribute("notifiedExpert", expertName);

        /*
         * Go back to main page.
         */
        destination = "/whitePages/jsp/notifyExpert.jsp";
      }

      /*
       * Send expert notification
       */
      else if (function.equals("sendExpertNotification")) {
        String message = request.getParameter("messageToExpert");
        scc.sendNotification(message);

        return getDestination("Main", scc, request);
      }

      else if (function.equals("portlet")) {
        request.setAttribute("IsEmailHidden", scc.isEmailHidden());

        request.setAttribute("listCards", scc.getVisibleCards());
        destination = "/whitePages/jsp/portlet.jsp";
      }

      else
        destination = "/admin/jsp/errorpage.jsp";

    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpage.jsp";
    }
    SilverTrace.info("whitePages", "WhitePagesRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "destination " + destination);
    return destination;
  }

  /**
   * Extract the container context from the request and save it in the session
   * controller.
   *
   * If this context is null then get the last one from the session controller.
   *
   * So the containerContext is the same in the request and the session.
   */
  private void resetContainerContext(WhitePagesSessionController scc,
      HttpServletRequest request) {
    ContainerContext containerContext = (ContainerContext) request
        .getAttribute("ContainerContext");

    if (containerContext != null) {
      scc.setContainerContext(containerContext);
    } else {
      containerContext = scc.getContainerContext();
      request.setAttribute("ContainerContext", containerContext);
    }
  }

  private void resetReturnURL(WhitePagesSessionController scc,
      HttpServletRequest request) {
    String returnURL = (String) request.getParameter("ReturnURL");

    if (returnURL != null && returnURL.length() > 0) {
      SilverTrace.info("whitePages",
          "WhitePagesRequestRouter.resetReturnURL()",
          "root.MSG_GEN_PARAM_VALUE", "returnURL != null");
      scc.setReturnURL(returnURL);
    } else {
      returnURL = scc.getReturnURL();
    }
    request.setAttribute("ReturnURL", returnURL);
  }
}
