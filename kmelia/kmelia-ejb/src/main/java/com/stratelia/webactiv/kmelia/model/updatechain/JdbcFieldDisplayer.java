/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.stratelia.webactiv.kmelia.model.updatechain;

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

import com.silverpeas.form.FormException;
import com.silverpeas.form.Util;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class JdbcFieldDisplayer {
  static private final String VARIABLE_REGEX_USER_ID = "\\$\\$userId";

  /**
   * Constructeur
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

    Collection listRes = null; // liste de valeurs String

    // Parameters
    String driverName = null;
    String url = null;
    String login = null;
    String password = null;
    String query = null;
    String valueFieldType = "1"; // valeurs possibles 1 = choix restreint à la liste ou 2 = saisie
                                 // libre, par défaut 1
    int size = 30;
    for (int i = 0; i < parameters.size(); i++) {
      FieldParameter param = parameters.get(i);
      if (param.getName().equals("url"))
        url = param.getValue();
      if (param.getName().equals("valueFieldType"))
        valueFieldType = param.getValue();
      if (param.getName().equals("query"))
        query = param.getValue();
      if (param.getName().equals("login"))
        login = param.getValue();
      if (param.getName().equals("password"))
        password = param.getValue();
      if (param.getName().equals("driverName"))
        driverName = param.getValue();
      if (param.getName().equals("size"))
        size = new Integer((String) param.getValue());
    }

    if (field != null) {
      // Connexion JDBC
      Connection jdbcConnection = null;

      try {
        jdbcConnection = connectJdbc(driverName, url, login, password);

        // Requête SQL
        listRes = selectSql(jdbcConnection, query);
      } finally {
        try {
          if (jdbcConnection != null)
            jdbcConnection.close();
        } catch (SQLException e) {
          SilverTrace.error("formTemplate", "JdbcFieldDisplayer.selectSql",
              "root.EX_CONNECTION_CLOSE_FAILED", e);
        }
      }
    }

    if (listRes != null && listRes.size() > 0) {
      String m_context =
          GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
      int zindex =
          (fieldsContext.getLastFieldIndex() - new Integer(fieldsContext.getCurrentFieldIndex())
              .intValue()) * 9000;

      html +=
          "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + m_context +
              "/util/yui/fonts/fonts-min.css\" />\n";
      html +=
          "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + m_context +
              "/util/yui/autocomplete/assets/skins/sam/autocomplete.css\" />\n";
      html +=
          "<script type=\"text/javascript\" src=\"" + m_context +
              "/util/yui/yahoo-dom-event/yahoo-dom-event.js\"></script>\n";
      html +=
          "<script type=\"text/javascript\" src=\"" + m_context +
              "/util/yui/animation/animation-min.js\"></script>\n";
      html +=
          "<script type=\"text/javascript\" src=\"" + m_context +
              "/util/yui/autocomplete/autocomplete-min.js\"></script>\n";
      html += "<style type=\"text/css\">\n";

      html += "	#listAutocomplete" + fieldName + " {\n";
      html += "		width:" + size / 2 + "em;\n";
      html += "		padding-bottom:2em;\n";
      html += "	}\n";
      html += "	#listAutocomplete" + fieldName + " {\n";
      html +=
          "		z-index:" + zindex +
              "; /* z-index needed on top instance for ie & sf absolute inside relative issue */\n";
      html += "	}\n";
      html += "	#" + fieldName + " {\n";
      html += "		_position:absolute; /* abs pos needed for ie quirks */\n";
      html += "	}\n";
      html += "</style>\n";

      html += "<div id=\"listAutocomplete" + fieldName + "\">\n";
      html +=
          "<input id=\"" + fieldName + "\" size=\"" + size + "\" name=\"" + fieldName +
              "\" type=\"text\"";
      if (value != null) {
        html += " value=\"" + value + "\"";
      }
      html += "/>\n";
      html += "<div id=\"container" + fieldName + "\"/>\n";
      html += "</div>\n";

      html += "<script type=\"text/javascript\">\n";
      html += "listArray" + fieldName + " = [\n";

      Iterator itRes = listRes.iterator();
      String val;
      while (itRes.hasNext()) {
        val = (String) itRes.next();

        html += "\"" + EncodeHelper.javaStringToJsString(val) + "\",\n";

      }

      // supprime dernière virgule inutile
      html = html.substring(0, html.length() - 1);

      html += "];\n";
      html += "</script>\n";

      html += "<script type=\"text/javascript\">\n";
      html +=
          "	this.oACDS" + fieldName + " = new YAHOO.widget.DS_JSArray(listArray" + fieldName +
              ");\n";
      html +=
          "	this.oAutoComp" + fieldName + " = new YAHOO.widget.AutoComplete('" + fieldName +
              "','container" + fieldName + "', this.oACDS" + fieldName + ");\n";
      html += "	this.oAutoComp" + fieldName + ".prehighlightClassName = \"yui-ac-prehighlight\";\n";
      html += "	this.oAutoComp" + fieldName + ".typeAhead = true;\n";
      html += "	this.oAutoComp" + fieldName + ".useShadow = true;\n";
      html += "	this.oAutoComp" + fieldName + ".minQueryLength = 0;\n";

      if ("1".equals(valueFieldType)) {// valeurs possibles 1 = choix restreint à la liste ou 2 =
                                       // saisie libre, par défaut 1
        html += "	this.oAutoComp" + fieldName + ".forceSelection = true;\n";
      }

      html += "	this.oAutoComp" + fieldName + ".textboxFocusEvent.subscribe(function(){\n";
      html += "		var sInputValue = YAHOO.util.Dom.get('" + fieldName + "').value;\n";
      html += "		if(sInputValue.length == 0) {\n";
      html += "			var oSelf = this;\n";
      html += "			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);\n";
      html += "		}\n";
      html += "	});\n";
      html += "</script>\n";

      if (mandatory) {
        String sizeMandatory = new Integer(size / 2 + 1).toString();
        html +=
            "<img src=\"" + mandatoryImg +
                "\" width=\"5\" height=\"5\" border=\"0\" style=\"position:absolute;left:" +
                sizeMandatory + "em;top:5px\">\n";
      }

    } else {
      if ("1".equals(valueFieldType)) {// valeurs possibles 1 = choix restreint à la liste ou 2 =
                                       // saisie libre, par défaut 1
        html += "<SELECT name=\"" + EncodeHelper.javaStringToHtmlString(fieldName) + "\"";
        html += " >\n";
        html += "</SELECT>\n";
      } else {
        html +=
            "<input type=\"text\" size=\"" + size + "\" name=\"" +
                EncodeHelper.javaStringToHtmlString(fieldName) + "\"";
        html += " >\n";
      }
      if (mandatory) {
        html +=
            "&nbsp;<img src=\"" + mandatoryImg +
                "\" width=\"5\" height=\"5\" border=\"0\">&nbsp;\n";
      }
    }

    out.println(html);
  }

  public Connection connectJdbc(String driverName, String url, String login, String password)
      throws FormException {
    Connection result = null;

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

  public Collection<String> selectSql(Connection jdbcConnection, String query) throws FormException {

    Collection<String> result = new ArrayList<String>();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

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
          for (int i = 1; i <= nbColumns; i++)
            value += rs.getString(i) + " ";
          result.add(value.trim());
        }
      } catch (SQLException e) {
        throw new FormException("JdbcField.selectSql", "form.EX_CANT_BROWSE_RESULT_JDBC", e);
      }

      finally {
        DBUtil.close(rs, prepStmt);
      }
    }
    return result;
  }

}