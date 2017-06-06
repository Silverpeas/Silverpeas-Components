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
package org.silverpeas.components.whitepages.control;

import org.silverpeas.core.admin.domain.DomainDriverManagerProvider;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.whitepages.WhitePagesException;
import org.silverpeas.components.whitepages.model.Card;
import org.silverpeas.components.whitepages.model.SearchField;
import org.silverpeas.components.whitepages.model.SearchFieldsType;
import org.silverpeas.components.whitepages.model.WhitePagesCard;
import org.silverpeas.components.whitepages.record.UserRecord;
import org.silverpeas.components.whitepages.record.UserTemplate;
import org.silverpeas.components.whitepages.service.WhitePageServiceProvider;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

public class WhitePagesSessionController extends AbstractComponentSessionController {

  private CardManager cardManager = null;
  private Card currentCard = null;
  private Card currentCreateCard = null;
  // liste des fiches (Collection de WhitePagesCard) inter-instance du user de la fiche courante
  private Collection<WhitePagesCard> currentUserCards = new ArrayList<>();
  // liste des id des instances d'annuaire pour lequel l'utilisateur courant (getUserId()) a des
  // droits (admin ou user)
  private Collection<String> userInstanceIds = null;
  // permet de gèrer le modèle d'affichage de l'identité d'un user à partir d'un modèle html
  private UserTemplate userTemplate = null;
  // permet la gestion du modèle des fiches
  private PublicationTemplate cardTemplate = null;
  private String[] hostParameters = null;
  private Card notifiedUserCard;
  private PdcManager pdcManager = null;
  private static DomainDriverManager mDDManager = DomainDriverManagerProvider.getCurrentDomainDriverManager();

  public boolean isAdmin() {
    return "admin".equals(getHighestSilverpeasUserRole().getName());
  }

  /**
   * Recherche une fiche Retourne currentCard si son id est le même que celui de la fiche
   * recherchée
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
      if ((currentCard == null) || (currentCard.getPK() == null) ||
          (currentCard.getPK().getId() == null) || (currentCard.getPK().getId().equals("")) ||
          (Long.parseLong(currentCard.getPK().getId()) != userCardId)) {
        Card card = getCardManager().getCard(userCardId);

        if (card == null) {
          return null;
        }

        if (card.getInstanceId().equals(getComponentId())) {
          // user can update card if he is admin or if it's his own card
          card.writeReadOnly(!isAdmin() && !getUserId().equals(card.getUserId()));
          card.writeCardUpdateForm(getCardTemplate().getUpdateForm());
        }

        PublicationTemplate template = getTemplate(card.getInstanceId());
        UserTemplate templateUser = getUserTemplate(card.getInstanceId());
        UserRecord userRecord = templateUser.getRecord(card.getUserId());
        if (userRecord.getUserDetail() == null) {
          Collection<String> cards = new ArrayList<>();
          cards.add(Long.toString(userCardId));
          delete(cards);
          return null;
        }
        DataRecord cardRecord = template.getRecordSet().getRecord(Long.toString(userCardId));
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
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException("WhitePagesSessionController.getCard",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }
  }

  /**
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
      if ((currentCard == null) || (currentCard.getPK() == null) ||
          (currentCard.getPK().getId() == null) || (currentCard.getPK().getId().equals("")) ||
          (Long.parseLong(currentCard.getPK().getId()) != userCardId)) {
        Card card = getCardManager().getCard(userCardId);
        PublicationTemplate template = getTemplate(card.getInstanceId());
        UserTemplate templateUser = getUserTemplate(card.getInstanceId());
        DataRecord cardRecord = template.getRecordSet().getRecord(Long.toString(userCardId));
        UserRecord userRecord = templateUser.getRecord(card.getUserId());
        if (userRecord.getUserDetail() == null) {
          Collection<String> cards = new ArrayList<>();
          cards.add(Long.toString(userCardId));
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
      throw new WhitePagesException("WhitePagesSessionController.getCardReadOnly",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException("WhitePagesSessionController.getCardReadOnly",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }

  }

  /**
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
    Iterator<WhitePagesCard> it;
    if (userCards != null) {
      it = userCards.iterator();
      if (it.hasNext()) {
        WhitePagesCard wpc = it.next();
        card = getCardReadOnly(wpc.getUserCardId());
      }
    }
    return card;
  }

  /**
   * Charge la liste des fiches inter-instance portant sur le user (et non masquées sauf instance
   * courante)et la met en session si le user est différent du user courant
   * (currentCard.getUserId()) Sinon retourne currentUserCards (Collection de WhitePagesCard)
   * @param userId id d'un user
   */
  private Collection<WhitePagesCard> getHomeWhitePagesCards(String userId)
      throws WhitePagesException {
    if (currentCard == null || !currentCard.getUserId().equals(userId) ||
        getCurrentUserCards().isEmpty()) {
      Collection<WhitePagesCard> cards =
          getCardManager().getHomeUserCards(userId, getUserInstanceIds(), getComponentId());
      Collections.sort((List) cards);
      setCurrentUserCards(cards);
    }
    return getCurrentUserCards();
  }

