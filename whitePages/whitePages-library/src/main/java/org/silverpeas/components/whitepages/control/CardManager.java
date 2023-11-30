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
package org.silverpeas.components.whitepages.control;

import org.silverpeas.components.whitepages.WhitePagesException;
import org.silverpeas.components.whitepages.model.Card;
import org.silverpeas.components.whitepages.model.SilverCard;
import org.silverpeas.components.whitepages.model.WhitePagesCard;
import org.silverpeas.components.whitepages.record.UserRecord;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngineProvider;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class CardManager {

  @Inject
  private WhitePagesContentManager contentManager;

  protected CardManager() {
  }

  private WhitePagesContentManager getWhitePagesContentManager() {
    return contentManager;
  }

  public static CardManager getInstance() {
    return ServiceProvider.getService(CardManager.class);
  }

  public long create(Card card, String creatorId, PdcClassification classification)
      throws WhitePagesException {
    long id = -1;

    Connection con = null;
    try {
      SilverpeasBeanDAO dao = getCardDAO();
      con = DBUtil.openConnection();
      con.setAutoCommit(false);

      card.setCreationDate(DateUtil.date2SQLDate(new Date()));
      card.setCreatorId(Integer.parseInt(creatorId));

      WAPrimaryKey pk = dao.add(con, card);
      id = Long.parseLong(pk.getId());
      card.setPK(pk);

      int silverContentId = getWhitePagesContentManager().createSilverContent(con, card);

      indexCard(card);
      con.commit();

      // classify the contribution on the PdC if its classification is defined
      if (classification != null && !classification.isEmpty()) {
        SilverCard silverCard = new SilverCard(card, silverContentId);
        classification.classifyContent(silverCard);
      }

    } catch (Exception e) {
      rollback(con, e);
    } finally {
      closeConnection(con);
    }

    return id;
  }

  public void delete(Collection<String> ids) throws WhitePagesException {
    Connection con = null;

    if (ids != null) {
      try {
        con = DBUtil.openConnection();
        con.setAutoCommit(false);

        SilverpeasBeanDAO dao = getCardDAO();

        IdPK pk = new IdPK();
        String peasId = null;

        for (String id : ids) {
          pk.setId(id);

          // le premier element donne l'id de l'instance.
          if (peasId == null) {
            Card card = getCard(Long.parseLong(pk.getId()));
            if (card == null) {
              continue;
            } else {
              peasId = card.getInstanceId();
            }
          }
          dao.remove(con, pk);

          // suppression de la reference par le content maneger.
          pk.setComponentName(peasId);
          getWhitePagesContentManager().deleteSilverContent(con, pk);

          con.commit();
          deleteIndex(pk);
        }
      } catch (Exception e) {
        rollback(con, e);
      } finally {
        closeConnection(con);
      }
    }
  }

  public Card getCard(long id) throws WhitePagesException {
    Card result;
    IdPK pk = new IdPK();
    try {
      SilverpeasBeanDAO dao = getCardDAO();
      pk.setIdAsLong(id);
      result = (Card) dao.findByPrimaryKey(pk);
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.getCard", SilverpeasException.ERROR,
          "whitePages.EX_CANT_GET_CARD", "", e);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public Collection<Card> getCards(String instanceId) throws WhitePagesException {
    String where = " instanceId = '" + instanceId + "'";
    Collection<Card> cards;
    try {
      IdPK pk = new IdPK();
      SilverpeasBeanDAO dao = getCardDAO();
      cards = dao.findByWhereClause(pk, where);
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.getCards", SilverpeasException.ERROR,
          "whitePages.EX_CANT_GET_CARDS", "", e);
    }
    return cards;
  }

  @SuppressWarnings("unchecked")
  public Collection<Card> getCardsByIds(List<String> ids) throws WhitePagesException {
    StringBuilder where = new StringBuilder();
    int sizeOfIds = ids.size();
    for (int i = 0; i < sizeOfIds - 1; i++) {
      where.append(" id = ").append(ids.get(i)).append(" or ");
    }
    if (sizeOfIds != 0) {
      where.append(" id = ").append(ids.get(sizeOfIds - 1));
    }

    Collection<Card> cards;
    try {
      IdPK pk = new IdPK();
      SilverpeasBeanDAO dao = getCardDAO();
      cards = dao.findByWhereClause(pk, where.toString());
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.getCards", SilverpeasException.ERROR,
          "whitePages.EX_CANT_GET_CARDS", "", e);
    }
    return cards;
  }

  @SuppressWarnings("unchecked")
  public Collection<Card> getVisibleCards(String instanceId) throws WhitePagesException {
    String where = " instanceId = '" + instanceId + "' and hideStatus = 0";
    Collection<Card> cards;
    try {
      IdPK pk = new IdPK();
      SilverpeasBeanDAO dao = getCardDAO();
      cards = dao.findByWhereClause(pk, where);
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.getVisibleCards", SilverpeasException.ERROR,
          "whitePages.EX_CANT_GET_CARDS", "", e);
    }
    return cards;
  }

  public Collection<WhitePagesCard> getUserCards(String userId, Collection<String> instanceIds)
      throws WhitePagesException {
    StringBuilder where = new StringBuilder(" userId = '" + userId + "' and hideStatus = 0");
    if (instanceIds != null) {
      Iterator<String> it = instanceIds.iterator();
      if (it.hasNext()) {
        where.append(" and instanceId IN (");
        String id = it.next();
        where.append("'").append(id).append("'");
        for (String appId : instanceIds) {
          where.append(", '").append(appId).append("'");
        }
        where.append(")");
        return getWhitePagesCards(where.toString());
      }

    }
    return new ArrayList<>();
  }

  public Collection<WhitePagesCard> getHomeUserCards(String userId, Collection<String> instanceIds,
      String instanceId) throws WhitePagesException {
    StringBuilder where = new StringBuilder(
        " userId = '" + userId + "' and ((instanceId = '" + instanceId + "') or (hideStatus = 0");
    if (instanceIds != null && !instanceIds.isEmpty()) {
      Iterator<String> it = instanceIds.iterator();
      if (it.hasNext()) {
        where.append(" and instanceId IN (");
        String id = it.next();
        where.append("'").append(id).append("'");
        for (String appId : instanceIds) {
          where.append(", '").append(appId).append("'");
        }
        where.append(")))");
        return getWhitePagesCards(where.toString());
      }

    }
    return new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  private Collection<WhitePagesCard> getWhitePagesCards(String whereClause)
      throws WhitePagesException {
    Collection<WhitePagesCard> wpcards = new ArrayList<>();
    try {
      IdPK pk = new IdPK();
      SilverpeasBeanDAO dao = getCardDAO();
      Collection<Card> cards = dao.findByWhereClause(pk, whereClause);
      if (cards != null) {
        for (Card card : cards) {
          wpcards
              .add(new WhitePagesCard(Long.parseLong(card.getPK().getId()), card.getInstanceId()));
        }
      }
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.getWhitePagesCards", SilverpeasException.ERROR,
          "whitePages.EX_CANT_GET_USERCARDS", "", e);
    }
    return wpcards;
  }

  public void setHideStatus(Collection<String> ids, int status) throws WhitePagesException {
    if (ids != null) {
      try {
        SilverpeasBeanDAO dao = getCardDAO();
        for (String sId : ids) {
          long id = Long.parseLong(sId);
          IdPK pk = new IdPK();
          pk.setIdAsLong(id);
          Card card = (Card) dao.findByPrimaryKey(pk);
          card.setHideStatus(status);
          dao.update(card);

          card.getPK().setComponentName(card.getInstanceId());
          getWhitePagesContentManager().updateSilverContentVisibility(card);
        }
      } catch (Exception e) {
        throw new WhitePagesException("CardManager.setHideStatus", SilverpeasException.ERROR,
            "whitePages.EX_UPDATE_CARDS_FAILED", "", e);
      }
    }
  }

  public void reverseHide(Collection<String> ids) throws WhitePagesException {
    if (ids != null) {
      try {
        SilverpeasBeanDAO dao = getCardDAO();
        for (String sId : ids) {
          long id = Long.parseLong(sId);
          IdPK pk = new IdPK();
          pk.setIdAsLong(id);
          Card card = (Card) dao.findByPrimaryKey(pk);
          int status = card.getHideStatus();
          if (status == 0) {
            status = 1;
          } else {
            status = 0;
          }
          card.setHideStatus(status);
          dao.update(card);

          card.getPK().setComponentName(card.getInstanceId());
          getWhitePagesContentManager().updateSilverContentVisibility(card);
        }
      } catch (Exception e) {
        throw new WhitePagesException("CardManager.reverseHide", SilverpeasException.ERROR,
            "whitePages.EX_UPDATE_CARDS_FAILED", "", e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public boolean existCard(String userId, String instanceId) throws WhitePagesException {
    String where = " instanceId = '" + instanceId + "' and userId = '" + userId + "'";
    boolean exist = false;
    try {
      IdPK pk = new IdPK();
      SilverpeasBeanDAO dao = getCardDAO();
      Collection<Card> cards = dao.findByWhereClause(pk, where);
      if (cards != null && !cards.isEmpty()) {
        exist = true;
      }
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.existCard", SilverpeasException.ERROR,
          "whitePages.EX_CANT_GET_CARDS", "", e);
    }
    return exist;
  }

  public boolean isPublicationClassifiedOnPDC(Card card)
      throws ContentManagerException, PdcException {
    ContentManagementEngine contentMgtEngine = ContentManagementEngineProvider.getContentManagementEngine();
    int contentId = contentMgtEngine.getSilverContentId(card.getPK().getId(), card.getInstanceId());
    PdcManager pdcManager = PdcManager.get();

    List<ClassifyPosition> positions = pdcManager.getPositions(contentId, card.getInstanceId());
    return !positions.isEmpty();
  }

  /**
   * Get card for a user and instance.
   * @param userId user id
   * @param instanceId instance id
   * @return the card, null if not found
   * @throws WhitePagesException
   */
  @SuppressWarnings("unchecked")
  public Card getUserCard(String userId, String instanceId) throws WhitePagesException {
    String where = " instanceId = '" + instanceId + "' and userId = '" + userId + "'";
    Card card = null;
    try {
      IdPK pk = new IdPK();
      SilverpeasBeanDAO dao = getCardDAO();
      Collection<Card> cards = dao.findByWhereClause(pk, where);
      if (cards != null && !cards.isEmpty()) {
        card = cards.iterator().next();
      }
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.getUserCard", SilverpeasException.ERROR,
          "whitePages.EX_CANT_GET_CARDS", "", e);
    }
    return card;
  }

  public void indexCard(Card card) {
    WAPrimaryKey pk = card.getPK();
    String userName = extractUserName(card);
    String userMail = extractUserMail(card);

    FullIndexEntry indexEntry = new FullIndexEntry(new IndexEntryKey(card.getInstanceId(), "card"
        , pk.getId()));
    indexEntry.setTitle(userName);
    indexEntry.setKeywords(userName);
    indexEntry.setPreview(userMail);
    try {
      indexEntry.setCreationDate(DateUtil.parse(card.getCreationDate()));
    } catch (ParseException e) {
      SilverLogger.getLogger(this).warn(e);
    }
    indexEntry.setCreationUser(Integer.toString(card.getCreatorId()));

    try {
      PublicationTemplate pub =
          PublicationTemplateManager.getInstance().getPublicationTemplate(card.getInstanceId());

      final String xmlFormShortName = pub.getFileName()
          .substring(pub.getFileName().indexOf("/") + 1, pub.getFileName().indexOf("."));

      RecordSet set = pub.getRecordSet();
      set.indexRecord(pk.getId(), xmlFormShortName, indexEntry);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("User card indexation failure", e);
    }

    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private void deleteIndex(WAPrimaryKey pk) {
    IndexEngineProxy.removeIndexEntry(new IndexEntryKey(pk.getComponentName(), "card", pk.getId()));
  }

  private String extractUserName(Card card) {
    StringBuilder text = new StringBuilder("");

    UserRecord user = card.readUserRecord();
    Field f;

    if (user != null) {
      try {
        f = user.getField("FirstName");
        text.append(f.getStringValue());
        text.append(" ");
      } catch (FormException e) {
        SilverLogger.getLogger(this).warn(e);
      }

      try {
        f = user.getField("LastName");
        text.append(f.getStringValue());
      } catch (FormException e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }
    return text.toString();
  }

  private String extractUserMail(Card card) {
    StringBuilder text = new StringBuilder("");

    UserRecord user = card.readUserRecord();
    Field f;
    if (user != null) {
      try {
        f = user.getField("Mail");
        text.append(f.getStringValue());
      } catch (FormException e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }

    return text.toString();
  }

  private void rollback(Connection con, Exception e) throws WhitePagesException {
    try {
      con.rollback();
    } catch (Exception e1) {
      throw new WhitePagesException("CardManager.create", SilverpeasException.ERROR,
          "whitePages.EX_CREATE_CARD_FAILED", "Error in rollback", e1);
    }

    throw new WhitePagesException("CardManager.create", SilverpeasException.ERROR,
        "whitePages.EX_CREATE_CARD_FAILED", "", e);
  }

  private void closeConnection(Connection con) throws WhitePagesException {
    if (con != null) {
      try {
        con.close();
      } catch (SQLException e) {
        throw new WhitePagesException("CardManager.create", SilverpeasException.ERROR,
            "whitePages.EX_CREATE_CARD_FAILED", "", e);
      }
    }
  }

  private SilverpeasBeanDAO getCardDAO() throws PersistenceException {
    return SilverpeasBeanDAOFactory.getDAO("org.silverpeas.components.whitepages.model.Card");
  }
}
