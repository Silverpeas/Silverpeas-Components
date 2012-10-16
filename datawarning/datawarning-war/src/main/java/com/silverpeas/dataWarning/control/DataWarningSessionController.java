/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.dataWarning.control;

import com.stratelia.silverpeas.peasCore.*;
import com.stratelia.silverpeas.util.*;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.viewGenerator.html.Encode;
import com.stratelia.webactiv.util.*;
import com.stratelia.webactiv.beans.admin.*;
import java.util.*;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.silverpeas.dataWarning.DataWarningDBDriver;
import com.silverpeas.dataWarning.DataWarningDBDrivers;
import com.silverpeas.dataWarning.DataWarningException;
import com.silverpeas.dataWarning.control.DataWarningEngine;
import com.silverpeas.dataWarning.model.*;

import com.stratelia.silverpeas.selection.Selection;

public class DataWarningSessionController extends AbstractComponentSessionController
{
    /** Interface metier du composant */
	private DataWarningEngine       dataWarningEngine = null;
    private int                     queryType = DataWarningQuery.QUERY_TYPE_RESULT;
    private DataWarningScheduler    editedScheduler = null;
    private DataWarning             editedDataWarning = null;
    private DataWarningDBDrivers dataWarningDBDrivers = null;
    
	private String currentDBDriver = "";

	private Selection sel;

	private String[] columns;

	/**
     * Standard Session Controller Constructeur
     *
     *
     * @param mainSessionCtrl   The user's profile
     * @param componentContext  The component's profile
     *
     * @see
     */
	public DataWarningSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
	{
			super(mainSessionCtrl, componentContext,  "com.silverpeas.dataWarning.multilang.dataWarning", "com.silverpeas.dataWarning.settings.dataWarningIcons");
            try
            {
        		dataWarningEngine = new DataWarningEngine(getComponentId());
            }
            catch (Exception e)
            {
                SilverTrace.error("dataWarning", "DataWarningSessionController.DataWarningSessionController", "DataWarning.EX_DATA_ACCESS_FAILED", null, e);
            }
			sel = getSelection();
	}

	public DataWarningDBDrivers getDataWarningDBDrivers()
	{
    if (dataWarningDBDrivers == null)
      dataWarningDBDrivers = new DataWarningDBDrivers();
    return dataWarningDBDrivers;
    
	}
	public DataWarningDBDriver[] getDBDrivers()
	{
		return getDataWarningDBDrivers().getDBDrivers();
	}

  public void setCurrentDBDriver(String DBDriverUniqueId)
  {
      currentDBDriver = DBDriverUniqueId;
  }

  public DataWarningDBDriver getCurrentDBDriver()
  {
      return getDataWarningDBDrivers().getDBDriver(currentDBDriver);
  }

	public String initSelectionPeas() throws DataWarningException
	{
		String retour = null;
		//get usersId and groupsId
		Collection groups = dataWarningEngine.getDataWarningGroups();
		Collection users = dataWarningEngine.getDataWarningUsers();
		//transforme la collection en tableau de string
		String[] idGroups = null;
		String[] idUsers = null;
		if(groups != null && groups.size() > 0)
		{
			idGroups = new String[groups.size()];
			DataWarningGroup[] Groups = (DataWarningGroup[])groups.toArray(new DataWarningGroup[0]);
			for(int i=0; i<groups.size(); i++)
				idGroups[i] = String.valueOf(Groups[i].getGroupId());
		}
		if(users != null && users.size() > 0)
		{
			idUsers = new String[users.size()];
			DataWarningUser[] Users = (DataWarningUser[])users.toArray(new DataWarningUser[0]);
			for(int i=0; i<users.size(); i++)
				idUsers[i] = String.valueOf(Users[i].getUserId());
		}
		try
		{
			String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
			String hostUrl = m_context + URLManager.getURL(getSpaceId(), getComponentId())+"SaveNotification";
			String cancelUrl = m_context + URLManager.getURL(getSpaceId(), getComponentId())+"schedulerParameters";
			PairObject hostComponentName = new PairObject("", ""); 
			String hostSpaceName = getSpaceLabel();

			sel.resetAll();
			sel.setHostSpaceName(hostSpaceName);
			sel.setHostComponentName(hostComponentName);
			sel.setHostPath(null);
			sel.setGoBackURL(hostUrl);
			sel.setCancelURL(cancelUrl);
			sel.setSelectedElements(idUsers);
			sel.setSelectedSets(idGroups);
			sel.setMultiSelect(true);
			sel.setPopupMode(true);

			if ((idUsers == null || idUsers.length == 0) && (idGroups == null || idGroups.length == 0))
			{
				//Affichera la page d'accueil du selectionPeas si aucun utilisateur n'était selectionne
				sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
			}
			else
			{
				//Affichera le caddie du selectionPeas si des utilisateurs ont d�j� �t� s�lectionn�s
				sel.setFirstPage(Selection.FIRST_PAGE_CART);
			}
			retour = Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
		}
		catch (Exception e)
		{	
			SilverTrace.error("dataWarning", "DataWarningSessionController.initSelectionPeas", "DataWarning.MSG_CANT_INIT_SELECTIONPEAS", null, e);
		}
		return retour;
	}

