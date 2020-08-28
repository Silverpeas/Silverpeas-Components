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

import org.silverpeas.components.formsonline.model.RequestCriteria.QUERY_ORDER_BY;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQueries;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.SilverpeasArrayList;
import org.silverpeas.core.util.SilverpeasList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.silverpeas.components.formsonline.model.FormDetail.RECEIVERS_TYPE_FINAL;
import static org.silverpeas.components.formsonline.model.FormDetail.RECEIVERS_TYPE_INTERMEDIATE;
import static org.silverpeas.components.formsonline.model.RequestCriteria.QUERY_ORDER_BY.CREATION_DATE_DESC;
import static org.silverpeas.components.formsonline.model.RequestCriteria.QUERY_ORDER_BY.ID_DESC;
import static org.silverpeas.core.SilverpeasExceptionMessages.*;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.*;
import static org.silverpeas.core.util.StringUtil.isDefined;

public class FormsOnlineDAOJdbc implements FormsOnlineDAO {

  // General infos
  private static final String FORMS_TABLENAME = "SC_FormsOnline_Forms";
  private static final String FORMS_INSTANCES_TABLENAME = "SC_FormsOnline_FormInstances";
  private static final String FORMS_INSTANCE_VALIDATIONS_TABLENAME = "SC_FormsOnline_FormInstVali";
  private static final String USER_RIGHTS_TABLENAME = "SC_FormsOnline_UserRights";
  private static final String GROUP_RIGHTS_TABLENAME = "SC_FormsOnline_GroupRights";

  private static final String SELECT_FROM = "SELECT * FROM ";
  private static final String INSERT_INTO = "INSERT INTO ";
  private static final String DELETE_FROM = "DELETE FROM ";
  // Queries about Forms
  private static final String QUERY_FIND_FORMS =
      SELECT_FROM + FORMS_TABLENAME + " where instanceId = ?";
  private static final String QUERY_LOAD_FORM =
      SELECT_FROM + FORMS_TABLENAME + " where instanceId = ? and id = ?";
  private static final String QUERY_INSERT_FORM = INSERT_INTO + FORMS_TABLENAME +
      "(id, xmlFormName, name, description, title, creatorId, creationDate, state, " +
      "instanceId, hierarchicalValidation, formInstExchangeReceiver, deleteAfterFormInstExchange) " +
      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  private static final String QUERY_UPDATE_FORM = "update " + FORMS_TABLENAME +
      " set xmlFormName = ?, name = ?, description = ?, title = ?, creatorId = ?, creationDate = " +
      "?, state = ?, hierarchicalValidation = ?, formInstExchangeReceiver = ?, " +
      "deleteAfterFormInstExchange = ? where instanceId = ? and id= ? ";
  private static final String QUERY_DELETE_FORM =
      DELETE_FROM + FORMS_TABLENAME + " where instanceId = ? and id = ? ";

  // Queries about Forms instances
  private static final String COUNT_REQUESTS_BY_FORM =
      "select formid, count(*) from " + FORMS_INSTANCES_TABLENAME + " where instanceId = ? group by formid";

  private static final String WHERE_FULL_RIGHT_CLAUSE = " where instanceId = ? and formId = ? and rightType = ?";
  // Queries about Rights
  private static final String QUERY_LOAD_USER_RIGHTS = SELECT_FROM + USER_RIGHTS_TABLENAME +
      WHERE_FULL_RIGHT_CLAUSE;
  private static final String QUERY_LOAD_GROUP_RIGHTS = SELECT_FROM + GROUP_RIGHTS_TABLENAME +
      WHERE_FULL_RIGHT_CLAUSE;
  private static final String QUERY_REMOVE_USER_RIGHTS = DELETE_FROM + USER_RIGHTS_TABLENAME +
      WHERE_FULL_RIGHT_CLAUSE;
  private static final String QUERY_REMOVE_GROUP_RIGHTS = DELETE_FROM + GROUP_RIGHTS_TABLENAME +
      WHERE_FULL_RIGHT_CLAUSE;
  private static final String QUERY_INSERT_USER_RIGHTS =
      INSERT_INTO + USER_RIGHTS_TABLENAME + "(formId, instanceId, rightType, userId) " +
          "VALUES (?, ?, ?, ?)";
  private static final String QUERY_INSERT_GROUP_RIGHTS =
      INSERT_INTO + GROUP_RIGHTS_TABLENAME + "(formId, instanceId, rightType, groupId) " +
          "VALUES (?, ?, ?, ?)";

