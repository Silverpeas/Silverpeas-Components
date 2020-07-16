/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.formsonline.model;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.persistence.datasource.repository.PaginationCriterion;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.SilverpeasList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.silverpeas.core.SilverpeasExceptionMessages.*;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.createSelect;

public class FormsOnlineDAOJdbc implements FormsOnlineDAO {

  // General infos
  private static final String FORMS_TABLENAME = "SC_FormsOnline_Forms";
  private static final String FORMS_INSTANCES_TABLENAME = "SC_FormsOnline_FormInstances";
  private static final String USER_RIGHTS_TABLENAME = "SC_FormsOnline_UserRights";
  private static final String GROUP_RIGHTS_TABLENAME = "SC_FormsOnline_GroupRights";

  // Queries about Forms
  private static final String QUERY_FIND_FORMS =
      "select * from " + FORMS_TABLENAME + " where instanceId = ?";
  private static final String QUERY_LOAD_FORM =
      "select * from " + FORMS_TABLENAME + " where instanceId = ? and id = ?";
  private static final String QUERY_INSERT_FORM = "INSERT INTO " +
      FORMS_TABLENAME +
      "(id, xmlFormName, name, description, title, creatorId, creationDate, state, " +
      "instanceId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
  private static final String QUERY_UPDATE_FORM = "update " +
      FORMS_TABLENAME +
      " set xmlFormName = ?, name = ?, description = ?, title = ?, creatorId = ?, creationDate = " +
      "?, state = ? where instanceId = ? and id= ? ";
  private static final String QUERY_DELETE_FORM =
      "delete from " + FORMS_TABLENAME + " where instanceId = ? and id = ? ";

  // Queries about Forms instances
  private static final String QUERY_LOAD_FORM_INSTANCE =
      "select * from " + FORMS_INSTANCES_TABLENAME + " where instanceId = ? and id = ?";
  private static final String QUERY_UPDATE_FORM_INSTANCE = "update " +
      FORMS_INSTANCES_TABLENAME +
      " set formId = ?, state = ?, creatorId = ?, creationDate = ?, validatorId = ?, " +
      "validationDate = ?, comments = ?, instanceId = ? where id = ? ";
  private static final String QUERY_INSERT_FORMINSTANCE = "INSERT INTO " +
      FORMS_INSTANCES_TABLENAME +
      "(id, formId, state, creatorId, creationDate, validatorId, validationDate, comments, " +
      "instanceId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String QUERY_DELETE_FORM_INSTANCE =
      "delete from " + FORMS_INSTANCES_TABLENAME + " where instanceId = ? and id = ? ";

  private static final String COUNT_REQUESTS_BY_FORM =
      "select formid, count(*) from " + FORMS_INSTANCES_TABLENAME + " where instanceId = ? group by formid";

  // Queries about Rights
  private static final String QUERY_LOAD_USER_RIGHTS = "select * from " + USER_RIGHTS_TABLENAME +
      " where instanceId = ? and formId = ? and rightType = ?";
  private static final String QUERY_LOAD_GROUP_RIGHTS = "select * from " + GROUP_RIGHTS_TABLENAME +
      " where instanceId = ? and formId = ? and rightType = ?";
  private static final String QUERY_REMOVE_USER_RIGHTS = "delete from " + USER_RIGHTS_TABLENAME +
      " where instanceId = ? and formId = ? and rightType = ?";
  private static final String QUERY_REMOVE_GROUP_RIGHTS = "delete from " + GROUP_RIGHTS_TABLENAME +
      " where instanceId = ? and formId = ? and rightType = ?";
  private static final String QUERY_INSERT_USER_RIGHTS =
      "INSERT INTO " + USER_RIGHTS_TABLENAME + "(formId, instanceId, rightType, userId) " +
          "VALUES (?, ?, ?, ?)";
  private static final String QUERY_INSERT_GROUP_RIGHTS =
      "INSERT INTO " + GROUP_RIGHTS_TABLENAME + "(formId, instanceId, rightType, groupId) " +
          "VALUES (?, ?, ?, ?)";
  private static final String STATE_FIELD = "state";
  private static final String INSTANCE_ID = "instanceId";

