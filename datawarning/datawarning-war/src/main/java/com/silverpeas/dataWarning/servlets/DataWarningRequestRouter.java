package com.silverpeas.dataWarning.servlets;

import javax.servlet.http.HttpServletRequest;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;

import com.silverpeas.dataWarning.control.DataWarningSessionController;
import com.silverpeas.dataWarning.control.*;
import com.silverpeas.dataWarning.model.*;
import com.stratelia.webactiv.util.viewGenerator.html.Encode;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class DataWarningRequestRouter extends ComponentRequestRouter
{
    /**
     * This method has to be implemented in the component request rooter class.
     * returns the session control bean name to be put in the request object
     * ex : for almanach, returns "almanach"
     */
    public String getSessionControlBeanName()
	 {
	    return "DataWarningSC";
	 }

    /**
     * Method declaration
     *
     *
     * @param mainSessionCtrl
     * @param componentContext
     *
     * @return
     *
     * @see
     */
    public ComponentSessionController createComponentSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
    {
        return new DataWarningSessionController(mainSessionCtrl, componentContext);
    }

    /**
     * This method has to be implemented by the component request rooter
     * it has to compute a destination page
     * @param function The entering request function (ex : "Main.jsp")
     * @param componentSC The component Session Control, build and initialised.
     * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach.jsp?flag=user")
     */
    public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request)
    {
        String  destination = "";
        DataWarningSessionController  DataWarningSC = (DataWarningSessionController)componentSC;
        SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId() + " Function=" + function);

		String flag = componentSC.getUserRoleLevel();
        try
        {
            DataWarningEngine dwe = DataWarningSC.getDataWarningEngine();

/********************** main *****************************************/
			if (function.startsWith("Main") || function.startsWith("dataWarning"))
			{
				// Init the DataWarningEngine each time we go to the first page
				dwe.init();
				
				DataWarning data = dwe.getDataWarning();
				DataWarningScheduler scheduler = dwe.getDataWarningScheduler();
                DataWarningResult dwr = dwe.run();

				//mettre en place un systeme de backlist... lors envoi mail, enlever les user qui sont ds la back list
				boolean isAbonne = false;
				if (DataWarningSC.isUserInDataWarningGroups())
					isAbonne = true;
				else if(dwe.getDataWarningUser(DataWarningSC.getUserId()) != null)
					isAbonne = true;
				request.setAttribute("isAbonne", new Boolean(isAbonne));
                request.setAttribute("analysisTypeString", DataWarningSC.getAnalysisTypeString());
                request.setAttribute("result", dwr);
                request.setAttribute("data", data);
				request.setAttribute("userId", DataWarningSC.getUserId());
				request.setAttribute("scheduler", scheduler);
				request.setAttribute("textFrequenceScheduler", DataWarningSC.getTextFrequenceScheduler());
				destination = "dataWarning.jsp";
			}
/********************** page des parametres de connexion *****************************************/
			else if(function.equals("connectionParameters"))
			{
                DataWarningSC.setCurrentDBDriver(dwe.getDataWarning().getJDBCDriverName());
				request.setAttribute("dataWarningObject", dwe.getDataWarning());
				request.setAttribute("dataWarningDBDrivers", DataWarningSC.getDBDrivers());
				request.setAttribute("currentDBDriver", DataWarningSC.getCurrentDBDriver());
				destination = "connectionParameters.jsp";
			}
/********************** validation des parametres de connexion *****************************************/
			else if (function.equals("updateConnection"))
			{
				DataWarningSC.setCurrentDBDriver((String)request.getParameter("JDBCdriverNameSelect"));
				request.setAttribute("dataWarningObject", dwe.getDataWarning());
				request.setAttribute("dataWarningDBDrivers", DataWarningSC.getDBDrivers());
				request.setAttribute("currentDBDriver", DataWarningSC.getCurrentDBDriver());
				destination = "connectionParameters.jsp";
			}
/********************** validation des parametres de connexion *****************************************/
			else if (function.equals("SetConnection"))
			{
				String DBDriverUniqueId = (String)request.getParameter("JDBCdriverNameSelect");
				String login = request.getParameter("Login");
				String password = request.getParameter("Password");
				int rowLimit = 0;
				if((request.getParameter("RowLimit")!=null)&&(! request.getParameter("RowLimit").trim().equals("")))
					rowLimit = Integer.parseInt(request.getParameter("RowLimit"));
				
				DataWarning data = dwe.getDataWarningWritable();

                data.setJDBCDriverName(DBDriverUniqueId);
				data.setLogin(login);
				data.setPwd(password);
				data.setRowLimit(rowLimit);

				dwe.updateDataWarning(data);
				
				request.setAttribute("dataWarningObject", dwe.getDataWarning());
				request.setAttribute("dataWarningDBDrivers", DataWarningSC.getDBDrivers());
				request.setAttribute("currentDBDriver", DataWarningSC.getCurrentDBDriver());
				destination = "connectionParameters.jsp";
			}
/********************** edition des parametres generaux **********************************/
			else if (function.equals("EditParamGen"))
			{
                request.setAttribute("data", dwe.getDataWarning());
				destination = "editParamGen.jsp";
			}
/********************** sauvegarde des parametres generaux **********************************/
			else if (function.equals("SaveParamGen"))
			{
				String description = (String)request.getParameter("SQLReqDescription");
				int typeAnalyse = Integer.parseInt((String)request.getParameter("typeAnalyse"));
				//get the datawarning and test if analysis type is different
				DataWarning data = dwe.getDataWarningWritable();
				if(data.getAnalysisType() != typeAnalyse)
				{
					data.setAnalysisType(typeAnalyse);
				    data.setDescription(description);
					dwe.updateDataWarning(data);
				}
                else
                {
                    data.setDescription(description);
                    dwe.updateDataWarning(data);
                }
                request.setAttribute("TheCommand","Main");
				destination = "closeJsp.jsp";
			}
/********************** affichage de la requete *****************************************/
			else if (function.equals("requestParameters"))
			{
				DataWarningQuery dataQuery = DataWarningSC.getCurrentQuery();
				DataWarning data = dwe.getDataWarning();
                SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "Query Type = " + Integer.toString(DataWarningSC.getCurrentQueryType()));
				if (dataQuery == null)
                    SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "dataQuery IS NULL");
				if (data == null)
                    SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "data IS NULL");
				
                request.setAttribute("dataQuery", dataQuery);
                request.setAttribute("data", data);
                request.setAttribute("currentQuery",new Integer(DataWarningSC.getCurrentQueryType()));
				destination = "requestParameters.jsp";
			}