  /**
   * Charge la liste des fiches inter-instance portant sur le user (et non masquées) et la met en
   * session si le user est différent du user courant (currentCard.getUserId()) Sinon retourne
   * currentUserCards (Collection de WhitePagesCard)
   * @param userId id d'un user
   */
  private Collection<WhitePagesCard> getWhitePagesCards(String userId) throws WhitePagesException {
    if (currentCard == null || !currentCard.getUserId().equals(userId) ||
        getCurrentUserCards().isEmpty()) {
      Collection<WhitePagesCard> cards =
          getCardManager().getUserCards(userId, getUserInstanceIds());
      Collections.sort((List) cards);
      setCurrentUserCards(cards);
    }
    return getCurrentUserCards();
  }

  /**
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
    setCurrentUserCards(new ArrayList<>());
    getWhitePagesCards(userDetail.getId());
    ((ArrayList<WhitePagesCard>) getCurrentUserCards())
        .add(0, new WhitePagesCard("Fiche en cours de création"));
    return getCurrentCreateCard();
  }

  /**
   * Affecte un DataRecord vide (cardTemplate.getRecordset().getEmptyRecord()), le cardViewForm
   * (cardTemplate.getViewForm()) et le cardUpdateForm à la fiche courante Retourne la fiche
   * courante
   */
  public Card setCardRecord() throws WhitePagesException {
    try {
      getCurrentCreateCard().writeCardUpdateForm(getCardTemplate().getUpdateForm());
      getCurrentCreateCard().writeCardRecord(getCardTemplate().getRecordSet().getEmptyRecord());
      getCurrentCreateCard().writeCardViewForm(getCardTemplate().getViewForm());
      return getCurrentCreateCard();
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.setCardRecord",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException("WhitePagesSessionController.setCardRecord",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }
  }

  /**
   * Rempli le DataRecord de la fiche courante en cours de création à partir de la request
   * @throws WhitePagesException
   */
  public void createCard(HttpServletRequest request) throws WhitePagesException {

    List<FileItem> items = HttpRequest.decorate(request).getFileItems();

    // get PDC classification
    String positions = FileUploadUtil.getParameter(items, "Positions");
    PdcClassification withClassification = null;
    PdcClassificationEntity classification;
    if (StringUtil.isDefined(positions)) {
      classification = PdcClassificationEntity.fromJSON(positions);

      List<PdcPosition> pdcPositions = classification.getPdcPositions();
      withClassification =
          aPdcClassificationOfContent(getCurrentCard().getPK().getId(), getComponentId())
              .withPositions(pdcPositions);
    }

    /*
     * Stores card, identity and data record.
     */
    insertCard(withClassification);

    // update form
    try {
      PagesContext pageContext = new PagesContext("", getLanguage());
      pageContext.setComponentId(getComponentId());
      pageContext.setObjectId(getCurrentCard().getPK().getId());
      pageContext.setUserId(getUserId());
      getCardTemplate().getUpdateForm()
          .update(items, getCurrentCreateCard().readCardRecord(), pageContext);
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.setCardRecord",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (Exception e) {
      throw new WhitePagesException("WhitePagesSessionController.setCardRecord",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }

    // save form
    saveCard();
  }

  /**
   * Rempli le DataRecord de la fiche courante à partir de la request
   */
  public void updateCardRecord(HttpServletRequest request) throws WhitePagesException {
    try {
      List<FileItem> items = HttpRequest.decorate(request).getFileItems();
      PagesContext pageContext = new PagesContext("", getLanguage());
      pageContext.setComponentId(getComponentId());
      pageContext.setObjectId(getCurrentCard().getPK().getId());
      pageContext.setUserId(getUserId());
      getCardTemplate().getUpdateForm()
          .update(items, getCurrentCard().readCardRecord(), pageContext);
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.updateCardRecord",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (Exception e) {
      throw new WhitePagesException("WhitePagesSessionController.updateCardRecord",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }

  }

  /**
   * Enregistre et crée la fiche courante : enregistrement de la fiche
   * (CardManager.create(currentCard)), recupération de l'id de la fiche créée (userCardId) et set
   * de l'id de la fiche courante Enregistre les données du modèle de la fiche :
   * currentCard.readCardRecord().setId(userCardId), saveCard()
   */
  private void insertCard(PdcClassification classification) throws WhitePagesException {
    try {
      String userCardId = Long.toString(
          getCardManager().create(getCurrentCreateCard(), getUserId(), classification));
      getCurrentCreateCard().readCardRecord().setId(userCardId);
      getCardTemplate().getRecordSet().save(getCurrentCreateCard().readCardRecord());
      setCurrentUserCards(new ArrayList<WhitePagesCard>());
      SilverTrace.spy("whitePages", "WhitePagesSessionController.insertCard", getSpaceId(),
          getComponentId(), userCardId, getUserDetail().getId(), SilverTrace.SPY_ACTION_CREATE);

    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.insertCard",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException("WhitePagesSessionController.insertCard",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }

  }

  /**
   * Enregistre la fiche courante : Enregistre les données du modèle de la fiche
   * (cardTemplate.getRecordset().save(currentCard.readCardRecord()))
   */
  public void saveCard() throws WhitePagesException {
    try {
      getCurrentCard().readCardRecord().setId(getCurrentCard().getPK().getId());
      getCardTemplate().getRecordSet().save(getCurrentCard().readCardRecord());
      getCardManager().indexCard(getCurrentCard());
      SilverTrace
          .spy("whitePages", "WhitePagesSessionController.saveCard", getSpaceId(), getComponentId(),
              getCurrentCard().getPK().getId(), getUserDetail().getId(),
              SilverTrace.SPY_ACTION_UPDATE);
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.saveCard",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException("WhitePagesSessionController.saveCard",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }

  }

  /**
   * Liste les fiches de l'annuaire
   * @return la liste de toutes les fiches de l'annuaire (Collection de Card)
   */
  public Collection<Card> getCards() throws WhitePagesException {
    return setUserRecords(getCardManager().getCards(getComponentId()));
  }

  /**
   * Liste les fiches de l'annuaire non masquées
   * @return la liste de toutes les fiches de l'annuaire (Collection de Card) non masquées
   * (hideStatus = 0)
   */
  public Collection<Card> getVisibleCards() throws WhitePagesException {
    return setUserRecords(getCardManager().getVisibleCards(getComponentId()));
  }

  /**
   * Affecte les UserRecord à chaque Card d'une liste
   */
  private Collection<Card> setUserRecords(Collection<Card> cards) throws WhitePagesException {
    List<Card> listCards = new ArrayList<>();
    try {
      if (cards != null) {
        for (Card card : cards) {
          if (getUserTemplate().getRecord(card.getUserId()).getUserDetail() == null) {
            // l'utilisateur n'existe plus
            String idCard = card.getPK().getId();
            List<String> listId = new ArrayList<>();
            listId.add(idCard);
            delete(listId);
          } else {
            card.writeUserRecord(getUserTemplate().getRecord(card.getUserId()));
            listCards.add(card);
          }
        }
      }
    } catch (WhitePagesException e) {
      throw new WhitePagesException("WhitePagesSessionController.setUserRecords",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_RECORD", "", e);
    }

    Collections.sort(listCards, new Comparator<Card>() {
      @Override
      public int compare(Card o1, Card o2) {
        int result = o1.readUserRecord().getUserDetail().getLastName()
            .compareTo(o2.readUserRecord().getUserDetail().getLastName());
        if (result == 0) {
          result = o1.readUserRecord().getUserDetail().getFirstName().compareTo(o2.readUserRecord().
              getUserDetail().getFirstName());
        }
        return result;
      }
    });

    return listCards;
  }

  /**
   * Supprime une liste de fiches de l'annuaire + liste des cardRecord correspondant
   * @param userCardIds liste des identifiants des fiches à supprimer
   */
  public void delete(Collection<String> userCardIds) throws WhitePagesException {

    try {
      if (userCardIds != null) {
        for (String userCardId : userCardIds) {
          DataRecord data = getCardTemplate().getRecordSet().getRecord(userCardId);
          getCardTemplate().getRecordSet().delete(data);
          SilverTrace.spy("whitePages", "WhitePagesSessionController.delete", getSpaceId(),
              getComponentId(), userCardId, getUserDetail().getId(), SilverTrace.SPY_ACTION_DELETE);
        }
        getCardManager().delete(userCardIds);
      }
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.delete", SilverpeasException.ERROR,
          "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    } catch (FormException e) {
      throw new WhitePagesException("WhitePagesSessionController.delete", SilverpeasException.ERROR,
          "whitePages.EX_CANT_GET_RECORD", "", e);
    }

  }

  /**
   * Masque une liste de fiches de l'annuaire
   * @param userCardIds liste des identifiants des fiches à masquer
   */
  public void hide(Collection<String> userCardIds) throws WhitePagesException {
    getCardManager().setHideStatus(userCardIds, 1);
  }

  /**
   * De Masque une liste de fiches de l'annuaire
   * @param userCardIds liste des identifiants des fiches à de masquer
   */
  public void unHide(Collection<String> userCardIds) throws WhitePagesException {
    getCardManager().setHideStatus(userCardIds, 0);
  }

  /**
   * Reverse le statut Masqué d'une liste de fiches de l'annuaire
   * @param userCardIds liste des identifiants des fiches
   */
  public void reverseHide(Collection<String> userCardIds) throws WhitePagesException {
    getCardManager().reverseHide(userCardIds);
    setCurrentCard(null);
  }

  /**
   * Indique si un utilisateur possède déjà une fiche dans l'annuaire courant
   * @param userId l'identifiant d'un utilisateur
   */
  public boolean existCard(String userId) throws WhitePagesException {
    return getCardManager().existCard(userId, getComponentId());
  }

  /**
   * retourne la valeur d'un paramètre affecté lors de l'instanciation
   */
  private String getParam(String paramName) {
    return getComponentParameterValue(paramName);
  }

  /**
   * retourne la valeur d'un paramètre affecté lors de l'instanciation pour un annuaire donné
   */
  private String getParam(String paramName, String instanceId) {
    return AdministrationServiceProvider.getAdminService()
        .getComponentParameterValue(instanceId, paramName);
  }

  /**
   * Retourne le cardTemplate d'un annuaire :
   * PublicationTemplateManager#getPublicationTemplate(instanceId, getParam("cardTemplate",
   * instanceId))
   */
  private PublicationTemplate getTemplate(String instanceId) throws WhitePagesException {
    try {
      return PublicationTemplateManager.getInstance()
          .getPublicationTemplate(instanceId, getParam("cardTemplate", instanceId));
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.getTemplate",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    }
  }

  /**
   * Retourne le userTemplate d'un annuaire :
   * PublicationTemplateManager#getPublicationTemplate(instanceId, getParam("userTemplate",
   * instanceId))
   */
  private UserTemplate getUserTemplate(String instanceId) {
    SettingBundle templateSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.whitePages.settings.template");
    String templateDir = templateSettings.getString("templateDir");
    return new UserTemplate(templateDir.replace('\\', '/') + "/" +
        getParam("userTemplate", instanceId).replace('\\', '/'), getLanguage());
  }

  /**
   * Appel UserPannel pour set des users selectionnable (4 [] vides) :
   */
  public String initUserPanel() {
    String mContext = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
    String hostSpaceName = getSpaceLabel();
    Pair<String, String> hostComponentName =
        new Pair<>(getComponentLabel(), mContext + "/RwhitePages/" + getComponentId() + "/Main");
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] =
        new Pair<>(getString("whitePages.usersList"), "/RwhitePages/" + getComponentId() + "/Main");
    String hostUrl = mContext + "/RwhitePages/" + getComponentId() + "/createIdentity";

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

    return Selection.getSelectionURL();
  }

  /**
   * Appel UserPannel pour récup du user sélectionné : UserDetail[]
   * UserPanel.getUserDetailSelected()
   */
  public UserDetail getUserDetailSelected() {
    UserDetail user = null;
    String selUser = getSelection().getFirstSelectedElement();
    if (StringUtil.isDefined(selUser)) {
      user = getOrganisationController().getUserDetail(selUser);
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
    this.currentUserCards = new ArrayList<>();
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
        cardTemplate = PublicationTemplateManager.getInstance()
            .getPublicationTemplate(getComponentId(), getParam("cardTemplate"));
      }
      return cardTemplate;
    } catch (PublicationTemplateException e) {
      throw new WhitePagesException("WhitePagesSessionController.getCardTemplate",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_PUBLICATIONTEMPLATE", "", e);
    }
  }

  public void setHostParameters(String hostSpaceName, String hostComponentName, String hostUrl,
      String hostPath) {
    hostParameters = new String[4];
    hostParameters[0] = hostComponentName;
    hostParameters[1] = hostUrl;
    hostParameters[2] = hostSpaceName;
    hostParameters[3] = hostPath;
  }

  private Collection<String> getUserInstanceIds() {
    if (userInstanceIds == null) {
      userInstanceIds = new ArrayList<>();
      CompoSpace[] instances =
          getOrganisationController().getCompoForUser(getUserId(), "whitePages");
      for (CompoSpace instance : instances) {
        userInstanceIds.add(instance.getComponentId());
      }
    }
    return userInstanceIds;
  }

  private UserTemplate getUserTemplate() {
    if (userTemplate == null) {
      SettingBundle templateSettings =
          ResourceLocator.getSettingBundle("org.silverpeas.whitePages.settings.template");
      String templateDir = templateSettings.getString("templateDir");
      this.userTemplate = new UserTemplate(
          templateDir.replace('\\', '/') + "/" + getParam("userTemplate").replace('\\', '/'),
          getLanguage());
    }
    return this.userTemplate;
  }

  public int getSilverObjectId(String objectId) {
    return Integer.parseInt(getCurrentCardContentId());
  }

  /*-------------- Methodes de la classe ------------------*/
  public WhitePagesSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.whitePages.multilang.whitePagesBundle",
        "org.silverpeas.whitePages.settings.whitePagesIcons",
        "org.silverpeas.whitePages.settings.settings");
  }

  public String getCurrentCardContentId() {
    String contentId = null;

    if (currentCard != null) {
      try {
        ContentManager contentManager = ContentManagerProvider.getContentManager();
        contentId = "" + contentManager
            .getSilverContentId(currentCard.getPK().getId(), currentCard.getInstanceId());
      } catch (ContentManagerException ignored) {
        SilverTrace.error("whitePages", "WhitePagesSessionController",
            "whitePages.EX_UNKNOWN_CONTENT_MANAGER", ignored);
        contentId = null;
      }
    }

    return contentId;
  }

  public void setNotifiedUserCard(Card card) {
    this.notifiedUserCard = card;
  }

  public void sendNotification(String bodyMessage) throws NotificationManagerException {
    String url = URLUtil.getURL(null, getComponentId()) + "consultIdentity?userCardId=" +
        notifiedUserCard.getPK().getId();

    LocalizationBundle message = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.whitePages.multilang.whitePagesBundle",
        DisplayI18NHelper.getDefaultLanguage());

    String subject = message.getString("whitePages.notificationTitle");
    NotificationMetaData notifMetaData =
        new NotificationMetaData(NotificationParameters.NORMAL, subject, bodyMessage);


    for (String language : DisplayI18NHelper.getLanguages()) {
      message = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.whitePages.multilang.whitePagesBundle", language);
      subject = message.getString("whitePages.notificationTitle");
      notifMetaData.addLanguage(language, subject, bodyMessage);

      Link link = new Link(url, message.getString("whitePages.notifLinkLabel"));
      notifMetaData.setLink(link, language);
    }

    notifMetaData.addUserRecipient(new UserRecipient(notifiedUserCard.getUserId()));
    notifMetaData.setAnswerAllowed(false);
    notifMetaData.setComponentId(getComponentId());
    notifMetaData.setDate(new Date());
    notifMetaData.setSender(getSettings().getString("whitePages.genericUserId"));

    NotificationSender sender = new NotificationSender(getComponentId());
    sender.notifyUser(notifMetaData);
  }

  public Boolean isEmailHidden() {
    // pour cacher ou non l'email pour les lecteurs
    return "yes".equalsIgnoreCase(getComponentParameterValue("isEmailHidden"));
  }

  public Boolean isFicheVisible() {
    // pour afficher ou non l'onglet fiche pour les lecteurs
    return "no".equalsIgnoreCase(getComponentParameterValue("isFicheVisible"));
  }

  private String getDomainId() {
    String domainId = getComponentParameterValue("domainId");
    return StringUtil.isDefined(domainId) ? domainId:"0";
  }

  public List<FieldTemplate> getAllXmlFieldsForSearch()
      throws WhitePagesException, PublicationTemplateException {
    PublicationTemplate template = getTemplate(getComponentId());
    RecordTemplate recordTemplate = template.getRecordTemplate();
    try {
      FieldTemplate[] fields = recordTemplate.getFieldTemplates();
      return Arrays.asList(fields);
    } catch (FormException e) {
      SilverTrace.error("whitePages", "WhitePagesSessionController.getAllXmlFieldsForSearch",
          "whitePages.CANT_GET_XML_FIELDS", e);
    }
    return new ArrayList<>();
  }

  public List<SearchField> getLdapAttributesList() throws Exception {
    Map<String, String> properties = getDomainProperties();
    List<SearchField> fields = new ArrayList<>();
    for (Map.Entry<String, String> prop : properties.entrySet()) {
      SearchField field = new SearchField();
      field.setFieldId(SearchFieldsType.LDAP.getLabelType() + prop.getKey());
      field.setLabel(prop.getValue());
      fields.add(field);
    }
    return fields;
  }

  private Map<String, String> getDomainProperties() throws Exception {
    return mDDManager.getDomainDriver(getDomainId()).getPropertiesLabels(getLanguage());
  }

  public void confirmFieldsChoice(String[] fields) {
    WhitePageServiceProvider.getWhitePagesService().createSearchFields(fields, getComponentId());
  }

  public SortedSet<SearchField> getSearchFields() throws WhitePagesException {
    SortedSet<SearchField> fields =
        WhitePageServiceProvider.getWhitePagesService().getSearchFields(getComponentId());
    if (!fields.isEmpty()) {
      PublicationTemplate template = null;
      Map<String, String> domainProperties = null;
      try {
        RecordTemplate recordTemplate = null;
        for (SearchField field : fields) {
          if (field.getFieldId().startsWith(SearchFieldsType.XML.getLabelType())) {
            if (template == null) {
              template = getTemplate(getComponentId());
              recordTemplate = template.getRecordTemplate();
            }
            field.setLabel(
                recordTemplate.getFieldTemplate(field.getFieldName()).getLabel(getLanguage()));
          } else if (field.getFieldId().startsWith(SearchFieldsType.LDAP.getLabelType())) {
            if (domainProperties == null) {
              domainProperties = getDomainProperties();
            }
            field.setLabel(domainProperties.get(field.getFieldName()));
          } else if (field.getFieldId().startsWith(SearchFieldsType.USER.getLabelType())) {
            if ("name".equals(field.getFieldName())) {
              field.setLabel(ResourceLocator.getGeneralLocalizationBundle(getLanguage())
                  .getString("GML.lastName"));
            } else if ("surname".equals(field.getFieldName())) {
              field.setLabel(ResourceLocator.getGeneralLocalizationBundle(getLanguage())
                  .getString("GML.surname"));
            } else if ("email".equals(field.getFieldName())) {
              field.setLabel(ResourceLocator.getGeneralLocalizationBundle(getLanguage())
                  .getString("GML.eMail"));
            }
          }
        }
      } catch (Exception e) {
        SilverTrace.error("whitePages", "WhitePagesSessionController.getSearchFields",
            "whitePages.CANT_GET_XML_FIELDS", e);
      }
    }
    return fields;
  }

  public Set<String> getSearchFieldIds() throws WhitePagesException {
    Set<String> ids = new HashSet<>();
    SortedSet<SearchField> searchFields = getSearchFields();
    if (searchFields != null && !searchFields.isEmpty()) {
      for (SearchField field : searchFields) {
        ids.add(field.getFieldId());
      }
    }
    return ids;
  }

  public List<Card> getSearchResult(String query, String taxonomyPosition,
      Map<String, String> xmlFields, List<FieldDescription> fieldsQuery) {
    List<Card> cards = new ArrayList<>();
    List<SearchResult> results = null;

    try {
      String xmlTemplate = "whitePages";
      results = WhitePageServiceProvider.getMixedSearchService()
          .search(getComponentId(), getUserId(), query, taxonomyPosition, xmlFields,
              xmlTemplate, fieldsQuery, getLanguage());
    } catch (Exception e) {

    }

    if (results != null) {
      try {
        Collection<Card> allCars = getCards();
        HashMap<String, Card> map = new HashMap<>();
        for (Card card : allCars) {
          map.put(card.getPK().getId(), card);
        }

        for (SearchResult result : results) {
          if (map.containsKey(result.getId())) {
            cards.add(map.get(result.getId()));
          }
        }
      } catch (Exception e) {

      }
    }
    return cards;
  }

  private PdcManager getPdcManager() {
    if (pdcManager == null) {
      pdcManager = new GlobalPdcManager();
    }
    return pdcManager;
  }

  public HashMap<String, Set<ClassifyValue>> getPdcPositions(int cardId) throws PdcException {

    HashMap<String, Set<ClassifyValue>> result = new HashMap<>();
    List<ClassifyPosition> listOfPositions = getPdcManager().getPositions(cardId, getComponentId());

    if (listOfPositions != null && listOfPositions.size() > 0) {
      for (ClassifyPosition position : listOfPositions) {
        for (ClassifyValue value : position.getValues()) {
          List<Value> path = value.getFullPath();
          if (path != null && !path.isEmpty()) {
            Value axis = path.get(0);
            String category = axis.getName(getLanguage());
            if (result.containsKey(category)) {
              result.get(category).add(value);
            } else {
              Set<ClassifyValue> values = new HashSet<>();
              values.add(value);
              result.put(category, values);
            }
          }
        }
      }
    }
    return result;

  }
}