    public void returnSelectionPeas() throws DataWarningException
    {
        //get groups and users selected
        String[] idGroups = sel.getSelectedSets();
        String[] idUsers = sel.getSelectedElements();
        //save groups and users selected
        dataWarningEngine.deleteDataWarningGroups();
        if(idGroups != null)
        {
            for(int i=0; i<idGroups.length; i++)
                dataWarningEngine.createDataWarningGroup(new DataWarningGroup(getComponentId(),Integer.parseInt(idGroups[i])));
        }
        dataWarningEngine.deleteDataWarningUsers();
        if(idUsers != null)
        {
            for(int i=0; i<idUsers.length; i++)
                dataWarningEngine.createDataWarningUser(new DataWarningUser(getComponentId(),Integer.parseInt(idUsers[i])));
        }
	}

	public String buildOptions(ArrayList ar, String selectValue, String selectText, boolean bSorted)
	{
		StringBuffer valret = new StringBuffer();
        Properties elmt = null;
        String selected ;
        ArrayList arToDisplay = ar;
        int i;

        if (selectText != null)
        {
            if (selectValue == null || selectValue.length() <= 0)
                selected = "SELECTED";
            else 
                selected = "";
            valret.append("<option value=\"\" " + selected + ">" + Encode.javaStringToHtmlString(selectText) + "</option>\n");
        }
        if (bSorted)
        {
            Properties[] theList = (Properties[]) ar.toArray(new Properties[0]) ;
            Arrays.sort(theList, new Comparator() {
                              public int compare(Object o1, Object o2) {
                                return (((Properties) o1).getProperty("name")).toUpperCase().compareTo(((Properties) o2).getProperty("name").toUpperCase()) ;
                              }
                              public boolean equals(Object o) {
                                return false ;
                              }
                            }
                        );
            arToDisplay = new ArrayList(theList.length) ;
            for (i=0 ; i<theList.length ; i++)
                arToDisplay.add(theList[i]) ;
        }
        if (arToDisplay != null)
        {
            for (i=0 ; i<arToDisplay.size() ; i++) 
            {
                elmt = (Properties)arToDisplay.get(i);
                if (elmt.getProperty("id").equalsIgnoreCase(selectValue)) 
                    selected = "SELECTED";
                else 
                    selected = "";
                valret.append("<option value=\"" + elmt.getProperty("id") + "\" " + selected + ">" + Encode.javaStringToHtmlString(elmt.getProperty("name")) + "</option>\n");
            }
        }
        return valret.toString();  
	}

	public ArrayList getSelectedGroups(String[] selectedGroupsId) throws DataWarningException
	{
			Group[] selectedGroups = null;
			Properties p;
			int i;
			ArrayList ar = new ArrayList();
			
			if(selectedGroupsId !=null && selectedGroupsId.length>0)
			{
				selectedGroups = getGroupList(selectedGroupsId);

				if (selectedGroups != null && selectedGroups.length > 0)
				{
					for (i = 0; i < selectedGroups.length; i++)
					{
						p = new Properties();
						p.setProperty("id", selectedGroups[i].getId());
						p.setProperty("name", selectedGroups[i].getName());
						ar.add(p);
					}
					if (ar.size() != selectedGroups.length)
						throw new DataWarningException("DataWarningSessionController.getSelectedGroups()", SilverpeasException.WARNING, "notificationUser.EX_CANT_GET_SELECTED_GROUPS_INFOS");
				}
			}
	return ar;
	}
	
