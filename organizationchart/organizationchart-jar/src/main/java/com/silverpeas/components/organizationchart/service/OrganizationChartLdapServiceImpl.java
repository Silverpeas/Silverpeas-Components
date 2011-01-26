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
package com.silverpeas.components.organizationchart.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.silverpeas.components.organizationchart.model.OrganizationalChart;
import com.silverpeas.components.organizationchart.model.OrganizationalPerson;
import com.silverpeas.components.organizationchart.model.OrganizationalRole;
import com.silverpeas.components.organizationchart.model.OrganizationalUnit;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;

public class OrganizationChartLdapServiceImpl implements OrganizationChartService {

  public static final String PARAM_SERVERURL = "serverURL";
  public static final String PARAM_CTXFACTORY = "initialContextFactory";
  public static final String PARAM_AUTHMODE = "authenticationMode";
  public static final String PARAM_PRINCIPAL = "principal";
  public static final String PARAM_CREDENTIAL = "credentials";
  public static final String PARAM_LDAP_ROOT = "ldapRoot";
  public static final String PARAM_LDAP_CLASS_PERSON = "ldapClassPerson";
  public static final String PARAM_LDAP_CLASS_UNIT = "ldapClassUnit";
  public static final String PARAM_LDAP_ATT_UNIT = "ldapAttUnit";
  public static final String PARAM_LDAP_ATT_NAME = "ldapAttName";
  public static final String PARAM_LDAP_ATT_TITLE = "ldapAttTitle"; // champ LDAP du titre 
  public static final String PARAM_LDAP_ATT_DESC = "ldapAttDesc"; // champ ldap de la description
  public static final String PARAM_UNITSCHART_CENTRAL_LABEL = "unitsChartCentralLabel";
  public static final String PARAM_UNITSCHART_RIGHT_LABEL = "unitsChartRightLabel";
  public static final String PARAM_UNITSCHART_LEFT_LABEL = "unitsChartLeftLabel";
  public static final String PARAM_PERSONNSCHART_CENTRAL_LABEL = "personnsChartCentralLabel";
  public static final String PARAM_PERSONNSCHART_CATEGORIES_LABEL = "personnsChartCategoriesLabel";
  public static final String PARAM_UNITSCHART_OTHERSINFOS_KEYS = "unitsChartOthersInfosKeys";
  public static final String PARAM_PERSONNSCHART_OTHERSINFOS_KEYS = "personnsChartOthersInfosKeys";
  
  public static final String PARAM_LDAP_ATT_ACTIF = "ldapAttActif";

  private String LDAP_ROOT;
  private String LDAP_CLASS_PERSON;
  private String LDAP_CLASS_UNIT;
  private String LDAP_ATT_UNIT;
  private String LDAP_ATT_NAME;
  private String LDAP_ATT_TITLE;
  private String LDAP_ATT_DESC;
  private String LDAP_ATT_ACTIF;

  private OrganizationalRole[] UNITSCHART_CENTRAL_LABEL;
  private OrganizationalRole[] UNITSCHART_RIGHT_LABEL;
  private OrganizationalRole[] UNITSCHART_LEFT_LABEL;
  private OrganizationalRole[] PERSONNSCHART_CENTRAL_LABEL;
  private OrganizationalRole[] PERSONNSCHART_CATEGORIES_LABEL;
  private Map<String,String> UNITSCHART_KEYSANDLABELOTHERSINFOS;
  private Map<String,String> PERSONNSCHART_KEYSANDLABELOTHERSINFOS;
  
  private OrganizationController controller;

  @Override
  public OrganizationalChart getOrganizationChart(String componentId, String baseOu, int type) {
    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_ENTER_METHOD",
        "componentId=" + componentId);
    Hashtable<String, String> env = initEnv(componentId);
    OrganizationalPerson[] arrayPerson = null;
    OrganizationalUnit[] units = null;
    
    // beginning node of the search
    String rootOu = LDAP_ROOT;
    if(baseOu != null){
    	rootOu = baseOu;
    }
    
