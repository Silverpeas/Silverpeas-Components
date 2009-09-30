package com.silverpeas.whitePages.control;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.whitePages.WhitePagesException;
import com.silverpeas.whitePages.model.Card;
import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * The whitePages implementation of ContentInterface.
 */
public class WhitePagesContentManager implements ContentInterface {
  /** Find all the SilverContent with the given SilverContentId */
  public List getSilverContentById(List ids, String peasId, String userId,
      List userRoles) {
    if (isUser(userRoles)) {
      boolean isAdmin = isAdmin(userRoles);
      return getHeaders(makeIdArray(ids), isAdmin, peasId);
    } else
      return new ArrayList();
  }

  private ArrayList makeIdArray(List idList) {
    ArrayList ids = new ArrayList();
    Iterator iter = idList.iterator();
    String id = null;
    while (iter.hasNext()) {
      int contentId = ((Integer) iter.next()).intValue();
      try {
        id = getContentManager().getInternalContentId(contentId);
        ids.add(id);
      } catch (ClassCastException ignored) {
        // ignore unknown item
      } catch (ContentManagerException ignored) {
        // ignore unknown item
      }
    }
    return ids;
  }

  private boolean isUser(List userRoles) {
    Iterator iter = userRoles.iterator();
    String role;
    while (iter.hasNext()) {
      role = (String) iter.next();
      if ("user".equals(role))
        return true;
      if ("admin".equals(role))
        return true;
    }
    return false;
  }

  private boolean isAdmin(List userRoles) {
    Iterator iter = userRoles.iterator();
    String role;
    while (iter.hasNext()) {
      role = (String) iter.next();
      if ("admin".equals(role))
        return true;
    }
    return false;
  }

  private List getHeaders(List ids, boolean isAdmin, String instanceId) {
    Card card;
    ArrayList headers = new ArrayList();
    try {
      ArrayList cards = (ArrayList) CardManager.getInstance().getCardsByIds(
          (ArrayList) ids);
      for (int i = 0; i < cards.size(); i++) {
        card = (Card) cards.get(i);
        headers.add(new CardHeader(new Long(card.getPK().getId()).longValue(),
            card, instanceId, card.getCreationDate(), new Integer(card
                .getCreatorId()).toString()));
      }
    } catch (WhitePagesException e) {
      // skip unknown and ill formed id.
    }

    Collections.sort(headers);
    return headers;
  }

  public int getSilverObjectId(String id, String peasId)
      throws WhitePagesException {
    SilverTrace.info("whitePages",
        "WhitePagesContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "id = " + id);
    try {
      return getContentManager().getSilverContentId(id, peasId);
    } catch (Exception e) {
      throw new WhitePagesException(
          "WhitePagesContentManager.getSilverObjectId()",
          SilverpeasException.ERROR,
          "whitePages.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * 
   * @param con
   *          a Connection
   * @param pubDetail
   *          the content to register
   * @param userId
   *          the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, Card card)
      throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(card));
    SilverTrace.info("whitePages",
        "WhitePagesContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
            + scv.toString());
    return getContentManager().addSilverContent(con, card.getPK().getId(),
        card.getInstanceId(), card.getUserId(), scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content
   * is a PublicationDetail
   * 
   * @param pubDetail
   *          the content
   * @param silverObjectId
   *          the unique identifier of the content
   */
  public void updateSilverContentVisibility(Card card)
      throws ContentManagerException {
    int silverContentId = getContentManager().getSilverContentId(
        card.getPK().getId(), card.getPK().getComponentName());
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(card));
    SilverTrace.info("whitePages",
        "WhitePagesContentManager.updateSilverContentVisibility()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
            + scv.toString());
    getContentManager().updateSilverContentVisibilityAttributes(scv,
        card.getPK().getComponentName(), silverContentId);
    ClassifyEngine.clearCache();
  }

  /**
   * delete a content. It is registered to contentManager service
   * 
   * @param con
   *          a Connection
   * @param pubPK
   *          the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, IdPK pk)
      throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(pk.getId(),
        pk.getComponentName());
    SilverTrace.info("whitePages",
        "WhitePagesContentManager.deleteSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "id = " + pk.getId() + ", contentId = "
            + contentId);
    getContentManager().removeSilverContent(con, contentId,
        pk.getComponentName());
  }

  private boolean isVisible(Card card) {
    return (card.getHideStatus() == 0);
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace.fatal("whitePages", "WhitePagesContentManager",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private ContentManager contentManager = null;
}