	private Group[] getGroupList(String[] idGroups)
	{
		Group[] setOfGroup = null;
		if(idGroups!=null && idGroups.length>0)
		{
			setOfGroup = new Group[idGroups.length];
			for(int i=0; i<idGroups.length; i++)
				setOfGroup[i] = getOrganizationController().getGroup(idGroups[i]);
		}
		return setOfGroup;
	}

	private UserDetail[] getUserDetailList(String[] idUsers)
	{
		return getOrganizationController().getUserDetails(idUsers);
	}

	public ArrayList getSelectedUsersNames(String[] selectedUsersId) throws DataWarningException
	{
		Properties p;
		ArrayList ar = new ArrayList();
		UserDetail[] selectedUsers = null;

		if(selectedUsersId!=null && selectedUsersId.length>0)
		{
			selectedUsers = getUserDetailList(selectedUsersId);
			if (selectedUsers != null && selectedUsers.length > 0)
			{
				for(int i=0; i<selectedUsers.length; i++)
				{
					p = new Properties();
					p.setProperty("id", selectedUsers[i].getId());
					p.setProperty("name", selectedUsers[i].getLastName() + " " + selectedUsers[i].getFirstName());
					ar.add(p);
				}
				if (ar.size() != selectedUsers.length)
					throw new DataWarningException("DataWarningSessionController.getSelectedUsersNames()", SilverpeasException.WARNING, "notificationUser.EX_CANT_GET_SELECTED_USERS_INFOS");
			}
		}
		return ar;
	}

	public ArrayList getSelectedGroupsNames(String[] selectedGroupsId) throws DataWarningException
	{
		Group[] selectedGroups = null;
		Properties p;
		ArrayList  ar = new ArrayList();
		
		if(selectedGroupsId !=null && selectedGroupsId.length>0)
		{
			selectedGroups = getGroupList(selectedGroupsId);
			if(selectedGroups != null && selectedGroups.length > 0)
			{
				for (int i=0; i<selectedGroups.length; i++)
				{
					p = new Properties();
					p.setProperty("id", selectedGroups[i].getId());
					p.setProperty("name", selectedGroups[i].getName());
					ar.add(p);
				}
				if (ar.size() != selectedGroups.length)
					throw new DataWarningException("DataWarningSessionController.getSelectedGroups()", SilverpeasException.WARNING, "notificationUser.EX_CANT_GET_SELECTED_GROUPS_INFOS");
			}
		}
		return ar;
	}
	
	public boolean isUserInDataWarningGroups() throws DataWarningException
	{
		boolean trouve = false;
		Collection dataGroups = dataWarningEngine.getDataWarningGroups();
		if(dataGroups != null)
		{
			try
			{
				String[] idGroups = new String[dataGroups.size()];
				Iterator it = dataGroups.iterator();
				int i = 0;
				while(it.hasNext())
				{
					DataWarningGroup group = (DataWarningGroup)it.next();
					idGroups[i] = String.valueOf(group.getGroupId());
					i++;
				}
				Group[] groups = getOrganizationController().getGroups(idGroups);
				for(int j=0; j<idGroups.length; j++)
				{
					Group group = groups[j];
					String[] userIds = group.getUserIds();
					if(userIds != null && userIds.length > 0)
						for(int k=0; k<userIds.length; k++)
							if(userIds[k].equals(getUserId()))
								trouve = true;
				}
			}
			catch(Exception e)
			{
				SilverTrace.error("dataWarning", "DataWarningSessionController.isUserInDataWarningGroups", 
						 "DataWarning.DataWarning.EX_LOAD_GROUPS", null, e);
			}
		}
		return trouve;
	}

