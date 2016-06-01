/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.components.kmelia.model.updatechain;

import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.ResourceLocator;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
    StringBuilder html = new StringBuilder();
    Collection<String> listRes = null;

    String mandatoryImg = Util.getIcon("mandatoryField");
    String fieldName = field.getName();
    List<FieldParameter> parameters = field.getParams();

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

    if (listRes != null && listRes.size() > 0) {
      String mContext = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
      int zindex = (fieldsContext.getLastFieldIndex() -
          Integer.parseInt(fieldsContext.getCurrentFieldIndex())) * 9000;

      // list of values
      html.append("<script type=\"text/javascript\">\n");
      html.append("listArray" + fieldName + " = [\n");
      for (String val : listRes) {
        html.append("\"" + EncodeHelper.javaStringToJsString(val) + "\",\n");
      }
      // remove last useless comma
      html.deleteCharAt(html.length() - 1);
      html.append("];\n");
      html.append("</script>\n");

      html.append("<script type=\"text/javascript\" src=\"")
          .append(mContext)
          .append("/util/yui/yuiloader/yuiloader-min.js\"></script>");
      html.append("<script type=\"text/javascript\">\n");
      html.append("var loader = new YAHOO.util.YUILoader({require: ['fonts', 'autocomplete', ")
          .append("'animation'], base: '")
          .append(mContext)
          .append("/util/yui/', Optional: false, \n");
      html.append("onSuccess: function(displayList) {\n");

      html.append(" this.oACDS")
          .append(fieldName)
          .append(" = new YAHOO.widget.DS_JSArray(listArray")
          .append(fieldName)
          .append(");\n");
      html.append(" this.oAutoComp")
          .append(fieldName)
          .append(" = new YAHOO.widget.AutoComplete('")
          .append(fieldName)
          .append("','container")
          .append(fieldName)
          .append("', this.oACDS")
          .append(fieldName)
          .append(");\n");
      html.append(" this.oAutoComp")
          .append(fieldName)
          .append(".prehighlightClassName = \"yui-ac-prehighlight\";\n");
      html.append(" this.oAutoComp").append(fieldName).append(".typeAhead = true;\n");
      html.append(" this.oAutoComp").append(fieldName).append(".useIFrame = true;\n");
      html.append(" this.oAutoComp").append(fieldName).append(".useShadow = true;\n");
      html.append(" this.oAutoComp").append(fieldName).append(".minQueryLength = 0;\n");

      // Values :  1 = list constraint, 2 = free input, default value is 1
      if ("1".equals(valueFieldType)) {
        html.append(" this.oAutoComp").append(fieldName).append(".forceSelection = true;\n");
      }

      html.append(" this.oAutoComp")
          .append(fieldName)
          .append(".textboxFocusEvent.subscribe(function(){\n");
      html.append("   var sInputValue = YAHOO.util.Dom.get('")
          .append(fieldName)
          .append("').value;\n");
      html.append("   if(sInputValue.length == 0) {\n");
      html.append("     var oSelf = this;\n");
      html.append("     setTimeout(function(){oSelf.sendQuery(sInputValue);},0);\n");
      html.append("   }\n");
      html.append(" });\n");
      html.append("}\n");

      html.append(",\n");
      html.append("onFailure: function(o)      {\n");
      html.append("alert(\"Error\");\n");
      html.append("}\n");
      html.append("});\n");
      html.append("loader.insert();\n");
      html.append("</script>\n");

      html.append("<style type=\"text/css\">\n");

      html.append("	#listAutocomplete").append(fieldName).append(" {\n");
      html.append("		width:").append(size / 2).append("em;\n");
      html.append("		padding-bottom:2em;\n");
      html.append("	}\n");
      html.append("	#listAutocomplete").append(fieldName).append(" {\n");
      html.append("		z-index:")
          .append(zindex)
          .append(
              "; /* z-index needed on top instance for ie & sf absolute inside relative issue " +
                  "*/\n");
      html.append("	}\n");
      html.append("	#").append(fieldName).append(" {\n");
      html.append("		_position:absolute; /* abs pos needed for ie quirks */\n");
      html.append("	}\n");
      html.append("</style>\n");

      html.append("<div id=\"listAutocomplete").append(fieldName).append("\">\n");
      html.append("<input id=\"")
          .append(fieldName)
          .append("\" size=\"")
          .append(size)
          .append("\" name=\"")
          .append(fieldName)
          .append("\" type=\"text\"");
      if (value != null) {
        html.append(" value=\"").append(value).append("\"");
      }
      html.append("/>\n");
      html.append("<div id=\"container").append(fieldName).append("\"/>\n");
      html.append("</div>\n");

      if (mandatory) {
        String sizeMandatory = Integer.toString(size / 2 + 1);
        html.append("<img src=\"")
            .append(mandatoryImg)
            .append("\" width=\"5\" height=\"5\" border=\"0\" style=\"position:absolute;left:")
            .append(sizeMandatory)
            .append("em;top:5px\">\n");
      }

    } else {
      // Values :  1 = list constraint, 2 = free input, default value is 1
      if ("1".equals(valueFieldType)) {
        html.append("<SELECT name=\"")
            .append(EncodeHelper.javaStringToHtmlString(fieldName))
            .append("\"");
        html.append(" >\n");
        html.append("</SELECT>\n");
      } else {
        html.append("<input type=\"text\" size=\"")
            .append(size)
            .append("\" name=\"")
            .append(EncodeHelper.javaStringToHtmlString(fieldName))
            .append("\"");
        html.append(" >\n");
      }
      if (mandatory) {
        html.append("&nbsp;<img src=\"")
            .append(mandatoryImg)
            .append("\" width=\"5\" height=\"5\" border=\"0\">&nbsp;\n");
      }
    }

    out.println(html.toString());
  }

  public Connection connectJdbc(String driverName, String url, String login, String password)
      throws FormException {
    Connection result;

    try {
      Class.forName(driverName);
    } catch (ClassNotFoundException e) {
      throw new FormException("JdbcField.connect", "form.EX_CANT_FIND_DRIVER_JDBC", e);
    }
    try {
      result = DriverManager.getConnection(url, login, password);
    } catch (SQLException e) {
      throw new FormException("JdbcField.connect", "form.EX_CANT_CONNECT_JDBC", e);
    }

    return result;
  }

  public Collection<String> selectSql(Connection jdbcConnection, String query)
      throws FormException {

    Collection<String> result = new ArrayList<>();

    if (jdbcConnection != null) {
      try (PreparedStatement prepStmt = jdbcConnection.prepareStatement(query)) {
        try (ResultSet rs = prepStmt.executeQuery()) {
          while (rs.next()) {
            ResultSetMetaData metadata = rs.getMetaData();
            int nbColumns = metadata.getColumnCount();
            StringBuilder value = new StringBuilder();
            for (int i = 1; i <= nbColumns; i++) {
              value.append(rs.getString(i)).append(" ");
            }
            result.add(value.toString().trim());
          }
        }
      } catch (SQLException e) {
        throw new FormException("JdbcField.selectSql", "form.EX_CANT_PREPARE_STATEMENT_JDBC", e);
      }
    }
    return result;
  }

}