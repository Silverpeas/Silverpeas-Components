/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.infoLetter.implementation;

import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.silverpeas.subscribe.service.GroupSubscriptionSubscriber;
import com.silverpeas.subscribe.service.UserSubscriptionSubscriber;
import com.silverpeas.subscribe.util.SubscriptionUtil;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.infoLetter.InfoLetterContentManager;
import com.stratelia.silverpeas.infoLetter.InfoLetterException;
import com.stratelia.silverpeas.infoLetter.model.InfoLetter;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublication;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublicationPdC;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.wysiwyg.control.WysiwygController;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Class declaration
 *
 * @author
 */
public class InfoLetterDataManager implements InfoLetterDataInterface {

  // Statiques
  private final static String TableExternalEmails = "SC_IL_ExtSus";
  // Membres
  private SilverpeasBeanDAO<InfoLetter> infoLetterDAO;
  private SilverpeasBeanDAO<InfoLetterPublication> infoLetterPublicationDAO;
  private InfoLetterContentManager infoLetterContentManager;

  // Constructeur
  public InfoLetterDataManager() {
    try {
      infoLetterDAO = SilverpeasBeanDAOFactory.getDAO(
          "com.stratelia.silverpeas.infoLetter.model.InfoLetter");
      infoLetterPublicationDAO = SilverpeasBeanDAOFactory.getDAO(
          "com.stratelia.silverpeas.infoLetter.model.InfoLetterPublication");
      infoLetterContentManager = new InfoLetterContentManager();
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  /**
   * Implementation of InfoLetterDataInterface interface
   */
  // Creation d'une lettre d'information
  @Override
  public void createInfoLetter(InfoLetter il) {
    try {
      WAPrimaryKey pk = infoLetterDAO.add(il);
      il.setPK(pk);
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Mise a jour d'une lettre d'information
  @Override
  public void updateInfoLetter(InfoLetter ie) {
    try {
      infoLetterDAO.update(ie);
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Recuperation de la liste des lettres
  @Override
  public List<InfoLetter> getInfoLetters(String instanceId) {
    String whereClause = "instanceId = '" + instanceId + "'";
    try {
      return new ArrayList<InfoLetter>(infoLetterDAO.findByWhereClause(new IdPK(),
          whereClause));
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Recuperation de la liste des publications
  @Override
  public List<InfoLetterPublication> getInfoLetterPublications(WAPrimaryKey letterPK) {
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String whereClause = "instanceId = '" + letter.getInstanceId() + "' AND letterId = "
          + letterPK.getId();
      return new ArrayList<InfoLetterPublication>(infoLetterPublicationDAO.findByWhereClause(
          letterPK, whereClause));
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Creation d'une publication
  @Override
  public void createInfoLetterPublication(InfoLetterPublicationPdC ilp, String userId) {
    SilverTrace.info("infoLetter", "InfoLetterDataManager.createInfoLetterPublication()",
        "root.MSG_GEN_ENTER_METHOD", "ilp = " + ilp.toString() + " userId=" + userId);
    Connection con = openConnection();

    try {
      WAPrimaryKey pk = infoLetterPublicationDAO.add(con, ilp);
      ilp.setPK(pk);
      infoLetterContentManager.createSilverContent(con, ilp, userId);
    } catch (Exception pe) {
      DBUtil.rollback(con);
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    } finally {
      DBUtil.close(con);
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface#deleteInfoLetterPublication
   * (com.stratelia.webactiv.util.WAPrimaryKey, java.lang.String)
   */
  @Override
  public void deleteInfoLetterPublication(WAPrimaryKey pk, String componentId) {
    Connection con = openConnection();
    try {
      infoLetterPublicationDAO.remove(pk);
      infoLetterContentManager.deleteSilverContent(con, pk.getId(), componentId);
    } catch (Exception pe) {
      DBUtil.rollback(con);
      throw new InfoLetterException("InfoLetterDataManager.createInfoLetterPublication()",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    } finally {
      DBUtil.close(con);
    }
  }

  // Mise a jour d'une publication
  @Override
  public void updateInfoLetterPublication(InfoLetterPublicationPdC ilp) {
    try {
      infoLetterPublicationDAO.update(ilp);
      infoLetterContentManager.updateSilverContentVisibility(ilp);
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
  }

  // Recuperation d'une lettre par sa clef
  @Override
  public InfoLetter getInfoLetter(WAPrimaryKey letterPK) {
    InfoLetter retour;
    try {
      retour = infoLetterDAO.findByPrimaryKey(letterPK);
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
    return retour;
  }

  // Recuperation d'une publication par sa clef
  @Override
  public InfoLetterPublicationPdC getInfoLetterPublication(WAPrimaryKey publiPK) {
    InfoLetterPublicationPdC retour;
    try {
      retour = new InfoLetterPublicationPdC(infoLetterPublicationDAO.findByPrimaryKey(publiPK));
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
    return retour;
  }

  // Creation de la lettre par defaut a l'instanciation
  @Override
  public InfoLetter createDefaultLetter(String componentId) {
    OrganizationController oc = new OrganizationController();
    ComponentInst ci = oc.getComponentInst(componentId);
    InfoLetter ie = new InfoLetter();
    ie.setInstanceId(componentId);
    ie.setName(ci.getLabel());
    createInfoLetter(ie);
    initTemplate(componentId, ie.getPK(), "0");
    return ie;
  }

  @Override
  public int getSilverObjectId(String pubId, String componentId) {
    SilverTrace.info("infoLetter", "InfoLetterDataManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
    try {
      int silverObjectId = infoLetterContentManager.getSilverObjectId(pubId, componentId);
      if (silverObjectId == -1) {
        IdPK publiPK = new IdPK();
        publiPK.setId(pubId);
        InfoLetterPublicationPdC infoLetter = getInfoLetterPublication(publiPK);
        silverObjectId = infoLetterContentManager.createSilverContent(null, infoLetter,
            infoLetter.getCreatorId());
      }
      return silverObjectId;
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterDataManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  @Override
  public Map<SubscriberType, Collection<String>> getInternalSuscribers(final String componentId) {
    return SubscriptionUtil.indexSubscriberIdsByType(
        SubscriptionServiceFactory.getFactory().getSubscribeService()
            .getSubscribers(ComponentSubscriptionResource.from(componentId)));
  }

  @Override
  public void setInternalSuscribers(final String componentId, final UserDetail[] users,
      final Group[] groups) {

    // Initializing necessary subscriptions
    Collection<Subscription> subscriptions =
        new ArrayList<Subscription>(users.length + groups.length);
    for (UserDetail user : users) {
      subscriptions.add(
          new ComponentSubscription(UserSubscriptionSubscriber.from(user.getId()), componentId));
    }
    for (Group group : groups) {
      subscriptions.add(
          new ComponentSubscription(GroupSubscriptionSubscriber.from(group.getId()), componentId));
    }

    // Getting all existing subscriptions and selecting those that have to be deleted
    Collection<Subscription> subscriptionsToDelete =
        SubscriptionServiceFactory.getFactory().getSubscribeService()
            .getByResource(ComponentSubscriptionResource.from(componentId));
    subscriptionsToDelete.removeAll(subscriptions);

    // Deleting
    SubscriptionServiceFactory.getFactory().getSubscribeService()
        .unsubscribe(subscriptionsToDelete);

    // Creating subscriptions (nothing is registered for subscriptions that already exist)
    SubscriptionServiceFactory.getFactory().getSubscribeService().subscribe(subscriptions);
  }

  // Recuperation de la liste des emails externes
  @Override
  public Collection<String> getExternalsSuscribers(WAPrimaryKey letterPK) {
    Connection con = openConnection();
    List<String> retour = new ArrayList<String>();
    Statement selectStmt = null;
    ResultSet rs = null;
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String selectQuery = "SELECT * FROM " + TableExternalEmails;
      selectQuery += " where instanceId = '" + letter.getInstanceId() + "' ";
      selectQuery += " and letter = " + letterPK.getId() + " ";
      SilverTrace.info("infoLetter",
          "InfoLetterDataManager.getExternalsSuscribers()",
          "root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery);
      selectStmt = con.createStatement();
      rs = selectStmt.executeQuery(selectQuery);
      while (rs.next()) {
        retour.add(rs.getString("email"));
      }
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    } finally {
      DBUtil.close(rs, selectStmt);
      DBUtil.close(con);
    }

    return retour;
  }

  // Sauvegarde de la liste des emails externes
  public void setExternalsSuscribers(WAPrimaryKey letterPK, Collection<String> emails) {
    Connection con = openConnection();
    Statement stmt = null;
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String query = "DELETE FROM " + TableExternalEmails;
      query += " where instanceId = '" + letter.getInstanceId() + "' ";
      query += " and letter = " + letterPK.getId() + " ";
      SilverTrace.info("infoLetter",
          "InfoLetterDataManager.setExternalsSuscribers()",
          "root.MSG_GEN_PARAM_VALUE", "query = " + query);
      stmt = con.createStatement();
      stmt.executeUpdate(query);
      if (emails.size() > 0) {
        for (String email : emails) {
          query = "INSERT INTO " + TableExternalEmails + "(letter, email, instanceId)";
          query += " values (" + letterPK.getId() + ", '" + email + "', '"
              + letter.getInstanceId() + "')";
          stmt = con.createStatement();
          stmt.executeUpdate(query);
        }
      }
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage());
    } finally {
      DBUtil.close(stmt);
      DBUtil.close(con);
    }
  }

  // abonnement ou desabonnement d'un utilisateur interne
  @Override
  public void toggleSuscriber(String userId, String componentId, boolean isUserSubscribing) {
    Subscription subscription =
        new ComponentSubscription(UserSubscriptionSubscriber.from(userId), componentId);
    if (isUserSubscribing) {
      SubscriptionServiceFactory.getFactory().getSubscribeService().subscribe(subscription);
    } else {
      SubscriptionServiceFactory.getFactory().getSubscribeService().unsubscribe(subscription);
    }
  }

  // test d'abonnement d'un utilisateur interne
  @Override
  public boolean isUserSuscribed(String userId, String componentId) {
    return SubscriptionServiceFactory.getFactory().getSubscribeService().existsSubscription(
        new ComponentSubscription(UserSubscriptionSubscriber.from(userId), componentId));
  }

  // initialisation du template
  @Override
  public void initTemplate(String componentId, WAPrimaryKey letterPK, String userId) {
    try {
      String basicTemplate = "<body></body>";
      WysiwygController.createUnindexedFileAndAttachment(basicTemplate,
          new ForeignPK(InfoLetterPublication.TEMPLATE_ID + letterPK.getId(), componentId), userId,
          I18NHelper.defaultLanguage);
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
  }

  /**
   * open connection
   *
   * @return Connection
   * @throws InfoLetterException
   * @author frageade
   * @since 26 Fevrier 2002
   */
  @Override
  public Connection openConnection() throws InfoLetterException {
    Connection con;
    try {
      con = DBUtil.makeConnection(JNDINames.INFOLETTER_DATASOURCE);
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterDataManager.openConnection()",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
    return con;
  }
}