  private static final String ID = "id";
  private static final String ID_CRITERIA = ID + " = ?";
  private static final String STATE = "state";
  private static final String INSTANCE_ID = "instanceId";
  private static final String FORM_ID = "formId";
  private static final String CREATION_DATE = "creationDate";
  private static final String CREATOR_ID = "creatorId";
  private static final String REQUEST_MSG = "instance on form";
  private static final String FORM_INST_ID = "formInstId";
  private static final String FORM_INST_ID_CLAUSE = FORM_INST_ID + " = r.id";
  private static final String VALIDATION_BY = "validationBy";

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
      stmt.setBoolean(10, formDetail.isHierarchicalValidation());
      stmt.setString(11, formDetail.getRequestExchangeReceiver().orElse(null));
      stmt.setBoolean(12, formDetail.isDeleteAfterRequestExchange());
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
      removeGroupRights(con, pk, RECEIVERS_TYPE_INTERMEDIATE);
      removeUserRights(con, pk, RECEIVERS_TYPE_INTERMEDIATE);
      removeGroupRights(con, pk, RECEIVERS_TYPE_FINAL);
      removeUserRights(con, pk, RECEIVERS_TYPE_FINAL);
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
      stmt.setBoolean(8, formDetail.isHierarchicalValidation());
      stmt.setString(9, formDetail.getRequestExchangeReceiver().orElse(null));
      stmt.setBoolean(10, formDetail.isDeleteAfterRequestExchange());
      stmt.setString(11, formDetail.getInstanceId());
      stmt.setInt(12, formDetail.getId());
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
  public List<String> getReceiversAsGroups(FormPK pk, String rightType) throws FormsOnlineException {
    return getGroupRights(pk, rightType);
  }

