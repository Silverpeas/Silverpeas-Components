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
      if (function.startsWith("Main") || function.startsWith("dataWarning")) {
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
        request.setAttribute("scheduler", scheduler);
        request.setAttribute("textFrequenceScheduler", dataWarningSC.getTextFrequenceScheduler());
        destination = "dataWarning.jsp";
      } else if ("connectionParameters".equals(function)) {
        /********************** page des parametres de connexion *********************************/
        dataWarningSC.setCurrentDBDriver(dwe.getDataWarning().getJdbcDriverName());
        request.setAttribute("dataWarningObject", dwe.getDataWarning());
        request.setAttribute("dataWarningDBDrivers", dataWarningSC.getDBDrivers());
        request.setAttribute("currentDBDriver", dataWarningSC.getCurrentDBDriver());
        destination = "connectionParameters.jsp";
      } else if ("updateConnection".equals(function)) {
        /********************** validation des parametres de connexion ***************************/
        dataWarningSC.setCurrentDBDriver(request.getParameter("JDBCdriverNameSelect"));
        request.setAttribute("dataWarningObject", dwe.getDataWarning());
        request.setAttribute("dataWarningDBDrivers", dataWarningSC.getDBDrivers());
        request.setAttribute("currentDBDriver", dataWarningSC.getCurrentDBDriver());
        destination = "connectionParameters.jsp";
      } else if ("SetConnection".equals(function)) {
        /********************** validation des parametres de connexion ***************************/
        String dbDriverUniqueId = request.getParameter("JDBCdriverNameSelect");
        String login = request.getParameter("Login");
        String password = request.getParameter("Password");
        int rowLimit = 0;
        if ((request.getParameter("RowLimit") != null) &&
            (!request.getParameter("RowLimit").trim().equals(""))) {
          rowLimit = Integer.parseInt(request.getParameter("RowLimit"));
        }

        DataWarning data = dwe.getDataWarningWritable();

        data.setJdbcDriverName(dbDriverUniqueId);
        data.setLogin(login);
        data.setPwd(password);
        data.setRowLimit(rowLimit);

        dwe.updateDataWarning(data);

        request.setAttribute("dataWarningObject", dwe.getDataWarning());
        request.setAttribute("dataWarningDBDrivers", dataWarningSC.getDBDrivers());
        request.setAttribute("currentDBDriver", dataWarningSC.getCurrentDBDriver());
        destination = "connectionParameters.jsp";
      }
/********************** edition des parametres generaux **********************************/
      else if ("EditParamGen".equals(function)) {
        request.setAttribute("data", dwe.getDataWarning());
        destination = "editParamGen.jsp";
      }
/********************** sauvegarde des parametres generaux **********************************/
      else if ("SaveParamGen".equals(function)) {
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
        request.setAttribute("TheCommand", "Main");
        destination = "closeJsp.jsp";
      }
/********************** affichage de la requete *****************************************/
      else if ("requestParameters".equals(function)) {
        DataWarningQuery dataQuery = dataWarningSC.getCurrentQuery();
        DataWarning data = dwe.getDataWarning();
        if (dataQuery == null) {

        }
        if (data == null) {

        }

        request.setAttribute("dataQuery", dataQuery);
        request.setAttribute("data", data);
        request.setAttribute("currentQuery", new Integer(dataWarningSC.getCurrentQueryType()));
        destination = "requestParameters.jsp";
      }
/********************** changement de la requete *****************************************/
      else if ("changeQuery".equals(function)) {

        dataWarningSC.setCurrentQueryType(Integer.parseInt(request.getParameter("typeRequete")));
        destination = getDestination("requestParameters", dataWarningSC, request);
      }
/********************** edition des parametres de seuil
 * *****************************************************/
      else if ("EditParamGenQuery".equals(function)) {
        request.setAttribute("dataQuery", dataWarningSC.getCurrentQuery());
        destination = "editParamGenQuery.jsp";
      }
