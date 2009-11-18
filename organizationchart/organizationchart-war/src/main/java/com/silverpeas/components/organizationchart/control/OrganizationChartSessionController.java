
package com.silverpeas.components.organizationchart.control;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;

public class OrganizationChartSessionController extends AbstractComponentSessionController
{
    /**
     * Standard Session Controller Constructeur
     *
     * @param mainSessionCtrl   The user's profile
     * @param componentContext  The component's profile
     *
     * @see
     */
	public OrganizationChartSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
	{
		super(mainSessionCtrl, componentContext,  
			"com.silverpeas.components.organizationchart.multilang.OrganizationChartBundle", 
			"com.silverpeas.components.organizationchart.settings.OrganizationChartIcons");
	}
	
	public String getLibelleAttribut(String attId) {
		String attName = "organizationchart.attribut." + attId;
		String libelle = getString(attName);
		if(libelle == null || libelle.equalsIgnoreCase(attName))
			return null;
		else 
			return libelle;
	}
	
}