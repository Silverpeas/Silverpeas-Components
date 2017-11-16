/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.datawarning.servlets;

import org.silverpeas.components.datawarning.service.DataWarningEngine;
import org.silverpeas.components.datawarning.control.DataWarningSessionController;
import org.silverpeas.components.datawarning.model.DataWarning;
import org.silverpeas.components.datawarning.model.DataWarningGroup;
import org.silverpeas.components.datawarning.model.DataWarningQuery;
import org.silverpeas.components.datawarning.model.DataWarningQueryResult;
import org.silverpeas.components.datawarning.model.DataWarningResult;
import org.silverpeas.components.datawarning.model.DataWarningScheduler;
import org.silverpeas.components.datawarning.model.DataWarningUser;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.web.util.viewgenerator.html.Encode;
import org.silverpeas.core.web.http.HttpRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DataWarningRequestRouter extends ComponentRequestRouter<DataWarningSessionController> {
  private static final long serialVersionUID = -2163372588036955415L;
  private static final String SCHEDULER_PARAMETERS = "schedulerParameters";
  private static final String SQL_REQ = "SQLReq";
  private static final String TABLE = "table";
  private static final String COLUMNS = "columns";
  private static final String DEFAULT_DESTINATION = "selectConstraints.jsp";
  private static final String DATA_WARNING = "dataWarning";
  private static final String SCHEDULER = "scheduler";
  private static final String DATA_WARNING_OBJECT = "dataWarningObject";
  private static final String DATA_WARNING_DB_DRIVERS = "dataWarningDBDrivers";
  private static final String CURRENT_DB_DRIVER = "currentDBDriver";
  private static final String CONNECTION_PARAMETERS_JSP = "connectionParameters.jsp";
  private static final String ROW_LIMIT = "RowLimit";
  private static final String THE_COMMAND = "TheCommand";
  private static final String CLOSE_JSP = "closeJsp.jsp";
  private static final String REQUEST_PARAMETERS = "requestParameters";
  private static final String DATA_QUERY = "dataQuery";
  private static final String MONTH = "month";
  private static final String DAY_OF_MONTH = "dayOfMonth";
  private static final String DAY_OF_WEEK = "dayOfWeek";
  private static final String HOUR = "heure";

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "DataWarningSC";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public DataWarningSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new DataWarningSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param dataWarningSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach
   * .jsp?flag=user")
   */
  public String getDestination(String function, DataWarningSessionController dataWarningSC,
      HttpRequest request) {
    String destination = "";


    String flag = dataWarningSC.getHighestSilverpeasUserRole().getName();
    try {
      DataWarningEngine dwe = dataWarningSC.getDataWarningEngine();

      /********************** main *****************************************/
      if (function.startsWith("Main") || function.startsWith(DATA_WARNING)) {
        // Init the DataWarningEngine each time we go to the first page
        dwe.init();

        DataWarning data = dwe.getDataWarning();
        DataWarningScheduler scheduler = dwe.getDataWarningScheduler();
        DataWarningResult dwr = dwe.run();

        //mettre en place un systeme de backlist... lors envoi mail, enlever les user qui sont ds
        // la back list
        boolean isAbonne = false;
        if (dataWarningSC.isUserInDataWarningGroups()) {
          isAbonne = true;
        } else if (dwe.getDataWarningUser(dataWarningSC.getUserId()) != null) {
          isAbonne = true;
        }
        request.setAttribute("isAbonne", Boolean.valueOf(isAbonne));
        request.setAttribute("analysisTypeString", dataWarningSC.getAnalysisTypeString());
        request.setAttribute("result", dwr);
        request.setAttribute("data", data);
        request.setAttribute("userId", dataWarningSC.getUserId());
        request.setAttribute(SCHEDULER, scheduler);
        request.setAttribute("textFrequenceScheduler", dataWarningSC.getTextFrequenceScheduler());
        destination = "dataWarning.jsp";
      } else if ("connectionParameters".equals(function)) {
        /********************** page des parametres de connexion *********************************/
        dataWarningSC.setCurrentDBDriver(dwe.getDataWarning().getJdbcDriverName());
        request.setAttribute(DATA_WARNING_OBJECT, dwe.getDataWarning());
        request.setAttribute(DATA_WARNING_DB_DRIVERS, dataWarningSC.getDBDrivers());
        request.setAttribute(CURRENT_DB_DRIVER, dataWarningSC.getCurrentDBDriver());
        destination = CONNECTION_PARAMETERS_JSP;
      } else if ("updateConnection".equals(function)) {
        /********************** validation des parametres de connexion ***************************/
        dataWarningSC.setCurrentDBDriver(request.getParameter("JDBCdriverNameSelect"));
        request.setAttribute(DATA_WARNING_OBJECT, dwe.getDataWarning());
        request.setAttribute(DATA_WARNING_DB_DRIVERS, dataWarningSC.getDBDrivers());
        request.setAttribute(CURRENT_DB_DRIVER, dataWarningSC.getCurrentDBDriver());
        destination = CONNECTION_PARAMETERS_JSP;
      } else if ("SetConnection".equals(function)) {
        /********************** validation des parametres de connexion ***************************/
        String dbDriverUniqueId = request.getParameter("JDBCdriverNameSelect");
        String login = request.getParameter("Login");
        String password = request.getParameter("Password");
        int rowLimit = 0;
        if ((request.getParameter(ROW_LIMIT) != null) &&
            (!request.getParameter(ROW_LIMIT).trim().isEmpty())) {
          rowLimit = Integer.parseInt(request.getParameter(ROW_LIMIT));
        }

        DataWarning data = dwe.getDataWarningWritable();

        data.setJdbcDriverName(dbDriverUniqueId);
        data.setLogin(login);
        data.setPwd(password);
        data.setRowLimit(rowLimit);

        dwe.updateDataWarning(data);

        request.setAttribute(DATA_WARNING_OBJECT, dwe.getDataWarning());
        request.setAttribute(DATA_WARNING_DB_DRIVERS, dataWarningSC.getDBDrivers());
        request.setAttribute(CURRENT_DB_DRIVER, dataWarningSC.getCurrentDBDriver());
        destination = CONNECTION_PARAMETERS_JSP;
      } else if ("EditParamGen".equals(function)) {
        /********************** edition des parametres generaux **********************************/
        request.setAttribute("data", dwe.getDataWarning());
        destination = "editParamGen.jsp";
      } else if ("SaveParamGen".equals(function)) {
        /********************** sauvegarde des parametres generaux ********************************/
        String description = request.getParameter("SQLReqDescription");
        int typeAnalyse = Integer.parseInt(request.getParameter("typeAnalyse"));
        //get the datawarning and test if analysis type is different
        DataWarning data = dwe.getDataWarningWritable();
        if (data.getAnalysisType() != typeAnalyse) {
          data.setAnalysisType(typeAnalyse);
          data.setDescription(description);
          dwe.updateDataWarning(data);
        } else {
          data.setDescription(description);
          dwe.updateDataWarning(data);
        }
        request.setAttribute(THE_COMMAND, "Main");
        destination = CLOSE_JSP;
      } else if (REQUEST_PARAMETERS.equals(function)) {
        /********************** affichage de la requete *****************************************/
        DataWarningQuery dataQuery = dataWarningSC.getCurrentQuery();
        DataWarning data = dwe.getDataWarning();
        request.setAttribute(DATA_QUERY, dataQuery);
        request.setAttribute("data", data);
        request.setAttribute("currentQuery", new Integer(dataWarningSC.getCurrentQueryType()));
        destination = "requestParameters.jsp";
      } else if ("changeQuery".equals(function)) {
        /********************** changement de la requete *****************************************/
        dataWarningSC.setCurrentQueryType(Integer.parseInt(request.getParameter("typeRequete")));
        destination = getDestination(REQUEST_PARAMETERS, dataWarningSC, request);
      } else if ("EditParamGenQuery".equals(function)) {
        /********************** edition des parametres de seuil ********************************/
        request.setAttribute(DATA_QUERY, dataWarningSC.getCurrentQuery());
        destination = "editParamGenQuery.jsp";
      } else if ("SaveParamGenQuery".equals(function)) {
        /********************** sauvegarde des parametres de seuil *******************************/
        DataWarningQuery dataQuery = dataWarningSC.getCurrentQuery();
        String description = request.getParameter("SQLReqDescription");

        dataQuery.setDescription(description);
        if (dataQuery.getType() == DataWarningQuery.QUERY_TYPE_TRIGGER) {
          int condition = Integer.parseInt(request.getParameter("condition"));
          long seuil = Long.parseLong(request.getParameter("seuil"));

          dataQuery.setTheTriggerCondition(condition);
          dataQuery.setTheTrigger(seuil);
        }

        if ((request.getParameter("PersoValid") != null) &&
            (request.getParameter("PersoValid").length() > 0)) {
          dataQuery.setPersoValid(DataWarningQuery.QUERY_PERSO_VALID);
        } else {
          dataQuery.setPersoValid(DataWarningQuery.QUERY_PERSO_NOT_VALID);
        }
        dataQuery.setPersoColNB(Integer.parseInt(request.getParameter("PersoCol")));
        dataQuery.setPersoUID(request.getParameter("PersoUID"));
        dwe.updateDataWarningQuery(dataQuery);
        request.setAttribute(THE_COMMAND, REQUEST_PARAMETERS);
        destination = CLOSE_JSP;
      } else if ("EditReqExpert".equals(function)) {
        /********************** edition de la requete en mode expert **************************/
        request.setAttribute("requete", dataWarningSC.getCurrentQuery().getQuery());
        destination = "editReqExpert.jsp";
      } else if ("SaveRequete".equals(function)) {
        /********************** sauvegarde de la requete en mode expert *************************/
        String requete = request.getParameter(SQL_REQ);

        DataWarning data = dwe.getDataWarning();
        DataWarningQuery dataQuery = dataWarningSC.getCurrentQuery();
        dataQuery.setQuery(requete);

        DataWarningQueryResult dwqr = dataQuery.executeQuery(data);
        if (!dwqr.hasError()) {
          parseTriggerAnalysisResult(dataWarningSC, data, dwqr);
        }
        if (dwqr.hasError()) {
          request.setAttribute("requete", requete);
          request
              .setAttribute("valeurRetour", Encode.javaStringToJsString(dwqr.getErrorFullText()));
          destination = "editReqExpert.jsp";
        } else {
          dwe.updateDataWarningQuery(dataQuery);
          request.setAttribute(THE_COMMAND, REQUEST_PARAMETERS);
          destination = CLOSE_JSP;
        }
      } else if ("PreviewReq".equals(function)) {
        /********************** previsualisation du resultat de la requete (mode expert) ********/
        String requete = request.getParameter(SQL_REQ);

        DataWarning data = dwe.getDataWarning();
        DataWarningQuery dataQuery = dataWarningSC.getCurrentQuery();

        dataQuery.setQuery(requete);
        executeSilentlyQuery(request, data, dataQuery);
        destination = "previewReq.jsp";
      } else if (SCHEDULER_PARAMETERS.equals(function) ||
          "schedulerParametersWithoutReset".equals(function)) {
        /********************** edition des parametres du scheduler ***************************/
        Collection groups = dwe.getDataWarningGroups();
        Collection users = dwe.getDataWarningUsers();
        String[] idGroups = null;
        String[] idUsers = null;
        if (groups != null && !groups.isEmpty()) {
          idGroups = new String[groups.size()];
          Iterator it = groups.iterator();
          int i = 0;
          while (it.hasNext()) {
            DataWarningGroup gr = (DataWarningGroup) it.next();
            idGroups[i] = Integer.toString(gr.getGroupId());
            i++;
          }
        }
        if (users != null && !users.isEmpty()) {
          idUsers = new String[users.size()];
          Iterator it = users.iterator();
          int i = 0;
          while (it.hasNext()) {
            DataWarningUser us = (DataWarningUser) it.next();
            idUsers[i] = Integer.toString(us.getUserId());
            i++;
          }
        }
        if (SCHEDULER_PARAMETERS.equals(function)) {
          request.setAttribute(SCHEDULER, dataWarningSC.resetEditedScheduler());
        } else {
          request.setAttribute(SCHEDULER, dataWarningSC.getEditedScheduler());
        }

        //put groups and users sleted in request
        request.setAttribute("listGroups", dataWarningSC
            .buildOptions(dataWarningSC.getSelectedGroupsNames(idGroups), "", null, true));
        request.setAttribute("listUsers", dataWarningSC
            .buildOptions(dataWarningSC.getSelectedUsersNames(idUsers), "", null, true));
        destination = "schedulerParameters.jsp";
      } else if ("UpdateLayer".equals(function)) {
        /********************** change the layers ***********************************************/
        DataWarningScheduler scheduler = dataWarningSC.getEditedScheduler();

        int numberOfTimes = Integer.parseInt(request.getParameter("numberOfTimes"));
        int numberOfTimesMoment = Integer.parseInt(request.getParameter("numberOfTimesMoment"));

        //save parameters
        scheduler.setNumberOfTimes(numberOfTimes);
        scheduler.setNumberOfTimesMoment(numberOfTimesMoment);
        if ((request.getParameter(MONTH) != null) &&
            (request.getParameter(MONTH).length() > 0)) {
          scheduler.setTheMonth(Integer.parseInt(request.getParameter(MONTH)));
        }
        if ((request.getParameter(DAY_OF_MONTH) != null) &&
            (request.getParameter(DAY_OF_MONTH).length() > 0)) {
          scheduler.setDayOfMonth(Integer.parseInt(request.getParameter(DAY_OF_MONTH)));
        }
        if ((request.getParameter(DAY_OF_WEEK) != null) &&
            (request.getParameter(DAY_OF_WEEK).length() > 0)) {
          scheduler.setDayOfWeek(Integer.parseInt(request.getParameter(DAY_OF_WEEK)));
        }
        if ((request.getParameter(HOUR) != null) &&
            (request.getParameter(HOUR).length() > 0)) {
          scheduler.setHours(Integer.parseInt(request.getParameter(HOUR)));
        }
        if ((request.getParameter("min") != null) && (request.getParameter("min").length() > 0)) {
          scheduler.setMinits(Integer.parseInt(request.getParameter("min")));
        }
        destination = getDestination("schedulerParametersWithoutReset", dataWarningSC, request);
      } else if ("SetScheduler".equals(function)) {
/********************** sauvegarde des parametres du scheduler et d�marrage de celui-ci
 * *****************************************************/
        getDestination("UpdateLayer", dataWarningSC, request);
        dwe.updateDataWarningScheduler(dataWarningSC.getEditedScheduler());
        destination = getDestination(SCHEDULER_PARAMETERS, dataWarningSC, request);
      } else if ("StartScheduler".equals(function)) {
/********************** lancer le scheduler *****************************************************/
        dwe.startScheduler();

        destination = getDestination(SCHEDULER_PARAMETERS, dataWarningSC, request);
      } else if ("StopScheduler".equals(function)) {
/********************** arreter le scheduler *****************************************************/
        dwe.stopScheduler();

        destination = getDestination(SCHEDULER_PARAMETERS, dataWarningSC, request);
      } else if ("Notification".equals(function)) {
        /********************** choix des groupes et users dans l'onglet scheduler
         * *****************************************************/
        return dataWarningSC.initSelectionPeas();
      } else if ("SaveNotification".equals(function)) {
        /********************** groupes et users choisis
         * *****************************************************/
        dataWarningSC.returnSelectionPeas();
        destination = getDestination(SCHEDULER_PARAMETERS, dataWarningSC, request);
      } else if ("Suscribe".equals(function)) {
        /********************** groupes et users choisis
         * *****************************************************/
        String suscribe = request.getParameter("suscribeAction");
        DataWarningUser user = new DataWarningUser(dataWarningSC.getComponentId(),
            Integer.parseInt(dataWarningSC.getUserId()));
        if ("true".equals(suscribe)) {
          dwe.createDataWarningUser(user);
        } else {
          dwe.deleteDataWarningUser(user);
          //TODO : pb si user fait parti d'un groupe
        }
        destination = getDestination(DATA_WARNING, dataWarningSC, request);
      } else if ("SelectTable".equals(function)) {
        /********************** selection d'une table lors de la requete simplifiee
         * *****************************************************/
        request.setAttribute("allTables", dwe.getDataWarning().getAllTableNames());
        request.setAttribute(DATA_QUERY, dataWarningSC.getCurrentQuery());
        destination = "selectTable.jsp";
      } else if ("SaveSelectTable".equals(function)) {
        /********************** sauvegarde de la table selectionn�e lors de la requete simplifiee
         * *****************************************************/
        String tableName = request.getParameter(TABLE);
        String req = "select * from " + tableName;
        DataWarningQuery dataQuery = dataWarningSC.getCurrentQuery();
        dataQuery.setQuery(req);
        dwe.updateDataWarningQuery(dataQuery);
        request.setAttribute(THE_COMMAND, REQUEST_PARAMETERS);
        destination = CLOSE_JSP;
      } else if ("SelectColumns".equals(function)) {
        /********************** selection de colonnes apres avoir selectionn� une table lors de la
         * requete simplifiee ****************************/
        String tableName = request.getParameter(TABLE);

        request.setAttribute("data", dwe.getDataWarning());
        request.setAttribute(DATA_QUERY, dataWarningSC.getCurrentQuery());
        request.setAttribute("nselectedColumns", dwe.getDataWarning().getColumnNames(tableName));
        request.setAttribute("tableName", tableName);
        destination = "selectColumns.jsp";
      } else if (function.startsWith("SaveSelectColumns")) {
        /********************** sauvegarde de la requete lors de la requete simplifiee
         * *****************************************************/
        String req = request.getParameter(SQL_REQ);
        DataWarningQuery dataQuery = dataWarningSC.getCurrentQuery();
        dataQuery.setQuery(req);
        dwe.updateDataWarningQuery(dataQuery);
        request.setAttribute(THE_COMMAND, REQUEST_PARAMETERS);
        destination = CLOSE_JSP;
      } else if (function.startsWith("SelectAgrega")) {
        /********************** selection des agregas lors de la requete simplifiee
         * *****************************************************/
        String tableName = request.getParameter(TABLE);
        String[] columns = request.getParameterValues("sColumns");

        request.setAttribute("tableName", tableName);
        request.setAttribute(COLUMNS, columns);
        destination = "selectAgrega.jsp";
      } else if (function.startsWith("SelectConstraints")) {
        /********************** selection des contraintes lors de la requete simplifiee
         * *****************************************************/
        int colSize = Integer.parseInt(request.getParameter("columnsSize"));
        String[] columns = new String[colSize];
        for (int i = 0; i < colSize; i++) {
          columns[i] = request.getParameter("Colonne" + i);
        }
        String req = request.getParameter(SQL_REQ);
        SilverTrace
            .info(DATA_WARNING, "DataWarningRequestRouter.getDestination().SelectConstraints",
                "root.MSG_GEN_PARAM_VALUE", "requete=" + req);
        request.setAttribute("req", req);
        dataWarningSC.setColumns(columns);
        request.setAttribute(COLUMNS, columns);
        destination = DEFAULT_DESTINATION;
      } else if (function.startsWith("AddCriter")) {
        /********************** ajout des contraintes lors de la requete simplifiee
         * *****************************************************/
        String[] columns = dataWarningSC.getColumns();
        String req = request.getParameter(SQL_REQ);

        String critere = request.getParameter("critere");
        String sizeString = request.getParameter("critereSize");
        int size;
        List<String> criteres = new ArrayList<>();
        if (sizeString != null && !sizeString.isEmpty()) {
          size = Integer.parseInt(sizeString);
          for (int i = 0; i < size; i++) {
            criteres.add(request.getParameter("Contrainte" + i));
          }
        }
        if (critere != null) {
          criteres.add(critere);
        }

        String[] constraints = criteres.toArray(new String[criteres.size()]);

        request.setAttribute("constraints", constraints);
        request.setAttribute("req", req);
        request.setAttribute(COLUMNS, columns);
        destination = DEFAULT_DESTINATION;
      } else if (function.startsWith("DelCriter")) {
        /********************** suppression des contraintes lors de la requete simplifiee
         * *****************************************************/
        String[] columns = dataWarningSC.getColumns();
        String req = request.getParameter(SQL_REQ);

        String sizeString = request.getParameter("critereSize");
        String[] constraints = fetchConstrains(request, sizeString);

        request.setAttribute("constraints", constraints);
        request.setAttribute("req", req);
        request.setAttribute(COLUMNS, columns);
        destination = DEFAULT_DESTINATION;
      } else {
        /********************** autre cas *****************************************/
        destination = function;
      }

      request.setAttribute("flag", flag);
      if (!destination.startsWith("/dataWarning")) {
        destination = "/dataWarning/jsp/" + destination;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }


    return destination;
  }

  private String[] fetchConstrains(final HttpRequest request, final String sizeString) {
    final int size;
    final List<String> criteres = new ArrayList<>();
    if (sizeString != null && !sizeString.isEmpty()) {
      size = Integer.parseInt(sizeString);
      for (int i = 0; i < size; i++) {
        String delCritere = request.getParameter("ContrainteSupp" + i);
        if (delCritere == null) {
          //rien a supprimer, alors on recupere l'ancien critere
          criteres.add(request.getParameter("Contrainte" + i));
        }
      }
    }
    return criteres.toArray(new String[criteres.size()]);
  }

  private void executeSilentlyQuery(final HttpRequest request, final DataWarning data,
      final DataWarningQuery dataQuery) {
    try {
      DataWarningQueryResult dwqr = dataQuery.executeQuery(data);
      request.setAttribute("resultQuery", dwqr);
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
  }

  private void parseTriggerAnalysisResult(final DataWarningSessionController dataWarningSC,
      final DataWarning data, final DataWarningQueryResult dwqr) {
    try {
      //Trigger Query only
      if (data.getAnalysisType() == DataWarning.TRIGGER_ANALYSIS &&
          dataWarningSC.getCurrentQueryType() == DataWarningQuery.QUERY_TYPE_TRIGGER) {
        dwqr.returnTriggerValueFromResult();
      }
    } catch (Exception e) {
      SilverTrace.warn(DATA_WARNING, "DataWarningRequestRouter.getDestination()",
          "root.MSG_GEN_ENTER_METHOD", "Error during Request Save Verification");
      dwqr.addError(e, "Value = " + dwqr.getValue(0, 0));
    }
  }

}