    // Parent definition = top of the chart
    String[] ous = rootOu.split(",");
    String[] firstOu = ous[0].split("=");
    OrganizationalUnit parent = new OrganizationalUnit(firstOu[1],rootOu,LDAP_ATT_UNIT);
    
    try {
      DirContext ctx = new InitialDirContext(env);
      SearchControls ctls = new SearchControls();
      ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
      ctls.setCountLimit(0);

      try {
        // get personns
    	arrayPerson = getPersons(ctx, ctls, rootOu, type);
    	if(arrayPerson!=null && arrayPerson.length > 0){
    		parent.setUnderPersonnsExists(true);
    	}
    	SilverTrace.info("organizationchart",
				"OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_PARAM_VALUE",
            	"personns retrieved !");
    	
        // get units
    	if(type == OrganizationalChart.UNITCHART){
    		units = getUnits(ctx, ctls, rootOu);
    		SilverTrace.info("organizationchart",
    				"OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_PARAM_VALUE",
                	"units retrieved !");
    	}

        // détermination du chef de chaque personne
        /*List<OrganizationalPerson> listService = new ArrayList<OrganizationalPerson>();
        for (Iterator<OrganizationalPerson> it = mapPerson.values().iterator(); it.hasNext();) {
          OrganizationalPerson pers = it.next();
          if (pers.isResponsable()) {
            if (listOu.containsKey(pers.getService())) {
              String ouRoot = listOu.get(pers.getService());
              i = setResponsable(pers, ouRoot, i, listOu, listResponsable, listService);
            }
          } else {
            i = setResponsable(pers, pers.getService(), i, listOu, listResponsable, listService);
          }
        }
        for (OrganizationalPerson pers : listService) {
          mapPerson.put(pers.getName() + pers.getId(), pers);
        }*/
        
      } catch (Exception ex) {
        ctx.close();
        ex.printStackTrace();
        SilverTrace.error("organizationchart",
            "OrganizationChartLdapServiceImpl.getOrganizationChart",
            "organizationChart.ldap.search.error", ex);
        return null;
      }

      ctx.close();
    } catch (NamingException e) {
      e.printStackTrace();
      SilverTrace.error("organizationchart",
          "OrganizationChartLdapServiceImpl.getOrganizationChart",
          "organizationChart.ldap.conection.error", e);
      return null;
    }
    
    OrganizationalChart chart = null;
    if(type == OrganizationalChart.UNITCHART){
    	chart = new OrganizationalChart(parent, units, arrayPerson);
    }else{
    	OrganizationalUnit[] categories = getCategories(arrayPerson);
    	chart = new OrganizationalChart(parent, arrayPerson, categories);
    }
    return chart;
  }

private OrganizationalPerson[] getPersons(DirContext ctx, SearchControls ctls, String rootOu, int type) throws NamingException {
	Map<String, OrganizationalPerson> mapPerson = new HashMap<String, OrganizationalPerson>();
	NamingEnumeration<SearchResult> results = ctx.search(rootOu,
	    "(objectclass=" + LDAP_CLASS_PERSON + ")", ctls);
	SilverTrace.info("organizationchart",
	    "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_PARAM_VALUE",
	    "users retrieved !");
	int i = 0;
	while (results != null && results.hasMore()) {
	  SearchResult entry = (SearchResult) results.next();
	  if (entry.getName() != null && !entry.getName().isEmpty()) {
	    Attributes attrs = entry.getAttributes();
	    if (isActif(attrs)) {
	      OrganizationalPerson pers = createPerson(i, attrs,
	          entry.getNameInNamespace(), type);
	      mapPerson.put(pers.getName() + pers.getId(), pers);
	      i++;
	    }
	  }
	}
	
	// alphabetical sort
	OrganizationalPerson[] arrayPerson = null;
    if (mapPerson.size() > 0) {
      arrayPerson = new OrganizationalPerson[mapPerson.size()];
      List<String> keys = new ArrayList<String>(mapPerson.keySet());
      Collections.sort(keys);
      int j = 0;
      for (String id : keys) {
        OrganizationalPerson pers = mapPerson.get(id);
        arrayPerson[j++] = pers;
      }
    }
    
    return arrayPerson;
}

