/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.components.mydb.web;

import org.silverpeas.components.mydb.model.DataSourceDefinition;
import org.silverpeas.components.mydb.model.DbColumn;
import org.silverpeas.components.mydb.model.DbTable;
import org.silverpeas.components.mydb.model.MyDBConnectionInfo;
import org.silverpeas.components.mydb.service.MyDBException;
import org.silverpeas.components.mydb.service.MyDBRuntimeException;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
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
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * The web controller of the MyDB application. Like all of the web controllers in
 * Silverpeas, it is both session-scoped and spawn per application instance.
 * @author mmoquillon
 */
@WebComponentController("myDB")
public class MyDBWebController
    extends org.silverpeas.core.web.mvc.webcomponent.WebComponentController<MyDBWebRequestContext> {

  private static final String TRANSLATIONS_PATH = "org.silverpeas.mydb.multilang.mydb";

  public static final String TABLE_VIEW = "tableView";
  public static final String ALL_TABLES = "tableNames";
  public static final String COMPARING_COLUMN = "comparingColumn";
  public static final String COMPARING_OPERATOR = "currentComparator";
  public static final String COMPARING_VALUE = "columnValue";
  public static final String COMPARING_OPERATORS = "comparators";

  private MyDBConnectionInfo connectionInfo;
  private TableView tableView = new TableView();

  /**
   * Constructs a new Web controller for the specified context and with the
   * {@link MainSessionController} instance that is specific to the user behind the access to the
   * underlying application instance.
   * @param controller the main session controller for the current user.
   * @param context the context identifying among others the targeted application instance.
   */
  public MyDBWebController(final MainSessionController controller, final ComponentContext context) {
    super(controller, context, TRANSLATIONS_PATH);
  }

  @Override
  protected void onInstantiation(final MyDBWebRequestContext context) {
    final String instanceId = context.getComponentInstanceId();
    final List<MyDBConnectionInfo> availableConnectionInfo =
        MyDBConnectionInfo.getFromComponentInstance(instanceId);
    if (availableConnectionInfo.isEmpty()) {
      this.connectionInfo = new MyDBConnectionInfo("", instanceId).withDefaultTableName("");
    } else {
      if (availableConnectionInfo.size() > 1) {
        throw new MyDBRuntimeException(
            "There is more than one defined data source for MyDB " + instanceId + ": " +
                availableConnectionInfo.size() + " data sources found!");
      }
      this.connectionInfo = availableConnectionInfo.get(0);
    }
  }

  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternal("{nextView}")
  @LowestRoleAccess(value = SilverpeasRole.reader)
  public void home(final MyDBWebRequestContext context) {
    final String nextView;
    if (this.connectionInfo.isDefined()) {
      nextView = "ViewTable";
    } else {
      nextView = "ConnectionSetting";
    }
    context.addRedirectVariable("nextView", nextView);
  }

  @GET
  @Path("ViewTable")
  @RedirectToInternalJsp("mydb.jsp")
  @LowestRoleAccess(value = SilverpeasRole.reader)
  public void viewTableContent(final MyDBWebRequestContext context) {
    try {
      if (!tableView.isDefined() && connectionInfo.isDefaultTableNameDefined()) {
        tableView.setTable(DbTable.defaultTable(connectionInfo));
      }
      setUpRequestAttributes(context.getRequest());
    } catch (MyDBRuntimeException e) {
      context.getMessager().addError(e.getMessage());
    }
  }

  @POST
  @Path("SetTable")
  @RedirectToInternalJsp("mydb.jsp")
  @LowestRoleAccess(value = SilverpeasRole.publisher)
  public void selectTable(final MyDBWebRequestContext context) {
    HttpRequest request = context.getRequest();
    final String tableName = request.getParameter(TABLE_VIEW);
    if (StringUtil.isDefined(tableName) &&
        !tableName.equals(connectionInfo.getDefaultTableName())) {
        connectionInfo.setDefaultTableName(tableName);
        connectionInfo.save();
        clearTableView();
    }
    viewTableContent(context);
  }

  @POST
  @Path("FilterTable")
  @RedirectToInternalJsp("mydb.jsp")
  @LowestRoleAccess(value = SilverpeasRole.reader)
  public void filterTableContent(final MyDBWebRequestContext context) {
    try {
      if (connectionInfo.isDefined()) {
        readRequestParameters(context.getRequest());
      }
      setUpRequestAttributes(context.getRequest());
    } catch(MyDBRuntimeException e) {
      context.getMessager().addError(e.getMessage());
    }
  }

  @GET
  @Path("ConnectionSetting")
  @RedirectToInternalJsp("connectionSettings.jsp")
  @LowestRoleAccess(value = SilverpeasRole.admin, onError = @RedirectTo("Main"))
  public void editConnection(final MyDBWebRequestContext context) {
    context.getRequest().setAttribute("currentConnectionInfo", connectionInfo);
    context.getRequest().setAttribute("availableDataSources", DataSourceDefinition.getAll());
  }

  @POST
  @Path("UpdateConnection")
  @RedirectToInternal("{nextView}")
  @LowestRoleAccess(value = SilverpeasRole.publisher, onError = @RedirectTo("Main"))
  public void saveConnection(final MyDBWebRequestContext context) {
    String nextView = "ViewTable";
    String dataSource = context.getRequest().getParameter("DataSource");
    String login = context.getRequest().getParameter("Login");
    String password = context.getRequest().getParameter("Password");
    int rowLimit = 0;
    if (context.getRequest().isParameterDefined("RowLimit")) {
      rowLimit = context.getRequest().getParameterAsInteger("RowLimit");
    }
    connectionInfo.withDataSourceName(dataSource)
        .withLoginAndPassword(login, password)
        .withDataMaxNumber(rowLimit)
        .withoutAnyDefaultTable();
    // we check the connection: it is saved only if the connection with the data source can be
    // established. In that case, the current table view is cleared.
    try {
      connectionInfo.checkConnection();
      connectionInfo.save();
      clearTableView();
    } catch (MyDBException e) {
      SilverLogger.getLogger(this).error(e);
      context.getMessager().addError(getString("mydb.error.invalidConnectionSettings"));
      nextView = "ConnectionSetting";
    }
    context.addRedirectVariable("nextView", nextView);
  }

  private void readRequestParameters(final HttpRequest request) {
    if (request.isParameterNotNull(COMPARING_COLUMN)) {
      final String fieldName = defaultStringIfNotDefined(request.getParameter(COMPARING_COLUMN),
          TableRowsFilter.FIELD_NONE);
      if (TableRowsFilter.FIELD_NONE.equals(fieldName)) {
        tableView.getFilter().clear();
      } else {
        tableView.filterOnColumn(fieldName);
        if (request.isParameterNotNull(COMPARING_OPERATOR)) {
          tableView.getFilter().setComparator(request.getParameter(COMPARING_OPERATOR));
        }
        if (request.isParameterNotNull(COMPARING_VALUE)) {
          tableView.getFilter().setColumnValue(request.getParameter(COMPARING_VALUE));
        }
      }
    }
  }

  private void clearTableView() {
    tableView.clear();
  }

  private void setUpRequestAttributes(final HttpRequest request) {
    final List<String> tableNames;
    if (connectionInfo.isDefined()) {
      tableNames = DbTable.list(connectionInfo);
    } else {
      tableNames = Collections.emptyList();
    }
    request.setAttribute(TABLE_VIEW, tableView);
    request.setAttribute(ALL_TABLES, tableNames);
    request.setAttribute(COMPARING_COLUMN, tableView.getFilter()
        .getColumn()
        .map(DbColumn::getName)
        .orElse(TableRowsFilter.FIELD_NONE));
    request.setAttribute(COMPARING_OPERATOR, tableView.getFilter().getComparator());
    request.setAttribute(COMPARING_VALUE, tableView.getFilter().getColumnValue());
    request.setAttribute(COMPARING_OPERATORS, TableRowsFilter.getAllComparators());
  }

}
  