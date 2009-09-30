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
package com.silverpeas.whitePages.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.whitePages.WhitePagesException;
import com.silverpeas.whitePages.model.Card;
import com.silverpeas.whitePages.model.WhitePagesCard;
import com.silverpeas.whitePages.record.UserRecord;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

public class CardManager {
  private static CardManager instance;
  private WhitePagesContentManager contentManager = null;

  private CardManager() {
  }

  private WhitePagesContentManager getWhitePagesContentManager() {
    if (contentManager == null) {
      contentManager = new WhitePagesContentManager();
    }
    return contentManager;
  }

  static public CardManager getInstance() {
    if (instance == null)
      instance = new CardManager();
    return instance;
  }

  public long create(Card card, String spaceId, String creatorId)
      throws WhitePagesException {
    SilverpeasBeanDAO dao;
    long id = -1;

    Connection con = null;
    try {
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.whitePages.model.Card");
      con = DBUtil.makeConnection(JNDINames.WHITEPAGES_DATASOURCE);
      con.setAutoCommit(false);

      card.setCreationDate(DateUtil.date2SQLDate(new Date()));
      card.setCreatorId(new Integer(creatorId).intValue());

      WAPrimaryKey pk = dao.add(con, card);
      id = new Long(pk.getId()).longValue();
      card.setPK(pk);

      getWhitePagesContentManager().createSilverContent(con, card);

      indexCard(card);
      con.commit();
    } catch (Exception e) {
      rollback(con, e);
    } finally {
      closeConnection(con);
    }

    return id;
  }

  public void delete(Collection ids, String spaceId) throws WhitePagesException {
    Connection con = null;

    if (ids != null) {
      Iterator it = ids.iterator();
      try {
        con = DBUtil.makeConnection(JNDINames.WHITEPAGES_DATASOURCE);
        con.setAutoCommit(false);

        SilverpeasBeanDAO dao;
        dao = SilverpeasBeanDAOFactory
            .getDAO("com.silverpeas.whitePages.model.Card");

        String id = null;
        IdPK pk = new IdPK();
        String peasId = null;

        while (it.hasNext()) {
          id = (String) it.next();
          pk.setId(id);

          // le premier element donne l'id de l'instance.
          if (peasId == null) {
            Card card = getCard(new Long(pk.getId()).longValue());
            if (card == null)
              continue;
            else {
              peasId = card.getInstanceId();
            }
          }

          dao.remove(con, pk);

          // suppression de la reference par le content maneger.
          pk.setComponentName(peasId);
          getWhitePagesContentManager().deleteSilverContent(con, pk);

          con.commit();
          deleteIndex(pk, spaceId);
        }
      } catch (Exception e) {
        rollback(con, e);
      } finally {
        closeConnection(con);
      }
    }
  }