private OrganizationalUnit[] getUnits(DirContext ctx,
		SearchControls ctls, String rootOu) throws NamingException {
	
	ArrayList<OrganizationalUnit> units = new ArrayList<OrganizationalUnit>();
	
	NamingEnumeration<SearchResult> results = ctx.search(rootOu, "(objectclass=" + LDAP_CLASS_UNIT + ")", ctls);
	SilverTrace.info("organizationchart",
	    "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_PARAM_VALUE",
	    "services retrieved !");
	while (results != null && results.hasMore()) {
	  SearchResult entry = (SearchResult) results.next();
	  Attributes attrs = entry.getAttributes();
	  String ou = getAttributValue(attrs.get(LDAP_ATT_UNIT));
	  String completeOu = entry.getNameInNamespace();
	  OrganizationalUnit unit = new OrganizationalUnit(ou, completeOu, LDAP_ATT_UNIT);
	  // search of sub ous
	  NamingEnumeration<SearchResult> subOus = ctx.search(completeOu, "(objectclass=" + LDAP_CLASS_UNIT + ")", ctls);
	  if(subOus != null && subOus.hasMoreElements()){
		  unit.setUnderOrganizationalUnitExists(true);
	  }
	  // search of personns in this unit
	  NamingEnumeration<SearchResult> subPersonns = ctx.search(completeOu,
			    "(objectclass=" + LDAP_CLASS_PERSON + ")", ctls);
	  if(subPersonns != null && subPersonns.hasMoreElements()){
		  unit.setUnderPersonnsExists(true);
	  }
	  units.add(unit);
	}
	return units.toArray(new OrganizationalUnit[units.size()]);
}