  /*
   * (non-Javadoc)
   * @see FormsOnlineDAO#getReceiversAsUsers(int, java.lang.String)
   */
  @Override
  public List<String> getReceiversAsUsers(FormPK pk, String rightType) throws FormsOnlineException {
    return getUserRights(pk, rightType);
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
  public void updateReceivers(FormPK pk, String[] newUserReceiverIds, String[] newGroupReceiverIds,
      String rightType) throws FormsOnlineException {
    updateRights(pk, newUserReceiverIds, newGroupReceiverIds, rightType);
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
  public List<FormDetail> getForms(Collection<String> formIds) throws FormsOnlineException {

    List<FormDetail> forms = new ArrayList<>();
    if ((formIds == null) || (formIds.isEmpty())) {
      return forms;
    }

    /* Build query */
    StringBuilder query = new StringBuilder(SELECT_FROM + FORMS_TABLENAME + " where id in (");
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
      final List<Integer> states, final PaginationPage paginationPage) throws FormsOnlineException {
    return getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(pk.getInstanceId())
        .andFormIds(pk.getId())
        .andCreatorId(userId)
        .andStates(states)
        .paginateBy(paginationPage));
  }

  /*
   * (non-Javadoc)
   * @see FormsOnlineDAO#getReceivedRequests(java.lang.String,
   * java.lang.String)
   */
  @Override
  public SilverpeasList<FormInstance> getReceivedRequests(FormDetail form, boolean allRequests,
      String validatorId, final Set<String> senderIdsManagedByValidator,
      final List<Integer> states, final PaginationPage paginationPage) throws FormsOnlineException {
    /*
     * then retrieve instances where :
     *  - user has been the validator
     *  - no validation has been done yet and form id in available form ids
     *  - sender ids managed by the validator
     */
    final FormPK pk = form.getPK();
    final RequestCriteria criteria = RequestCriteria
        .onComponentInstanceIds(pk.getInstanceId())
        .andFormIds(pk.getId())
        .andStates(states)
        .paginateBy(paginationPage);
    if (!allRequests) {
      criteria.andValidatorIdOrNoValidatorOrSenderIdsManagedByValidator(validatorId, senderIdsManagedByValidator);
    }
    return getRequestsByCriteria(criteria);
  }

  /**
   * Centralization of request queries execution.<br>
   * Common filtering and ordering are applied to the given {@link JdbcSqlQuery} instance.
   * @param criteria criteria to apply.
   * @return the result list.
   */
  SilverpeasList<FormInstance> getRequestsByCriteria(final RequestCriteria criteria)
      throws FormsOnlineException {
    if (criteria.emptyResultWhenNoFilteringOnComponentInstances()) {
      return new SilverpeasArrayList<>(0);
    }
    final JdbcSqlQuery query = JdbcSqlQuery
        .createSelect("r.*")
        .from(FORMS_INSTANCES_TABLENAME + " r")
        .where("instanceid").in(criteria.getComponentInstanceIds());
    if (!criteria.getIds().isEmpty()) {
      query.and("id").in(criteria.getIds().stream().map(Integer::parseInt).collect(toSet()));
    }
    if (!criteria.getFormIds().isEmpty()) {
      query.and(FORM_ID).in(criteria.getFormIds().stream().map(Integer::parseInt).collect(toSet()));
    }
    if (!criteria.getCreatorIds().isEmpty()) {
      query.and(CREATOR_ID).in(criteria.getCreatorIds());
    }
    if (!criteria.getStates().isEmpty()) {
      query.and(STATE).in(criteria.getStates());
    }
    applyValidationCriteria(criteria, query);
    // default order by if none defined
    final List<QUERY_ORDER_BY> orderBies = criteria.getOrderByList().isEmpty()
        ? Arrays.asList(CREATION_DATE_DESC, ID_DESC)
        : criteria.getOrderByList();
    query.orderBy(orderBies.stream()
        .map(o -> o.getPropertyName() + " " + (o.isAsc() ? "asc" : "desc"))
        .collect(Collectors.joining(",")));
    // pagination
    if (criteria.getPagination() != null) {
      query.withPagination(criteria.getPagination().asCriterion());
    }
    // execution
    try (final Connection connection = DBUtil.openConnection()) {
      final SilverpeasList<FormInstance> requests = query.executeWith(connection, this::fetchFormInstance);
      return decorateWithValidations(connection, requests);
    } catch (Exception e) {
      throw new FormsOnlineException(failureOnGetting("form instance", criteria.toString()), e);
    }
  }

  private void applyValidationCriteria(final RequestCriteria criteria, final JdbcSqlQuery query) {
    if (isDefined(criteria.getValidatorId())) {
      query.and("(");
      query.addSqlPart("EXISTS(SELECT 1")
          .from(FORMS_INSTANCE_VALIDATIONS_TABLENAME)
          .where(FORM_INST_ID_CLAUSE)
          .and(VALIDATION_BY + " = ?)", criteria.getValidatorId());
      if (criteria.isNoValidator()) {
        query.or("NOT EXISTS(SELECT 1")
             .from(FORMS_INSTANCE_VALIDATIONS_TABLENAME)
             .where(FORM_INST_ID_CLAUSE)
             .addSqlPart(")");
      }
      if (!criteria.getSenderIdsManagedByValidator().isEmpty()) {
        query.or(CREATOR_ID).in(criteria.getSenderIdsManagedByValidator());
      }
      query.addSqlPart(")");
    } else if (criteria.isNoValidator()) {
      query.and("(");
      query.addSqlPart("NOT EXISTS(SELECT 1")
          .from(FORMS_INSTANCE_VALIDATIONS_TABLENAME)
          .where(FORM_INST_ID_CLAUSE)
          .addSqlPart(")");
      if (!criteria.getSenderIdsManagedByValidator().isEmpty()) {
        query.or(CREATOR_ID).in(criteria.getSenderIdsManagedByValidator());
      }
      query.addSqlPart(")");
    } else if (!criteria.getSenderIdsManagedByValidator().isEmpty()) {
      query.and(CREATOR_ID).in(criteria.getSenderIdsManagedByValidator());
    }
  }

  private SilverpeasList<FormInstance> decorateWithValidations(final Connection con,
      final SilverpeasList<FormInstance> requests) throws SQLException {
    if (!requests.isEmpty()) {
      final Mutable<Integer> min = Mutable.of(Integer.MAX_VALUE);
      final Mutable<Integer> max = Mutable.of(Integer.MIN_VALUE);
      final Map<Integer, FormInstance> indexedById = requests.stream()
          .map(i -> {
            final int idAsInt = i.getIdAsInt();
            min.set(Math.min(min.get(), idAsInt));
            max.set(Math.max(max.get(), idAsInt));
            return i;
          })
          .collect(toMap(FormInstance::getIdAsInt, r -> r));
      JdbcSqlQuery.createSelect("*")
          .from(FORMS_INSTANCE_VALIDATIONS_TABLENAME)
          .where(FORM_INST_ID + " BETWEEN ? AND ?", min.get(), max.get())
          .executeWith(con, r -> {
            final FormInstance request = indexedById.get(r.getInt(FORM_INST_ID));
            if (request != null) {
              final FormInstanceValidation validation = new FormInstanceValidation(request);
              validation.setId(r.getInt("id"));
              validation.setStatus(ContributionStatus.valueOf(r.getString("status")));
              validation.setComment(r.getString("validationComment"));
              validation.setValidationBy(r.getString(VALIDATION_BY));
              validation.setDate(r.getTimestamp("validationDate"));
              validation.setValidationType(FormInstanceValidationType.valueOf(r.getString("validationType")));
              validation.setFollower(r.getBoolean("follower"));
              request.getValidations().add(validation);
            }
            return null;
          });
    }
    return requests;
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
    form.setCreatorId(rs.getString(CREATOR_ID));
    form.setCreationDate(rs.getTimestamp(CREATION_DATE));
    form.setInstanceId(rs.getString(INSTANCE_ID));
    form.setState(rs.getInt(STATE));
    form.setHierarchicalValidation(rs.getBoolean("hierarchicalValidation"));
    form.setRequestExchangeReceiver(rs.getString("formInstExchangeReceiver"));
    form.setDeleteAfterRequestExchange(rs.getBoolean("deleteAfterFormInstExchange"));
    return form;
  }

  @Override
  public FormInstance saveRequest(final FormInstance request) throws FormsOnlineException {
    final boolean isInsert = !isSqlDefined(request.getId());
    try {
      final JdbcSqlQueries saveQueries = new JdbcSqlQueries();
      final int formInstId;
      final JdbcSqlQuery formInstanceSave;
      if (isInsert) {
        formInstId = DBUtil.getNextId(FORMS_INSTANCES_TABLENAME, "id");
        request.setId(formInstId);
        formInstanceSave = createInsertFor(FORMS_INSTANCES_TABLENAME);
        formInstanceSave.addInsertParam(ID, formInstId);
      } else {
        formInstId = request.getIdAsInt();
        formInstanceSave = createUpdateFor(FORMS_INSTANCES_TABLENAME);
      }
      saveQueries.add(formInstanceSave);
      formInstanceSave.addSaveParam(FORM_ID, request.getFormId(), isInsert);
      formInstanceSave.addSaveParam(STATE, request.getState(), isInsert);
      formInstanceSave.addSaveParam(INSTANCE_ID, request.getComponentInstanceId(), isInsert);
      if (isInsert) {
        formInstanceSave.addSaveParam(CREATOR_ID, request.getCreatorId(), true);
        formInstanceSave.addSaveParam(CREATION_DATE, Timestamp.from(Instant.now()), true);
      } else {
        if (request.getState() <= FormInstance.STATE_UNREAD) {
          formInstanceSave.addSaveParam(CREATION_DATE, Timestamp.from(Instant.now()), false);
        }
        formInstanceSave.where(ID_CRITERIA, formInstId);
      }
      saveQueries.addAll(prepareSaveValidations(request.getValidations()));
      saveQueries.execute();
      return request;
    } catch (Exception e) {
      final String idAsString = String.valueOf(request.getFormId());
      if (isInsert) {
        throw new FormsOnlineException(failureOnAdding(REQUEST_MSG, idAsString), e);
      } else {
        throw new FormsOnlineException(failureOnUpdate(REQUEST_MSG, idAsString), e);
      }
    }
  }

  private List<JdbcSqlQuery> prepareSaveValidations(
      final Collection<FormInstanceValidation> validations) {
    return validations.stream()
        .map(v -> {
          final JdbcSqlQuery validationSave;
          final boolean isInsert = v.getId() == null || v.getId() == -1;
          final int validationId;
          if (isInsert) {
            validationId = DBUtil.getNextId(FORMS_INSTANCE_VALIDATIONS_TABLENAME, "id");
            v.setId(validationId);
            validationSave = createInsertFor(FORMS_INSTANCE_VALIDATIONS_TABLENAME);
            validationSave.addInsertParam(ID, validationId);
          } else {
            validationId = v.getId();
            validationSave = createUpdateFor(FORMS_INSTANCE_VALIDATIONS_TABLENAME);
          }
          validationSave.addSaveParam(FORM_INST_ID, v.getFormInstance().getIdAsInt(), isInsert);
          validationSave.addSaveParam(VALIDATION_BY, v.getValidator().getId(), isInsert);
          validationSave.addSaveParam("validationType", v.getValidationType().name(), isInsert);
          validationSave.addSaveParam("status", v.getStatus().name(), isInsert);
          validationSave.addSaveParam("validationComment", v.getComment(), isInsert);
          validationSave.addSaveParam("follower", v.isFollower(), isInsert);
          if (isInsert) {
            final Timestamp validationDate = v.getDate() != null
                ? new Timestamp(v.getDate().getTime())
                : Timestamp.from(Instant.now());
            validationSave.addSaveParam("validationDate", validationDate, true);
          } else {
            validationSave.where(ID_CRITERIA, validationId);
          }
          return validationSave;
        })
        .collect(Collectors.toList());
  }

  @Override
  public void saveRequestState(final FormInstance request) throws FormsOnlineException {
    try {
      if (!isSqlDefined(request.getId())) {
        throw new FormsOnlineException(failureOnUpdate(REQUEST_MSG, request.getFormId()));
      }
      Transaction.performInOne(() -> {
        final JdbcSqlQuery formInstanceStateSave = createUpdateFor(FORMS_INSTANCES_TABLENAME);
        formInstanceStateSave.addUpdateParam(STATE, request.getState());
        formInstanceStateSave.where(ID_CRITERIA, request.getIdAsInt());
        return formInstanceStateSave.execute();
      });
    } catch (Exception e) {
      throw new FormsOnlineException(failureOnUpdate("instance state on form", request.getFormId()), e);
    }
  }

  @Override
  public FormInstance getRequest(RequestPK pk) throws FormsOnlineException {
    return unique(getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(pk.getInstanceId())
        .andIds(pk.getId())));
  }