  public Card getCard(long id) throws WhitePagesException {
    Card result = null;
    SilverpeasBeanDAO dao;
    IdPK pk = new IdPK();
    try {
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.whitePages.model.Card");
      pk.setIdAsLong(id);
      result = (Card) dao.findByPrimaryKey(pk);
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.getCard",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_CARD", "", e);
    }
    return result;
  }

  public Collection getCards(String instanceId) throws WhitePagesException {
    String where = " instanceId = '" + instanceId + "'";
    Collection cards = new ArrayList();
    try {
      SilverpeasBeanDAO dao;
      IdPK pk = new IdPK();
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.whitePages.model.Card");
      cards = dao.findByWhereClause(pk, where);
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.getCards",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_CARDS", "", e);
    }
    return cards;
  }

  public Collection getCardsByIds(ArrayList ids) throws WhitePagesException {
    StringBuffer where = new StringBuffer();
    int sizeOfIds = ids.size();
    for (int i = 0; i < sizeOfIds - 1; i++) {
      where.append(" id = " + (String) ids.get(i) + " or ");
    }
    if (sizeOfIds != 0) {
      where.append(" id = " + (String) ids.get(sizeOfIds - 1));
    }

    Collection cards = new ArrayList();
    try {
      SilverpeasBeanDAO dao;
      IdPK pk = new IdPK();
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.whitePages.model.Card");
      cards = dao.findByWhereClause(pk, where.toString());
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.getCards",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_CARDS", "", e);
    }
    return cards;
  }

  public Collection getVisibleCards(String instanceId)
      throws WhitePagesException {
    String where = " instanceId = '" + instanceId + "' and hideStatus = 0";
    Collection cards = new ArrayList();
    try {
      SilverpeasBeanDAO dao;
      IdPK pk = new IdPK();
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.whitePages.model.Card");
      cards = dao.findByWhereClause(pk, where);
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.getVisibleCards",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_CARDS", "", e);
    }
    return cards;
  }

  public Collection getUserCards(String userId, Collection instanceIds)
      throws WhitePagesException {
    String where = " userId = '" + userId + "' and hideStatus = 0";
    Collection wpcards = new ArrayList();
    if (instanceIds != null) {
      Iterator it = instanceIds.iterator();
      if (it.hasNext()) {
        where += " and instanceId IN (";
        String id = (String) it.next();
        where += "'" + id + "'";
        while (it.hasNext()) {
          id = (String) it.next();
          where += ", '" + id + "'";
        }
        where += ")";
        try {
          SilverpeasBeanDAO dao;
          IdPK pk = new IdPK();
          dao = SilverpeasBeanDAOFactory
              .getDAO("com.silverpeas.whitePages.model.Card");
          Collection cards = dao.findByWhereClause(pk, where);
          if (cards != null) {
            Iterator itCard = cards.iterator();
            while (itCard.hasNext()) {
              Card card = (Card) itCard.next();
              wpcards.add(new WhitePagesCard(new Long(card.getPK().getId())
                  .longValue(), card.getInstanceId()));
            }
          }
        } catch (PersistenceException e) {
          throw new WhitePagesException("CardManager.getUserCards",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_USERCARDS",
              "", e);
        }
      }

    }
    return wpcards;
  }

  public Collection getHomeUserCards(String userId, Collection instanceIds,
      String instanceId) throws WhitePagesException {
    String where = " userId = '" + userId + "' and ((instanceId = '"
        + instanceId + "') or (hideStatus = 0";
    Collection wpcards = new ArrayList();
    if (instanceIds != null) {
      Iterator it = instanceIds.iterator();
      if (it.hasNext()) {
        where += " and instanceId IN (";
        String id = (String) it.next();
        where += "'" + id + "'";
        while (it.hasNext()) {
          id = (String) it.next();
          where += ", '" + id + "'";
        }
        where += ")))";
        try {
          SilverpeasBeanDAO dao;
          IdPK pk = new IdPK();
          dao = SilverpeasBeanDAOFactory
              .getDAO("com.silverpeas.whitePages.model.Card");
          Collection cards = dao.findByWhereClause(pk, where);
          if (cards != null) {
            Iterator itCard = cards.iterator();
            while (itCard.hasNext()) {
              Card card = (Card) itCard.next();
              wpcards.add(new WhitePagesCard(new Long(card.getPK().getId())
                  .longValue(), card.getInstanceId()));
            }
          }
        } catch (PersistenceException e) {
          throw new WhitePagesException("CardManager.getHomeUserCards",
              SilverpeasException.ERROR, "whitePages.EX_CANT_GET_USERCARDS",
              "", e);
        }
      }

    }
    return wpcards;
  }

  public void setHideStatus(Collection ids, int status)
      throws WhitePagesException {
    if (ids != null) {
      Iterator it = ids.iterator();
      try {
        SilverpeasBeanDAO dao;
        dao = SilverpeasBeanDAOFactory
            .getDAO("com.silverpeas.whitePages.model.Card");
        while (it.hasNext()) {
          long id = new Long((String) it.next()).longValue();
          IdPK pk = new IdPK();
          pk.setIdAsLong(id);
          Card card = (Card) dao.findByPrimaryKey(pk);
          card.setHideStatus(status);
          dao.update(card);

          card.getPK().setComponentName(card.getInstanceId());
          getWhitePagesContentManager().updateSilverContentVisibility(card);
        }
      } catch (Exception e) {
        throw new WhitePagesException("CardManager.setHideStatus",
            SilverpeasException.ERROR, "whitePages.EX_UPDATE_CARDS_FAILED", "",
            e);
      }
    }
  }

  public void reverseHide(Collection ids) throws WhitePagesException {
    if (ids != null) {
      Iterator it = ids.iterator();
      try {
        SilverpeasBeanDAO dao;
        dao = SilverpeasBeanDAOFactory
            .getDAO("com.silverpeas.whitePages.model.Card");
        while (it.hasNext()) {
          long id = new Long((String) it.next()).longValue();
          IdPK pk = new IdPK();
          pk.setIdAsLong(id);
          Card card = (Card) dao.findByPrimaryKey(pk);
          int status = card.getHideStatus();
          if (status == 0)
            status = 1;
          else
            status = 0;
          card.setHideStatus(status);
          dao.update(card);

          card.getPK().setComponentName(card.getInstanceId());
          getWhitePagesContentManager().updateSilverContentVisibility(card);
        }
      } catch (Exception e) {
        throw new WhitePagesException("CardManager.reverseHide",
            SilverpeasException.ERROR, "whitePages.EX_UPDATE_CARDS_FAILED", "",
            e);
      }
    }
  }

  public boolean existCard(String userId, String instanceId)
      throws WhitePagesException {
    String where = " instanceId = '" + instanceId + "' and userId = '" + userId
        + "'";
    boolean exist = false;
    try {
      SilverpeasBeanDAO dao;
      IdPK pk = new IdPK();
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.whitePages.model.Card");
      Collection cards = dao.findByWhereClause(pk, where);
      if ((cards != null) && (cards.size() > 0))
        exist = true;
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.existCard",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_CARDS", "", e);
    }
    return exist;
  }

  public boolean isPublicationClassifiedOnPDC(Card card)
      throws ContentManagerException, PdcException {
    ContentManager contentManager = new ContentManager();
    int contentId = contentManager.getSilverContentId(card.getPK().getId(),
        card.getInstanceId());
    PdcBm pdcBm = new PdcBmImpl();

    List positions = pdcBm.getPositions(contentId, card.getInstanceId());
    return (positions.size() > 0);
  }

  /**
   * Get card for a user and instance.
   * 
   * @param userId
   *          user id
   * @param instanceId
   *          instance id
   * @return the card, null if not found
   * @throws WhitePagesException
   */
  public Card getUserCard(String userId, String instanceId)
      throws WhitePagesException {
    String where = " instanceId = '" + instanceId + "' and userId = '" + userId
        + "'";
    Card card = null;
    try {
      SilverpeasBeanDAO dao;
      IdPK pk = new IdPK();
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.whitePages.model.Card");
      Collection cards = dao.findByWhereClause(pk, where);
      if ((cards != null) && (cards.size() > 0))
        card = (Card) cards.iterator().next();
    } catch (PersistenceException e) {
      throw new WhitePagesException("CardManager.getUserCard",
          SilverpeasException.ERROR, "whitePages.EX_CANT_GET_CARDS", "", e);
    }
    return card;
  }

  /*
   * public void indexVisibleCards(String instanceId) throws WhitePagesException
   * { Iterator cards = getVisibleCards(instanceId).iterator(); Card card =
   * null; while (cards.hasNext()) { card = (Card) cards.next();
   * indexCard(card); } }
   */

  public void indexCard(Card card) {
    WAPrimaryKey pk = card.getPK();
    String userName = extractUserName(card);
    String userMail = extractUserMail(card);
    // String userInfo = extractUserInfo(card);

    FullIndexEntry indexEntry = new FullIndexEntry(card.getInstanceId(),
        "card", pk.getId());
    indexEntry.setTitle(userName);
    indexEntry.setKeyWords(userName);
    indexEntry.setPreView(userMail);
    indexEntry.setCreationDate(card.getCreationDate());
    indexEntry.setCreationUser(Integer.toString(card.getCreatorId()));
    // indexEntry.addTextContent(userInfo);

    try {
      PublicationTemplate pub = PublicationTemplateManager
          .getPublicationTemplate(card.getInstanceId());

      RecordSet set = pub.getRecordSet();
      set.indexRecord(pk.getId(), "", indexEntry);
    } catch (Exception e) {
      SilverTrace.error("whitePages", "CardManager.indexCard",
          "whitePages.EX_CANT_INDEX_CARD", e);
    }

    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private void deleteIndex(WAPrimaryKey pk, String spaceId) {
    IndexEngineProxy.removeIndexEntry(new IndexEntryPK(pk.getComponentName(),
        "card", pk.getId()));
  }

  private String extractUserName(Card card) {
    StringBuffer text = new StringBuffer("");

    UserRecord user = card.readUserRecord();
    Field f = null;

    if (user != null) {
      try {
        f = user.getField("FirstName");
        text.append(f.getStringValue());
        text.append(" ");
      } catch (FormException ignored) {
      }

      try {
        f = user.getField("LastName");
        text.append(f.getStringValue());
      } catch (FormException ignored) {
      }
    }
    return text.toString();
  }

  private String extractUserMail(Card card) {
    StringBuffer text = new StringBuffer("");

    UserRecord user = card.readUserRecord();
    Field f = null;

    if (user != null) {
      try {
        f = user.getField("Mail");
        text.append(f.getStringValue());
      } catch (FormException ignored) {
      }
    }

    return text.toString();
  }

  private void rollback(Connection con, Exception e) throws WhitePagesException {
    try {
      con.rollback();
    } catch (Exception e1) {
      throw new WhitePagesException("CardManager.create",
          SilverpeasException.ERROR, "whitePages.EX_CREATE_CARD_FAILED",
          "Error in rollback", e1);
    }

    throw new WhitePagesException("CardManager.create",
        SilverpeasException.ERROR, "whitePages.EX_CREATE_CARD_FAILED", "", e);
  }

  private void closeConnection(Connection con) throws WhitePagesException {
    if (con != null)
      try {
        con.close();
      } catch (SQLException e) {
        throw new WhitePagesException("CardManager.create",
            SilverpeasException.ERROR, "whitePages.EX_CREATE_CARD_FAILED", "",
            e);
      }
  }

}