private OrganizationalUnit[] getCategories(OrganizationalPerson[] arrayPerson) {
	if(arrayPerson!= null && arrayPerson.length > 0){
		ArrayList<OrganizationalUnit> categories = new ArrayList<OrganizationalUnit>();
		ArrayList<String> categoriesLabelFound = new ArrayList<String>();
		boolean alreadyCreateOthersCase = false;
			for (int j = 0; j < arrayPerson.length; j++) {
				// check if key found just one time
				boolean otherExist = true;
				for (int i = 0; i < PERSONNSCHART_CATEGORIES_LABEL.length; i++) {
					String key = PERSONNSCHART_CATEGORIES_LABEL[i].getLdapKey();	
					if(key.equals(arrayPerson[j].getVisibleCategory())){
						// clé trouvé - on la rajoute si nécessaire et on passe à la personne suivante
						if(!categoriesLabelFound.contains(PERSONNSCHART_CATEGORIES_LABEL[i].getLabel())){
							OrganizationalUnit newUnit = new OrganizationalUnit(PERSONNSCHART_CATEGORIES_LABEL[i].getLabel(), PERSONNSCHART_CATEGORIES_LABEL[i].getLdapKey());
							categories.add(newUnit);
							categoriesLabelFound.add(PERSONNSCHART_CATEGORIES_LABEL[i].getLabel());
						}
						otherExist = false;
						break;
					}
				}
				if(otherExist && !alreadyCreateOthersCase){
					// on vérifie que la personne ne soit pas centrale
					// si elle l'est -> on la considère trouver et donc pas besoin de créer la catégorie others
					if(arrayPerson[j].isVisibleOnCenter()){
							otherExist = false;
					}	
					if(otherExist){
						// on doit vraiment créer la catégorie "Autres"
						OrganizationalUnit otherUnit = new OrganizationalUnit("Personnel");
						categories.add(otherUnit);
						alreadyCreateOthersCase = true;
					}
				}
			}
		if(categories.size() > 0){
			return categories.toArray(new OrganizationalUnit[categories.size()]);
		}else{
			return null;
		}
	}else{
		return null;
	}
}

  public Map<String, String> getOrganizationalPerson(OrganizationalPerson[] org, int id) {
    for (OrganizationalPerson pers : org) {
      if (pers.getId() == id) {
        Map<String, String> details = pers.getDetail();
        return details;
      }
    }
    return null;
  }

  private OrganizationalPerson createPerson(int id, Attributes attrs, String dn, int type) {
    Attribute cn = attrs.get(LDAP_ATT_NAME);
    Attribute title = attrs.get(LDAP_ATT_TITLE);
    String service = "";
    Attribute ou = attrs.get(LDAP_ATT_UNIT);
    if (ou == null) {
      // on parse le dn pour récupérer l'ou
      String[] ous = dn.split(",");
      if (ous.length > 1) {
        for (int j = 0; j < ous.length; j++) {
          String[] atr = ous[j].split("=");
          if (atr.length > 1 && atr[0].equalsIgnoreCase(LDAP_ATT_UNIT)) {
            service = atr[1];
            break;
          }
        }
      }
    } else
      service = getAttributValue(ou);

    String fonction = getAttributValue(title);
    
    Attribute desc = attrs.get(LDAP_ATT_DESC);
    OrganizationalPerson pers =
        new OrganizationalPerson(id, -1,
            getAttributValue(cn), fonction, getAttributValue(desc), service);
    
    NamingEnumeration<?> attributs = attrs.getAll();
    Map<String, String> details = new HashMap<String, String>();
    
    Map<String, String> attributesToReturn = null;
    if(type == OrganizationalChart.UNITCHART){
    	attributesToReturn = UNITSCHART_KEYSANDLABELOTHERSINFOS;
    }else{
    	attributesToReturn = PERSONNSCHART_KEYSANDLABELOTHERSINFOS;
    }
    
    // get only the attributes defined in the organizationChart parameters
    try {
      while (attributs.hasMore()) {
        Attribute att = (Attribute) attributs.next();
        if (att != null && !att.getID().equalsIgnoreCase("objectClass") &&
            attributesToReturn != null && attributesToReturn.size() > 0 && attributesToReturn.containsKey(att.getID())){
          String detail = "";
          if (att.size() > 1) {
            NamingEnumeration<?> vals = att.getAll();
            while (vals.hasMore()) {
              String val = (String) vals.next();
              detail += val + ", ";
            }
            detail = detail.substring(0, detail.length() - 2);
          } else {
            detail = getAttributValue(att);
          }
          details.put(attributesToReturn.get(att.getID()), detail);
        }
      }
    } catch (NamingException e) {
        SilverTrace.warn("organizationchart", "OrganizationChartLdapServiceImpl.createPerson",
            "organizationChart.ldap.search.error", e);
    }  
    
    pers.setDetail(details);
    
    // defined the boxes with personns inside
    if(type == OrganizationalChart.UNITCHART){
    	defineUnitChartRoles(pers, fonction);
    }else{
    	defineDetailledChartRoles(pers, fonction);
    }

    return pers;
  }
  
  private void defineUnitChartRoles(OrganizationalPerson pers, String function){
     
	 // principle: the left and right have the most complicated labels
	 // so we look for them before the central
	 // ex: left = maire-adjoint
	 //     central = maire
	  
	 boolean finish = false;
	 // right 
	 if(UNITSCHART_RIGHT_LABEL!=null){
	 	 for (OrganizationalRole role : UNITSCHART_RIGHT_LABEL) {
			  if (function != null && role != null && role.getLdapKey() != null && !role.getLdapKey().isEmpty() &&
				  function.toLowerCase().indexOf(role.getLdapKey()) != -1) {
				  	pers.setVisibleOnRight(true);
				  	pers.setVisibleRightLabel(role.getLabel());
				  	finish = true;
				  	break;
			  }
	     }
	 }
	 // left
	 if(!finish){
		 if(UNITSCHART_LEFT_LABEL!=null){
			 for (OrganizationalRole role : UNITSCHART_LEFT_LABEL) {
				  if (function != null && role != null && role.getLdapKey() != null && !role.getLdapKey().isEmpty() &&
					  function.toLowerCase().indexOf(role.getLdapKey()) != -1) {
					  	pers.setVisibleOnLeft(true);
					  	pers.setVisibleLeftLabel(role.getLabel());
					  	finish = true;
					  	break;
				  }
		     }
		 }
	 }
	 if(!finish){
		 // central
		 for (OrganizationalRole role : UNITSCHART_CENTRAL_LABEL) {
			  if (function != null && role != null && role.getLdapKey() != null && !role.getLdapKey().isEmpty() &&
				  function.toLowerCase().indexOf(role.getLdapKey()) != -1) {
				  	pers.setVisibleOnCenter(true);
				  	pers.setVisibleCenterLabel(role.getLabel());
				  	finish = true;
				  	break;
			  }
	     }
	 }
  }
  
  private void defineDetailledChartRoles(OrganizationalPerson pers, String function){
	     boolean finish = false;
		 // central
		 for (OrganizationalRole role : PERSONNSCHART_CENTRAL_LABEL) {
			  if (function != null && role != null && role.getLdapKey() != null && !role.getLdapKey().isEmpty() &&
				  function.toLowerCase().indexOf(role.getLdapKey()) != -1) {
				  	pers.setVisibleOnCenter(true);
				  	pers.setVisibleCenterLabel(role.getLabel());
				  	finish = true;
				  	break;
			  }
	     }
		 // categories 
		 if(!finish){
			 for (OrganizationalRole role : PERSONNSCHART_CATEGORIES_LABEL) {
				  if (function != null && role != null && role.getLdapKey() != null && !role.getLdapKey().isEmpty() &&
					  function.toLowerCase().indexOf(role.getLdapKey().toLowerCase()) != -1) {
					  	pers.setVisibleCategory(role.getLabel());
					  	finish = true;
					  	break;
				  }
		     }
		 }
	  }
  
  
  private String getAttributValue(Attribute att) {
    if (att == null)
      return null;
    try {
      String val = (String) att.get();
      if (val == null || val.isEmpty())
        return null;
      return val;
    } catch (Exception e) {
      return null;
    }
  }

  private boolean isActif(Attributes attrs) {
    if (LDAP_ATT_ACTIF == null)
      return true;
    String actif = getAttributValue(attrs.get(LDAP_ATT_ACTIF));
    if (actif == null || actif.equalsIgnoreCase("false"))
      return true;
    return false;
  }

  private Hashtable<String, String> initEnv(String componentId) {
    SilverTrace.info("organizationchart", "OrganizationChartLdapServiceImpl.initEnv()",
        "root.MSG_GEN_ENTER_METHOD", "componentId=" + componentId);
    Hashtable<String, String> env = new Hashtable<String, String>();
    // initialise connexion ldap
    String jndiURL = controller.getComponentParameterValue(componentId, PARAM_SERVERURL);// "ldap://localhost:389/";
    String initialContextFactory =
        controller.getComponentParameterValue(componentId, PARAM_CTXFACTORY);// "com.sun.jndi.ldap.LdapCtxFactory";
    String authenticationMode = controller.getComponentParameterValue(componentId, PARAM_AUTHMODE);// "simple";
    String principal = controller.getComponentParameterValue(componentId, PARAM_PRINCIPAL);// "cn=Manager,dc=mondomain,dc=com";
    String credentials = controller.getComponentParameterValue(componentId, PARAM_CREDENTIAL);
    String contextReferral = "ignore";

    env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
    env.put(Context.PROVIDER_URL, jndiURL);
    env.put(Context.SECURITY_AUTHENTICATION, authenticationMode);
    env.put(Context.SECURITY_PRINCIPAL, principal);
    env.put(Context.SECURITY_CREDENTIALS, credentials);
    env.put(Context.REFERRAL, contextReferral);

    // initialisation des attribut LDAP pour la recherche et l'affichage
    LDAP_ROOT = controller.getComponentParameterValue(componentId, PARAM_LDAP_ROOT);
    LDAP_CLASS_PERSON = controller.getComponentParameterValue(componentId, PARAM_LDAP_CLASS_PERSON);// "organizationalPerson";
    LDAP_CLASS_UNIT = controller.getComponentParameterValue(componentId, PARAM_LDAP_CLASS_UNIT);// "organizationalUnit";
    LDAP_ATT_UNIT = controller.getComponentParameterValue(componentId, PARAM_LDAP_ATT_UNIT);// "ou";
    LDAP_ATT_NAME = controller.getComponentParameterValue(componentId, PARAM_LDAP_ATT_NAME);// "cn";
    LDAP_ATT_TITLE = controller.getComponentParameterValue(componentId, PARAM_LDAP_ATT_TITLE);// "title";
    LDAP_ATT_DESC = controller.getComponentParameterValue(componentId, PARAM_LDAP_ATT_DESC);// "description";
    
    // unit chart parameters
    String unitsCentral = controller.getComponentParameterValue(componentId, PARAM_UNITSCHART_CENTRAL_LABEL);
    UNITSCHART_CENTRAL_LABEL = getRoles(unitsCentral);
    String unitsRight = controller.getComponentParameterValue(componentId, PARAM_UNITSCHART_RIGHT_LABEL);
    UNITSCHART_RIGHT_LABEL = getRoles(unitsRight);
    String unitsLeft = controller.getComponentParameterValue(componentId, PARAM_UNITSCHART_LEFT_LABEL);
    UNITSCHART_LEFT_LABEL = getRoles(unitsLeft);
    UNITSCHART_KEYSANDLABELOTHERSINFOS = getKeysAndLabel(controller.getComponentParameterValue(componentId, PARAM_UNITSCHART_OTHERSINFOS_KEYS));
    
    // detailled chart parameters
    String personnsCentral = controller.getComponentParameterValue(componentId, PARAM_PERSONNSCHART_CENTRAL_LABEL);
    PERSONNSCHART_CENTRAL_LABEL = getRoles(personnsCentral);
    String personnsCategories = controller.getComponentParameterValue(componentId, PARAM_PERSONNSCHART_CATEGORIES_LABEL);
    PERSONNSCHART_CATEGORIES_LABEL = getRoles(personnsCategories);
    PERSONNSCHART_KEYSANDLABELOTHERSINFOS = getKeysAndLabel(controller.getComponentParameterValue(componentId, PARAM_PERSONNSCHART_OTHERSINFOS_KEYS));
    
    LDAP_ATT_ACTIF = controller.getComponentParameterValue(componentId, PARAM_LDAP_ATT_ACTIF);
    if (LDAP_ATT_ACTIF == null || LDAP_ATT_ACTIF.isEmpty())
      LDAP_ATT_ACTIF = null;
    SilverTrace.info("organizationchart", "OrganizationChartLdapServiceImpl.initEnv()",
        "root.MSG_GEN_EXIT_METHOD");
    return env;
  }
  
  private Map<String, String> getKeysAndLabel(String parameterValue){
	  Map<String, String> others = new HashMap<String, String>();
	  if(parameterValue != null && parameterValue.length() > 0){
		  String[] couples = parameterValue.toLowerCase().split(";");
		  // ex: labelChamp1=keyLdap1;labelChamp2=keyLdap2
		  if(couples!= null && couples.length > 0){
			  for (int i = 0; i < couples.length; i++) {
				  String[] details = couples[i].split("=");
				  others.put(details[1],details[0]);
			  }
		  }
	  }
	  return others;
  }
  
  
  private OrganizationalRole[] getRoles(String parameterValue){
	  OrganizationalRole[] roles = null;
	  if(parameterValue != null && parameterValue.length() > 0){
		  String[] roleCouple = parameterValue.split(";");
		  
		  // ex: labelRole1=keyRole1;labelRole2=keyRole2
		  if(roleCouple!= null && roleCouple.length > 0){
			  roles = new OrganizationalRole[roleCouple.length];
			  for (int i = 0; i < roleCouple.length; i++) {
				String[] roleDetails = roleCouple[i].split("=");
				if(roleDetails.length == 2){
					roles[i] = new OrganizationalRole(roleDetails[0],roleDetails[1]);
				}else{
					SilverTrace.info("organizationchart",
							"OrganizationChartLdapServiceImpl.getRole()", "root.MSG_GEN_PARAM_VALUE",
			            	"bad format for a role couple " + roleCouple[i]);
				}
			  }
		  }
	  }
	  return roles;
  }

  public void setController(OrganizationController controller) {
    this.controller = controller;
  }
  
  
}