  @Override
  public void deleteRequest(RequestPK pk) throws FormsOnlineException {
    try {
      final JdbcSqlQueries deleteQueries = new JdbcSqlQueries();
      deleteQueries.add(JdbcSqlQuery
          .createDeleteFor(FORMS_INSTANCE_VALIDATIONS_TABLENAME)
          .where("formInstId = ?", Integer.parseInt(pk.getId())));
      deleteQueries.add(JdbcSqlQuery
          .createDeleteFor(FORMS_INSTANCES_TABLENAME)
          .where("instanceId = ?", pk.getInstanceId())
          .and("id = ?", Integer.parseInt(pk.getId())));
      deleteQueries.execute();
    } catch (Exception e) {
      throw new FormsOnlineException(failureOnDeleting("form instance", pk.toString()), e);
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
  public SilverpeasList<FormInstance> getAllRequests(FormPK pk) throws FormsOnlineException {
    return getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(pk.getInstanceId())
        .andFormIds(pk.getId()));
  }

  private FormInstance fetchFormInstance(final ResultSet rs) throws SQLException {
    final FormInstance formInstance = new FormInstance();
    formInstance.setId(rs.getInt("id"));
    formInstance.setFormId(rs.getInt(FORM_ID));
    formInstance.setState(rs.getInt(STATE));
    formInstance.setCreatorId(rs.getString(CREATOR_ID));
    formInstance.setCreationDate(rs.getTimestamp(CREATION_DATE));
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
