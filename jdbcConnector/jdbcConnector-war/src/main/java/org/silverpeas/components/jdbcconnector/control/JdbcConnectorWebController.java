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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.jdbcconnector.control;

import org.silverpeas.components.jdbcconnector.model.DataSourceDefinition;
import org.silverpeas.components.jdbcconnector.service.JdbcConnectorException;
import org.silverpeas.components.jdbcconnector.service.JdbcConnectorRuntimeException;
import org.silverpeas.components.jdbcconnector.service.JdbcRequester;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectTo;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternal;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * The web controller of the ConnecteurJDBC application. Like all of the web controllers in
 * Silverpeas, it is both session-scoped and spawn per application instance.
 * @author mmoquillon
 */
@WebComponentController("connecteurJDBC")
public class JdbcConnectorWebController extends
    org.silverpeas.core.web.mvc.webcomponent
        .WebComponentController<JdbcConnectorWebRequestContext> {

  private static final String TRANSLATIONS_PATH =
      "org.silverpeas.jdbcConnector.multilang.jdbcConnector";

  public static final String QUERY_RESULT = "queryResult";
  public static final String COMPARING_COLUMN = "comparingColumn";
  public static final String COMPARING_OPERATOR = "currentComparator";
  public static final String COMPARING_VALUE = "columnValue";
  public static final String COMPARING_OPERATORS = "comparators";

  private JdbcRequester requester;
  private String lastSqlQueryInError = EMPTY;
  private QueryResult queryResult = new QueryResult();

  /**
   * Constructs a new Web controller for the specified context and with the
   * {@link MainSessionController} instance that is specific to the user behind the access to the
   * underlying application instance.
   * @param controller the main session controller for the current user.
   * @param context the context identifying among others the targeted application instance.
   */
  public JdbcConnectorWebController(final MainSessionController controller,
      final ComponentContext context) {
    super(controller, context, TRANSLATIONS_PATH);
  }

  @Override
  protected void onInstantiation(final JdbcConnectorWebRequestContext context) {
    requester = new JdbcRequester(context.getComponentInstanceId());
  }

  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("jdbcConnector.jsp")
  @LowestRoleAccess(value = SilverpeasRole.READER)
  public void home(final JdbcConnectorWebRequestContext context) {
    viewResultSet(context);
  }

  @GET
  @Path("ViewResultSet")
  @RedirectToInternalJsp("jdbcConnector.jsp")
  @LowestRoleAccess(value = SilverpeasRole.READER)
  public void viewResultSet(final JdbcConnectorWebRequestContext context) {
    if (requester.isDataSourceDefined()) {
      String reload = context.getRequest().getParameter("reload");
      if (StringUtil.getBooleanValue(reload)) {
        clearQueryResult();
      }
      executeSQLQuery(context);
    }
    setQueryResult(context.getRequest());
  }

  @GET
  @Path("portlet")
  @RedirectToInternalJsp("portlet.jsp")
  @LowestRoleAccess(value = SilverpeasRole.READER)
  public void portlet(final JdbcConnectorWebRequestContext context) {
    home(context);
  }

  @GET
  @Path("ParameterRequest")
  @RedirectToInternalJsp("requestParameters.jsp")
  @LowestRoleAccess(value = SilverpeasRole.PUBLISHER, onError = @RedirectTo("Main"))
  public void editSQLRequest(final JdbcConnectorWebRequestContext context) {
    HttpRequest request = context.getRequest();
    request.setAttribute("sqlRequestInError", lastSqlQueryInError);
    request.setAttribute("sqlRequest", requester.getCurrentConnectionInfo().getSqlRequest());
    request.setAttribute("editorUrl",
        request.getContextPath() + getComponentUrl() + "/RequestEditor");
  }

  @POST
  @Path("DoRequest")
  @RedirectToInternalJsp("jdbcConnector.jsp")
  @LowestRoleAccess(value = SilverpeasRole.READER)
  public void performSQLRequest(final JdbcConnectorWebRequestContext context) {
    if (requester.isDataSourceDefined()) {
      executeSQLQuery(context);
      readRequestParameters(context.getRequest());
    }
    setQueryResult(context.getRequest());
  }

  @GET
  @Path("ParameterConnection")
  @RedirectToInternalJsp("connectionParameters.jsp")
  @LowestRoleAccess(value = SilverpeasRole.ADMIN, onError = @RedirectTo("Main"))
  public void editConnection(final JdbcConnectorWebRequestContext context) {
    context.getRequest()
        .setAttribute("currentConnectionInfo", requester.getCurrentConnectionInfo());
    context.getRequest().setAttribute("availableDataSources", DataSourceDefinition.getAll());
  }

  @POST
  @Path("UpdateConnection")
  @RedirectToInternal("{nextView}")
  @LowestRoleAccess(value = SilverpeasRole.PUBLISHER, onError = @RedirectTo("Main"))
  public void saveConnection(final JdbcConnectorWebRequestContext context) {
    String nextView = "ParameterRequest";
    String dataSource = context.getRequest().getParameter("DataSource");
    String login = context.getRequest().getParameter("Login");
    String password = context.getRequest().getParameter("Password");
    int rowLimit = 0;
    if (context.getRequest().isParameterDefined("RowLimit")) {
      rowLimit = context.getRequest().getParameterAsInteger("RowLimit");
    }
    requester.getCurrentConnectionInfo()
        .withDataSourceName(dataSource)
        .withLoginAndPassword(login, password)
        .withDataMaxNumber(rowLimit);
    // we check the connection: it is saved only if the connection with the data source can be
    // established. In that case, any previous SQL query and its result are cleared.
    try {
      requester.checkConnection();
      requester.getCurrentConnectionInfo().save();
      clearQueryResult();
    } catch (JdbcConnectorException e) {
      SilverLogger.getLogger(this).error(e);
      context.getMessager().addError(getString("erreurParametresConnectionIncorrects"));
      nextView = "ParameterConnection";
    }
    context.addRedirectVariable("nextView", nextView);
  }

  @POST
  @Path("SetSQLRequest")
  @RedirectToInternal("{nextView}")
  @LowestRoleAccess(value = SilverpeasRole.PUBLISHER, onError = @RedirectTo("Main"))
  public void saveSQLRequest(final JdbcConnectorWebRequestContext context) {
    String nextView = "Main";
    String sqlRequest = context.getRequest().getParameter("SQLReq");
    Optional<String> validationFailure = validateSQLRequest(sqlRequest);
    if (validationFailure.isPresent()) {
      lastSqlQueryInError = sqlRequest;
      context.getMessager().addError(
          getString("erreurRequeteIncorrect") + ": " + validationFailure.get());
      nextView = "ParameterRequest";
    } else {
      lastSqlQueryInError = EMPTY;
      requester.getCurrentConnectionInfo().withSqlRequest(sqlRequest.trim()).save();
      clearQueryResult();
    }
    context.addRedirectVariable("nextView", nextView);
  }

  @GET
  @Path("RequestEditor")
  @RedirectToInternalJsp("requestEditor.jsp")
  @LowestRoleAccess(value = SilverpeasRole.PUBLISHER, onError = @RedirectTo("Main"))
  public void openRequestEditor(final JdbcConnectorWebRequestContext context) {
    HttpRequest request = context.getRequest();
    try {
      List<String> tableNames = requester.getTableNames();
      Map<String, String> tables = new LinkedHashMap<>(tableNames.size());
      tableNames.forEach(t -> tables.put(t, String.join(",", requester.getColumnNames(t))));
      request.setAttribute("tables", tables);
      request.setAttribute(COMPARING_OPERATORS, TableRowsFilter.getAllComparators());
    } catch (JdbcConnectorRuntimeException e) {
      SilverLogger.getLogger(this).error(e);
      context.getMessager().addError(getString("sqlRequestExecutionFailure"));
    }
  }

  private Optional<String> validateSQLRequest(String request) {
    Optional<String> validationFailure = Optional.empty();
    if (!requester.isDataSourceDefined()) {
      validationFailure = Optional.of(getString("erreurParametresConnectionIncorrects"));
    } else if (StringUtil.isNotDefined(request)) {
      validationFailure = Optional.of(getString("erreurRequeteVide"));
    } else if (!request.trim().toLowerCase().startsWith("select")) {
      validationFailure = Optional.of(getString("erreurModifTable"));
    } else {
      try {
        queryResult.setNewResult(requester.request(request.trim()));
      } catch (JdbcConnectorException e) {
        SilverLogger.getLogger(this).error("Error while validating SQL request: " + request, e);
        validationFailure = Optional.of(e.getLocalizedMessage());
      }
    }
    return validationFailure;
  }

  private void readRequestParameters(final HttpRequest request) {
    if (request.isParameterNotNull(COMPARING_COLUMN)) {
      final Mutable<Object> firstNonNullValue = Mutable.of(EMPTY);
      final String fieldName =
          defaultStringIfNotDefined(request.getParameter(COMPARING_COLUMN), TableRowsFilter.FIELD_NONE);
      if (!TableRowsFilter.FIELD_NONE.equals(fieldName)) {
        queryResult.getFirstNonNullValueOfColumn(fieldName).ifPresent(firstNonNullValue::set);
      }
      queryResult.getFilter().setFieldName(fieldName, firstNonNullValue.get().getClass());
    }
    if (request.isParameterNotNull(COMPARING_OPERATOR)) {
      queryResult.getFilter().setComparator(request.getParameter(COMPARING_OPERATOR));
    }
    if (request.isParameterNotNull(COMPARING_VALUE)) {
      queryResult.getFilter().setFieldValue(request.getParameter(COMPARING_VALUE));
    }
  }

  private void executeSQLQuery(final JdbcConnectorWebRequestContext context) {
    if (!queryResult.existsRows()) {
      // the request is executed only once, when no query hasn't be yet performed.
      // after that, the query result is cached in this web controller.
      try {
        queryResult.setNewResult(requester.request());
      } catch (JdbcConnectorException e) {
        SilverLogger.getLogger(this).error(e);
        context.getMessager().addError(getString("sqlRequestExecutionFailure"));
      }
    }
  }

  private void clearQueryResult() {
    queryResult.clear();
  }

  private void setQueryResult(final HttpRequest request) {
    request.setAttribute(QUERY_RESULT, queryResult);
    request.setAttribute(COMPARING_COLUMN, queryResult.getFilter().getFieldName());
    request.setAttribute(COMPARING_OPERATOR, queryResult.getFilter().getComparator());
    request.setAttribute(COMPARING_VALUE, queryResult.getFilter().getFieldValue());
    request.setAttribute(COMPARING_OPERATORS, TableRowsFilter.getAllComparators());
  }

}
  