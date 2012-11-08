/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.stratelia.silverpeas.infoLetter.InfoLetterContentManager;
import com.stratelia.silverpeas.infoLetter.InfoLetterException;
import com.stratelia.silverpeas.infoLetter.model.InfoLetter;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublication;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublicationPdC;
import com.stratelia.silverpeas.infoLetter.model.InternalSubscribers;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * Class declaration
 * @author
 */
public class InfoLetterDataManager implements InfoLetterDataInterface {

  // Statiques
  private final static String TableExternalEmails = "SC_IL_ExtSus";
  private final static String TableInternalEmails = "SC_IL_IntSus";

  // Membres
  private SilverpeasBeanDAO<InfoLetter> infoLetterDAO;
  private SilverpeasBeanDAO<InfoLetterPublication> infoLetterPublicationDAO;
  private InfoLetterContentManager infoLetterContentManager;

  // Constructeur
  public InfoLetterDataManager() {
    try {
      infoLetterDAO = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.infoLetter.model.InfoLetter");
      infoLetterPublicationDAO = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.infoLetter.model.InfoLetterPublication");
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

  // Suppression d'une lettre d'information
  @Override
  public void deleteInfoLetter(WAPrimaryKey pk) {
    try {
      infoLetterDAO.remove(pk);
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
  public List<InfoLetter> getInfoLetters(String instanceId) {
    String whereClause = "instanceId = '" + instanceId + "'";
    List<InfoLetter> infoLetters = new ArrayList<InfoLetter>();
    try {
      infoLetters =
          new ArrayList<InfoLetter>(infoLetterDAO.findByWhereClause(new IdPK(), whereClause));
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
    return infoLetters;
  }

  // Recuperation de la liste des publications
  public List<InfoLetterPublication> getInfoLetterPublications(WAPrimaryKey letterPK) {

    List<InfoLetterPublication> publications = new ArrayList<InfoLetterPublication>();
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String whereClause =
          "instanceId = '" + letter.getInstanceId() + "' and letterId = " + letterPK.getId();
      publications =
          new ArrayList<InfoLetterPublication>(infoLetterPublicationDAO.findByWhereClause(letterPK,
              whereClause));
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
    return publications;
  }

  // Creation d'une publication
  public void createInfoLetterPublication(InfoLetterPublicationPdC ilp, String userId) {
    SilverTrace.info("infoLetter", "InfoLetterDataManager.createInfoLetterPublication()",
        "root.MSG_GEN_ENTER_METHOD", "ilp = " + ilp.toString() + " userId=" + userId);
    Connection con = openConnection();

    try {
      WAPrimaryKey pk = infoLetterPublicationDAO.add(con, (InfoLetterPublication) ilp);
      ilp.setPK(pk);
      infoLetterContentManager.createSilverContent(con, ilp, userId);
    } catch (Exception pe) {
      try {
        if (con != null) {
          con.rollback();
        }
      } catch (Exception e) {
        SilverTrace.error("infoLetter",
            "InfoLetterDataManager.createInfoLetterPublication()",
            "root.EX_ERR_ROLLBACK", e);
      }
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (Exception e) {
        SilverTrace.error("infoLetter",
            "InfoLetterDataManager.createInfoLetterPublication()",
            "root.EX_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface#deleteInfoLetterPublication
   * (com.stratelia.webactiv.util.WAPrimaryKey, java.lang.String)
   */
  public void deleteInfoLetterPublication(WAPrimaryKey pk, String componentId) {
    Connection con = openConnection();
    try {
      infoLetterPublicationDAO.remove(pk);
      infoLetterContentManager.deleteSilverContent(con, pk.getId(), componentId);
    } catch (Exception pe) {
      try {
        if (con != null) {
          con.rollback();
        }
      } catch (Exception e) {
        SilverTrace.error("infoLetter", "InfoLetterDataManager.deleteInfoLetterPublication()",
            "root.EX_ERR_ROLLBACK", e);
      }
      throw new InfoLetterException("InfoLetterDataManager.createInfoLetterPublication()",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (Exception e) {
        SilverTrace.error("infoLetter", "InfoLetterDataManager.createInfoLetterPublication()",
            "root.EX_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  // Mise a jour d'une publication
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

  // Validation d'une publication
  public void validateInfoLetterPublication(InfoLetterPublication ilp) {
  }

  // Recuperation d'une lettre par sa clef
  public InfoLetter getInfoLetter(WAPrimaryKey letterPK) {
    InfoLetter retour = null;
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
  public InfoLetterPublicationPdC getInfoLetterPublication(WAPrimaryKey publiPK) {
    InfoLetterPublicationPdC retour = null;
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
  public InfoLetter createDefaultLetter(String spaceId, String componentId) {
    com.stratelia.webactiv.beans.admin.OrganizationController oc =
        new com.stratelia.webactiv.beans.admin.OrganizationController();
    com.stratelia.webactiv.beans.admin.ComponentInst ci = oc
        .getComponentInst(componentId);
    InfoLetter ie = new InfoLetter();
    ie.setInstanceId(componentId);
    ie.setName(ci.getLabel());
    createInfoLetter(ie);
    initTemplate(spaceId, componentId, ie.getPK());
    return ie;
  }

  public int getSilverObjectId(String pubId, String componentId) {
    SilverTrace.info("infoLetter", "InfoLetterDataManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
    int silverObjectId = -1;
    InfoLetterPublicationPdC infoLetter = null;
    try {
      silverObjectId = infoLetterContentManager.getSilverObjectId(pubId,
          componentId);
      if (silverObjectId == -1) {
        IdPK publiPK = new IdPK();
        publiPK.setId(pubId);
        infoLetter = getInfoLetterPublication(publiPK);
        silverObjectId = infoLetterContentManager.createSilverContent(null,
            infoLetter, infoLetter.getCreatorId());
      }
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterDataManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  /**
   * @see InfoLetterDataInterface
   */
  public InternalSubscribers getInternalSuscribers(WAPrimaryKey letterPK) {
    Connection con = openConnection();
    List<UserDetail> users = new ArrayList<UserDetail>();
    List<Group> groups = new ArrayList<Group>();
    Statement selectStmt = null;
    ResultSet rs = null;
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String selectQuery = "select * from " + TableInternalEmails;
      selectQuery += " where instanceId = '" + letter.getInstanceId() + "' ";
      selectQuery += " and letter = " + letterPK.getId() + " ";
      SilverTrace.info("infoLetter", "InfoLetterDataManager.getInternalSuscribers()",
          "root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery);
      selectStmt = con.createStatement();
      rs = selectStmt.executeQuery(selectQuery);
      while (rs.next()) {
        String value = rs.getString("userId");
        String type = value.substring(0, 1);
        String id = value.substring(1);
        try {
          if ("U".equalsIgnoreCase(type)) {
            users.add(AdminReference.getAdminService().getUserDetail(id));
          } else {
            groups.add(AdminReference.getAdminService().getGroup(id));
          }
        } catch (AdminException ae) {
          SilverTrace.error("infoLetter", "InfoLetterDataManager.getInternalSuscribers()",
              "root.MSG_GEN_PARAM_VALUE", "id = " + id);
        }
      }
      // rs.close();
      // selectStmt.close();
      // con.close();
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (selectStmt != null) {
          selectStmt.close();
        }
        if (con != null) {
          con.close();
        }
      } catch (Exception e) {
        throw new InfoLetterException(
            "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
            SilverpeasRuntimeException.FATAL, e.getMessage(), e);
      }
    }
    return new InternalSubscribers(users, groups);
  }

  // Mise a jour de la liste des abonnes internes
  public void setInternalSuscribers(WAPrimaryKey letterPK, InternalSubscribers internalSubscribers) {
    Connection con = openConnection();
    Statement stmt = null;
    List<UserDetail> users = internalSubscribers.getUsers();
    List<Group> groups = internalSubscribers.getGroups();
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String deleteQuery = "DELETE FROM " + TableInternalEmails;
      deleteQuery += " WHERE instanceId = '" + letter.getInstanceId() + "' ";
      deleteQuery += " AND letter = " + letterPK.getId() + " ";
      SilverTrace.info("infoLetter",
          "InfoLetterDataManager.setInternalSuscribers()",
          "root.MSG_GEN_PARAM_VALUE", "query = " + deleteQuery);
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
      if (!groups.isEmpty()) {
        for (Group group : groups) {
          StringBuilder queryBuilder = new StringBuilder();
          queryBuilder
              .append("INSERT INTO " + TableInternalEmails + "(letter, userId, instanceId)");
          queryBuilder.append(" VALUES (" + letterPK.getId() + ", 'G" + group.getId() + "', ");
          queryBuilder.append("'" + letter.getInstanceId() + "')");
          SilverTrace.info("infoLetter", "InfoLetterDataManager.setInternalSuscribers()",
              "root.MSG_GEN_PARAM_VALUE", "query = " + queryBuilder.toString());
          stmt = con.createStatement();
          stmt.executeUpdate(queryBuilder.toString());
        }
      }
      if (!users.isEmpty()) {
        for (UserDetail userDetail : users) {
          StringBuilder queryBuilder = new StringBuilder();
          queryBuilder.append("INSERT INTO ").append(TableInternalEmails)
              .append("(letter, userId, instanceId)");
          queryBuilder.append(" VALUES (").append(letterPK.getId()).append(", 'U")
              .append(userDetail.getId()).append("', ");
          queryBuilder.append("'").append(letter.getInstanceId()).append("')");
          SilverTrace.info("infoLetter", "InfoLetterDataManager.setInternalSuscribers()",
              "root.MSG_GEN_PARAM_VALUE", "query = " + queryBuilder.toString());
          stmt = con.createStatement();
          stmt.executeUpdate(queryBuilder.toString());
        }
      }
      // stmt.close();
      // con.close();
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterDataManager.setInternalSuscribers()",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    } finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
        if (con != null) {
          con.close();
        }
      } catch (Exception e) {
        throw new InfoLetterException("InfoLetterDataManager.setInternalSuscribers()",
            SilverpeasRuntimeException.FATAL, "error when closing connection :" + e.getMessage(), e);
      }
    }
  }

  // Recuperation de la liste des emails externes
  public Collection<String> getExternalsSuscribers(WAPrimaryKey letterPK) {
    Connection con = openConnection();
    List<String> retour = new ArrayList<String>();
    Statement selectStmt = null;
    ResultSet rs = null;
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String selectQuery = "select * from " + TableExternalEmails;
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
      // rs.close();
      // selectStmt.close();
      // con.close();
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (selectStmt != null) {
          selectStmt.close();
        }
        if (con != null) {
          con.close();
        }
      } catch (Exception e) {
        throw new InfoLetterException(
            "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
            SilverpeasRuntimeException.FATAL, e.getMessage(), e);
      }
    }

    return retour;
  }

  // Sauvegarde de la liste des emails externes
  public void setExternalsSuscribers(WAPrimaryKey letterPK, Collection<String> emails) {
    Connection con = openConnection();
    Statement stmt = null;
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String query = "delete from " + TableExternalEmails;
      query += " where instanceId = '" + letter.getInstanceId() + "' ";
      query += " and letter = " + letterPK.getId() + " ";
      SilverTrace.info("infoLetter",
          "InfoLetterDataManager.setExternalsSuscribers()",
          "root.MSG_GEN_PARAM_VALUE", "query = " + query);
      stmt = con.createStatement();
      stmt.executeUpdate(query);
      if (emails.size() > 0) {
        for (String email : emails) {
          query = "insert into " + TableExternalEmails
              + "(letter, email, instanceId)";
          query += " values (" + letterPK.getId() + ", '" + email + "', '"
              + letter.getInstanceId() + "')";
          stmt = con.createStatement();
          stmt.executeUpdate(query);
        }
      }
      // stmt.close();
      // con.close();
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage());
    } finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
        if (con != null) {
          con.close();
        }
      } catch (Exception e) {
        throw new InfoLetterException(
            "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
            SilverpeasRuntimeException.FATAL, e.getMessage(), e);
      }
    }
  }

  // abonnement ou desabonnement d'un utilisateur interne
  public void toggleSuscriber(String userId, WAPrimaryKey letterPK, boolean flag) {
    Connection con = openConnection();
    Statement stmt = null;
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String query = "delete from " + TableInternalEmails;
      query += " where instanceId = '" + letter.getInstanceId() + "' ";
      query += " and letter = " + letterPK.getId() + " ";
      query += " and userId = 'U" + userId + "' ";
      SilverTrace.info("infoLetter", "InfoLetterDataManager.toggleSuscriber()",
          "root.MSG_GEN_PARAM_VALUE", "query = " + query);
      stmt = con.createStatement();
      stmt.executeUpdate(query);
      if (flag) {
        query = "insert into " + TableInternalEmails
            + "(letter, userId, instanceId)";
        query += " values (" + letterPK.getId() + ", 'U" + userId + "', ";
        query += "'" + letter.getInstanceId() + "')";
        SilverTrace.info("infoLetter",
            "InfoLetterDataManager.toggleSuscriber()",
            "root.MSG_GEN_PARAM_VALUE", "query = " + query);
        stmt = con.createStatement();
        stmt.executeUpdate(query);
      }
      // stmt.close();
      // con.close();
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    } finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
        if (con != null) {
          con.close();
        }
      } catch (Exception e) {
        throw new InfoLetterException(
            "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
            SilverpeasRuntimeException.FATAL, e.getMessage(), e);
      }
    }
  }

  // test d'abonnement d'un utilisateur interne
  public boolean isSuscriber(String userId, WAPrimaryKey letterPK) {
    boolean retour = false;
    Connection con = openConnection();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String query = "select userId from " + TableInternalEmails;
      query += " where instanceId = '" + letter.getInstanceId() + "' ";
      query += " and letter = " + letterPK.getId() + " ";
      query += " and userId = 'U" + userId + "' ";
      SilverTrace.info("infoLetter", "InfoLetterDataManager.isSuscriber()",
          "root.MSG_GEN_PARAM_VALUE", "query = " + query);
      stmt = con.createStatement();
      rs = stmt.executeQuery(query);
      if (rs.next()) {
        retour = true;
      }
      // rs.close();
      // stmt.close();
      // con.close();
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (stmt != null) {
          stmt.close();
        }
        if (con != null) {
          con.close();
        }
      } catch (Exception e) {
        throw new InfoLetterException(
            "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
            SilverpeasRuntimeException.FATAL, e.getMessage(), e);
      }
    }
    return retour;
  }

  // initialisation du template
  public void initTemplate(String spaceId, String componentId,
      WAPrimaryKey letterPK) {
    try {
      String basicTemplate = "<body></body>";
      WysiwygController.createFileAndAttachment(basicTemplate, spaceId,
          componentId, InfoLetterPublication.TEMPLATE_ID + letterPK.getId());
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
  }

  /**
   * open connection
   * @return Connection
   * @throws InfoLetterException
   * @author frageade
   * @since 26 Fevrier 2002
   */
  public Connection openConnection() throws InfoLetterException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.INFOLETTER_DATASOURCE);
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterDataManager.openConnection()",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
    return con;
  }
}