/********************** changement de la requete *****************************************/
			else if (function.equals("changeQuery"))
			{
				SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination().changeQuery", "root.MSG_GEN_PARAM_VALUE", "Type requete="+request.getParameter("typeRequete"));
				DataWarningSC.setCurrentQueryType(Integer.parseInt(request.getParameter("typeRequete")));
				SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination().changeQuery", "root.MSG_GEN_PARAM_VALUE", "Type requete sortie="+DataWarningSC.getCurrentQueryType());
                destination = getDestination("requestParameters", componentSC, request);
			}
/********************** edition des param�tres de seuil *****************************************************/
			else if (function.equals("EditParamGenQuery"))
			{
                request.setAttribute("dataQuery", DataWarningSC.getCurrentQuery());
				destination = "editParamGenQuery.jsp";
			}
/********************** sauvegarde des param�tres de seuil *****************************************************/
			else if (function.equals("SaveParamGenQuery"))
			{
				DataWarningQuery dataQuery = DataWarningSC.getCurrentQuery();
				String description = (String)request.getParameter("SQLReqDescription");

				dataQuery.setDescription(description);
                if (dataQuery.getType() == DataWarningQuery.QUERY_TYPE_TRIGGER)
                {
                    int condition = Integer.parseInt((String)request.getParameter("condition"));
                    long seuil = Long.parseLong((String)request.getParameter("seuil"));

				dataQuery.setTheTriggerCondition(condition);
				dataQuery.setTheTrigger(seuil);
                }
                SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "PersoValid=" + request.getParameter("PersoValid"));
                if ((request.getParameter("PersoValid") != null) && (request.getParameter("PersoValid").length() > 0))
                	dataQuery.setPersoValid(DataWarningQuery.QUERY_PERSO_VALID);
                else
                	dataQuery.setPersoValid(DataWarningQuery.QUERY_PERSO_NOT_VALID);
                dataQuery.setPersoColNB(Integer.parseInt(request.getParameter("PersoCol")));
                dataQuery.setPersoUID(request.getParameter("PersoUID"));
				dwe.updateDataWarningQuery(dataQuery);
                request.setAttribute("TheCommand","requestParameters");
				destination = "closeJsp.jsp";
			}