/********************** sauvegarde des parametres de seuil
 * *****************************************************/
      else if ("SaveParamGenQuery".equals(function)) {
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
        request.setAttribute("TheCommand", "requestParameters");
        destination = "closeJsp.jsp";
      }
/********************** edition de la requete en mode expert
 * *****************************************************/
      else if (function.equals("EditReqExpert")) {
        request.setAttribute("requete", dataWarningSC.getCurrentQuery().getQuery());
        destination = "editReqExpert.jsp";
      }
/********************** sauvegarde de la requete en mode expert
 * *****************************************************/
      else if (function.equals("SaveRequete")) {
        String requete = request.getParameter("SQLReq");

        DataWarning data = dwe.getDataWarning();
        DataWarningQuery dataQuery = dataWarningSC.getCurrentQuery();
        dataQuery.setQuery(requete);

        DataWarningQueryResult dwqr = dataQuery.executeQuery(data);
        if (!dwqr.hasError()) {
          try {
            //Trigger Query only
            if (data.getAnalysisType() == DataWarning.TRIGGER_ANALYSIS &&
                dataWarningSC.getCurrentQueryType() == DataWarningQuery.QUERY_TYPE_TRIGGER) {
              dwqr.returnTriggerValueFromResult();
            }
          } catch (Exception e) {
            SilverTrace.warn("dataWarning", "DataWarningRequestRouter.getDestination()",
                "root.MSG_GEN_ENTER_METHOD", "Error during Request Save Verification");
            dwqr.addError(e, "Value = " + dwqr.getValue(0, 0));
          }
        }
        if (dwqr.hasError()) {
          request.setAttribute("requete", requete);
          request
              .setAttribute("valeurRetour", Encode.javaStringToJsString(dwqr.getErrorFullText()));
          destination = "editReqExpert.jsp";
        } else {
          dwe.updateDataWarningQuery(dataQuery);
          request.setAttribute("TheCommand", "requestParameters");
          destination = "closeJsp.jsp";
        }
      }
/********************** previsualisation du resultat de la requete (mode expert)
 * *****************************************************/
      else if (function.equals("PreviewReq")) {
        String requete = request.getParameter("SQLReq");

        DataWarning data = dwe.getDataWarning();
        DataWarningQuery dataQuery = dataWarningSC.getCurrentQuery();

        dataQuery.setQuery(requete);
        try {
          DataWarningQueryResult dwqr = dataQuery.executeQuery(data);
          request.setAttribute("resultQuery", dwqr);
        } catch (Exception e) {

        }
        destination = "previewReq.jsp";
      }
/********************** edition des parametres du scheduler
 * *****************************************************/
      else if (function.equals("schedulerParameters") ||
          function.equals("schedulerParametersWithoutReset")) {
        Collection groups = dwe.getDataWarningGroups();
        Collection users = dwe.getDataWarningUsers();
        String[] idGroups = null;
        String[] idUsers = null;
        if (groups != null && groups.size() > 0) {
          idGroups = new String[groups.size()];
          Iterator it = groups.iterator();
          int i = 0;
          while (it.hasNext()) {
            DataWarningGroup gr = (DataWarningGroup) it.next();
            idGroups[i] = Integer.toString(gr.getGroupId());
            i++;
          }
        }
        if (users != null && users.size() > 0) {
          idUsers = new String[users.size()];
          Iterator it = users.iterator();
          int i = 0;
          while (it.hasNext()) {
            DataWarningUser us = (DataWarningUser) it.next();
            idUsers[i] = Integer.toString(us.getUserId());
            i++;
          }
        }
        if (function.equals("schedulerParameters")) {
          request.setAttribute("scheduler", dataWarningSC.resetEditedScheduler());
        } else {
          request.setAttribute("scheduler", dataWarningSC.getEditedScheduler());
        }

        //put groups and users sleted in request
        request.setAttribute("listGroups", dataWarningSC
            .buildOptions(dataWarningSC.getSelectedGroupsNames(idGroups), "", null, true));
        request.setAttribute("listUsers", dataWarningSC
            .buildOptions(dataWarningSC.getSelectedUsersNames(idUsers), "", null, true));
        destination = "schedulerParameters.jsp";
      }