	public String getTextFrequenceScheduler() throws DataWarningException
	{
		String retour = "";
		DataWarningScheduler scheduler = dataWarningEngine.getDataWarningScheduler();
		ResourceLocator messages = new ResourceLocator("com.silverpeas.dataWarning.multilang.dataWarning", "");
		if(scheduler != null)
        {
            if (scheduler.getNumberOfTimes() == 1)
            {
                switch (scheduler.getNumberOfTimesMoment())
                {
                case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR :
    				retour += messages.getString("frequenceSchedulerRequete") + " " + messages.getString("schedulerPeriodicity0") + " (" + messages.getString("minute") + " : " + scheduler.getMinits() + ")";
                    break;
                case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY :
                    retour += messages.getString("frequenceSchedulerRequete") + " " + messages.getString("schedulerPeriodicity1") + " (" + messages.getString("heure") + " : " + scheduler.getHours() + ", " + messages.getString("minute") + " : " + scheduler.getMinits() + ")";
                    break;
                case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK :
    				retour += messages.getString("frequenceSchedulerRequete") + " " + messages.getString("schedulerPeriodicity2") + " (" + messages.getString("jourSemaine") + " : " + scheduler.getDayOfWeek() + ", " + messages.getString("heure") + " : " + scheduler.getHours() + ", " + messages.getString("minute") + " : " + scheduler.getMinits() + ")";
                    break;
                case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH :
    				retour += messages.getString("frequenceSchedulerRequete") + " " + messages.getString("schedulerPeriodicity3") + " (" + messages.getString("jourMois") + " : " + (scheduler.getDayOfMonth() + 1) + ", " + messages.getString("heure") + " : " + scheduler.getHours() + ", " + messages.getString("minute") + " : " + scheduler.getMinits() + ")";
                    break;
                case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR :
    				retour += messages.getString("frequenceSchedulerRequete") + " " + messages.getString("schedulerPeriodicity4") + " (" + messages.getString("mois") + " : " + (scheduler.getTheMonth() + 1) + ", " + messages.getString("jourMois") + " : " + (scheduler.getDayOfMonth() + 1) + ", " + messages.getString("heure") + " : " + scheduler.getHours() + ", " + messages.getString("minute") + " : " + scheduler.getMinits() + ")";
                    break;
                }
            }
            else
			{
				retour += messages.getString("frequenceSchedulerRequete") + " " + scheduler.getNumberOfTimes() + " " + messages.getString("schedulerPeriodicity5") + " ";
				if(scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR)
					retour += messages.getString("heure");
				else if(scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY)
					retour += messages.getString("jour");
				else if(scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK)
					retour += messages.getString("semaine");
				else if(scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH)
					retour += messages.getString("mois");
				else if(scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR)
					retour += messages.getString("annee");
			}
        }
		return retour;
	}

	public String[] getColumns()
	{
		return columns;
	}

	public void setColumns(String[] col)
	{
		columns = col;
	}

    public String getAnalysisTypeString() throws DataWarningException
    {
        return getString("typeAnalyse" + Integer.toString(dataWarningEngine.getDataWarning().getAnalysisType()));
    }

	public DataWarningEngine getDataWarningEngine() throws DataWarningException 
	{
        return dataWarningEngine;
	}

	public DataWarningQuery setCurrentQueryType(int qt) throws DataWarningException 
	{
		queryType = qt;
		return getCurrentQuery();
	}

    public DataWarningQuery getCurrentQuery() throws DataWarningException 
    {
        return dataWarningEngine.getDataWarningQuery(queryType);
    }

    public int getCurrentQueryType()
    {
        return queryType;
    }

    public DataWarningScheduler resetEditedScheduler()
    {
        editedScheduler = (DataWarningScheduler)dataWarningEngine.getDataWarningScheduler().clone();
        return editedScheduler;
    }

    public DataWarningScheduler getEditedScheduler()
    {
        if (editedScheduler == null)
        {
            return resetEditedScheduler();
        }
        else
        {
            return editedScheduler;
        }
    }

    public DataWarning resetEditedDataWarning()
    {
        editedDataWarning = (DataWarning)dataWarningEngine.getDataWarning().clone();
        return editedDataWarning;
    }

    public DataWarning getEditedDataWarning()
    {
        if (editedDataWarning == null)
        {
            return resetEditedDataWarning();
        }
        else
        {
            return editedDataWarning;
        }
    }
}
