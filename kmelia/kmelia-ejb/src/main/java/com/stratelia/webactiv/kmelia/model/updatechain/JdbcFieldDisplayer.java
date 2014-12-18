/**
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.kmelia.model.updatechain;

import com.silverpeas.form.FormException;
import com.silverpeas.form.Util;
import org.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.GeneralPropertiesManager;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class JdbcFieldDisplayer {
  static private final String VARIABLE_REGEX_USER_ID = "\\$\\$userId";

  /**
   * default constructor
   */
  public JdbcFieldDisplayer() {
  }

  public void display(PrintWriter out, FieldUpdateChainDescriptor field,
      FieldsContext fieldsContext, boolean mandatory) throws FormException {
    String value = "";
    String html = "";

    String mandatoryImg = Util.getIcon("mandatoryField");

    String fieldName = field.getName();
    List<FieldParameter> parameters = field.getParams();

    Collection<String> listRes = null;

    // Parameters
    String driverName = null;
    String url = null;
    String login = null;
    String password = null;
    String query = null;
    // Values :  1 = list constraint, 2 = free input, default value is 1
    String valueFieldType = "1";
    int size = 30;
    for (FieldParameter param : parameters) {
      if ("url".equals(param.getName())) {
        url = param.getValue();
      }
      if ("valueFieldType".equals(param.getName())) {
        valueFieldType = param.getValue();
      }
      if ("query".equals(param.getName())) {
        query = param.getValue();
      }
      if ("login".equals(param.getName())) {
        login = param.getValue();
      }
      if ("password".equals(param.getName())) {
        password = param.getValue();
      }
      if ("driverName".equals(param.getName())) {
        driverName = param.getValue();
      }
      if ("size".equals(param.getName())) {
        size = Integer.parseInt(param.getValue());
      }
    }

    if (field != null) {
      // JDBC connection
      Connection jdbcConnection = null;

      try {
        jdbcConnection = connectJdbc(driverName, url, login, password);

        // SQL query
        listRes = selectSql(jdbcConnection, query);
      } finally {
        try {
          if (jdbcConnection != null) {
            jdbcConnection.close();
          }
        } catch (SQLException e) {
          SilverTrace.error("formTemplate", "JdbcFieldDisplayer.selectSql",
              "root.EX_CONNECTION_CLOSE_FAILED", e);
        }
      }
    }

    if (listRes != null && listRes.size() > 0) {
      String m_context =
          GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
      int zindex = (fieldsContext.getLastFieldIndex() -
          Integer.parseInt(fieldsContext.getCurrentFieldIndex())) * 9000;

      // list of values
      html += "<script type=\"text/javascript\">\n";
      html += "listArray" + fieldName + " = [\n";
      Iterator itRes = listRes.iterator();
      String val;
      while (itRes.hasNext()) {
        val = (String) itRes.next();

        html += "\"" + EncodeHelper.javaStringToJsString(val) + "\",\n";

      }
      // remove last useless comma
      html = html.substring(0, html.length() - 1);

      html += "];\n";
      html += "</script>\n";

      html += "<script type=\"text/javascript\" src=\"" + m_context +
          "/util/yui/yuiloader/yuiloader-min.js\"></script>";
      html += "<script type=\"text/javascript\">\n";
      html += "var loader = new YAHOO.util.YUILoader({require: ['fonts', 'autocomplete', " +
          "'animation'], base: '" +
          m_context + "/util/yui/', Optional: false, \n";
      html += "onSuccess: function(displayList) {\n";

      html += " this.oACDS" + fieldName + " = new YAHOO.widget.DS_JSArray(listArray" + fieldName +
          ");\n";
      html += " this.oAutoComp" + fieldName + " = new YAHOO.widget.AutoComplete('" + fieldName +
          "','container" + fieldName + "', this.oACDS" + fieldName + ");\n";
      html += " this.oAutoComp" + fieldName + ".prehighlightClassName = \"yui-ac-prehighlight\";\n";
      html += " this.oAutoComp" + fieldName + ".typeAhead = true;\n";
      html += " this.oAutoComp" + fieldName + ".useIFrame = true;\n";
      html += " this.oAutoComp" + fieldName + ".useShadow = true;\n";
      html += " this.oAutoComp" + fieldName + ".minQueryLength = 0;\n";

      // Values :  1 = list constraint, 2 = free input, default value is 1
      if ("1".equals(valueFieldType)) {
        html += " this.oAutoComp" + fieldName + ".forceSelection = true;\n";
      }

      html += " this.oAutoComp" + fieldName + ".textboxFocusEvent.subscribe(function(){\n";
      html += "   var sInputValue = YAHOO.util.Dom.get('" + fieldName + "').value;\n";
      html += "   if(sInputValue.length == 0) {\n";
      html += "     var oSelf = this;\n";
      html += "     setTimeout(function(){oSelf.sendQuery(sInputValue);},0);\n";
      html += "   }\n";
      html += " });\n";
      html += "}\n";

      html += ",\n";
      html += "onFailure: function(o)      {\n";
      html += "alert(\"Error\");\n";
      html += "}\n";
      html += "});\n";
      html += "loader.insert();\n";
      html += "</script>\n";

      html += "<style type=\"text/css\">\n";

      html += "	#listAutocomplete" + fieldName + " {\n";
      html += "		width:" + size / 2 + "em;\n";
      html += "		padding-bottom:2em;\n";
      html += "	}\n";
      html += "	#listAutocomplete" + fieldName + " {\n";
      html += "		z-index:" + zindex +
          "; /* z-index needed on top instance for ie & sf absolute inside relative issue */\n";
      html += "	}\n";
      html += "	#" + fieldName + " {\n";
      html += "		_position:absolute; /* abs pos needed for ie quirks */\n";
      html += "	}\n";
      html += "</style>\n";

      html += "<div id=\"listAutocomplete" + fieldName + "\">\n";
      html += "<input id=\"" + fieldName + "\" size=\"" + size + "\" name=\"" + fieldName +
          "\" type=\"text\"";
      if (value != null) {
        html += " value=\"" + value + "\"";
      }
      html += "/>\n";
      html += "<div id=\"container" + fieldName + "\"/>\n";
      html += "</div>\n";

      if (mandatory) {
        String sizeMandatory = Integer.toString(size / 2 + 1);
        html += "<img src=\"" + mandatoryImg +
            "\" width=\"5\" height=\"5\" border=\"0\" style=\"position:absolute;left:" +
            sizeMandatory + "em;top:5px\">\n";
      }

    } else {
      // Values :  1 = list constraint, 2 = free input, default value is 1
      if ("1".equals(valueFieldType)) {
        html += "<SELECT name=\"" + EncodeHelper.javaStringToHtmlString(fieldName) + "\"";
        html += " >\n";
        html += "</SELECT>\n";
      } else {
        html += "<input type=\"text\" size=\"" + size + "\" name=\"" +
            EncodeHelper.javaStringToHtmlString(fieldName) + "\"";
        html += " >\n";
      }
      if (mandatory) {
        html += "&nbsp;<img src=\"" + mandatoryImg +
            "\" width=\"5\" height=\"5\" border=\"0\">&nbsp;\n";
      }
    }

    out.println(html);
  }

  public Connection connectJdbc(String driverName, String url, String login, String password)
      throws FormException {
    Connection result;

    try {
      Class.forName(driverName);
    } catch (ClassNotFoundException e) {
      throw new FormException("JdbcField.connectJdbc", "form.EX_CANT_FIND_DRIVER_JDBC", e);
    }
    try {
      result = DriverManager.getConnection(url, login, password);
    } catch (SQLException e) {
      throw new FormException("JdbcField.connectJdbc", "form.EX_CANT_CONNECT_JDBC", e);
    }

    return result;
  }

  public Collection<String> selectSql(Connection jdbcConnection, String query)
      throws FormException {

    Collection<String> result = new ArrayList<>();

    PreparedStatement prepStmt;
    ResultSet rs;

    if (jdbcConnection != null) {
      try {
        prepStmt = jdbcConnection.prepareStatement(query);
      } catch (SQLException e) {
        throw new FormException("JdbcField.selectSql", "form.EX_CANT_PREPARE_STATEMENT_JDBC", e);
      }

      try {
        rs = prepStmt.executeQuery();
      } catch (SQLException e) {
        throw new FormException("JdbcField.selectSql", "form.EX_CANT_EXECUTE_QUERY_JDBC", e);
      }

      try {
        while (rs.next()) {
          ResultSetMetaData metadata = rs.getMetaData();
          int nbColumns = metadata.getColumnCount();
          String value = "";
          for (int i = 1; i <= nbColumns; i++) {
            value += rs.getString(i) + " ";
          }
          result.add(value.trim());
        }
      } catch (SQLException e) {
        throw new FormException("JdbcField.selectSql", "form.EX_CANT_BROWSE_RESULT_JDBC", e);
      } finally {
        DBUtil.close(rs, prepStmt);
      }
    }
    return result;
  }

}