/********************** change the layers *****************************************************/
      else if (function.equals("UpdateLayer")) {
        DataWarningScheduler scheduler = dataWarningSC.getEditedScheduler();

        int numberOfTimes = Integer.parseInt(request.getParameter("numberOfTimes"));
        int numberOfTimesMoment = Integer.parseInt(request.getParameter("numberOfTimesMoment"));

        //save parameters
        scheduler.setNumberOfTimes(numberOfTimes);
        scheduler.setNumberOfTimesMoment(numberOfTimesMoment);
        if ((request.getParameter("month") != null) &&
            (request.getParameter("month").length() > 0)) {
          scheduler.setTheMonth(Integer.parseInt(request.getParameter("month")));
        }
        if ((request.getParameter("dayOfMonth") != null) &&
            (request.getParameter("dayOfMonth").length() > 0)) {
          scheduler.setDayOfMonth(Integer.parseInt(request.getParameter("dayOfMonth")));
        }
        if ((request.getParameter("dayOfWeek") != null) &&
            (request.getParameter("dayOfWeek").length() > 0)) {
          scheduler.setDayOfWeek(Integer.parseInt(request.getParameter("dayOfWeek")));
        }
        if ((request.getParameter("heure") != null) &&
            (request.getParameter("heure").length() > 0)) {
          scheduler.setHours(Integer.parseInt(request.getParameter("heure")));
        }
        if ((request.getParameter("min") != null) && (request.getParameter("min").length() > 0)) {
          scheduler.setMinits(Integer.parseInt(request.getParameter("min")));
        }
        destination = getDestination("schedulerParametersWithoutReset", dataWarningSC, request);
      } else if (function.equals("SetScheduler")) {
/********************** sauvegarde des parametres du scheduler et d�marrage de celui-ci
 * *****************************************************/
        getDestination("UpdateLayer", dataWarningSC, request);
        dwe.updateDataWarningScheduler(dataWarningSC.getEditedScheduler());
        destination = getDestination("schedulerParameters", dataWarningSC, request);
      } else if (function.equals("StartScheduler")) {
/********************** lancer le scheduler *****************************************************/
        dwe.startScheduler();

        destination = getDestination("schedulerParameters", dataWarningSC, request);
      } else if (function.equals("StopScheduler")) {
/********************** arreter le scheduler *****************************************************/
        dwe.stopScheduler();

        destination = getDestination("schedulerParameters", dataWarningSC, request);
      }
/********************** choix des groupes et users dans l'onglet scheduler
 * *****************************************************/
      else if (function.equals("Notification")) {
        return dataWarningSC.initSelectionPeas();
      }
/********************** groupes et users choisis
 * *****************************************************/
      else if (function.equals("SaveNotification")) {
        dataWarningSC.returnSelectionPeas();
        destination = getDestination("schedulerParameters", dataWarningSC, request);
      }
/********************** groupes et users choisis
 * *****************************************************/
      else if (function.equals("Suscribe")) {
        boolean isAbonne = false;
        String suscribe = request.getParameter("suscribeAction");
        DataWarningUser user = new DataWarningUser(dataWarningSC.getComponentId(),
            Integer.parseInt(dataWarningSC.getUserId()));
        if ("true".equals(suscribe)) {
          isAbonne = true;
          dwe.createDataWarningUser(user);
        } else {
          dwe.deleteDataWarningUser(user);
          //TODO : pb si user fait parti d'un groupe
        }
        destination = getDestination("dataWarning", dataWarningSC, request);
      }
/********************** selection d'une table lors de la requete simplifiee
 * *****************************************************/
      else if (function.equals("SelectTable")) {
        request.setAttribute("allTables", dwe.getDataWarning().getAllTableNames());
        request.setAttribute("dataQuery", dataWarningSC.getCurrentQuery());
        destination = "selectTable.jsp";
      }