  @Override
  public FormDetail createForm(FormDetail formDetail) throws FormsOnlineException {
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(QUERY_INSERT_FORM)) {
      int id = DBUtil.getNextId(FORMS_TABLENAME, "id");
      stmt.setInt(1, id);
      stmt.setString(2, formDetail.getXmlFormName());
      stmt.setString(3, formDetail.getName());
      stmt.setString(4, formDetail.getDescription());
      stmt.setString(5, formDetail.getTitle());
      stmt.setString(6, formDetail.getCreatorId());
      prepareDateStatement(stmt, 7, formDetail.getCreationDate());
      stmt.setInt(8, formDetail.getState());
      stmt.setString(9, formDetail.getInstanceId());

      stmt.executeUpdate();
      formDetail.setId(id);

      return formDetail;
    } catch (SQLException se) {
      throw new FormsOnlineException(failureOnAdding("form", formDetail.getName()), se);
    }
  }

  @Override
  public FormDetail deleteForm(FormPK pk) throws FormsOnlineException {
    FormDetail form = getForm(pk);
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(QUERY_DELETE_FORM)) {
      removeGroupRights(con, pk, "S");
      removeUserRights(con, pk, "S");
      removeGroupRights(con, pk, "R");
      removeUserRights(con, pk, "R");

      stmt.setString(1, pk.getInstanceId());
      stmt.setInt(2, Integer.parseInt(pk.getId()));
      stmt.executeUpdate();
      return form;
    } catch (SQLException se) {
      throw new FormsOnlineException(failureOnDeleting("form", pk.toString()), se);
    }
  }

  @Override
  public FormDetail getForm(FormPK pk) throws FormsOnlineException {
    FormDetail form = null;
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(QUERY_LOAD_FORM)) {
      stmt.setString(1, pk.getInstanceId());
      stmt.setInt(2, Integer.parseInt(pk.getId()));
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          form = fetchFormDetail(rs);
        }
      }
    } catch (SQLException se) {
      throw new FormsOnlineException(failureOnGetting("form", pk.toString()), se);
    }
    return form;
  }

  @Override
  public List<FormDetail> findAllForms(String instanceId) throws FormsOnlineException {
    List<FormDetail> forms = new ArrayList<>();
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(QUERY_FIND_FORMS)) {
      stmt.setString(1, instanceId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          FormDetail form = fetchFormDetail(rs);
          forms.add(form);
        }
      }
    } catch (SQLException se) {
      throw new FormsOnlineException(failureOnGetting("all forms of instance", instanceId), se);
    }
    return forms;
  }

  @Override
  public void updateForm(FormDetail formDetail) throws FormsOnlineException {
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(QUERY_UPDATE_FORM)) {
      stmt.setString(1, formDetail.getXmlFormName());
      stmt.setString(2, formDetail.getName());
      stmt.setString(3, formDetail.getDescription());
      stmt.setString(4, formDetail.getTitle());
      stmt.setString(5, formDetail.getCreatorId());
      prepareDateStatement(stmt, 6, formDetail.getCreationDate());
      stmt.setInt(7, formDetail.getState());
      stmt.setString(8, formDetail.getInstanceId());
      stmt.setInt(9, formDetail.getId());
      stmt.executeUpdate();
    } catch (SQLException se) {
      throw new FormsOnlineException(failureOnUpdate("form", formDetail.getId()), se);
    }
  }

  /*
   * (non-Javadoc)
   * @see FormsOnlineDAO#getReceiversAsGroups(int,
   * java.lang.String)
   */
  @Override
  public List<String> getReceiversAsGroups(FormPK pk) throws FormsOnlineException {
    return getGroupRights(pk, "R");
  }

  /*
   * (non-Javadoc)
   * @see FormsOnlineDAO#getReceiversAsUsers(int, java.lang.String)
   */
  @Override
  public List<String> getReceiversAsUsers(FormPK pk) throws FormsOnlineException {
    return getUserRights(pk, "R");
  }

  /*
   * (non-Javadoc)
   * @see FormsOnlineDAO#getSendersAsGroups(int, java.lang.String)
   */
  @Override
  public List<String> getSendersAsGroups(FormPK pk) throws FormsOnlineException {
    return getGroupRights(pk, "S");
  }

  /*
   * (non-Javadoc)
   * @see FormsOnlineDAO#getSendersAsUsers(int, java.lang.String)
   */
  @Override
  public List<String> getSendersAsUsers(FormPK pk) throws FormsOnlineException {
    return getUserRights(pk, "S");
  }

  private List<String> getGroupRights(FormPK pk, String rightType)
      throws FormsOnlineException {
    List<String> groupsIds = new ArrayList<>();
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(QUERY_LOAD_GROUP_RIGHTS)) {
      stmt.setString(1, pk.getInstanceId());
      stmt.setInt(2, Integer.parseInt(pk.getId()));
      stmt.setString(3, rightType);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          groupsIds.add(rs.getString("groupId"));
        }
      }
    } catch (SQLException se) {
      throw new FormsOnlineException(failureOnGetting("senders as users of form", pk.toString()),
          se);
    }
    return groupsIds;
  }

  private List<String> getUserRights(FormPK pk, String rightType)
      throws FormsOnlineException {
    List<String> userIds = new ArrayList<>();
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(QUERY_LOAD_USER_RIGHTS)) {
      stmt.setString(1, pk.getInstanceId());
      stmt.setInt(2, Integer.parseInt(pk.getId()));
      stmt.setString(3, rightType);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          userIds.add(rs.getString("userId"));
        }
      }
    } catch (SQLException se) {
      throw new FormsOnlineException(
          failureOnGetting("user rights (" + rightType + ") of form", pk.toString()), se);
    }
    return userIds;
  }

  @Override
  public void updateReceivers(FormPK pk, String[] newUserReceiverIds, String[] newGroupReceiverIds)
      throws FormsOnlineException {
    updateRights(pk, newUserReceiverIds, newGroupReceiverIds, "R");
  }

  @Override
  public void updateSenders(FormPK pk, String[] newUserSenderIds, String[] newGroupSenderIds)
      throws FormsOnlineException {
    updateRights(pk, newUserSenderIds, newGroupSenderIds, "S");
  }

  private void updateRights(FormPK pk, String[] newUserIds, String[] newGroupIds, String rightType)
      throws FormsOnlineException {
    try (final Connection con = getConnection()) {
      removeGroupRights(con, pk, rightType);
      removeUserRights(con, pk, rightType);
      for (final String newUserId : newUserIds) {
        addUserRights(con, pk, newUserId, rightType);
      }
      for (final String newGroupId : newGroupIds) {
        addGroupRights(con, pk, newGroupId, rightType);
      }
    } catch (Exception e) {
      throw new FormsOnlineException(
          failureOnUpdate("user rights (" + rightType + ") of form", pk.toString()), e);
    }
  }

  private void addUserRights(Connection con, FormPK pk, String userId, String rightType)
      throws SQLException {
    try (PreparedStatement stmt = con.prepareStatement(QUERY_INSERT_USER_RIGHTS)) {
      stmt.setInt(1, Integer.parseInt(pk.getId()));
      stmt.setString(2, pk.getInstanceId());
      stmt.setString(3, rightType);
      stmt.setString(4, userId);

      stmt.executeUpdate();
    }
  }

  private void addGroupRights(Connection con, FormPK pk, String groupId, String rightType)
      throws SQLException {
    try (PreparedStatement stmt = con.prepareStatement(QUERY_INSERT_GROUP_RIGHTS)) {
      stmt.setInt(1, Integer.parseInt(pk.getId()));
      stmt.setString(2, pk.getInstanceId());
      stmt.setString(3, rightType);
      stmt.setString(4, groupId);
      stmt.executeUpdate();
    }
  }

  private void removeGroupRights(Connection con, FormPK pk, String rightType) throws SQLException {
    try (PreparedStatement stmt = con.prepareStatement(QUERY_REMOVE_GROUP_RIGHTS)) {
      stmt.setString(1, pk.getInstanceId());
      stmt.setInt(2, Integer.parseInt(pk.getId()));
      stmt.setString(3, rightType);
      stmt.executeUpdate();
    }
  }

  private void removeUserRights(Connection con, FormPK pk, String rightType) throws SQLException {
    try (PreparedStatement stmt = con.prepareStatement(QUERY_REMOVE_USER_RIGHTS)) {
      stmt.setString(1, pk.getInstanceId());
      stmt.setInt(2, Integer.parseInt(pk.getId()));
      stmt.setString(3, rightType);
      stmt.executeUpdate();
    }
  }

  /*
   * (non-Javadoc)
   * @see FormsOnlineDAO#getUserAvailableForms(java.lang.String,
   * java.lang.String, java.lang.String[])
   */
  @Override
  public List<FormDetail> getUserAvailableForms(final Collection<String> instanceIds,
      final String userId, final String[] userGroupIds) throws FormsOnlineException {
    try {
      final List<FormDetail> forms = new ArrayList<>();
      JdbcSqlQuery.executeBySplittingOn(instanceIds, (idBatch, ignore) -> {
          final JdbcSqlQuery query = JdbcSqlQuery.createSelect("*")
          .from(FORMS_TABLENAME)
          // 1st criteria : correct instanceId
          .where(INSTANCE_ID).in(instanceIds)
          // 2nd criteria : state published or old forms that user has sent before unpublish
          .and("(state = ?", FormDetail.STATE_PUBLISHED)
            .or("id in (select formId from " + FORMS_INSTANCES_TABLENAME + " where creatorId = ?))", userId)
          // 3rd criteria : user has receiver rights (directly or from a group)
          .and("(id in (select formId from " + USER_RIGHTS_TABLENAME + " where rightType='S' and userId = ?)", userId);
          if (isNotEmpty(userGroupIds)) {
            query.or("id in (select formId from " + GROUP_RIGHTS_TABLENAME + " where rightType='S' and groupId").in(userGroupIds).addSqlPart(")"); }
          query.addSqlPart(")");
          query.execute(r -> {
            forms.add(fetchFormDetail(r));
            return null;
          });
      });
      return forms;
    } catch (SQLException se) {
      throw new FormsOnlineException(
          failureOnGetting("user (" + userId + ") available forms of instances",
              String.join(",", instanceIds)), se);
    }
  }

  /*
   * (non-Javadoc)
   * @see FormsOnlineDAO#getForms(java.util.List)
   */
  @Override
  public List<FormDetail> getForms(List<String> formIds) throws FormsOnlineException {

    List<FormDetail> forms = new ArrayList<>();
    if ((formIds == null) || (formIds.isEmpty())) {
      return forms;
    }

    /* Build query */
    StringBuilder query = new StringBuilder("select * from " + FORMS_TABLENAME + " where id in (");
    int pos = 0;
    for (final String formId : formIds) {
      if (pos++ != 0) {
        query.append(", ");
      }
      query.append(formId);
    }
    query.append(")");

    /* launch query */
    try (final Connection con = getConnection();
         final Statement stmt = con.createStatement()) {
      try (ResultSet rs = stmt.executeQuery(query.toString())) {
        while (rs.next()) {
          FormDetail form = fetchFormDetail(rs);
          forms.add(form);
        }
      }
    } catch (SQLException se) {
      throw new FormsOnlineException(failureOnGetting("forms", String.join(",", formIds)), se);
    }
    return forms;
  }

  /*
   * (non-Javadoc)
   * @see FormsOnlineDAO#getSentFormInstances(java.lang.String,
   * int, java.lang.String)
   */
  @Override
  public SilverpeasList<FormInstance> getSentFormInstances(FormPK pk, String userId,
      final List<Integer> states, final PaginationCriterion paginationCriterion) {

    JdbcSqlQuery query = createSelect("*")
                        .from(FORMS_INSTANCES_TABLENAME)
                        .where("instanceid = ?", pk.getInstanceId())
                        .and("formId = ?", Integer.parseInt(pk.getId()))
                        .and("creatorId = ?", userId);

    return getFormInstances(query, states, paginationCriterion);
  }

  /*
   * (non-Javadoc)
   * @see FormsOnlineDAO#getReceivedRequests(java.lang.String,
   * java.lang.String)
   */
  @Override
  public SilverpeasList<FormInstance> getReceivedRequests(FormPK pk, boolean allRequests,
      String userId, final List<Integer> states, final PaginationCriterion paginationCriterion) {
    /*
     * then retrieve instances where : - user has been the validator - no validation has been done
     * yet and formid in available form ids
     */

    JdbcSqlQuery query = createSelect("*")
                         .from(FORMS_INSTANCES_TABLENAME)
                         .where("instanceid = ?", pk.getInstanceId())
                         .and("formId = ?", Integer.parseInt(pk.getId()));
    if (!allRequests) {
      query.and("(validatorId = ? or (validatorId is null))", userId);
    }

    return getFormInstances(query, states, paginationCriterion);
  }

  /**
   * Centralization of form instances queries execution.<br>
   * Common filtering and ordering are applied to the given {@link JdbcSqlQuery} instance.
   * @param preparedQuery an initialized and prepared {@link JdbcSqlQuery} instance.
   * @param states the states to filter on.
   * @param paginationCriterion the pagination criterion.
   * @return the result list.
   */
  private SilverpeasList<FormInstance> getFormInstances(final JdbcSqlQuery preparedQuery,
      final List<Integer> states, final PaginationCriterion paginationCriterion) {
    if (states != null && !states.isEmpty()) {
      preparedQuery.and(STATE_FIELD).in(states);
    }

    preparedQuery.orderBy("creationDate desc, id desc");

    try {
      return preparedQuery.withPagination(paginationCriterion).execute(this::fetchFormInstance);
    } catch (SQLException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  @Override
  public List<String> getAvailableFormIdsAsReceiver(String instanceId, String userId,
      String[] userGroupIds) throws FormsOnlineException {
    List<String> availableFormIds = new ArrayList<>();

    /* build query */
    StringBuilder query =
        new StringBuilder("select distinct id from " + FORMS_TABLENAME + " where instanceId = '" +
            instanceId + "' and id in (select formId from " + USER_RIGHTS_TABLENAME +
            " where rightType='R' and userId = '" + userId + "') ");
    if ((userGroupIds != null) && (userGroupIds.length > 0)) {
      query.append("or id in (select formId from " + GROUP_RIGHTS_TABLENAME +
          " where rightType='R' and groupId in ( ");
      for (int i = 0; i < userGroupIds.length; i++) {
        if (i != 0) {
          query.append(", ");
        }
        query.append("'").append(userGroupIds[i]).append("'");
      }
      query.append(") )");
    }

    try (final Connection con = getConnection();
         final Statement stmt = con.createStatement()) {
      try (ResultSet rs = stmt.executeQuery(query.toString())) {
        while (rs.next()) {
          availableFormIds.add(String.valueOf(rs.getInt("id")));
        }
      }
    } catch (SQLException se) {
      throw new FormsOnlineException(
          failureOnGetting("user (" + userId + ") available form as receiver ids of instance",
              instanceId), se);
    }
    return availableFormIds;
  }

  private FormDetail fetchFormDetail(ResultSet rs) throws SQLException {
    FormDetail form = new FormDetail();
    form.setId(rs.getInt("id"));
    form.setXmlFormName(rs.getString("xmlFormName"));
    form.setName(rs.getString("name"));
    form.setDescription(rs.getString("description"));
    form.setTitle(rs.getString("title"));
    form.setCreatorId(rs.getString("creatorId"));
    form.setCreationDate(new Date(rs.getTimestamp("creationDate").getTime()));
    form.setInstanceId(rs.getString(INSTANCE_ID));
    form.setState(rs.getInt(STATE_FIELD));
    return form;
  }

  /*
   * (non-Javadoc)
   *
   * FormsOnlineDAO#createInstance(com.silverpeas.formsonline.model
   * .FormInstance)
   */
  @Override
  public FormInstance createInstance(FormInstance instance) throws FormsOnlineException {
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(QUERY_INSERT_FORMINSTANCE)) {
      int id = DBUtil.getNextId(FORMS_INSTANCES_TABLENAME, "id");
      stmt.setInt(1, id);
      stmt.setInt(2, instance.getFormId());
      stmt.setInt(3, instance.getState());
      stmt.setString(4, instance.getCreatorId());
      prepareDateStatement(stmt, 5, instance.getCreationDate());
      stmt.setString(6, instance.getValidatorId());
      prepareDateStatement(stmt, 7, instance.getValidationDate());
      stmt.setString(8, instance.getComments());
      stmt.setString(9, instance.getComponentInstanceId());
      stmt.executeUpdate();
      instance.setId(id);
      return instance;
    } catch (SQLException se) {
      throw new FormsOnlineException(failureOnAdding("instance on form", instance.getFormId()),
          se);
    }
  }

  @Override
  public FormInstance getRequest(RequestPK pk) throws FormsOnlineException {
    FormInstance formInstance = null;
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(QUERY_LOAD_FORM_INSTANCE)) {
      stmt.setString(1, pk.getInstanceId());
      stmt.setInt(2, Integer.parseInt(pk.getId()));
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          formInstance = fetchFormInstance(rs);
        }
      }
    } catch (SQLException se) {
      throw new FormsOnlineException(failureOnGetting("form instance", pk.toString()), se);
    }
    return formInstance;
  }

  @Override
  public void updateRequest(FormInstance formInstance) throws FormsOnlineException {
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(QUERY_UPDATE_FORM_INSTANCE)) {
      stmt.setInt(1, formInstance.getFormId());
      stmt.setInt(2, formInstance.getState());
      stmt.setString(3, formInstance.getCreatorId());
      prepareDateStatement(stmt, 4, formInstance.getCreationDate());
      stmt.setString(5, formInstance.getValidatorId());
      prepareDateStatement(stmt, 6, formInstance.getValidationDate());
      stmt.setString(7, formInstance.getComments());
      stmt.setString(8, formInstance.getComponentInstanceId());
      stmt.setInt(9, formInstance.getIdAsInt());

      stmt.executeUpdate();
    } catch (SQLException se) {
      throw new FormsOnlineException(
          failureOnUpdate("form instance", formInstance.getPK().toString()), se);
    }
  }

  @Override
  public void deleteRequest(RequestPK pk) throws FormsOnlineException {
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(QUERY_DELETE_FORM_INSTANCE)) {
      stmt.setString(1, pk.getInstanceId());
      stmt.setInt(2, Integer.parseInt(pk.getId()));
      stmt.executeUpdate();
    } catch (SQLException se) {
      throw new FormsOnlineException(failureOnDeleting("form instance", pk.toString()), se);
    }
  }

  @Override
  public Map<Integer, Integer> getNumberOfRequestsByForm(String instanceId)
      throws FormsOnlineException {
    try (final Connection con = getConnection();
         final PreparedStatement stmt = con.prepareStatement(COUNT_REQUESTS_BY_FORM)) {
      stmt.setString(1, instanceId);
      final Map<Integer, Integer> map = new HashMap<>();
      try (final ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          map.put(rs.getInt(1), rs.getInt(2));
        }
      }
      return map;
    } catch (SQLException se) {
      throw new FormsOnlineException(
          failureOnGetting("number of instance by form of component instance", instanceId), se);
    }
  }

  @Override
  public SilverpeasList<FormInstance> getAllRequests(FormPK pk) {
    JdbcSqlQuery query = createSelect("*")
        .from(FORMS_INSTANCES_TABLENAME)
        .where("instanceid = ?", pk.getInstanceId())
        .and("formId = ?", Integer.parseInt(pk.getId()));

    return getFormInstances(query, null, PaginationCriterion.NO_PAGINATION);
  }

  private FormInstance fetchFormInstance(ResultSet rs) throws SQLException {
    FormInstance formInstance = new FormInstance();

    formInstance.setId(rs.getInt("id"));
    formInstance.setFormId(rs.getInt("formId"));
    formInstance.setState(rs.getInt(STATE_FIELD));
    formInstance.setCreatorId(rs.getString("creatorId"));
    formInstance.setCreationDate(rs.getTimestamp("creationDate"));
    formInstance.setValidatorId(rs.getString("validatorId"));
    formInstance.setValidationDate(rs.getTimestamp("validationDate"));
    formInstance.setComments(rs.getString("comments"));
    formInstance.setInstanceId(rs.getString(INSTANCE_ID));

    return formInstance;
  }

  /**
   * Get a new connection.
   * @return the initialized connection.
   */
  protected Connection getConnection() throws SQLException {
    return DBUtil.openConnection();
  }

  private static void prepareDateStatement(PreparedStatement stat, int pos, Date dateValue)
      throws SQLException {
    if (dateValue == null) {
      stat.setNull(pos, Types.DATE);
    } else {
      stat.setTimestamp(pos, new Timestamp(dateValue.getTime()));
    }
  }
}
