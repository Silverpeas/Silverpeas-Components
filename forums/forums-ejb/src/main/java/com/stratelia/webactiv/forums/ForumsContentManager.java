/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.forums;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.forums.forumEntity.ejb.ForumDetail;
import com.stratelia.webactiv.forums.forumEntity.ejb.ForumPK;
import com.stratelia.webactiv.forums.forumsException.ForumsRuntimeException;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBMHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * The forums implementation of ContentInterface.
 */
public class ForumsContentManager implements ContentInterface {

  /**
   * Find all the SilverContent with the given list of SilverContentId
   * 
   * @param ids
   *          list of silverContentId to retrieve
   * @param peasId
   *          the id of the instance
   * @param userId
   *          the id of the user who wants to retrieve silverContent
   * @param userRoles
   *          the roles of the user
   * @return a List of SilverContent
   */
  public List getSilverContentById(List ids, String peasId, String userId,
      List userRoles) {
    if (getContentManager() == null) {
      return new ArrayList();
    }
    return getHeaders(makePKArray(ids, peasId));
  }

  /**
   * Method declaration
   * 
   * 
   * @param pubId
   * @param peasId
   * 
   * @return
   * 
   * @see
   */
  public int getSilverObjectId(String pubId, String peasId) {
    SilverTrace.info("forums", "ForumsContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
    try {
      return getContentManager().getSilverContentId(pubId, peasId);
    } catch (Exception e) {
      throw new ForumsRuntimeException(
          "ForumsContentManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
          "forums.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * 
   * @param con
   *          a Connection
   * @param forumDetail
   *          the content to register
   * @param userId
   *          the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, ForumPK forumPK, String userId)
      throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(true);
    SilverTrace.info("forums", "ForumsContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
            + scv.toString());
    return getContentManager().addSilverContent(con, forumPK.getId(),
        forumPK.getComponentName(), userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content
   * is a ForumDetail
   * 
   * @param forumDetail
   *          the content
   * @param silverObjectId
   *          the unique identifier of the content
   */
  public void updateSilverContentVisibility(ForumPK forumPK, String userId)
      throws ContentManagerException {
    int silverContentId = getContentManager().getSilverContentId(
        forumPK.getId(), forumPK.getComponentName());
    if (silverContentId == -1) {
      createSilverContent(null, forumPK, userId);
    } else {
      SilverContentVisibility scv = new SilverContentVisibility(
          isVisible(forumPK));
      SilverTrace.info("forums",
          "ForumsContentManager.updateSilverContentVisibility()",
          "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
              + scv.toString());
      getContentManager().updateSilverContentVisibilityAttributes(scv,
          forumPK.getComponentName(), silverContentId);
      ClassifyEngine.clearCache();
    }
  }

  /**
   * delete a content. It is registered to contentManager service
   * 
   * @param con
   *          a Connection
   * @param forumPK
   *          the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, ForumPK forumPK)
      throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(forumPK.getId(),
        forumPK.getComponentName());
    if (contentId != -1) {
      SilverTrace.info("forums", "ForumsContentManager.deleteSilverContent()",
          "root.MSG_GEN_ENTER_METHOD", "pubId = " + forumPK.getId()
              + ", contentId = " + contentId);
      getContentManager().removeSilverContent(con, contentId,
          forumPK.getComponentName());
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param forumDetail
   * 
   * @return
   * 
   * @see
   */
  private boolean isVisible(ForumPK forumPK) {
    return true;
  }

  /**
   * return a list of forumPK according to a list of silverContentId
   * 
   * @param idList
   *          a list of silverContentId
   * @param peasId
   *          the id of the instance
   * @return a list of forumPK
   */
  private ArrayList makePKArray(List idList, String peasId) {
    ArrayList fks = new ArrayList();
    ForumPK forumPK = null;
    Iterator iter = idList.iterator();
    String id = null;

    // for each silverContentId, we get the corresponding forumId
    while (iter.hasNext()) {
      int contentId = ((Integer) iter.next()).intValue();

      try {
        id = getContentManager().getInternalContentId(contentId);
        forumPK = new ForumPK(peasId, null, id);
        fks.add(forumPK);
      } catch (ClassCastException ignored) {
        // ignore unknown item
      } catch (ContentManagerException ignored) {
        // ignore unknown item
      }
    }
    return fks;
  }

  /**
   * return a list of silverContent according to a list of ForumPK
   * 
   * @param ids
   *          a list of ForumPK
   * @return a list of ForumDetail
   */
  private List getHeaders(List ids) {
    ForumDetail forumDetail = null;
    ArrayList headers = new ArrayList();

    try {
      ArrayList forumDetails = (ArrayList) getForumsBM().getForums(ids);

      for (int i = 0; i < forumDetails.size(); i++) {
        forumDetail = (ForumDetail) forumDetails.get(i);
        forumDetail.setIconUrl("forumsSmall.gif");
        headers.add(forumDetail);
      }
    } catch (RemoteException e) {
      // skip unknown and ill formed id.
    }

    return headers;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace.fatal("forums", "ForumsContentManager",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  private ForumsBM getForumsBM() {
    if (forumsBM == null) {
      try {
        ForumsBMHome forumsBMHome = (ForumsBMHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.FORUMSBM_EJBHOME, ForumsBMHome.class);

        forumsBM = forumsBMHome.create();
      } catch (Exception e) {
        throw new ForumsRuntimeException("ForumsContentManager.getForumsBM()",
            SilverpeasRuntimeException.ERROR,
            "forums.EX_IMPOSSIBLE_DE_FABRIQUER_FORUMSBM_HOME", e);
      }
    }
    return forumsBM;
  }

  private ContentManager contentManager = null;
  private ForumsBM forumsBM = null;
}