/********************** sauvegarde de la table selectionn�e lors de la requete simplifiee
 * *****************************************************/
      else if (function.equals("SaveSelectTable")) {
        String tableName = request.getParameter("table");
        String req = "select * from " + tableName;
        DataWarningQuery dataQuery = dataWarningSC.getCurrentQuery();
        dataQuery.setQuery(req);
        dwe.updateDataWarningQuery(dataQuery);
        request.setAttribute("TheCommand", "requestParameters");
        destination = "closeJsp.jsp";
      }
/********************** selection de colonnes apres avoir selectionn� une table lors de la
 * requete simplifiee ****************************/
      else if (function.equals("SelectColumns")) {
        String tableName = request.getParameter("table");

        request.setAttribute("data", dwe.getDataWarning());
        request.setAttribute("dataQuery", dataWarningSC.getCurrentQuery());
        request.setAttribute("nselectedColumns", dwe.getDataWarning().getColumnNames(tableName));
        request.setAttribute("tableName", tableName);
        destination = "selectColumns.jsp";
      }
/********************** sauvegarde de la requete lors de la requete simplifiee
 * *****************************************************/
      else if (function.startsWith("SaveSelectColumns")) {
        String req = request.getParameter("SQLReq");
        DataWarningQuery dataQuery = dataWarningSC.getCurrentQuery();
        dataQuery.setQuery(req);
        dwe.updateDataWarningQuery(dataQuery);
        request.setAttribute("TheCommand", "requestParameters");
        destination = "closeJsp.jsp";
      }
/********************** selection des agregas lors de la requete simplifiee
 * *****************************************************/
      else if (function.startsWith("SelectAgrega")) {
        String tableName = request.getParameter("table");
        String[] columns = request.getParameterValues("sColumns");

        request.setAttribute("tableName", tableName);
        request.setAttribute("columns", columns);
        destination = "selectAgrega.jsp";
      }
/********************** selection des contraintes lors de la requete simplifiee
 * *****************************************************/
      else if (function.startsWith("SelectConstraints")) {
        int colSize = Integer.parseInt(request.getParameter("columnsSize"));
        String[] columns = new String[colSize];
        for (int i = 0; i < colSize; i++) {
          columns[i] = request.getParameter("Colonne" + i);
        }
        String req = request.getParameter("SQLReq");
        SilverTrace
            .info("dataWarning", "DataWarningRequestRouter.getDestination().SelectConstraints",
                "root.MSG_GEN_PARAM_VALUE", "requete=" + req);
        request.setAttribute("req", req);
        dataWarningSC.setColumns(columns);
        request.setAttribute("columns", columns);
        destination = "selectConstraints.jsp";
      }
/********************** ajout des contraintes lors de la requete simplifiee
 * *****************************************************/
      else if (function.startsWith("AddCriter")) {
        String[] columns = dataWarningSC.getColumns();
        String req = request.getParameter("SQLReq");

        String critere = request.getParameter("critere");
        String sizeString = request.getParameter("critereSize");
        int size;
        List<String> criteres = new ArrayList<>();
        if (sizeString != null && !sizeString.equals("")) {
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
        request.setAttribute("columns", columns);
        destination = "selectConstraints.jsp";
      }
/********************** suppression des contraintes lors de la requete simplifiee
 * *****************************************************/
      else if (function.startsWith("DelCriter")) {
        String[] columns = dataWarningSC.getColumns();
        String req = request.getParameter("SQLReq");

        String sizeString = request.getParameter("critereSize");
        int size;
        List<String> criteres = new ArrayList<>();
        if (sizeString != null && !sizeString.equals("")) {
          size = Integer.parseInt(sizeString);
          for (int i = 0; i < size; i++) {
            String delCritere = request.getParameter("ContrainteSupp" + i);
            if (delCritere == null) //rien a supprimer, alors on recupere l'ancien critere
            {
              criteres.add(request.getParameter("Contrainte" + i));
            }
          }
        }

        String[] constraints = criteres.toArray(new String[criteres.size()]);

        request.setAttribute("constraints", constraints);
        request.setAttribute("req", req);
        request.setAttribute("columns", columns);
        destination = "selectConstraints.jsp";
      }
      else {
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

}
