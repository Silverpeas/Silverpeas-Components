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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package com.silverpeas.whitePages.servlets;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.util.StringUtil;
import com.silverpeas.whitePages.WhitePagesException;
import com.silverpeas.whitePages.control.WhitePagesSessionController;
import com.silverpeas.whitePages.filters.LoginFilter;
import com.silverpeas.whitePages.model.Card;
import com.silverpeas.whitePages.model.SearchField;
import com.silverpeas.whitePages.model.SearchFieldsType;
import com.stratelia.silverpeas.containerManager.ContainerContext;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchAxis;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.SearchCriteria;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.indexEngine.model.FieldDescription;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
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
    SilverTrace.info("whitePages", "WhitePagesRequestRouter.getSessionControlBeanName()", "", "");
    return "whitePages";
  }

  /*
   * (non-Javadoc)
   * @seecom.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter#
   * createComponentSessionController (com.stratelia.silverpeas.peasCore.MainSessionController,
   * com.stratelia.silverpeas.peasCore.ComponentContext)
   */
  public WhitePagesSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    SilverTrace.info("whitePages",
        "WhitePagesRequestRouter.createComponentSessionController()", "", "");
    return new WhitePagesSessionController(mainSessionCtrl, componentContext,
        "com.silverpeas.whitePages.multilang.whitePagesBundle",
        "com.silverpeas.whitePages.settings.whitePagesIcons");
  }

  /*
   * (non-Javadoc)
   * @seecom.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter#
   * getDestination(java.lang.String, com.stratelia.silverpeas.peasCore.ComponentSessionController,
   * javax.servlet.http.HttpServletRequest)
   */
  public String getDestination(String function, WhitePagesSessionController scc,
      HttpServletRequest request) {
    SilverTrace.info("whitePages", "WhitePagesRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", function);
    String destination = "";

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
           * If user has been redirected to this page, he must create his own card, so user is
           * redirected to his own card creation (excepted if card already exists).
           */
          HttpSession session = request.getSession(true);
          if ((session.getAttribute(LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION) != null)) {
            destination = getDestination("ForceCardCreation", scc, request);
          }

          /*
           * else redirects to card list
           */
          else {
            request.setAttribute("Main", "true");
            destination = getDestination("searchInWhitePage", scc, request);
          }
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
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
            destination = getDestination("createIdentity", scc, request);
          }

          /*
           * Else user should not have been redirected
           */
          else {
            request.getSession(true).removeAttribute(
                LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION);
            destination = getDestination("Main", scc, request);
          }
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      }

      /*
       * New card (administrator only) : redirects to user panel.
       */
      else if (function.equals("createQuery")) {
        if (flag.equals("admin")) {
          destination = scc.initUserPanel();
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
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
         * else if user is administrator, the card is for the user he has just selected...
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
         * Updates record object with new values.
         */
        scc.createCard(request);

        /*
         * If user has been forced to create his own card and done it, removes forced redirection.
         */
        // HttpSession session = request.getSession(true);
        // session.removeAttribute(LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION);

        /*
         * Go back to main page.
         */
        destination = getDestination("Main", scc, request);
      } else if (function.equals("consultCard")) {
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
        /*if (flag.equals("admin")
            || (card != null && card.getUserId().equals(scc.getUserId()))) {*/

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
          request.setAttribute("userFull",
              scc.getOrganizationController().getUserFull(card.getUserId()));
          request.setAttribute("pdcPositions",
              scc.getPdcPositions(scc.getSilverObjectId(String.valueOf(userCardId))));

          destination = "/whitePages/jsp/consultCard.jsp";
        }
      } else if (function.equals("updateCard")) {
        String userCardIdString = request.getParameter("userCardId");

        long userCardId = new Long(userCardIdString).longValue();
        Card card = scc.getCard(userCardId);
        if (card == null) {
          destination = "/whitePages/jsp/errorIdentity.jsp";
        } else if ((flag.equals("admin"))
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
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if (function.equals("effectiveUpdate")) {
        scc.updateCardRecord(request);
        scc.saveCard();
        destination = "/whitePages/jsp/closeUpdate.jsp?userCardId="
            + request.getParameter("userCardId");
      } else if (function.equals("reverseHide")) {
        if (flag.equals("admin")) {
          String retour = request.getParameter("returnPage"); // consultIdentity
          // ou consultCard
          String userCardIdString = request.getParameter("userCardId");
          List<String> arrayCards = new ArrayList<String>();
          arrayCards.add(userCardIdString);
          scc.reverseHide(arrayCards);

          request.setAttribute("userCardId", userCardIdString);
          destination = getDestination(retour, scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if (function.equals("delete")) {
        if (flag.equals("admin")) {
          String[] checkCards = request.getParameterValues("checkedCard");
          List<String> listToDelete = new ArrayList<String>();
          if (checkCards != null) {
            for (String userCardId : checkCards) {
              listToDelete.add(userCardId);
            }
          }
          scc.delete(listToDelete);

          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if (function.equals("hide")) {
        if (flag.equals("admin")) {
          String[] checkCards = request.getParameterValues("checkedCard");
          List<String> listToMask = new ArrayList<String>();
          if (checkCards != null) {
            for (String userCardId : checkCards) {
              listToMask.add(userCardId);
            }
          }
          scc.hide(listToMask);
          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if (function.equals("unHide")) {
        if (flag.equals("admin")) {
          String[] checkCards = request.getParameterValues("checkedCard");
          List<String> listToUnMask = new ArrayList<String>();
          if (checkCards != null) {
            for (String userCardId : checkCards) {
              listToUnMask.add(userCardId);
            }
          }
          scc.unHide(listToUnMask);
          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      }

      else if (function.equals("searchResult") || function.equals("Consult")) {
        request.setAttribute("userCardId", request.getParameter("Id"));
        destination = getDestination("consultCard", scc, request);
      }

      /*
       * Expert notification
       */
      else if (function.equals("NotifyExpert")) {
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
      }

      /*
       * Send expert notification
       */
      else if (function.equals("sendExpertNotification")) {
        String message = request.getParameter("messageToExpert");
        scc.sendNotification(message);

        return getDestination("Main", scc, request);
      } else if (function.equals("portlet")) {
        request.setAttribute("IsEmailHidden", scc.isEmailHidden());

        request.setAttribute("listCards", scc.getVisibleCards());
        destination = "/whitePages/jsp/portlet.jsp";
      } else if (function.equals("searchInWhitePage")) {
        boolean isMain = false;
        if (request.getAttribute("Main") != null) {
          isMain = "true".equals((String) request.getAttribute("Main"));
        }
        if (isMain) {
          // set default cards to see
          if (flag.equals("admin")) {
            request.setAttribute("cards", scc.getCards());
          } else if (flag.equals("user")) {
            request.setAttribute("cards", scc.getVisibleCards());
          } else {
            destination = "/admin/jsp/errorpage.jsp";
          }
        } else {
          // get form fields values
          SortedSet<SearchField> fields = scc.getSearchFields();
          request.setAttribute("searchFields", fields);

          SearchContext searchContext = new SearchContext();
          searchContext.setUserId(scc.getUserId());

          // primary axis for form 
          List<SearchAxis> primaryPdcFields = scc.getUsedAxisList(searchContext, "P");
          request.setAttribute("primaryPdcFields", primaryPdcFields);

          // secondary axis for form 
          List<SearchAxis> secondaryPdcFields = scc.getUsedAxisList(searchContext, "S");
          request.setAttribute("secondaryPdcFields", secondaryPdcFields);
        }

        destination = "/whitePages/jsp/dynamicSearch.jsp";
      } else if (function.equals("dynamicFieldsChoice")) {

        List<FieldTemplate> xmlFields = scc.getAllXmlFieldsForSearch();
        request.setAttribute("xmlFields", xmlFields);

        List<SearchField> ldapFields = scc.getLdapAttributesList();
        request.setAttribute("ldapFields", ldapFields);

        Set<String> alreadySelectedFields = scc.getSearchFieldIds();
        request.setAttribute("alreadySelectedFields", alreadySelectedFields);

        destination = "/whitePages/jsp/dynamicFieldsChoice.jsp";
      } else if (function.equals("comfirmFieldsChoice")) {

        String[] fields = request.getParameterValues("checkedFields");
        if (fields != null && fields.length > 0) {
          scc.confirmFieldsChoice(fields);
        }
        return getDestination("searchInWhitePage", scc, request);
      } else if (function.equals("getSearchResult")) {

        String query = request.getParameter("query");
        if (query != null) {
          request.setAttribute("query", query);
        }

        SearchContext pdcContext = getPdcContext(scc, request);

        Hashtable<String, String> xmlFields = getXmlFieldsQuery(scc, request);
        List<FieldDescription> fieldsQuery = getOthersFieldsQuery(scc, request);

        List<Card> cards = scc.getSearchResult(query, pdcContext, xmlFields, fieldsQuery);
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
    SilverTrace.info("whitePages", "WhitePagesRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "destination " + destination);
    return destination;
  }

  /**
   * Extract the container context from the request and save it in the session controller. If this
   * context is null then get the last one from the session controller. So the containerContext is
   * the same in the request and the session.
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

  private SearchContext getPdcContext(WhitePagesSessionController scc, HttpServletRequest request)
      throws PdcException {

    SearchContext pdcContext = null;

    // Axes primaires
    SearchContext searchContext = new SearchContext();
    searchContext.setUserId(scc.getUserId());
    List<SearchAxis> primaryPdcFields = scc.getUsedAxisList(searchContext, "P");
    if (primaryPdcFields != null && primaryPdcFields.size() > 0) {
      Iterator<SearchAxis> iterPrimaryPdcFields = primaryPdcFields.iterator();
      while (iterPrimaryPdcFields.hasNext()) {
        SearchAxis current = iterPrimaryPdcFields.next();
        String value = request.getParameter("Axis" + String.valueOf(current.getAxisId()));
        if (value != null && value.length() > 0) {
          request.setAttribute("Axis" + String.valueOf(current.getAxisId()), value);
          if (pdcContext == null) {
            pdcContext = new SearchContext();
            pdcContext.setUserId(scc.getUserId());
          }
          SearchCriteria criteria = new SearchCriteria(current.getAxisId(), value);
          pdcContext.addCriteria(criteria);
        }
      }
    }

    // Axes secondaires
    List<SearchAxis> secondaryPdcFields = scc.getUsedAxisList(searchContext, "S");
    if (secondaryPdcFields != null && secondaryPdcFields.size() > 0) {
      Iterator<SearchAxis> iterSecondaryPdcFields = secondaryPdcFields.iterator();
      while (iterSecondaryPdcFields.hasNext()) {
        SearchAxis current = iterSecondaryPdcFields.next();
        String value = request.getParameter("Axis" + String.valueOf(current.getAxisId()));
        if (value != null && value.length() > 0) {
          request.setAttribute("Axis" + String.valueOf(current.getAxisId()), value);
          if (pdcContext == null) {
            pdcContext = new SearchContext();
            pdcContext.setUserId(scc.getUserId());
          }
          SearchCriteria criteria = new SearchCriteria(current.getAxisId(), value);
          pdcContext.addCriteria(criteria);
        }
      }
    }
    return pdcContext;
  }


  private Hashtable<String, String> getXmlFieldsQuery(WhitePagesSessionController scc,
      HttpServletRequest request) throws UtilException, WhitePagesException {
    Hashtable<String, String> xmlFields = new Hashtable<String, String>();

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
      HttpServletRequest request) throws UtilException, WhitePagesException {
    List<FieldDescription> othersFields = new ArrayList<FieldDescription>();

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
                new FieldDescription(field.getFieldName(), searchValue,
                    scc.getLanguage());
            othersFields.add(fieldDescription);
          } else if (fieldId.startsWith(SearchFieldsType.USER.getLabelType())) {
            request.setAttribute(fieldId, searchValue);
            if (fieldId.equals("USR_name")) {
              FieldDescription fieldDescription =
                  new FieldDescription("LastName", searchValue, scc.getLanguage());
              othersFields.add(fieldDescription);
            } else if (fieldId.equals("USR_surname")) {
              FieldDescription fieldDescription =
                  new FieldDescription("FirstName", searchValue, scc.getLanguage());
              othersFields.add(fieldDescription);
            } else if (fieldId.equals("USR_email")) {
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