/********************** edition de la requete en mode expert *****************************************************/
			else if (function.equals("EditReqExpert"))
			{
                request.setAttribute("requete", DataWarningSC.getCurrentQuery().getQuery());
				destination = "editReqExpert.jsp";
			}
/********************** sauvegarde de la requete en mode expert *****************************************************/
			else if (function.equals("SaveRequete"))
			{
				String requete = (String)request.getParameter("SQLReq");
				SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination().SaveRequete", "root.MSG_GEN_PARAM_VALUE", "requete=" + requete);
				DataWarning data = dwe.getDataWarning();
				DataWarningQuery dataQuery = DataWarningSC.getCurrentQuery();
                dataQuery.setQuery(requete);

                DataWarningQueryResult dwqr = dataQuery.executeQuery(data);
				if(!dwqr.hasError())
				{
					try
					{
						//Trigger Query only
	                    if(data.getAnalysisType() == DataWarning.TRIGGER_ANALYSIS && DataWarningSC.getCurrentQueryType()==DataWarningQuery.QUERY_TYPE_TRIGGER)
	                    	dwqr.returnTriggerValueFromResult();
	                }
	                catch(Exception e)
	                {
		                SilverTrace.warn("dataWarning", "DataWarningRequestRouter.getDestination()","root.MSG_GEN_ENTER_METHOD","Error during Request Save Verification");
		                dwqr.addError(e,"Value = "+dwqr.getValue(0,0));
	                }
				}
				if(dwqr.hasError())
				{
					request.setAttribute("requete", requete);
					request.setAttribute("valeurRetour", Encode.javaStringToJsString(dwqr.getErrorFullText()));
					destination = "editReqExpert.jsp";
				}
				else
				{
                    dwe.updateDataWarningQuery(dataQuery);
                    request.setAttribute("TheCommand","requestParameters");
					destination = "closeJsp.jsp";
				}
			}
/********************** previsualisation du resultat de la requete (mode expert) *****************************************************/
			else if (function.equals("PreviewReq"))
			{
				String requete = (String)request.getParameter("SQLReq");
				SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination().PreviewReq", "root.MSG_GEN_PARAM_VALUE", "requete=" + requete);
				DataWarning data = dwe.getDataWarning();
				DataWarningQuery dataQuery = DataWarningSC.getCurrentQuery();

                dataQuery.setQuery(requete);
                try
                {
                    DataWarningQueryResult dwqr = dataQuery.executeQuery(data);
                    request.setAttribute("resultQuery", dwqr);
                }
                catch(Exception e)
                {
                    SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "Error Testing Query=" + requete,e);
                }
				destination = "previewReq.jsp";
			}
/********************** edition des parametres du scheduler *****************************************************/
			else if (function.equals("schedulerParameters") || function.equals("schedulerParametersWithoutReset"))
			{
				Collection groups = dwe.getDataWarningGroups();
				Collection users = dwe.getDataWarningUsers();
				String[] idGroups = null;
				String[] idUsers = null;
				if(groups != null && groups.size() > 0)
				{
					idGroups = new String[groups.size()];
					Iterator it = groups.iterator();
					int i = 0;
					while(it.hasNext())
					{
						DataWarningGroup gr = (DataWarningGroup)it.next();
						idGroups[i] = Integer.toString(gr.getGroupId());
						i++;
					}
				}
				if(users != null && users.size() > 0)
				{
					idUsers = new String[users.size()];
					Iterator it = users.iterator();
					int i = 0;
					while(it.hasNext())
					{
						DataWarningUser us = (DataWarningUser)it.next();
						idUsers[i] = Integer.toString(us.getUserId());
						i++;
					}
				}
				if (function.equals("schedulerParameters"))
				{
					request.setAttribute("scheduler", DataWarningSC.resetEditedScheduler());
				}
				else
				{
					request.setAttribute("scheduler", DataWarningSC.getEditedScheduler());
				}

				//put groups and users sleted in request
				request.setAttribute("listGroups", DataWarningSC.buildOptions(DataWarningSC.getSelectedGroupsNames(idGroups), "", null, true));
				request.setAttribute("listUsers", DataWarningSC.buildOptions(DataWarningSC.getSelectedUsersNames(idUsers), "", null, true));
				destination = "schedulerParameters.jsp";
			}
