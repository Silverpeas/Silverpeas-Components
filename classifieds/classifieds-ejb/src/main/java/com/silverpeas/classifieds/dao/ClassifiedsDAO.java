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
package com.silverpeas.classifieds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.Subscribe;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

public class ClassifiedsDAO {

  /**
   * Create a classified
   * @param con : Connection
   * @param classified : ClassifiedDetail
   * @return classifiedId : String
   * @throws SQLException
   * @throws UtilException
   */
  public static String createClassified(Connection con, ClassifiedDetail classified)
      throws SQLException, UtilException {
    // Création d'une nouvelle petite annonce
    String id = "";
    PreparedStatement prepStmt = null;
    try {
      int newId = DBUtil.getNextId("SC_Classifieds_Classifieds", "classifiedId");
      id = new Integer(newId).toString();
      // création de la requete
      String query =
          "insert into SC_Classifieds_Classifieds (classifiedId, instanceId, title, creatorId, creationDate, "
              + "updateDate, status, validatorId, validateDate) "
              + "values (?,?,?,?,?,?,?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      initParam(prepStmt, newId, classified);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
    return id;
  }

  /**
   * update a classified
   * @param con : Connection
   * @param classified : ClassifiedDetail
   * @throws SQLException
   */
  public static void updateClassified(Connection con, ClassifiedDetail classified)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // création de la requete
      String query =
          "update SC_Classifieds_Classifieds set title = ? , status = ?  , updateDate = ? , validatorId = ? , validateDate = ? "
              +
              " where classifiedId = ? ";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, classified.getTitle());
      prepStmt.setString(2, classified.getStatus());
      if (classified.getUpdateDate() != null) {
        prepStmt.setString(3, Long.toString((classified.getUpdateDate()).getTime()));
      } else {
        prepStmt.setString(3, null);
      }
      prepStmt.setString(4, classified.getValidatorId());
      if (classified.getValidateDate() != null) {
        prepStmt.setString(5, Long.toString((classified.getValidateDate()).getTime()));
      } else {
        prepStmt.setString(5, null);
      }
      prepStmt.setInt(6, new Integer(classified.getClassifiedId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  /**
   * delete the classified corresponding to classifiedId
   * @param con : Connection
   * @param classifiedId : String
   * @throws SQLException
   * @throws UtilException
   */
  public static void deleteClassified(Connection con, String classifiedId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // création de la requete
      String query = "delete from SC_Classifieds_Classifieds where classifiedId = ? ";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, new Integer(classifiedId));
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  /**
   * get the classified correspond to classifiedId
   * @param con : Connection
   * @param classifiedId : String
   * @return classified : ClassifiedDetail
   * @throws SQLException
   */
  public static ClassifiedDetail getClassified(Connection con, String classifiedId)
      throws SQLException {
    // récupérer la petite annonce
    String query = "select * from SC_Classifieds_Classifieds where classifiedId = ? ";
    ClassifiedDetail classified = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(classifiedId));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        classified = recupClassified(rs);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return classified;
  }

  /**
   * get all classifieds of a instance corresponding to instanceId
   * @param con : Connection
   * @param instanceId : String
   * @return a collection of ClassifiedDetail
   * @throws SQLException
   */
  public static Collection<ClassifiedDetail> getAllClassifieds(Connection con, String instanceId)
      throws SQLException {
    // récupérer toutes les petites annonces
    ArrayList<ClassifiedDetail> listClassifieds = new ArrayList<ClassifiedDetail>();
    String query = "select * from SC_Classifieds_Classifieds where instanceId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        ClassifiedDetail classified = recupClassified(rs);
        listClassifieds.add(classified);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listClassifieds;
  }

  /**
   * get the number of classifieds to be validated for an instance corresponding to instanceId
   * @param con : Connection
   * @param instanceId : String
   * @return the number : String
   * @throws SQLException
   */
  public static String getNbTotalClassifieds(Connection con, String instanceId) throws SQLException {
    // récupérer le nombre total d'annonces validées
    String nb = "";
    String query =
        "select count(classifiedId) from SC_Classifieds_Classifieds where instanceId = ? and status = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, ClassifiedDetail.VALID);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        nb = rs.getString(1);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return nb;
  }

  /**
   * get all classifieds for user and instance, corresponding to userId and instanceId
   * @param con : Connection
   * @param instanceId : String
   * @param userId : String
   * @return a collection of ClassifiedDetail
   * @throws SQLException
   */
  public static Collection<ClassifiedDetail> getClassifiedsByUser(Connection con,
      String instanceId, String userId) throws SQLException {
    // récupérer toutes les petites annonces de l'utilisateur
    ArrayList<ClassifiedDetail> listClassifieds = new ArrayList<ClassifiedDetail>();
    String query =
        "select * from SC_Classifieds_Classifieds where instanceId = ? and creatorId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, userId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        ClassifiedDetail classified = recupClassified(rs);
        listClassifieds.add(classified);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listClassifieds;
  }

  /**
   * get all classifieds with given status for an instance corresponding to instanceId
   * @param con : Connection
   * @param instanceId : String
   * @param status : status
   * @return a collection of ClassifiedDetail
   * @throws SQLException
   */
  public static Collection<ClassifiedDetail> getClassifiedsWithStatus(Connection con,
      String instanceId, String status) throws SQLException {
    ArrayList<ClassifiedDetail> listClassifieds = new ArrayList<ClassifiedDetail>();
    String query = "select * from SC_Classifieds_Classifieds where instanceId = ? and status = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, status);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        ClassifiedDetail classified = recupClassified(rs);
        listClassifieds.add(classified);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listClassifieds;
  }

  /**
   * get all expiring classifieds (corresponding of a number of day nbDays)
   * @param con : Connection
   * @param nbDays : int
   * @param instanceId : component instance id
   * @return a list of ClassifiedDetail
   * @throws SQLException
   */
  public static List<ClassifiedDetail> getAllClassifiedsToUnpublish(Connection con, int nbDays, String instanceId)
      throws SQLException {
    SilverTrace.debug("classifieds", "ClassifiedsDAO.getAllClassifiedsToUnpublish()",
        "root.MSG_GEN_PARAM_VALUE", "nbDays = " + nbDays);
    // récupérer toutes les petites annonces arrivant à échéance
    ArrayList<ClassifiedDetail> listClassifieds = new ArrayList<ClassifiedDetail>();

    // calcul de la date de fin
    Calendar calendar = Calendar.getInstance(Locale.FRENCH);
    calendar.add(Calendar.DATE, -nbDays);
    Date date = calendar.getTime();

    SilverTrace.debug("classifieds", "ClassifiedsDAO.getAllClassifiedsToUnpublish()",
        "root.MSG_GEN_PARAM_VALUE", "date = " + Long.toString(date.getTime()));

    String query = "select * from SC_Classifieds_Classifieds where ( (updateDate is null and creationDate < ?) or (updateDate is not null and updateDate < ?) ) and instanceId = ? and status = 'Valid'";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, Long.toString(date.getTime()));
      prepStmt.setString(2, Long.toString(date.getTime()));
      prepStmt.setString(3, instanceId);

      rs = prepStmt.executeQuery();
      while (rs.next()) {
        ClassifiedDetail classified = recupClassified(rs);
        SilverTrace.debug("classifieds", "ClassifiedsDAO.getAllClassifiedsToUnpublish()",
            "root.MSG_GEN_PARAM_VALUE", "classifiedId = " + classified.getClassifiedId());
        SilverTrace.debug("classifieds", "ClassifiedsDAO.getAllClassifiedsToUnpublish()",
            "root.MSG_GEN_PARAM_VALUE", "classifiedTitle = " + classified.getTitle());

        listClassifieds.add(classified);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listClassifieds;
  }

  /**
   * create a subscription
   * @param con : Connection
   * @param subscribe : Subscribe
   * @return subscribeId : String
   * @throws SQLException
   * @throws UtilException
   */
  public static String createSubscribe(Connection con, Subscribe subscribe) throws SQLException,
      UtilException {
    // Création d'un abonnement
    String id = "";
    PreparedStatement prepStmt = null;
    try {
      int newId = DBUtil.getNextId("SC_Classifieds_Subscribes", "subscribeId");
      id = new Integer(newId).toString();
      // création de la requete
      String query =
          "insert into SC_Classifieds_Subscribes (subscribeId, userId, instanceId, field1, field2) "
              + "values (?,?,?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      initParamSubscribe(prepStmt, newId, subscribe);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
    return id;
  }

  /**
   * delete a subscription corresponding to subscribeId
   * @param con : Connection
   * @param subscribeId : String
   * @throws SQLException
   */
  public static void deleteSubscribe(Connection con, String subscribeId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // création de la requete
      String query = "delete from SC_Classifieds_Subscribes where subscribeId = ? ";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, new Integer(subscribeId));
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  /**
   * get all subscriptions for an instance corresponding to instanceId
   * @param con : connection
   * @param instanceId : String
   * @return a collection of Subscribe
   * @throws SQLException
   */
  public static Collection<Subscribe> getAllSubscribes(Connection con, String instanceId)
      throws SQLException {
    // récupérer tous les abonnements
    ArrayList<Subscribe> listSubscribes = new ArrayList<Subscribe>();
    String query = "select * from SC_Classifieds_Subscribes where instanceId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        Subscribe subscribe = recupSubscribe(rs);
        listSubscribes.add(subscribe);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listSubscribes;
  }

  /**
   * get all subscriptions for user and instance corresponding to userId and instanceId
   * @param con : Connection
   * @param instanceId : String
   * @param userId : String
   * @return a collection of Subscribe
   * @throws SQLException
   */
  public static Collection<Subscribe> getSubscribesByUser(Connection con, String instanceId,
      String userId) throws SQLException {
    // récupérer tous les abonnements de l'utilisateur
    ArrayList<Subscribe> listSubscribes = new ArrayList<Subscribe>();
    String query = "select * from SC_Classifieds_Subscribes where instanceId = ? and userId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, userId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        Subscribe subscribe = recupSubscribe(rs);
        listSubscribes.add(subscribe);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listSubscribes;
  }

  /**
   * get all subscribing users to a search corresponding to fields field1 and field2
   * @param con : Connection
   * @param field1 : String
   * @param field2 : String
   * @return a collection of userId (String)
   * @throws SQLException
   */
  public static Collection<String> getUsersBySubscribe(Connection con, String field1, String field2)
      throws SQLException {
    // récupérer tous les utilisateurs abonnés à une recherche
    ArrayList<String> listUsers = new ArrayList<String>();
    String query = "select userId from SC_Classifieds_Subscribes where field1 = ? and field2 = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, field1);
      prepStmt.setString(2, field2);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        String userId = rs.getString("userId");
        listUsers.add(userId);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listUsers;
  }

  /**
   * get the classified to resultSet
   * @param rs : ResultSet
   * @return classified : ClassifiedDetail
   * @throws SQLException
   */
  private static ClassifiedDetail recupClassified(ResultSet rs) throws SQLException {
    ClassifiedDetail classified = new ClassifiedDetail();
    int classifiedId = rs.getInt("classifiedId");
    String instanceId = rs.getString("instanceId");
    String title = rs.getString("title");
    String creatorId = rs.getString("creatorId");
    Date creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
    Date updateDate = null;
    if (rs.getString("updateDate") != null) {
      updateDate = new Date(Long.parseLong(rs.getString("updateDate")));
    }
    String status = rs.getString("status");
    String validatorId = rs.getString("validatorId");
    Date validateDate = null;
    if (rs.getString("validateDate") != null) {
      validateDate = new Date(Long.parseLong(rs.getString("validateDate")));
    }
    classified.setClassifiedId(classifiedId);
    classified.setInstanceId(instanceId);
    classified.setTitle(title);
    classified.setCreatorId(creatorId);
    classified.setCreationDate(creationDate);
    classified.setUpdateDate(updateDate);
    classified.setStatus(status);
    classified.setValidatorId(validatorId);
    classified.setValidateDate(validateDate);

    return classified;
  }

  /**
   * initialise parameters
   * @param prepStmt : PreparedStatement
   * @param classifiedId : String
   * @param classified : ClassifiedDetail
   * @throws SQLException
   */
  private static void initParam(PreparedStatement prepStmt, int classifiedId,
      ClassifiedDetail classified) throws SQLException {
    prepStmt.setInt(1, new Integer(classifiedId).intValue());
    prepStmt.setString(2, classified.getInstanceId());
    prepStmt.setString(3, classified.getTitle());
    prepStmt.setString(4, classified.getCreatorId());
    prepStmt.setString(5, Long.toString((classified.getCreationDate()).getTime()));
    if (classified.getUpdateDate() != null) {
      prepStmt.setString(6, Long.toString((classified.getUpdateDate()).getTime()));
    } else {
      prepStmt.setString(6, null);
    }
    prepStmt.setString(7, classified.getStatus());
    prepStmt.setString(8, classified.getValidatorId());
    if (classified.getValidateDate() != null) {
      prepStmt.setString(9, Long.toString((classified.getValidateDate()).getTime()));
    } else {
      prepStmt.setString(9, null);
    }
  }

  /**
   * get a subscription to resultSet
   * @param rs : ResultSet
   * @return Subscribe
   * @throws SQLException
   */
  private static Subscribe recupSubscribe(ResultSet rs) throws SQLException {
    Subscribe subscribe = new Subscribe();
    int subscribeId = rs.getInt("subscribeId");
    String userId = rs.getString("userId");
    String instanceId = rs.getString("instanceId");
    String field1 = rs.getString("field1");
    String field2 = rs.getString("field2");

    subscribe.setSubscribeId(Integer.toString(subscribeId));
    subscribe.setUserId(userId);
    subscribe.setInstanceId(instanceId);
    subscribe.setField1(field1);
    subscribe.setField2(field2);
    return subscribe;
  }

  /**
   * initialise parameters
   * @param prepStmt : PreparedStatement
   * @param subscribeId : String
   * @param subscribe : Subscribe
   * @throws SQLException
   */
  private static void initParamSubscribe(PreparedStatement prepStmt, int subscribeId,
      Subscribe subscribe) throws SQLException {
    prepStmt.setInt(1, new Integer(subscribeId).intValue());
    prepStmt.setString(2, subscribe.getUserId());
    prepStmt.setString(3, subscribe.getInstanceId());
    prepStmt.setString(4, subscribe.getField1());
    prepStmt.setString(5, subscribe.getField2());
  }

  public static Collection<ClassifiedDetail> getUnpublishedClassifieds(Connection con,
      String instanceId, String userId)
      throws SQLException {
    ArrayList<ClassifiedDetail> listClassifieds = new ArrayList<ClassifiedDetail>();
    String query = "select * from SC_Classifieds_Classifieds where instanceId = ? and status = 'Unpublished' and creatorId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, userId);

      rs = prepStmt.executeQuery();
      while (rs.next()) {
        ClassifiedDetail classified = recupClassified(rs);
        listClassifieds.add(classified);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listClassifieds;
  }

}
