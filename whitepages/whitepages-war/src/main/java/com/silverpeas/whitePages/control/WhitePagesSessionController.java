/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.whitePages.control;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementProvider;
import com.stratelia.silverpeas.pdc.control.PdcManager;
import org.silverpeas.util.StringUtil;
import org.silverpeas.servlet.FileUploadUtil;
import com.silverpeas.whitePages.WhitePagesException;
import com.silverpeas.whitePages.model.Card;
import com.silverpeas.whitePages.model.SearchField;
import com.silverpeas.whitePages.model.SearchFieldsType;
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
import com.stratelia.silverpeas.pdc.control.GlobalPdcManager;
import com.stratelia.silverpeas.pdc.model.*;
import com.stratelia.silverpeas.peasCore.*;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.Pair;
import com.stratelia.webactiv.beans.admin.*;
import org.silverpeas.util.GeneralPropertiesManager;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.exception.UtilException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.search.indexEngine.model.FieldDescription;
import org.silverpeas.servlet.HttpRequest;

import static com.silverpeas.pdc.model.PdcClassification.aPdcClassificationOfContent;

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
  private PdcManager pdcManager = null;
  private static DomainDriverManager m_DDManager = new DomainDriverManager();

  public boolean isAdmin() {
    return Boolean.valueOf(getUserRoleLevel().equals("admin"));
  }

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
          // user can update card if he is admin or if it's his own card
          card.writeReadOnly(!isAdmin() && !getUserId().equals(card.getUserId()));
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
  public void createCard(HttpServletRequest request)
      throws WhitePagesException, JAXBException {

    List<FileItem> items = HttpRequest.decorate(request).getFileItems();

    // get PDC classification
    String positions = FileUploadUtil.getParameter(items, "Positions");
    PdcClassification withClassification = null;
    PdcClassificationEntity classification;
    if (StringUtil.isDefined(positions)) {
      classification = PdcClassificationEntity.fromJSON(positions);

      List<PdcPosition> pdcPositions = classification.getPdcPositions();
      withClassification = aPdcClassificationOfContent(getCurrentCard().getPK().getId(),
          getComponentId()).withPositions(pdcPositions);
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

    // save form
    saveCard();
  }

  /*
   * Rempli le DataRecord de la fiche courante à partir de la request
   */
  public void updateCardRecord(HttpServletRequest request)
      throws WhitePagesException {
    try {
      List<FileItem> items = HttpRequest.decorate(request).getFileItems();
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
  private void insertCard(PdcClassification classification) throws WhitePagesException {
    try {
      String userCardId = Long.toString(getCardManager().create(
          getCurrentCreateCard(), getUserId(), classification));
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

    Collections.sort(listCards, new Comparator<Card>() {
      @Override
      public int compare(Card o1, Card o2) {
        int result = o1.readUserRecord().getUserDetail().getLastName().compareTo(
            o2.readUserRecord().getUserDetail().getLastName());
        if (result == 0) {
          result = o1.readUserRecord().getUserDetail().getFirstName().compareTo(o2.readUserRecord().
              getUserDetail().getFirstName());
        }
        return result;
      }
    });

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
          DataRecord data = getCardTemplate().getRecordSet().getRecord(userCardId);
          getCardTemplate().getRecordSet().delete(data);
          SilverTrace.spy("whitePages", "WhitePagesSessionController.delete",
              getSpaceId(), getComponentId(), userCardId, getUserDetail().getId(),
              SilverTrace.SPY_ACTION_DELETE);
        }
        getCardManager().delete(userCardIds);
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
    return AdministrationServiceProvider.getAdminService().getComponentParameterValue(instanceId, paramName);
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
  private UserTemplate getUserTemplate(String instanceId) {
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
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(),
        m_context + "/RwhitePages/" + getComponentId() + "/Main");
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("whitePages.usersList"),
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
      CompoSpace[] instances = getOrganisationController().getCompoForUser(
          getUserId(), "whitePages");
      for (CompoSpace instance : instances) {
        userInstanceIds.add(instance.getComponentId());
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
  public WhitePagesSessionController(MainSessionController mainSessionCtrl, ComponentContext context) {
    super(mainSessionCtrl, context, "com.silverpeas.whitePages.multilang.whitePagesBundle",
        "com.silverpeas.whitePages.settings.whitePagesIcons",
        "com.silverpeas.whitePages.settings.settings");
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
    notifMetaData.setSender(getSettings().getString("whitePages.genericUserId"));
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
    if (StringUtil.isDefined(domainId)) {
      try {
        domainIdReturn = Integer.parseInt(domainId);
      } catch (NumberFormatException nexp) {
        SilverTrace.error("whitePages", "WhitePagesSessionController",
            "whitePages.EX_UNKNOWN_DOMAIN_ID", nexp);
      }
    }
    return domainIdReturn;

  }

  public List<FieldTemplate> getAllXmlFieldsForSearch() throws WhitePagesException,
      PublicationTemplateException {
    PublicationTemplate template = getTemplate(getComponentId());
    RecordTemplate recordTemplate = template.getRecordTemplate();
    try {
      FieldTemplate[] fields = recordTemplate.getFieldTemplates();
      return Arrays.asList(fields);
    } catch (FormException e) {
      SilverTrace.error("whitePages", "WhitePagesSessionController.getAllXmlFieldsForSearch",
          "whitePages.CANT_GET_XML_FIELDS", e);
    }
    return new ArrayList<FieldTemplate>();
  }

  public List<SearchAxis> getUsedAxisList(SearchContext searchContext, String axisType) throws
      PdcException {
    List<SearchAxis> searchAxis = getPdcManager().getPertinentAxisByInstanceId(searchContext, axisType,
        getComponentId());
    if (searchAxis != null && !searchAxis.isEmpty()) {
      for (SearchAxis axis : searchAxis) {
        axis.setValues(getPdcManager().getDaughters(Integer.toString(axis.getAxisId()), "0"));
      }
    }
    return searchAxis;
  }

  public List<SearchField> getLdapAttributesList() throws Exception {
    Map<String, String> properties = getDomainProperties();
    List<SearchField> fields = new ArrayList<SearchField>();
    for (String property : properties.keySet()) {
      SearchField field = new SearchField();
      field.setFieldId(SearchFieldsType.LDAP.getLabelType() + property);
      field.setLabel(properties.get(property));
      fields.add(field);
    }
    return fields;
  }

  private Map<String, String> getDomainProperties() throws Exception {
    return m_DDManager.getDomainDriver(getDomainId()).getPropertiesLabels(getLanguage());
  }

  public void confirmFieldsChoice(String[] fields) throws UtilException {
    ServicesFactory.getFactory().getWhitePagesService().createSearchFields(fields, getComponentId());
  }

  public SortedSet<SearchField> getSearchFields() throws UtilException, WhitePagesException {
    SortedSet<SearchField> fields =
        ServicesFactory.getFactory().getWhitePagesService().getSearchFields(getComponentId());
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
            field.setLabel(recordTemplate.getFieldTemplate(field.getFieldName()).getLabel(
                getLanguage()));
          } else if (field.getFieldId().startsWith(SearchFieldsType.LDAP.getLabelType())) {
            if (domainProperties == null) {
              domainProperties = getDomainProperties();
            }
            field.setLabel(domainProperties.get(field.getFieldName()));
          } else if (field.getFieldId().startsWith(SearchFieldsType.USER.getLabelType())) {
            if (field.getFieldName().equals("name")) {
              field.setLabel(GeneralPropertiesManager.getGeneralMultilang(getLanguage()).getString(
                  "GML.lastName"));
            } else if (field.getFieldName().equals("surname")) {
              field.setLabel(GeneralPropertiesManager.getGeneralMultilang(getLanguage()).getString(
                  "GML.surname"));
            } else if (field.getFieldName().equals("email")) {
              field.setLabel(GeneralPropertiesManager.getGeneralMultilang(getLanguage()).getString(
                  "GML.eMail"));
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

  public Set<String> getSearchFieldIds() throws UtilException, WhitePagesException {
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
      Map<String, String> xmlFields, List<FieldDescription> fieldsQuery) {
    List<Card> cards = new ArrayList<Card>();
    Collection<GlobalSilverContent> contents = null;

    try {
      PublicationTemplate template = getTemplate(getComponentId());
      String xmlTemplate = template.getName();
      contents = ServicesFactory.getFactory().getMixedSearchService().search(getSpaceId(),
          getComponentId(),
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
              SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
              Collection<SessionInfo> sessionInfos = sessionManagement.getConnectedUsersList();
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

  private PdcManager getPdcManager() {
    if (pdcManager == null) {
      pdcManager = (PdcManager) new GlobalPdcManager();
    }
    return pdcManager;
  }

  public HashMap<String, Set<ClassifyValue>> getPdcPositions(int cardId) throws PdcException {

    HashMap<String, Set<ClassifyValue>> result = new HashMap<String, Set<ClassifyValue>>();
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
              Set<ClassifyValue> values = new HashSet<ClassifyValue>();
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