/********************** change the layers *****************************************************/
			else if (function.equals("UpdateLayer"))
			{
				DataWarningScheduler scheduler = DataWarningSC.getEditedScheduler();

				int numberOfTimes = Integer.parseInt((String)request.getParameter("numberOfTimes"));
				int numberOfTimesMoment = Integer.parseInt((String)request.getParameter("numberOfTimesMoment"));

				//save parameters
				scheduler.setNumberOfTimes(numberOfTimes);
				scheduler.setNumberOfTimesMoment(numberOfTimesMoment);
                if ((request.getParameter("month") != null) && (request.getParameter("month").length() > 0))
                {
                    scheduler.setTheMonth(Integer.parseInt((String)request.getParameter("month")));
                }
                if ((request.getParameter("dayOfMonth") != null) && (request.getParameter("dayOfMonth").length() > 0))
                {
                    scheduler.setDayOfMonth(Integer.parseInt((String)request.getParameter("dayOfMonth")));
                }
                if ((request.getParameter("dayOfWeek") != null) && (request.getParameter("dayOfWeek").length() > 0))
                {
                    scheduler.setDayOfWeek(Integer.parseInt((String)request.getParameter("dayOfWeek")));
                }
                if ((request.getParameter("heure") != null) && (request.getParameter("heure").length() > 0))
                {
                    scheduler.setHours(Integer.parseInt((String)request.getParameter("heure")));
                }
                if ((request.getParameter("min") != null) && (request.getParameter("min").length() > 0))
                {
                    scheduler.setMinits(Integer.parseInt((String)request.getParameter("min")));
                }
                destination = getDestination("schedulerParametersWithoutReset", componentSC, request);
			}
/********************** sauvegarde des parametres du scheduler et d�marrage de celui-ci *****************************************************/
			else if (function.equals("SetScheduler"))
			{
                getDestination("UpdateLayer", componentSC, request);
				dwe.updateDataWarningScheduler(DataWarningSC.getEditedScheduler());
                destination = getDestination("schedulerParameters", componentSC, request);
			}
/********************** lancer le scheduler *****************************************************/
			else if (function.equals("StartScheduler"))
			{
                dwe.startScheduler();

                destination = getDestination("schedulerParameters", componentSC, request);
			}
/********************** arreter le scheduler *****************************************************/
			else if (function.equals("StopScheduler"))
			{
                dwe.stopScheduler();
			
                destination = getDestination("schedulerParameters", componentSC, request);
			}
/********************** choix des groupes et users dans l'onglet scheduler *****************************************************/
			else if (function.equals("Notification"))
			{
				return DataWarningSC.initSelectionPeas();
			}
/********************** groupes et users choisis *****************************************************/
			else if (function.equals("SaveNotification"))
			{
                DataWarningSC.returnSelectionPeas();
                destination = getDestination("schedulerParameters", componentSC, request);
			}
/********************** groupes et users choisis *****************************************************/
			else if (function.equals("Suscribe"))
			{
				boolean isAbonne = false;
				String suscribe = (String)request.getParameter("suscribeAction");
				DataWarningUser user = new DataWarningUser(componentSC.getComponentId(),Integer.parseInt(componentSC.getUserId()));
				if(suscribe.equals("true"))
				{
					isAbonne = true;
					dwe.createDataWarningUser(user);
				}
				else
				{
					dwe.deleteDataWarningUser(user);
					//TODO : pb si user fait parti d'un groupe
				}
				destination = getDestination("dataWarning", componentSC, request);
			}
/********************** selection d'une table lors de la requete simplifiee *****************************************************/
			else if (function.equals("SelectTable"))
			{
				request.setAttribute("allTables", dwe.getDataWarning().getAllTableNames());
                request.setAttribute("dataQuery", DataWarningSC.getCurrentQuery());
				destination = "selectTable.jsp";
			}
/********************** sauvegarde de la table selectionn�e lors de la requete simplifiee *****************************************************/
			else if (function.equals("SaveSelectTable"))
			{
				String tableName = (String)request.getParameter("table");
				String req = "select * from " + tableName;
				DataWarningQuery dataQuery = DataWarningSC.getCurrentQuery();
				dataQuery.setQuery(req);
				dwe.updateDataWarningQuery(dataQuery);
                request.setAttribute("TheCommand","requestParameters");
				destination = "closeJsp.jsp";
			}
/********************** selection de colonnes apres avoir selectionn� une table lors de la requete simplifiee ****************************/
			else if (function.equals("SelectColumns"))
			{
				String tableName = (String)request.getParameter("table");

                request.setAttribute("data", dwe.getDataWarning());
                request.setAttribute("dataQuery", DataWarningSC.getCurrentQuery());
				request.setAttribute("nselectedColumns", dwe.getDataWarning().getColumnNames(tableName));
				request.setAttribute("tableName", tableName);
				destination = "selectColumns.jsp";
			}
