/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.classifieds.dao;

import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.components.classifieds.model.Subscribe;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ClassifiedsDAO {

  private ClassifiedsDAO() {
    throw new IllegalAccessError("Utility class");
  }

  /**
   * Create a classified
   * @param con connection to the database
   * @param classified the new classified
   * @return classifiedId the unique identifier of the created classified
   * @throws SQLException if the classified cannot be created.
   */
  public static String createClassified(Connection con, ClassifiedDetail classified)
      throws SQLException {
    // Création d'une nouvelle petite annonce
    String query =
        "insert into SC_Classifieds_Classifieds (classifiedId, instanceId, title, description, " +
            "price, creatorId, creationDate, " +
            "updateDate, status, validatorId, validateDate) " + "values (?,?,?,?,?,?,?,?,?,?,?)";
    try(PreparedStatement prepStmt =  con.prepareStatement(query)) {
      int newId = DBUtil.getNextId("SC_Classifieds_Classifieds", "classifiedId");
      String id = Integer.toString(newId);
      initParam(prepStmt, newId, classified);
      prepStmt.executeUpdate();
      return id;
    }
  }


  /**
   * Update a classified
   * @param con connection to the database.
   * @param classified the ClassifiedDetail with the data to update.
   * @throws SQLException if the classified cannot be updated
   */
  public static void updateClassified(Connection con, ClassifiedDetail classified)
      throws SQLException {
    String query =
        "update SC_Classifieds_Classifieds set title = ? , description = ? , price = ? , status" +
            " = ?  , updateDate = ? , validatorId = ? , validateDate = ? " +
            " where classifiedId = ? ";
    try(PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setString(1, classified.getTitle());
      prepStmt.setString(2, classified.getDescription());
      prepStmt.setInt(3, classified.getPrice());
      prepStmt.setString(4, classified.getStatus());
      if (classified.getUpdateDate() != null) {
        prepStmt.setString(5, Long.toString((classified.getUpdateDate()).getTime()));
      } else {
        prepStmt.setString(5, null);
      }
      prepStmt.setString(6, classified.getValidatorId());
      if (classified.getValidateDate() != null) {
        prepStmt.setString(7, Long.toString((classified.getValidateDate()).getTime()));
      } else {
        prepStmt.setString(7, null);
      }
      prepStmt.setInt(8, classified.getClassifiedId());
      prepStmt.executeUpdate();
    }
  }

  /**
   * Delete the classified with the specified identifier.
   * @param con connection to the database
   * @param classifiedId the unique identifier of a classified
   * @throws SQLException if the classified fail to be deleted.
   */
  public static void deleteClassified(Connection con, String classifiedId) throws SQLException {
    String query = "delete from SC_Classifieds_Classifieds where classifiedId = ? ";
    try(PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setInt(1, Integer.parseInt(classifiedId));
      prepStmt.executeUpdate();
    }
  }

  /**
   * Get the classified with the specified identifier.
   * @param con connection to the database
   * @param classifiedId the unique identifier of the classified
   * @return classified the {@link ClassifiedDetail} instance
   * @throws SQLException if the classified cannot be fetched
   */
  public static ClassifiedDetail getClassified(Connection con, String classifiedId)
      throws SQLException {
    // récupérer la petite annonce
    String query = "select * from SC_Classifieds_Classifieds where classifiedId = ? ";
    ClassifiedDetail classified = null;
    try (PreparedStatement prepStmt = con.prepareStatement(query)){
      prepStmt.setInt(1, Integer.parseInt(classifiedId));
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          classified = fetchClassified(rs);
        }
      }
    }
    return classified;
  }

  /**
   * Get all classifieds of an instance corresponding to instanceId
   * @param con connection to the database
   * @param instanceId the unique identifier of a classified component instance
   * @return a collection of {@link ClassifiedDetail} instances.
   * @throws SQLException if the fetching of the classifieds fails
   */
  public static Collection<ClassifiedDetail> getAllClassifieds(Connection con, String instanceId)
      throws SQLException {
    // récupérer toutes les petites annonces
    ArrayList<ClassifiedDetail> listClassifieds = new ArrayList<>();
    String query = "select * from SC_Classifieds_Classifieds where instanceId = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setString(1, instanceId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          ClassifiedDetail classified = fetchClassified(rs);
          listClassifieds.add(classified);
        }
      }
    }
    return listClassifieds;
  }

  /**
   * Get the number of classifieds to be validated for an instance corresponding to instanceId
   * @param con the connection to the database
   * @param instanceId the unique identifier of a classified component instance.
   * @return the count of classifieds in the component instance.
   * @throws SQLException if an error occurs while getting the classifieds count.
   */
  public static String getNbTotalClassifieds(Connection con, String instanceId)
      throws SQLException {
    // récupérer le nombre total d'annonces validées
    String nb = "";
    String query =
        "select count(classifiedId) from SC_Classifieds_Classifieds where instanceId = ? and " +
            "status = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(query) ) {
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, ClassifiedDetail.VALID);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          nb = rs.getString(1);
        }
      }
    }
    return nb;
  }

  /**
   * Get all the classifieds for user and instance, corresponding to userId and instanceId
   * @param con connection to the database
   * @param instanceId the unique identifier of a classifieds component instance.
   * @param userId the unique identifier of the user
   * @return a collection of {@link ClassifiedDetail}s of the user.
   * @throws SQLException if the classifieds getting fails.
   */
  public static List<ClassifiedDetail> getClassifiedsByUser(Connection con, String instanceId,
      String userId) throws SQLException {
    // récupérer toutes les petites annonces de l'utilisateur
    List<ClassifiedDetail> listClassifieds = new ArrayList<>();
    String query =
        "SELECT * FROM SC_Classifieds_Classifieds WHERE instanceId = ? AND creatorId = ? ORDER BY" +
            " CASE WHEN updatedate IS NULL THEN creationdate ELSE updatedate END DESC, " +
            "classifiedId DESC";
    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, userId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          ClassifiedDetail classified = fetchClassified(rs);
          listClassifieds.add(classified);
        }
      }
    }
    return listClassifieds;
  }

  /**
   * Get all classifieds with given status for an instance corresponding to instanceId
   * @param con connection to the database
   * @param instanceId the classifieds component instance
   * @param status status of the classifieds
   * @return a list of {@link ClassifiedDetail}
   * @throws SQLException if the classifieds cannot be fetched.
   */
  public static List<ClassifiedDetail> getClassifiedsWithStatus(Connection con, String instanceId,
      String status, int firstItemIndex, int elementsPerPage) throws SQLException {
    List<ClassifiedDetail> listClassifieds = new ArrayList<>();
    String query = "select * from SC_Classifieds_Classifieds where instanceId = ? and status = ? " +
        " order by CASE WHEN validatedate IS NULL THEN " +
        " CASE WHEN updatedate IS NULL THEN creationdate ELSE updatedate END " +
        " ELSE validatedate END DESC, " +
        " validatedate DESC, updatedate DESC, creationdate DESC";
    int lastIndexResult = firstItemIndex + elementsPerPage - 1;
    boolean displayAllElements = elementsPerPage == -1;
    try(PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, status);
      try (ResultSet rs = prepStmt.executeQuery()) {
        int index = 0;
        while (rs.next()) {
          if (displayAllElements || (index >= firstItemIndex && index <= lastIndexResult)) {
            ClassifiedDetail classified = fetchClassified(rs);
            listClassifieds.add(classified);
          }
          index++;
        }
      }
    }
    return listClassifieds;
  }

  /**
   * Get all expiring classifieds (corresponding of a number of days)
   * @param con connection to the database
   * @param nbDays the number of days
   * @param instanceId component instance identifier
   * @return a list of {@link ClassifiedDetail}
   * @throws SQLException if the classifieds cannot be fetched
   */
  public static List<ClassifiedDetail> getAllClassifiedsToUnpublish(Connection con, int nbDays,
      String instanceId) throws SQLException {
    ArrayList<ClassifiedDetail> listClassifieds = new ArrayList<>();

    // calcul de la date de fin
    Calendar calendar = Calendar.getInstance(Locale.FRENCH);
    calendar.add(Calendar.DATE, -nbDays);
    Date date = calendar.getTime();
    String query =
        "select * from SC_Classifieds_Classifieds where ( (updateDate is null and creationDate < " +
            "?) or (updateDate is not null and updateDate < ?) ) and instanceId = ? and status = " +
            "'Valid'";
    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setString(1, Long.toString(date.getTime()));
      prepStmt.setString(2, Long.toString(date.getTime()));
      prepStmt.setString(3, instanceId);

      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          ClassifiedDetail classified = fetchClassified(rs);
          listClassifieds.add(classified);
        }
      }
    }
    return listClassifieds;
  }

  /**
   * Create a subscription for user.
   * @param con connection to the database
   * @param subscribe the subscription to create.
   * @return subscribeId the unique identifier of a subscription.
   * @throws SQLException if the creation fails.
   */
  public static String createSubscribe(Connection con, Subscribe subscribe) throws SQLException {
    String id;
    String query =
        "insert into SC_Classifieds_Subscribes (subscribeId, userId, instanceId, field1, " +
            "field2) " +
            "values (?,?,?,?,?)";
    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      int newId = DBUtil.getNextId("SC_Classifieds_Subscribes", "subscribeId");
      id = Integer.toString(newId);
      initParamSubscribe(prepStmt, newId, subscribe);
      prepStmt.executeUpdate();
    }
    return id;
  }

  /**
   * Delete a subscription corresponding to the subscriber identifier
   * @param con connection to the database
   * @param subscribeId the unique identifier of a subscription
   * @throws SQLException if the deletion fails.
   */
  public static void deleteSubscribe(Connection con, String subscribeId) throws SQLException {
    String query = "delete from SC_Classifieds_Subscribes where subscribeId = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setInt(1, Integer.parseInt(subscribeId));
      prepStmt.executeUpdate();
    }
  }

  /**
   * Get all subscriptions for an instance corresponding to instanceId
   * @param con connection to the database
   * @param instanceId the unique identifier of a component instance.
   * @return a collection of {@link Subscribe}
   * @throws SQLException if the fetching fails
   */
  public static Collection<Subscribe> getAllSubscribes(Connection con, String instanceId)
      throws SQLException {
    ArrayList<Subscribe> listSubscribes = new ArrayList<>();
    String query = "select * from SC_Classifieds_Subscribes where instanceId = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setString(1, instanceId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          Subscribe subscribe = fetchSubscribe(rs);
          listSubscribes.add(subscribe);
        }
      }
    }
    return listSubscribes;
  }

  /**
   * Get all subscriptions for user and instance corresponding to userId and instanceId
   * @param con connection to the database
   * @param instanceId the unique identifier of a component instance
   * @param userId the unique identifier of a user
   * @return a collection of {@link Subscribe}
   * @throws SQLException if the fetching fails.
   */
  public static Collection<Subscribe> getSubscribesByUser(Connection con, String instanceId,
      String userId) throws SQLException {
    // récupérer tous les abonnements de l'utilisateur
    ArrayList<Subscribe> listSubscribes = new ArrayList<>();
    String query = "select * from SC_Classifieds_Subscribes where instanceId = ? and userId = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, userId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          Subscribe subscribe = fetchSubscribe(rs);
          listSubscribes.add(subscribe);
        }
      }
    }
    return listSubscribes;
  }

  /**
   * Get all subscribing users to a search corresponding to fields field1 and field2
   * @param con connection to the database
   * @param instanceId the unique identifier of the component instance on which the subscriptions
   * have to be found.
   * @param field1 value of the first field of a classified on which subscription can be done
   * @param field2 value of the second field of a classified on which subscription can be done
   * @return a collection of user identifiers.
   * @throws SQLException if the getting of the subscribers fail.
   */
  public static Collection<String> getUsersBySubscribe(Connection con, String instanceId,
      String field1, String field2) throws SQLException {
    // récupérer tous les utilisateurs abonnés à une recherche
    ArrayList<String> listUsers = new ArrayList<>();
    String query =
        "select userId from SC_Classifieds_Subscribes where ((field1 = ? and field2 = ?) or " +
            "(field1 = ? and field2 = '') or (field1 = '' and field2 = ?)) and instanceId = ?";
    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setString(1, field1);
      prepStmt.setString(2, field2);
      prepStmt.setString(3, field1);
      prepStmt.setString(4, field2);
      prepStmt.setString(5, instanceId);
      try (ResultSet rs =prepStmt.executeQuery()){
        while (rs.next()) {
          String userId = rs.getString("userId");
          listUsers.add(userId);
        }
      }
    }
    return listUsers;
  }

  /**
   * Fetches from the {@link ResultSet} the encoded {@link ClassifiedDetail}
   * @param rs the {@link ResultSet}
   * @return the {@link ClassifiedDetail} instance.
   * @throws SQLException if the data of the classified cannot be got.
   */
  private static ClassifiedDetail fetchClassified(ResultSet rs) throws SQLException {
    ClassifiedDetail classified = new ClassifiedDetail();
    int classifiedId = rs.getInt("classifiedId");
    String instanceId = rs.getString("instanceId");
    String title = rs.getString("title");
    String description = rs.getString("description");
    int price = 0;
    if (rs.getString("price") != null) {
      price = Integer.parseInt(rs.getString("price"));
    }
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
    classified.setDescription(description);
    classified.setPrice(price);
    classified.setCreatorId(creatorId);
    classified.setCreationDate(creationDate);
    classified.setUpdateDate(updateDate);
    classified.setStatus(status);
    classified.setValidatorId(validatorId);
    classified.setValidateDate(validateDate);

    return classified;
  }

  /**
   * Initialize the parameters of the statement with the data of the {@link ClassifiedDetail}
   * @param prepStmt the SQL statement
   * @param classifiedId the unique identifier of a classified
   * @param classified the ClassifiedDetail from which the statement has to be filled.
   * @throws SQLException if the parameters cannot be set.
   */
  private static void initParam(PreparedStatement prepStmt, int classifiedId,
      ClassifiedDetail classified) throws SQLException {
    prepStmt.setInt(1, classifiedId);
    prepStmt.setString(2, classified.getInstanceId());
    prepStmt.setString(3, classified.getTitle());
    prepStmt.setString(4, classified.getDescription());
    prepStmt.setInt(5, classified.getPrice());
    prepStmt.setString(6, classified.getCreatorId());
    prepStmt.setString(7, Long.toString((classified.getCreationDate()).getTime()));
    if (classified.getUpdateDate() != null) {
      prepStmt.setString(8, Long.toString((classified.getUpdateDate()).getTime()));
    } else {
      prepStmt.setString(8, null);
    }
    prepStmt.setString(9, classified.getStatus());
    prepStmt.setString(10, classified.getValidatorId());
    if (classified.getValidateDate() != null) {
      prepStmt.setString(11, Long.toString((classified.getValidateDate()).getTime()));
    } else {
      prepStmt.setString(11, null);
    }
  }

  /**
   * Fetches a subscription from the {@link ResultSet}
   * @param rs the {@link ResultSet}
   * @return a {@link Subscribe} instance
   * @throws SQLException if the {@link Subscribe} cannot be fetched.
   */
  private static Subscribe fetchSubscribe(ResultSet rs) throws SQLException {
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
   * Initialize the parameters of the statement with the data of the {@link Subscribe}
   * @param prepStmt the statement
   * @param subscribeId the unique identifier of a {@link Subscribe}
   * @param subscribe the {@link Subscribe}
   * @throws SQLException if the statement cannot be filled
   */
  private static void initParamSubscribe(PreparedStatement prepStmt, int subscribeId,
      Subscribe subscribe) throws SQLException {
    prepStmt.setInt(1, subscribeId);
    prepStmt.setString(2, subscribe.getUserId());
    prepStmt.setString(3, subscribe.getInstanceId());
    prepStmt.setString(4, subscribe.getField1());
    prepStmt.setString(5, subscribe.getField2());
  }
}
