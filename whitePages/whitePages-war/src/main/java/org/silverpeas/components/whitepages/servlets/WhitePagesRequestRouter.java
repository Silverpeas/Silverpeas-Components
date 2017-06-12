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
package org.silverpeas.components.whitepages.servlets;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.components.whitepages.WhitePagesException;
import org.silverpeas.components.whitepages.control.WhitePagesSessionController;
import org.silverpeas.components.whitepages.filters.LoginFilter;
import org.silverpeas.components.whitepages.model.Card;
import org.silverpeas.components.whitepages.model.SearchField;
import org.silverpeas.components.whitepages.model.SearchFieldsType;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * Expert Locator request rooter.
 */
public class WhitePagesRequestRouter extends ComponentRequestRouter<WhitePagesSessionController> {

  private static final long serialVersionUID = 4102577993362198152L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for whitePages, returns "whitePages"
   */
  public String getSessionControlBeanName() {

    return "whitePages";
  }

  public WhitePagesSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new WhitePagesSessionController(mainSessionCtrl, componentContext);
  }

  public String getDestination(String function, WhitePagesSessionController scc,
      HttpRequest request) {
    String destination;

    /*
     * Retrieves user highest role and store information in request.
     */
    String flag = scc.getHighestSilverpeasUserRole().getName();
    request.setAttribute("isAdmin", scc.isAdmin());

    try {
      /*
       * Default page.
       */
      if (function.startsWith("Main")) {
        request.setAttribute("FirstVisite", "0");
        if ("admin".equals(flag) || "user".equals(flag)) {
          // re init combo des fiches
          scc.initCurrentUserCards();
          /*
           * If user has been redirected to this page, he must create his own card, so user is
           * redirected to his own card creation (excepted if card already exists).
           */
          HttpSession session = request.getSession(true);
          if ((session.getAttribute(LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION) != null)) {
            destination = getDestination("ForceCardCreation", scc, request);
          } else {
            //else redirects to card list
            request.setAttribute("Main", "true");
            destination = getDestination("searchInWhitePage", scc, request);
          }
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if (function.startsWith("ForceCardCreation")) {
        //User is forced to create his own card and classify it.
        if ("admin".equals(flag) || "user".equals(flag)) {
          scc.initCurrentUserCards(); // re init combo des fiches

          /*
           * 1st case : user card has not been created
           */
          if (!scc.existCard(scc.getUserId())) {
            destination = getDestination("createIdentity", scc, request);
          } else {
            //Else user should not have been redirected
            request.getSession(true).removeAttribute(LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION);
            destination = getDestination("Main", scc, request);
          }
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("createQuery".equals(function)) {
        // New card (administrator only) : redirects to user panel.
        if (flag.equals("admin")) {
          destination = scc.initUserPanel();
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("createIdentity".equals(function)) {
        // Identity creation form.
        UserDetail user;

        /*
         * If forceCardCreation flag is set card is current user's one
         */
        HttpSession session = request.getSession(true);
        boolean ownCard = (session.getAttribute(LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION) != null);
        if (ownCard) {
          user = scc.getUserDetail();
        } else if (flag.equals("admin")) {
          // else if user is administrator, the card is for the user he has just selected...
          user = scc.getUserDetailSelected();
        } else {
          // ...else user create his own card.
          user = scc.getUserDetail();
        }

        if ((user == null) || (scc.existCard(user.getId()))) {
          request.setAttribute("user", user);
          destination = "/whitePages/jsp/errorSelectUser.jsp";
        } else {
          // création Card à partir d'un UserDetail et set de ViewIdentityForm, currentCard en
          // session
          Card card = scc.createCard(user);
          if (card == null) {
            destination = "/whitePages/jsp/errorIdentity.jsp";
          } else {
            card = scc.setCardRecord();
            Form updateForm = card.readCardUpdateForm();
            PagesContext context = new PagesContext("myForm", "1", scc.getLanguage());
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
      } else if ("createCard".equals(function)) {
        // Card creation form.
        Card card = scc.setCardRecord();
        Form updateForm = card.readCardUpdateForm();
        PagesContext context = new PagesContext("myForm", "1", scc.getLanguage());
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
      } else if ("effectiveCreate".equals(function)) {
        // Persistence of new card information.
        /*
         * Updates record object with new values.
         */
        scc.createCard(request);
        /*
         * Go back to main page.
         */
        destination = getDestination("Main", scc, request);
      } else if ("consultCard".equals(function)) {
        String userCardIdString = request.getParameter("userCardId");
        if (!StringUtil.isDefined(userCardIdString)) {
          // on vient de reverseHide
          userCardIdString = (String) request.getAttribute("userCardId");
        }

        long userCardId = Long.parseLong(userCardIdString);
        Card card = scc.getCard(userCardId);

        /*
         * Owners and administrators only can edit a card.
         */
        if (card == null) {
          destination = "/whitePages/jsp/errorIdentity.jsp";
        } else {
          Form viewForm = card.readCardViewForm();
          PagesContext context = new PagesContext("myForm", "0", scc.getLanguage());
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
          request.setAttribute("userFull", scc.getOrganisationController().getUserFull(card.
              getUserId()));
          request.setAttribute("pdcPositions",
              scc.getPdcPositions(scc.getSilverObjectId(String.valueOf(userCardId))));

          destination = "/whitePages/jsp/consultCard.jsp";
        }
      } else if ("updateCard".equals(function)) {
        String userCardIdString = request.getParameter("userCardId");

        long userCardId = Long.parseLong(userCardIdString);
        Card card = scc.getCard(userCardId);
        if (card == null) {
          destination = "/whitePages/jsp/errorIdentity.jsp";
        } else if (("admin".equals(flag)) || (card.getUserId().equals(scc.getUserId()))) {
          // retourne un objet Card contenant les infos de la fiche y compris le form et le
          // dataRecord.
          Form updateForm = card.readCardUpdateForm();
          PagesContext context = new PagesContext("myForm", "0", scc.getLanguage());
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
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("effectiveUpdate".equals(function)) {
        scc.updateCardRecord(request);
        scc.saveCard();
        destination =
            "/whitePages/jsp/closeUpdate.jsp?userCardId=" + request.getParameter("userCardId");
      } else if ("reverseHide".equals(function)) {
        if ("admin".equals(flag)) {
          // consultIdentity ou consultCard
          String retour = request.getParameter("returnPage");
          String userCardIdString = request.getParameter("userCardId");
          List<String> arrayCards = new ArrayList<>();
          arrayCards.add(userCardIdString);
          scc.reverseHide(arrayCards);

          request.setAttribute("userCardId", userCardIdString);
          destination = getDestination(retour, scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("delete".equals(function)) {
        if ("admin".equals(flag)) {
          String[] checkCards = request.getParameterValues("checkedCard");
          List<String> listToDelete = new ArrayList<>();
          if (checkCards != null) {
            Collections.addAll(listToDelete, checkCards);
          }
          scc.delete(listToDelete);

          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("hide".equals(function)) {
        if ("admin".equals(flag)) {
          String[] checkCards = request.getParameterValues("checkedCard");
          List<String> listToMask = new ArrayList<>();
          if (checkCards != null) {
            Collections.addAll(listToMask, checkCards);
          }
          scc.hide(listToMask);
          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("unHide".equals(function)) {
        if (flag.equals("admin")) {
          String[] checkCards = request.getParameterValues("checkedCard");
          List<String> listToUnMask = new ArrayList<>();
          if (checkCards != null) {
            Collections.addAll(listToUnMask, checkCards);
          }
          scc.unHide(listToUnMask);
          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("searchResult".equals(function) || "consultIdentity".equals(function)) {
        request.setAttribute("userCardId", request.getParameter("Id"));
        destination = getDestination("consultCard", scc, request);
      } else if ("NotifyExpert".equals(function)) {
        // Expert notification
        String cardId = request.getParameter("cardId");
        /*
         * if no card id defined, redirects to main page.
         */
        if (cardId == null) {
          return getDestination("Main", scc, request);
        }

        /*
         * Retrieves notified user card and store it in request
         */
        Card card = scc.getCard(Long.parseLong(cardId));
        String expertName = card.readUserRecord().getUserDetail().getDisplayedName();

        scc.setNotifiedUserCard(card);
        request.setAttribute("notifiedExpert", expertName);

        /*
         * Go back to main page.
         */
        destination = "/whitePages/jsp/notifyExpert.jsp";
      } else if ("sendExpertNotification".equals(function)) {
        // Send expert notification
        String message = request.getParameter("messageToExpert");
        scc.sendNotification(message);

        return getDestination("Main", scc, request);
      } else if ("portlet".equals(function)) {
        request.setAttribute("IsEmailHidden", scc.isEmailHidden());

        request.setAttribute("listCards", scc.getVisibleCards());
        destination = "/whitePages/jsp/portlet.jsp";
      } else if ("searchInWhitePage".equals(function)) {
        boolean isMain = false;
        if (request.getAttribute("Main") != null) {
          isMain = "true".equals(request.getAttribute("Main"));
        }
        if (isMain) {
          // set default cards to see
          if ("admin".equals(flag)) {
            request.setAttribute("cards", scc.getCards());
          } else if ("user".equals(flag)) {
            request.setAttribute("cards", scc.getVisibleCards());
          } else {
            destination = "/admin/jsp/errorpage.jsp";
          }
        } else {
          // get form fields values
          SortedSet<SearchField> fields = scc.getSearchFields();
          request.setAttribute("searchFields", fields);
        }

        destination = "/whitePages/jsp/dynamicSearch.jsp";
      } else if ("dynamicFieldsChoice".equals(function)) {

        List<FieldTemplate> xmlFields = scc.getAllXmlFieldsForSearch();
        request.setAttribute("xmlFields", xmlFields);

        List<SearchField> ldapFields = scc.getLdapAttributesList();
        request.setAttribute("ldapFields", ldapFields);

        Set<String> alreadySelectedFields = scc.getSearchFieldIds();
        request.setAttribute("alreadySelectedFields", alreadySelectedFields);

        destination = "/whitePages/jsp/dynamicFieldsChoice.jsp";
      } else if ("comfirmFieldsChoice".equals(function)) {

        String[] fields = request.getParameterValues("checkedFields");
        if (fields != null && fields.length > 0) {
          scc.confirmFieldsChoice(fields);
        }
        return getDestination("searchInWhitePage", scc, request);
      } else if ("getSearchResult".equals(function)) {

        String query = request.getParameter("query");
        if (query != null) {
          request.setAttribute("query", query);
        }

        String taxonomyPosition = request.getParameter("AxisValueCouples");

        Hashtable<String, String> xmlFields = getXmlFieldsQuery(scc, request);
        List<FieldDescription> fieldsQuery = getOthersFieldsQuery(scc, request);

        List<Card> cards = scc.getSearchResult(query, taxonomyPosition, xmlFields, fieldsQuery);
        request.setAttribute("cards", cards);
        request.setAttribute("searchDone", "true"); // for no result case

        return getDestination("searchInWhitePage", scc, request);
      } else {
        destination = "/admin/jsp/errorpage.jsp";
      }

    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpage.jsp";
    }
    SilverTrace
        .info("whitePages", "WhitePagesRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
            "destination " + destination);
    return destination;
  }

  private Hashtable<String, String> getXmlFieldsQuery(WhitePagesSessionController scc,
      HttpServletRequest request) throws WhitePagesException {
    Hashtable<String, String> xmlFields = new Hashtable<>();

    // champs personnalisables xml
    SortedSet<SearchField> fields = scc.getSearchFields();
    if (fields != null && !fields.isEmpty()) {
      for (SearchField field : fields) {
        String fieldId = field.getFieldId();
        String searchValue = request.getParameter(fieldId);
        if (searchValue != null && searchValue.length() > 0) {
          if (fieldId.startsWith(SearchFieldsType.XML.getLabelType())) {
            request.setAttribute(fieldId, searchValue);
            // champs XML
            xmlFields.put(field.getFieldName(), searchValue);
          }
        }
      }
    }
    return xmlFields;
  }

  private List<FieldDescription> getOthersFieldsQuery(WhitePagesSessionController scc,
      HttpServletRequest request) throws WhitePagesException {
    List<FieldDescription> othersFields = new ArrayList<>();

    // champs personnalisables non xml (user silverpeas ou ldap)
    SortedSet<SearchField> fields = scc.getSearchFields();
    if (fields != null && !fields.isEmpty()) {
      for (SearchField field : fields) {
        String fieldId = field.getFieldId();
        String searchValue = request.getParameter(fieldId);
        if (searchValue != null && searchValue.length() > 0) {
          if (fieldId.startsWith(SearchFieldsType.LDAP.getLabelType())) {
            request.setAttribute(fieldId, searchValue);
            // champs XML
            FieldDescription fieldDescription =
                new FieldDescription(field.getFieldName(), searchValue, scc.getLanguage());
            othersFields.add(fieldDescription);
          } else if (fieldId.startsWith(SearchFieldsType.USER.getLabelType())) {
            request.setAttribute(fieldId, searchValue);
            if ("USR_name".equals(fieldId)) {
              FieldDescription fieldDescription =
                  new FieldDescription("LastName", searchValue, scc.getLanguage());
              othersFields.add(fieldDescription);
            } else if ("USR_surname".equals(fieldId)) {
              FieldDescription fieldDescription =
                  new FieldDescription("FirstName", searchValue, scc.getLanguage());
              othersFields.add(fieldDescription);
            } else if ("USR_email".equals(fieldId)) {
              FieldDescription fieldDescription =
                  new FieldDescription("Mail", searchValue, scc.getLanguage());
              othersFields.add(fieldDescription);
            }
          }
        }
      }
    }
    return othersFields;
  }
}
