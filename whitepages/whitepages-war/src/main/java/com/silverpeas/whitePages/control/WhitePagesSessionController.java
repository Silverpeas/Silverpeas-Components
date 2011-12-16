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
package com.silverpeas.whitePages.control;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.silverpeas.whitePages.WhitePagesException;
import com.silverpeas.whitePages.model.Card;
import com.silverpeas.whitePages.model.SearchField;
import com.silverpeas.whitePages.model.WhitePagesCard;
import com.silverpeas.whitePages.record.UserRecord;
import com.silverpeas.whitePages.record.UserTemplate;
import com.silverpeas.whitePages.service.ServicesFactory;
import com.stratelia.silverpeas.containerManager.ContainerContext;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchAxis;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SessionInfo;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.beans.admin.DomainDriver;
import com.stratelia.webactiv.beans.admin.DomainDriverManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.indexEngine.model.FieldDescription;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class WhitePagesSessionController extends AbstractComponentSessionController {

  /*-------------- Attributs ------------------*/
  private CardManager cardManager = null;
  private Card currentCard = null; // fiche courante
  private Card currentCreateCard = null; // fiche en cours de création
  private Collection<WhitePagesCard> currentUserCards = new ArrayList<WhitePagesCard>(); // liste des fiches
  // (Collection de
  // WhitePagesCard)
  // inter-instance du
  // user de la fiche
  // courante
  // (currentCard.getUserId())
  private Collection<String> userInstanceIds = null; // liste des id des instances
  // d'annuaire pour lequel
  // l'utilisateur courant
  // (getUserId()) a des droits
  // (admin ou user)
  private UserTemplate userTemplate = null; // permet de gèrer le modèle
  // d'affichage de l'identité d'un
  // user à partir d'un modèle html
  private PublicationTemplate cardTemplate = null; // permet la gestion du
  // modèle des fiches
  private String[] hostParameters = null;
  private String returnURL = "";
  private ContainerContext containerContext;
  private Card notifiedUserCard;
  private final static ResourceLocator whitePagesSettings = new ResourceLocator(
          "com.silverpeas.whitePages.settings.settings", "");
  private PdcBm pdcBm = null;
  private static DomainDriverManager m_DDManager = new DomainDriverManager();

  /*
   * Recherche une fiche Retourne currentCard si son id est le même que celui de la fiche recherchée
   * Demande au CardManager la fiche sinon Affecte l'attribut ReadOnly de Card à false si la fiche
   * fait partie de l'instance (instanceId) Recherche et affecte le cardRecord de la fiche
   * (getTemplate(currentCard .getInstanceId()).getRecordset().getRecord(userCardId)) Recherche et
   * affecte le userRecord de la fiche (userTemplate.getRecord(userCardId)) Affecte le cardViewForm
   * (getTemplate(currentCard.getInstanceId()).getViewForm()) Affecte le cardUpdateForm
   * (cardTemplate.getUpdateForm()) Affecte le userForm (userTemplate.getViewForm()) Appel
   * getWhitePagesCards pour mettre à jour la liste des fiches inter-instance portant sur le même
   * user Met la fiche en session puis la retourne
   * @param userCardId id de la fiche
   */
  public Card getCard(long userCardId) throws WhitePagesException {
    try {
      if ((currentCard == null) || (currentCard.getPK() == null)
              || (currentCard.getPK().getId() == null)
              || (currentCard.getPK().getId().equals(""))
              || (new Long(currentCard.getPK().getId()).longValue() != userCardId)) {
        Card card = getCardManager().getCard(userCardId);

        if (card == null) {
          return null;
        }

        if (card.getInstanceId().equals(getComponentId())) {
          card.writeReadOnly(false);
          card.writeCardUpdateForm(getCardTemplate().getUpdateForm());
        }

        PublicationTemplate template = getTemplate(card.getInstanceId());
        UserTemplate templateUser = getUserTemplate(card.getInstanceId());
        UserRecord userRecord = templateUser.getRecord(card.getUserId());
        if (userRecord.getUserDetail() == null) {
          Collection<String> cards = new ArrayList<String>();
          cards.add(new Long(userCardId).toString());
          delete(cards);
          return null;
        }
        DataRecord cardRecord = template.getRecordSet().getRecord(
                new Long(userCardId).toString());
        if (cardRecord == null) {
          cardRecord = template.getRecordSet().getEmptyRecord();
        }
        card.writeCardRecord(cardRecord);
        card.writeCardViewForm(template.getViewForm());
        card.writeUserForm(templateUser.getViewForm());
        card.writeUserRecord(userRecord);
        getHomeWhitePagesCards(card.getUserId());
        setCurrentCard(card);
        return card;
      }
      getHomeWhitePagesCards(currentCard.getUserId());
      return currentCard;
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.getCard",
              SilverpeasException.ERROR,
              "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException("WhitePagesSessionController.getCard",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }
  }

  /*
   * Recherche une fiche en lecture seule pour accès externe ou rôle user Retourne currentCard si
   * son id est le même que celui de la fiche recherchée Demande au CardManager la fiche sinon
   * Recherche et affecte le cardRecord de la fiche
   * (getTemplate(currentCard.getInstanceId()).getRecordset().getRecord( userCardId)) Recherche et
   * affecte le userRecord de la fiche (userTemplate.getRecord(userCardId)) Affecte le cardViewForm
   * (getTemplate(currentCard.getInstanceId()).getViewForm()) Affecte le userForm
   * (userTemplate.getViewForm()) Appel getWhitePagesCards pour mettre à jour la liste des fiches
   * inter-instance portant sur le même user Met la fiche en session puis la retourne
   * @param userCardId id de la fiche
   */
  public Card getCardReadOnly(long userCardId) throws WhitePagesException {
    try {
      if ((currentCard == null) || (currentCard.getPK() == null)
              || (currentCard.getPK().getId() == null)
              || (currentCard.getPK().getId().equals(""))
              || (new Long(currentCard.getPK().getId()).longValue() != userCardId)) {
        Card card = getCardManager().getCard(userCardId);
        PublicationTemplate template = getTemplate(card.getInstanceId());
        UserTemplate templateUser = getUserTemplate(card.getInstanceId());
        DataRecord cardRecord = template.getRecordSet().getRecord(
                new Long(userCardId).toString());
        UserRecord userRecord = templateUser.getRecord(card.getUserId());
        if (userRecord.getUserDetail() == null) {
          Collection<String> cards = new ArrayList<String>();
          cards.add(new Long(userCardId).toString());
          delete(cards);
          return null;
        }
        if (cardRecord == null) {
          cardRecord = template.getRecordSet().getEmptyRecord();
        }
        card.writeCardRecord(cardRecord);
        card.writeCardViewForm(template.getViewForm());
        card.writeUserForm(templateUser.getViewForm());
        card.writeUserRecord(userRecord);
        getWhitePagesCards(card.getUserId());
        setCurrentCard(card);
        return card;
      }
      getWhitePagesCards(currentCard.getUserId());
      return currentCard;

    } catch (PublicationTemplateException e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.getCardReadOnly",
              SilverpeasException.ERROR,
              "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.getCardReadOnly",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }

  }

  /*
   * Recherche une fiche à partir d'un userId (appel de WhitePages à partir d'un autre composant)
   * Récupère le premier élement de la liste des fiches inter-instance portant sur le user
   * (getWhitePagesCards) Appel la recherche fiche (getCardReadOnly) à partir de l'id du premier
   * elèment de currentUserCards
   * @param userId id d'un user
   * @return une Card ou NULL
   */
  public Card getUserCard(String userId) throws WhitePagesException {
    Card card = null;
    Collection<WhitePagesCard> userCards = getWhitePagesCards(userId);
    Iterator<WhitePagesCard> it = null;
    if (userCards != null) {
      it = userCards.iterator();
      if (it.hasNext()) {
        WhitePagesCard wpc = it.next();
        card = getCardReadOnly(wpc.getUserCardId());
      }
    }
    return card;
  }

  /*
   * Charge la liste des fiches inter-instance portant sur le user (et non masquées sauf instance
   * courante)et la met en session si le user est différent du user courant
   * (currentCard.getUserId()) Sinon retourne currentUserCards (Collection de WhitePagesCard)
   * @param userId id d'un user
   */
  private Collection<WhitePagesCard> getHomeWhitePagesCards(String userId)
          throws WhitePagesException {
    if (currentCard == null || !currentCard.getUserId().equals(userId)
            || getCurrentUserCards().isEmpty()) {
      Collection<WhitePagesCard> cards = getCardManager().getHomeUserCards(userId,
              getUserInstanceIds(), getComponentId());
      Collections.sort((List) cards);
      setCurrentUserCards(cards);
    }
    return getCurrentUserCards();
  }

  /*
   * Charge la liste des fiches inter-instance portant sur le user (et non masquées) et la met en
   * session si le user est différent du user courant (currentCard.getUserId()) Sinon retourne
   * currentUserCards (Collection de WhitePagesCard)
   * @param userId id d'un user
   */
  private Collection<WhitePagesCard> getWhitePagesCards(String userId)
          throws WhitePagesException {
    if (currentCard == null || !currentCard.getUserId().equals(userId)
            || getCurrentUserCards().isEmpty()) {
      Collection<WhitePagesCard> cards = getCardManager().getUserCards(userId,
              getUserInstanceIds());
      Collections.sort((List) cards);
      setCurrentUserCards(cards);
    }
    return getCurrentUserCards();
  }

  /*
   * Crée une nouvelle fiche (new Card()) et affecte le UserRecord de la fiche et le userForm
   * (userTemplate.getViewForm()) Met la fiche en session et la retourne Ajoute un new
   * WhitePages(" fiche en cours de création " ) à la liste des fiches
   * @param userDetail détail de l'utilisateur sur lequel porte la fiche
   */
  public Card createCard(UserDetail userDetail) throws WhitePagesException {
    Card card = new Card(getComponentId());
    card.writeUserForm(getUserTemplate().getViewForm());
    UserRecord userRecord = getUserTemplate().getRecord(userDetail.getId());
    if (userRecord.getUserDetail() == null) {
      return null;
    }
    card.writeUserRecord(userRecord);
    card.writeReadOnly(false);
    setCurrentCreateCard(card);
    setCurrentUserCards(new ArrayList<WhitePagesCard>());
    getWhitePagesCards(userDetail.getId());
    ((ArrayList<WhitePagesCard>) getCurrentUserCards()).add(0, new WhitePagesCard(
            "Fiche en cours de création"));
    return getCurrentCreateCard();
  }

  /*
   * Affecte un DataRecord vide (cardTemplate.getRecordset().getEmptyRecord()), le cardViewForm
   * (cardTemplate.getViewForm()) et le cardUpdateForm à la fiche courante Retourne la fiche
   * courante
   */
  public Card setCardRecord() throws WhitePagesException {
    try {
      getCurrentCreateCard().writeCardUpdateForm(
              getCardTemplate().getUpdateForm());
      getCurrentCreateCard().writeCardRecord(
              getCardTemplate().getRecordSet().getEmptyRecord());
      getCurrentCreateCard().writeCardViewForm(getCardTemplate().getViewForm());
      return getCurrentCreateCard();
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.setCardRecord",
              SilverpeasException.ERROR,
              "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.setCardRecord",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }
  }

  /*
   * Rempli le DataRecord de la fiche courante en cours de création à partir de la request
   */
  public void setCardRecord(HttpServletRequest request)
          throws WhitePagesException {
    try {
      List<FileItem> items = FileUploadUtil.parseRequest(request);
      PagesContext pageContext = new PagesContext("", getLanguage());
      pageContext.setComponentId(getComponentId());
      pageContext.setObjectId(getCurrentCard().getPK().getId());
      pageContext.setUserId(getUserId());
      getCardTemplate().getUpdateForm().update(items,
              getCurrentCreateCard().readCardRecord(), pageContext);
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.setCardRecord",
              SilverpeasException.ERROR,
              "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (Exception e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.setCardRecord",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }
  }

  /*
   * Rempli le DataRecord de la fiche courante à partir de la request
   */
  public void updateCardRecord(HttpServletRequest request)
          throws WhitePagesException {
    try {
      List<FileItem> items = FileUploadUtil.parseRequest(request);
      PagesContext pageContext = new PagesContext("", getLanguage());
      pageContext.setComponentId(getComponentId());
      pageContext.setObjectId(getCurrentCard().getPK().getId());
      pageContext.setUserId(getUserId());
      getCardTemplate().getUpdateForm().update(items,
              getCurrentCard().readCardRecord(), pageContext);
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.updateCardRecord",
              SilverpeasException.ERROR,
              "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (Exception e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.updateCardRecord",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }

  }

  /*
   * Enregistre et crée la fiche courante : enregistrement de la fiche
   * (CardManager.create(currentCard)), recupération de l'id de la fiche créée (userCardId) et set
   * de l'id de la fiche courante Enregistre les données du modèle de la fiche :
   * currentCard.readCardRecord().setId(userCardId), saveCard()
   */
  public void insertCard() throws WhitePagesException {
    try {
      String userCardId = new Long(getCardManager().create(
              getCurrentCreateCard(), getSpaceId(), getUserId())).toString();
      getCurrentCreateCard().readCardRecord().setId(userCardId);
      getCardTemplate().getRecordSet().save(
              getCurrentCreateCard().readCardRecord());
      setCurrentUserCards(new ArrayList<WhitePagesCard>());
      SilverTrace.spy("whitePages", "WhitePagesSessionController.insertCard",
              getSpaceId(), getComponentId(), userCardId, getUserDetail().getId(),
              SilverTrace.SPY_ACTION_CREATE);

    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.insertCard",
              SilverpeasException.ERROR,
              "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException("WhitePagesSessionController.insertCard",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }

  }

  /*
   * Enregistre la fiche courante : Enregistre les données du modèle de la fiche
   * (cardTemplate.getRecordset().save(currentCard.readCardRecord()))
   */
  public void saveCard() throws WhitePagesException {
    try {
      getCurrentCard().readCardRecord().setId(getCurrentCard().getPK().getId());
      getCardTemplate().getRecordSet().save(getCurrentCard().readCardRecord());
      getCardManager().indexCard(getCurrentCard());
      SilverTrace.spy("whitePages", "WhitePagesSessionController.saveCard",
              getSpaceId(), getComponentId(), getCurrentCard().getPK().getId(),
              getUserDetail().getId(), SilverTrace.SPY_ACTION_UPDATE);
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.saveCard",
              SilverpeasException.ERROR,
              "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException("WhitePagesSessionController.saveCard",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }

  }

  /*
   * Liste les fiches de l'annuaire
   * @return la liste de toutes les fiches de l'annuaire (Collection de Card)
   */
  public Collection<Card> getCards() throws WhitePagesException {
    return setUserRecords(getCardManager().getCards(getComponentId()));
  }

  /*
   * Liste les fiches de l'annuaire non masquées
   * @return la liste de toutes les fiches de l'annuaire (Collection de Card) non masquées
   * (hideStatus = 0)
   */
  public Collection<Card> getVisibleCards() throws WhitePagesException {
    return setUserRecords(getCardManager().getVisibleCards(getComponentId()));
  }

  public void indexVisibleCards() throws WhitePagesException {
    Collection<Card> visibleCards = setUserRecordsAndCardRecords(getCardManager().getVisibleCards(
            getComponentId()));
    for (Card card : visibleCards) {
      getCardManager().indexCard(card);
    }
  }

  /*
   * Affecte les UserRecord à chaque Card d'une liste
   */
  private Collection<Card> setUserRecords(Collection<Card> cards)
          throws WhitePagesException {
    List<Card> listCards = new ArrayList<Card>();
    try {
      if (cards != null) {
        for (Card card : cards) {
          if (getUserTemplate().getRecord(card.getUserId()).getUserDetail() == null) {// l'utilisateur
            // n'existe
            // plus
            String idCard = card.getPK().getId();
            List<String> listId = new ArrayList<String>();
            listId.add(idCard);
            delete(listId);
          } else {
            card.writeUserRecord(getUserTemplate().getRecord(card.getUserId()));
            listCards.add(card);
          }
        }
      }
    } catch (WhitePagesException e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.setUserRecords",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }
    return listCards;
  }

  /*
   * Affecte les UserRecord & CardRecord à chaque Card d'une liste
   */
  private Collection<Card> setUserRecordsAndCardRecords(Collection<Card> cards)
          throws WhitePagesException {
    List<Card> listCards = new ArrayList<Card>();
    try {
      if (cards != null) {
        PublicationTemplate template = getTemplate(getComponentId());
        DataRecord cardRecord = null;
        String idCard = null;
        for (Card card : cards) {
          idCard = card.getPK().getId();
          if (getUserTemplate().getRecord(card.getUserId()).getUserDetail() == null) {
            // l'utilisateur n'existe plus
            List<String> listId = new ArrayList<String>();
            listId.add(idCard);
            delete(listId);
          } else {
            card.writeUserRecord(getUserTemplate().getRecord(card.getUserId()));

            cardRecord = template.getRecordSet().getRecord(idCard);
            if (cardRecord == null) {
              cardRecord = template.getRecordSet().getEmptyRecord();
            }
            card.writeCardRecord(cardRecord);
            listCards.add(card);
          }
        }
      }
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.setUserRecordsAndCardRecords",
              SilverpeasException.ERROR,
              "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.setUserRecordsAndCardRecords",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }
    return listCards;
  }

  /*
   * Supprime une liste de fiches de l'annuaire + liste des cardRecord correspondant
   * @param userCardIds liste des identifiants des fiches à supprimer
   */
  public void delete(Collection<String> userCardIds) throws WhitePagesException {

    try {
      if (userCardIds != null) {
        for (String userCardId : userCardIds) {
          DataRecord data = getCardTemplate().getRecordSet().getRecord(
                  userCardId);
          getCardTemplate().getRecordSet().delete(data);
          SilverTrace.spy("whitePages", "WhitePagesSessionController.delete",
                  getSpaceId(), getComponentId(), userCardId, getUserDetail().getId(),
                  SilverTrace.SPY_ACTION_DELETE);
        }
        getCardManager().delete(userCardIds, getSpaceId());
      }
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.delete",
              SilverpeasException.ERROR,
              "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException("WhitePagesSessionController.delete",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }

  }

  /*
   * Masque une liste de fiches de l'annuaire
   * @param userCardIds liste des identifiants des fiches à masquer
   */
  public void hide(Collection<String> userCardIds) throws WhitePagesException {
    getCardManager().setHideStatus(userCardIds, 1);
  }

  /*
   * De Masque une liste de fiches de l'annuaire
   * @param userCardIds liste des identifiants des fiches à de masquer
   */
  public void unHide(Collection<String> userCardIds) throws WhitePagesException {
    getCardManager().setHideStatus(userCardIds, 0);
  }

  /*
   * Reverse le statut Masqué d'une liste de fiches de l'annuaire
   * @param userCardIds liste des identifiants des fiches
   */
  public void reverseHide(Collection<String> userCardIds) throws WhitePagesException {
    getCardManager().reverseHide(userCardIds);
    setCurrentCard(null);
  }

  /*
   * Indique si un utilisateur possède déjà une fiche dans l'annuaire courant
   * @param userId l'identifiant d'un utilisateur
   */
  public boolean existCard(String userId) throws WhitePagesException {
    return getCardManager().existCard(userId, getComponentId());
  }

  /*
   * retourne la valeur d'un paramètre affecté lors de l'instanciation
   */
  private String getParam(String paramName) {
    return getComponentParameterValue(paramName);
  }

  /*
   * retourne la valeur d'un paramètre affecté lors de l'instanciation pour un annuaire donné
   */
  private String getParam(String paramName, String instanceId) {
    return AdminReference.getAdminService().getComponentParameterValue(instanceId, paramName);
  }

  /*
   * Retourne le cardTemplate d'un annuaire :
   * PublicationTemplateManager#getPublicationTemplate(instanceId, getParam("cardTemplate",
   * instanceId))
   */
  private PublicationTemplate getTemplate(String instanceId)
          throws WhitePagesException {
    try {
      return PublicationTemplateManager.getInstance().getPublicationTemplate(instanceId,
              getParam("cardTemplate", instanceId));
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.getTemplate",
              SilverpeasException.ERROR,
              "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    }
  }

  /*
   * Retourne le userTemplate d'un annuaire :
   * PublicationTemplateManager#getPublicationTemplate(instanceId, getParam("userTemplate",
   * instanceId))
   */
  private UserTemplate getUserTemplate(String instanceId)
          throws WhitePagesException {
    ResourceLocator templateSettings = new ResourceLocator(
            "com.silverpeas.whitePages.settings.template", "");
    String templateDir = templateSettings.getString("templateDir");
    return new UserTemplate(templateDir.replace('\\', '/') + "/"
            + getParam("userTemplate", instanceId).replace('\\', '/'),
            getLanguage());
  }

  /*
   * Appel UserPannel pour set des users selectionnable (4 [] vides) :
   */
  public String initUserPanel() {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
            "ApplicationURL");
    String hostSpaceName = getSpaceLabel();
    PairObject hostComponentName = new PairObject(getComponentLabel(),
            m_context + "/RwhitePages/" + getComponentId() + "/Main");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("whitePages.usersList"),
            "/RwhitePages/" + getComponentId() + "/Main");
    String hostUrl = m_context + "/RwhitePages/" + getComponentId()
            + "/createIdentity";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(hostUrl);

    // Contraintes
    sel.setMultiSelect(false);
    sel.setPopupMode(false);
    sel.setSetSelectable(false);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Appel UserPannel pour récup du user sélectionné : UserDetail[]
   * UserPanel.getUserDetailSelected()
   */
  public UserDetail getUserDetailSelected() {
    UserDetail user = null;
    String selUser = getSelection().getFirstSelectedElement();
    if ((selUser != null) && (selUser.length() > 0)) {
      user = getOrganizationController().getUserDetail(selUser);
    }
    return user;
  }

  /*-------------- Methodes eléments en session------------*/
  private CardManager getCardManager() {
    if (cardManager == null) {
      cardManager = CardManager.getInstance();
    }
    return cardManager;
  }

  private void setCurrentCard(Card card) {
    this.currentCard = card;
  }

  private void setCurrentCreateCard(Card card) {
    this.currentCreateCard = card;
    setCurrentCard(card);
  }

  private void setCurrentUserCards(Collection<WhitePagesCard> userCards) {
    this.currentUserCards = userCards;
  }

  public void initCurrentUserCards() {
    this.currentUserCards = new ArrayList<WhitePagesCard>();
  }

  public Card getCurrentCard() {
    return currentCard;
  }

  public Card getCurrentCreateCard() {
    return currentCreateCard;
  }

  public Collection<WhitePagesCard> getCurrentUserCards() {
    return currentUserCards;
  }

  private PublicationTemplate getCardTemplate() throws WhitePagesException {
    try {
      if (cardTemplate == null) {
        cardTemplate = PublicationTemplateManager.getInstance().getPublicationTemplate(
                getComponentId(), getParam("cardTemplate"));
      }
      return cardTemplate;
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException(
              "WhitePagesSessionController.getCardTemplate",
              SilverpeasException.ERROR,
              "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    }
  }

  public void setHostParameters(String hostSpaceName, String hostComponentName,
          String hostUrl, String hostPath) {
    hostParameters = new String[4];
    hostParameters[0] = hostComponentName;
    hostParameters[1] = hostUrl;
    hostParameters[2] = hostSpaceName;
    hostParameters[3] = hostPath;
  }

  public String[] getHostParameters() {
    if (hostParameters == null) {
      hostParameters = new String[4];
    }
    if (hostParameters[0] == null) {
      hostParameters[0] = getComponentLabel();
    }
    if (hostParameters[1] == null) {
      if (containerContext != null) {
        hostParameters[1] = containerContext.getReturnURL();
      } else {
        hostParameters[1] = "Main";
      }
    }
    if (hostParameters[2] == null) {
      hostParameters[2] = getSpaceLabel();
    }
    if (hostParameters[3] == null) {
      hostParameters[3] = getString("whitePages.usersList") + " > "
              + getString("whitePages.consultCard");
    }
    return hostParameters;
  }

  private Collection<String> getUserInstanceIds() {
    if (userInstanceIds == null) {
      userInstanceIds = new ArrayList<String>();
      CompoSpace[] instances = getOrganizationController().getCompoForUser(
              getUserId(), "whitePages");
      for (int i = 0; i < instances.length; i++) {
        userInstanceIds.add(instances[i].getComponentId());
      }
    }
    return userInstanceIds;
  }

  private UserTemplate getUserTemplate() {
    if (userTemplate == null) {
      ResourceLocator templateSettings = new ResourceLocator(
              "com.silverpeas.whitePages.settings.template", "");
      String templateDir = templateSettings.getString("templateDir");
      this.userTemplate = new UserTemplate(templateDir.replace('\\', '/') + "/"
              + getParam("userTemplate").replace('\\', '/'), getLanguage());
    }
    return this.userTemplate;
  }

  public int getSilverObjectId(String objectId) {
    return new Integer(getCurrentCardContentId()).intValue();
  }

  /*-------------- Methodes de la classe ------------------*/
  public WhitePagesSessionController(MainSessionController mainSessionCtrl,
          ComponentContext context, String multilangBaseName, String iconBaseName) {
    super(mainSessionCtrl, context, multilangBaseName, iconBaseName);
    if (context == null) {
      setComponentRootName(URLManager.CMP_WHITEPAGESPEAS);
    }
  }

  public String getCurrentCardContentId() {
    String contentId = null;

    if (currentCard != null) {
      try {
        ContentManager contentManager = new ContentManager();
        contentId = ""
                + contentManager.getSilverContentId(currentCard.getPK().getId(),
                currentCard.getInstanceId());
      } catch (ContentManagerException ignored) {
        SilverTrace.error("whitePages", "WhitePagesSessionController",
                "whitePages.EX_UNKNOWN_CONTENT_MANAGER", ignored);
        contentId = null;
      }
    }

    return contentId;
  }

  public void setContainerContext(ContainerContext containerContext) {
    this.containerContext = containerContext;
  }

  public ContainerContext getContainerContext() {
    return containerContext;
  }

  public void setReturnURL(String returnURL) {
    this.returnURL = returnURL;
  }

  public String getReturnURL() {
    return returnURL;
  }

  public void setNotifiedUserCard(Card card) {
    this.notifiedUserCard = card;

  }

  public void sendNotification(String message)
          throws NotificationManagerException {
    NotificationMetaData notifMetaData = new NotificationMetaData();
    notifMetaData.addUserRecipient(new UserRecipient(notifiedUserCard.getUserId()));
    notifMetaData.setAnswerAllowed(false);
    notifMetaData.setComponentId(getComponentId());
    notifMetaData.setContent(message);
    notifMetaData.setDate(new Date());
    notifMetaData.setSender(whitePagesSettings.getString("whitePages.genericUserId"));
    notifMetaData.setTitle(getString("whitePages.notificationTitle"));

    String link = URLManager.getURL(null, getComponentId())
            + "consultIdentity?userCardId=" + notifiedUserCard.getPK().getId();
    notifMetaData.setLink(link);

    NotificationSender sender = new NotificationSender(getComponentId());
    sender.notifyUser(notifMetaData);
  }

  public boolean isCardClassifiedOnPdc() throws WhitePagesException,
          ContentManagerException, PdcException {
    Card card = getUserCard(getUserId());
    return getCardManager().isPublicationClassifiedOnPDC(card);
  }

  public Boolean isEmailHidden() {
    // pour cacher ou non l'email pour les lecteurs
    return "yes".equalsIgnoreCase(getComponentParameterValue("isEmailHidden"));
  }

  public Boolean isFicheVisible() {
    // pour afficher ou non l'onglet fiche pour les lecteurs
    return "no".equalsIgnoreCase(getComponentParameterValue("isFicheVisible"));
  }

  public int getDomainId() {
    int domainIdReturn = 0; // default value

    // pour recupèrer le domainId auquel rattaché l'annuaire
    String domainId = getComponentParameterValue("domainId");
    if (domainId != null && domainId.length() > 0) {
      try {
        domainIdReturn = Integer.parseInt(domainId);
      } catch (NumberFormatException nexp) {
        SilverTrace.error("whitePages", "WhitePagesSessionController",
                "whitePages.EX_UNKNOWN_DOMAIN_ID", nexp);
      }
    }
    return domainIdReturn;

  }

  public List<String> getAllXmlFieldsForSearch() throws WhitePagesException,
          PublicationTemplateException {
    List<String> xmlFields = new ArrayList<String>();
    PublicationTemplate template = getTemplate(getComponentId());
    RecordTemplate recordTemplate = template.getRecordTemplate();
    xmlFields = Arrays.asList(recordTemplate.getFieldNames());
    return xmlFields;
  }

  public List<SearchAxis> getUsedAxisList(SearchContext searchContext, String axisType) throws
          PdcException {
    List<SearchAxis> searchAxis = getPdcBm().getPertinentAxisByInstanceId(searchContext, axisType,
            getComponentId());
    if (searchAxis != null && !searchAxis.isEmpty()) {
      for (SearchAxis axis : searchAxis) {
        axis.setValues(getPdcBm().getDaughters(Integer.toString(axis.getAxisId()), "0"));
      }
    }
    return searchAxis;
  }

  public List<String> getLdapAttributesList() throws Exception {
    DomainDriver domainDriver = m_DDManager.getDomainDriver(getDomainId());
    return domainDriver.getUserAttributes();
  }

  public void confirmFieldsChoice(String[] fields) throws UtilException {
    ServicesFactory.getWhitePagesService().createSearchFields(fields, getComponentId());
  }

  public SortedSet<SearchField> getSearchFields() throws UtilException {
    return ServicesFactory.getWhitePagesService().getSearchFields(getComponentId());
  }

  public Set<String> getSearchFieldIds() throws UtilException {
    Set<String> ids = new HashSet<String>();
    SortedSet<SearchField> searchFields = getSearchFields();
    if (searchFields != null && !searchFields.isEmpty()) {
      for (SearchField field : searchFields) {
        ids.add(field.getFieldId());
      }
    }
    return ids;
  }

  public List<Card> getSearchResult(String query, SearchContext pdcContext,
          Hashtable<String, String> xmlFields, List<FieldDescription> fieldsQuery) {
    List<Card> cards = new ArrayList<Card>();
    Collection<GlobalSilverContent> contents = null;

    try {
      PublicationTemplate template = getTemplate(getComponentId());
      String xmlTemplate = template.getName();
      contents = ServicesFactory.getMixedSearchService().search(getSpaceId(), getComponentId(),
              getUserId(), query,
              pdcContext, xmlFields, xmlTemplate, fieldsQuery, getLanguage());
    } catch (Exception e) {
      SilverTrace.info("whitePages", "WhitePagesSessionController.getSearchResult",
              "whitePages.EX_SEARCH_GETRESULT", e);
    }

    if (contents != null) {
      try {
        Collection<Card> allCars = getCards();
        HashMap<String, Card> map = new HashMap<String, Card>();
        for (Card card : allCars) {
          map.put(card.getPK().getId(), card);
        }

        for (GlobalSilverContent content : contents) {
          if (map.containsKey(content.getId())) {
            cards.add(map.get(content.getId()));
          }
        }

        if (cards != null) {
          for (Card card : cards) {
            UserRecord userRecord = card.readUserRecord();
            if (userRecord != null) {
              Collection<SessionInfo> sessionInfos = SessionManager.getInstance().
                      getConnectedUsersList();
              for (SessionInfo varSi : sessionInfos) {
                if (varSi.getUserDetail().equals(userRecord.getUserDetail())) {
                  userRecord.setConnected(true);
                  break;
                }
              }
            }
          }
        }

      } catch (Exception e) {
        SilverTrace.info("whitePages", "WhitePagesSessionController.getSearchResult",
                "whitePages.EX_SEARCH_GETCARDS", e);
      }
    }
    return cards;
  }

  private PdcBm getPdcBm() {
    if (pdcBm == null) {
      pdcBm = (PdcBm) new PdcBmImpl();
    }
    return pdcBm;
  }

  public HashMap<String, List<ClassifyValue>> getPdcPositions(int cardId) throws PdcException {

    HashMap<String, List<ClassifyValue>> result = new HashMap<String, List<ClassifyValue>>();

    List<ClassifyPosition> list = getPdcBm().getPositions(cardId, getComponentId());

    if (list != null && list.size() > 0) {

      Iterator<ClassifyPosition> iter = list.iterator();
      while (iter.hasNext()) {
        List<Value> pathValues = null;
        ClassifyPosition position = iter.next();
        List<ClassifyValue> values = position.getValues();
        for (ClassifyValue value : values) {
          pathValues = value.getFullPath();
          if (pathValues != null && !pathValues.isEmpty()) {
            List<ClassifyValue> valuesForPrincipal = null;
            Value term = pathValues.get(0);
            String principal = term.getName(getLanguage());
            if (result.get(principal) != null) {
              valuesForPrincipal = result.get(principal);
              valuesForPrincipal.add(value);
              result.put(principal, valuesForPrincipal);
            } else {
              valuesForPrincipal = new ArrayList<ClassifyValue>();
              valuesForPrincipal.add(value);
              result.put(principal, valuesForPrincipal);
            }
          }
        }
      }
    }
    return result;

  }
}