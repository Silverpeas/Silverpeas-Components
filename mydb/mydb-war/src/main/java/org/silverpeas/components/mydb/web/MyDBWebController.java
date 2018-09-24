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
import org.silverpeas.components.mydb.model.TableFieldValue;
import org.silverpeas.components.mydb.model.TableRow;
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
import java.sql.JDBCType;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.silverpeas.components.mydb.web.TableRowsFilter.FIELD_NONE;
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
  private static final String ICONS_PATH = "org.silverpeas.mydb.settings.mydbIcons";

  public static final String TABLE_VIEW = "tableView";
  public static final String ALL_TABLES = "tableNames";
  public static final String COMPARING_COLUMN = "comparingColumn";
  public static final String COMPARING_OPERATOR = "currentComparator";
  public static final String COMPARING_VALUE = "columnValue";
  public static final String COMPARING_OPERATORS = "comparators";
  public static final String ALL_COLUMNS = "tableColumns";
  public static final String ROW_INDEX = "row";
  public static final String ROW = "row";
  public static final String ERROR_MESSAGE = "error";
  private static final String ERROR_NO_SELECTED_TABLE = "mydb.error.noSelectedTable";

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
    super(controller, context, TRANSLATIONS_PATH, ICONS_PATH);
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
      context.getMessager().addError(e.getLocalizedMessage());
    }
  }

  @GET
  @Path("NewRow")
  @RedirectToInternalJsp("newRowForm.jsp")
  @LowestRoleAccess(value = SilverpeasRole.publisher)
  public void getNewTableRowForm(final MyDBWebRequestContext context) {
    if (tableView.isDefined()) {
      context.getRequest().setAttribute(ALL_COLUMNS, tableView.getColumns());
    } else {
      context.getRequest()
          .setAttribute(ERROR_MESSAGE, getMultilang().getString(ERROR_NO_SELECTED_TABLE));
    }
  }

  @GET
  @Path("GetRow")
  @RedirectToInternalJsp("updateRowForm.jsp")
  @LowestRoleAccess(value = SilverpeasRole.publisher)
  public void getTableRowForm(final MyDBWebRequestContext context) {
    try {
      if (tableView.isDefined()) {
        final int rowIdx = context.getRequest().getParameterAsInteger(ROW_INDEX);
        final List<TableRow> rows = tableView.getRows();
        if (rowIdx >= rows.size()) {
          context.getRequest()
              .setAttribute(ERROR_MESSAGE, getMultilang().getString("mydb.error.invalidRow"));
        } else {
          context.getRequest().setAttribute(ROW, rows.get(rowIdx));
          context.getRequest().setAttribute(ALL_COLUMNS, tableView.getColumns());
        }
      } else {
        context.getRequest()
            .setAttribute(ERROR_MESSAGE, getMultilang().getString(ERROR_NO_SELECTED_TABLE));
      }
    } catch (Exception e) {
      context.getRequest().setAttribute(ERROR_MESSAGE, e.getLocalizedMessage());
    }
  }

  @GET
  @Path("ViewTargetTable")
  @RedirectToInternalJsp("fkTable.jsp")
  @LowestRoleAccess(value = SilverpeasRole.publisher)
  public void getForeignKeyTableViewFrom(final MyDBWebRequestContext context) {
    try {
      final String targetTableName = context.getRequest().getParameter(TABLE_VIEW);
      final Optional<DbTable> targetTable = DbTable.table(targetTableName, connectionInfo);
      if (targetTable.isPresent()) {
        TableView targetTableView = new TableView();
        targetTableView.setTable(targetTable);
        context.getRequest().setAttribute(TABLE_VIEW, targetTableView);
      } else {
        context.getRequest()
            .setAttribute(ERROR_MESSAGE, getMultilang().getString("mydb.error.nonExistingTable"));
      }
    } catch (Exception e) {
      context.getRequest().setAttribute(ERROR_MESSAGE, e.getLocalizedMessage());
    }
  }

  @POST
  @Path("AddRow")
  @RedirectToInternalJsp("mydb.jsp")
  @LowestRoleAccess(value = SilverpeasRole.publisher)
  public void addNewTableRow(final MyDBWebRequestContext context) {
    try {
      if (tableView.isDefined()) {
        final HttpRequest request = context.getRequest();
        final Enumeration<String> params = request.getParameterNames();
        final Map<String, TableFieldValue> tuples = new HashMap<>();
        while (params.hasMoreElements()) {
          final String paramName = params.nextElement();
          final Optional<DbColumn> column = tableView.getColumn(paramName);
          column.ifPresent(c -> {
            final String paramValue = request.getParameter(paramName);
            if (paramValue == null || (paramValue.isEmpty() && !c.isOfTypeText())) {
              throwInvalidValueType(paramName, c.getType());
            }
            final TableFieldValue value = TableFieldValue.fromString(paramValue, c.getType());
            tuples.put(paramName, value);
          });
        }
        if (!tuples.isEmpty()) {
          tableView.addRow(new TableRow(tuples));
        } else {
          context.getMessager().addError(getMultilang().getString("mydb.error.invalidRow"));
        }
      } else {
        context.getMessager().addError(getMultilang().getString(ERROR_NO_SELECTED_TABLE));
      }
    } catch (IllegalArgumentException | MyDBRuntimeException e) {
      context.getMessager().addError(e.getLocalizedMessage());
    }
    viewTableContent(context);
  }

  @POST
  @Path("UpdateRow")
  @RedirectToInternalJsp("mydb.jsp")
  @LowestRoleAccess(value = SilverpeasRole.publisher)
  public void updateTableRow(final MyDBWebRequestContext context) {
    try {
      if (tableView.isDefined()) {
        final HttpRequest request = context.getRequest();
        final int rowIndex = request.getParameterAsInteger(ROW_INDEX);
        final TableRow row = tableView.getRows().get(rowIndex);
        final Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
          final String paramName = params.nextElement();
          if (!paramName.equals(ROW_INDEX)) {
            final TableFieldValue value = row.getFieldValue(paramName);
            final String newValue = request.getParameter(paramName);
            updateValue(paramName, value, newValue);
          }
        }
        tableView.updateRow(rowIndex, row);
      } else {
        context.getMessager().addError(getMultilang().getString(ERROR_NO_SELECTED_TABLE));
      }
    } catch (IllegalArgumentException | MyDBRuntimeException e) {
      context.getMessager().addError(e.getLocalizedMessage());
    }
    viewTableContent(context);
  }

  private void updateValue(final String name, final TableFieldValue value, final String newValue) {
    if (value != null && !value.toString().equals(newValue)) {
      if (newValue == null || (newValue.isEmpty() && !value.isText())) {
        throwInvalidValueType(name, value.getType());
      }
      value.update(newValue);
    }
  }

  @POST
  @Path("DeleteRow")
  @RedirectToInternalJsp("mydb.jsp")
  @LowestRoleAccess(value = SilverpeasRole.publisher)
  public void deleteTableRow(final MyDBWebRequestContext context) {
    try {
      if (tableView.isDefined()) {
        final int rowIdx = context.getRequest().getParameterAsInteger(ROW_INDEX);
        tableView.deleteRow(rowIdx);
      } else {
        context.getMessager().addError(getMultilang().getString(ERROR_NO_SELECTED_TABLE));
      }
    } catch (MyDBRuntimeException e) {
      context.getMessager().addError(e.getLocalizedMessage());
    }
    viewTableContent(context);
  }

  @POST
  @Path("SetTable")
  @RedirectToInternalJsp("mydb.jsp")
  @LowestRoleAccess(value = SilverpeasRole.reader)
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
      context.getMessager().addError(e.getLocalizedMessage());
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
    if (request.isParameterNotNull(COMPARING_COLUMN) &&
        request.isParameterNotNull(COMPARING_OPERATOR)) {
      final String fieldName = defaultStringIfNotDefined(request.getParameter(COMPARING_COLUMN),
          FIELD_NONE);
      final String comparator =
          defaultStringIfNotDefined(request.getParameter(COMPARING_OPERATOR), FIELD_NONE);
      final String value =
          defaultStringIfNotDefined(request.getParameter(COMPARING_VALUE), FIELD_NONE);
      if (FIELD_NONE.equals(fieldName) || FIELD_NONE.equals(comparator) ||
          FIELD_NONE.equals(value)) {
        tableView.getFilter().clear();
      } else {
        tableView.filterOnColumn(fieldName);
        tableView.getFilter().setComparator(comparator);
        tableView.getFilter().setColumnValue(value);
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
        .map(DbColumn::getName).orElse(FIELD_NONE));
    request.setAttribute(COMPARING_OPERATOR, tableView.getFilter().getComparator());
    request.setAttribute(COMPARING_VALUE, tableView.getFilter().getColumnValue());
    request.setAttribute(COMPARING_OPERATORS, TableRowsFilter.getAllComparators());
  }

  private void throwInvalidValueType(final String name, final int expectedType) {
    throw new IllegalArgumentException(getMultilang().getString("mydb.error.invalidValue") + ". " +
        name + ": " + JDBCType.valueOf(expectedType).getName());
  }
}
  