/********************** sauvegarde de la requete lors de la requete simplifiee *****************************************************/
			else if (function.startsWith("SaveSelectColumns"))
			{
				String req = (String)request.getParameter("SQLReq");
				SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination().SaveSelectColumns", "root.MSG_GEN_PARAM_VALUE", "requete=" + req);
				DataWarningQuery dataQuery = DataWarningSC.getCurrentQuery();
				dataQuery.setQuery(req);
				dwe.updateDataWarningQuery(dataQuery);
                request.setAttribute("TheCommand","requestParameters");
				destination = "closeJsp.jsp";
			}
/********************** selection des agregas lors de la requete simplifiee *****************************************************/
			else if (function.startsWith("SelectAgrega"))
			{
				String tableName = (String)request.getParameter("table");
				String[] columns = (String[])request.getParameterValues("sColumns");

				request.setAttribute("tableName", tableName);
				request.setAttribute("columns", columns);
				destination = "selectAgrega.jsp";
			}
/********************** selection des contraintes lors de la requete simplifiee *****************************************************/
			else if (function.startsWith("SelectConstraints"))
			{
				int colSize = Integer.parseInt((String)request.getParameter("columnsSize"));
				String[] columns = new String[colSize];
				for(int i=0; i<colSize; i++)
					columns[i] = request.getParameter("Colonne" + i);
				String req = (String)request.getParameter("SQLReq");
				SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination().SelectConstraints", "root.MSG_GEN_PARAM_VALUE", "requete=" + req);
				request.setAttribute("req", req);
				DataWarningSC.setColumns(columns);
				request.setAttribute("columns", columns);
				destination = "selectConstraints.jsp";
			}
/********************** ajout des contraintes lors de la requete simplifiee *****************************************************/
			else if (function.startsWith("AddCriter"))
			{
				String[] columns = DataWarningSC.getColumns();
				String req = request.getParameter("SQLReq");
				SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination().AddCriter", "root.MSG_GEN_PARAM_VALUE", "requete=" + req);
				String critere = request.getParameter("critere");
				String sizeString = request.getParameter("critereSize");
				int size;
				List criteres = new ArrayList();
				if(sizeString != null && !sizeString.equals(""))
				{
					size = Integer.parseInt(sizeString);
					for(int i=0; i<size; i++)
						criteres.add(request.getParameter("Contrainte" + i));
				}
				if(critere != null)
					criteres.add(critere);

				String[] constraints = (String[])criteres.toArray(new String[]{});

				request.setAttribute("constraints", constraints);
				request.setAttribute("req", req);
				request.setAttribute("columns", columns);
				destination = "selectConstraints.jsp";
			}
/********************** suppression des contraintes lors de la requete simplifiee *****************************************************/
			else if (function.startsWith("DelCriter"))
			{
				String[] columns = DataWarningSC.getColumns();
				String req = request.getParameter("SQLReq");
				SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination().DelCriter", "root.MSG_GEN_PARAM_VALUE", "requete=" + req);
				String sizeString = request.getParameter("critereSize");
				int size;
				List criteres = new ArrayList();
				if(sizeString != null && !sizeString.equals(""))
				{
					size = Integer.parseInt(sizeString);
					for(int i=0; i<size; i++)
					{
						String delCritere = request.getParameter("ContrainteSupp" + i);
						if(delCritere == null) //rien a supprimer, alors on recupere l'ancien critere
							criteres.add(request.getParameter("Contrainte" + i));
					}
				}

				String[] constraints = (String[])criteres.toArray(new String[]{});

				request.setAttribute("constraints", constraints);
				request.setAttribute("req", req);
				request.setAttribute("columns", columns);
				destination = "selectConstraints.jsp";
			}
/********************** autre cas *****************************************/
			else
				destination = function;

			request.setAttribute("flag", flag);
            if (!destination.startsWith("/dataWarning"))
            {
			destination = "/dataWarning/jsp/" + destination;
            }
        }
        catch (Exception e)
        {
            request.setAttribute("javax.servlet.jsp.jspException", e);
            destination = "/admin/jsp/errorpageMain.jsp";
        }

        SilverTrace.info("dataWarning", "DataWarningRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
        return destination;
    }

}
