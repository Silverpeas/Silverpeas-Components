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

package com.silverpeas.whitePages.service;

import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.silverpeas.pdc.ejb.PdcBm;
import com.silverpeas.pdc.ejb.PdcBmHome;
import com.silverpeas.pdc.ejb.PdcBmRuntimeException;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.contentManager.GlobalSilverContentI18N;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.searchEngine.model.ScoreComparator;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FieldDescription;


public class MixedSearchServiceImpl implements MixedSearchService {
  
  private PdcBm pdcBm = null;
  private SearchEngineBm searchEngine = null;

  public Collection<GlobalSilverContent> search(String spaceId, String componentId, String userId, 
                           String queryString, // standard search
                           SearchContext pdcContext, // PDC filter
                           Hashtable<String,String> xmlFields, String xmlTemplate, // xml fields filter
                           List<FieldDescription> fieldsQuery, // ldap and silverpeas fields filter
                           String language) throws Exception
  {
    //build the search
    QueryDescription query = new QueryDescription(queryString);

    //Set the identity of the user who processing the search
    query.setSearchingUser(userId);

    //Set the list of all components which are available for the user
    query.addSpaceComponentPair(spaceId, componentId);

    List<String> alSilverContentIds = new ArrayList<String>();
    ArrayList<GlobalSilverContent> silverContents = new ArrayList<GlobalSilverContent>();

    if (pdcContext != null && !pdcContext.isEmpty()) {
      SilverTrace.info("searchEngine", "MixedSearchServiceImpl.search()", "root.MSG_GEN_PARAM_VALUE", "pdc search start !");
      //the pdc context is not empty. We have to search all silvercontentIds according to query settings
      
      ArrayList<String> alComponentIds = new ArrayList<String>();
      alComponentIds.add(componentId);
      
      boolean visibilitySensitive = true;
      List alSilverContents = getPdcBm().findGlobalSilverContents(pdcContext, alComponentIds, true, visibilitySensitive);
      
      GlobalSilverContent silverContent = null;
      if (queryString != null && queryString.length() > 0)
      {
        //extract the silvercontent ids
        for (int sc=0; sc<alSilverContents.size(); sc++)
        {
          silverContent = (GlobalSilverContent) alSilverContents.get(sc);
          alSilverContentIds.add(silverContent.getId());
        }
      }
      else
      {
        // no econd request necessary -> return directy the contents
        for (int sc=0; sc<alSilverContents.size(); sc++)
        {
          silverContent = (GlobalSilverContent) alSilverContents.get(sc);
          silverContents.add(getTranslatedGlobalSilverContent(silverContent, language));
        }
        return silverContents;
      }
      SilverTrace.info("searchEngine", "MixedSearchServiceImpl.search()", "root.MSG_GEN_PARAM_VALUE", "pdc search done !");
    }

    // XML search
    if (xmlFields != null && !xmlFields.isEmpty() && xmlTemplate != null)
    {
      Hashtable<String,String> newXmlQuery = new Hashtable<String,String>();

      Set<String> keys = xmlFields.keySet();
      Iterator<String> i = keys.iterator();
      String key = null;
      String value = null;
      while (i.hasNext())
      {
        key = (String) i.next();
        value = (String) xmlFields.get(key);
        value = value.trim().replaceAll("##", " AND ");
        newXmlQuery.put(xmlTemplate+"$$"+key, value);

        SilverTrace.info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE", "newXmlQuery.put("+xmlTemplate+"$$"+key+","+value+")");
      }

      query.setXmlQuery(newXmlQuery);
    }

    // LDAP and classicals Silverpeas fields search
    if (fieldsQuery != null && fieldsQuery.size() > 0)
    {
      query.setFieldQueries(fieldsQuery);
    }

    MatchingIndexEntry[] result = null;
    if (StringUtil.isDefined(query.getQuery()) || query.getXmlQuery() != null || query.getMultiFieldQuery() != null)
    {
      //launch the full text search
      try
      {
        getSearchEngineBm().search(query);
      } catch (NoSuchObjectException nsoe) {
        // reference to EJB Session statefull is expired
        // getting a new one...
        searchEngine = null;
        // re-launching the search
        getSearchEngineBm().search(query);
      }
      catch (Exception e)
      {
        SilverTrace.warn("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE", "search method failed with query = "+query+" probably due to a parse exception !");
        return silverContents;
      }

      SilverTrace.info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE", "search processed !");

      //retrieve results
      MatchingIndexEntry[] fullTextResult = getSearchEngineBm().getRange(0, getSearchEngineBm().getResultLength());
      SilverTrace.info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE", "results retrieved !");

      if (pdcContext != null && !pdcContext.isEmpty()) {
        // We retain only objects which are presents in the both search result list
        result = mixedSearch(fullTextResult, alSilverContentIds);
        SilverTrace.info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE", "both searches have been mixed !");
      } else {
        result = fullTextResult;
        SilverTrace.info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE", "results are full text results !");
      }
    }

    if (result != null)
    {
      //get each result according to result's list
      MatchingIndexEntry  mie       = null;
      GlobalSilverContent silverContent = null;
      LinkedList<String>      returnedObjects = new LinkedList<String>();
      for (int r=0; r<result.length; r++)
      {
        mie = result[r];
        SilverTrace.info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE", "mie.getTitle() = "+mie.getTitle()+", mie.getObjectType() = "+mie.getObjectType());
        if (mie.getTitle().endsWith("wysiwyg.txt"))
        {
          //we don't show it as result.
        }
        else
        {
          //Added by NEY - 22/01/2004
          //Some explanations to lines below
          //If a publication have got the word "truck" in its title and an associated wysiwyg which content the same word
          //The search engine will return 2 same lines (One for the publication and the other for the wysiwyg)
          //Following lines filters one and only one line. The choice between both lines is not important.
          if ("Wysiwyg".equals(mie.getObjectType()))
          {
            //We must search if the eventual associated Publication have not been already added to the result
            String objectIdAndObjectType = mie.getObjectId()+"&&Publication&&"+mie.getComponent();
            if (returnedObjects.contains(objectIdAndObjectType))
            {
              //the Publication have already been added
              continue;
            } else {
              objectIdAndObjectType = mie.getObjectId()+"&&Wysiwyg&&"+mie.getComponent();
              returnedObjects.add(objectIdAndObjectType);
            }
          }
          else if ("Publication".equals(mie.getObjectType()))
          {
            //We must search if the eventual associated Wysiwyg have not been already added to the result
            String objectIdAndObjectType = mie.getObjectId()+"&&Wysiwyg&&"+mie.getComponent();
            if (returnedObjects.contains(objectIdAndObjectType))
            {
              //the Wysiwyg have already been added
              continue;
            } else {
              objectIdAndObjectType = mie.getObjectId()+"&&Publication&&"+mie.getComponent();
              returnedObjects.add(objectIdAndObjectType);
            }
          }

          silverContent = matchingIndexEntry2SilverContent(mie, language);
          if (silverContent != null)
            silverContents.add(silverContent);
        }
      }
      SilverTrace.info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE", "results transformed !");
    }

    return silverContents;
  }
  
  private PdcBm getPdcBm() {
    if (pdcBm == null) {
      try {
        PdcBmHome pdcBmHome = ((PdcBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.PDCBM_EJBHOME, PdcBmHome.class));
        pdcBm = pdcBmHome.create();
      } catch (Exception e) {
        throw new PdcBmRuntimeException("MixedSearchServiceImpl.getPdcBm", SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
      }
    }
    return pdcBm;
  }
  
  private SearchEngineBm getSearchEngineBm() throws PdcException {
    if (searchEngine == null) {
      try {
        SearchEngineBmHome home = (SearchEngineBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.SEARCHBM_EJBHOME,
            SearchEngineBmHome.class);
        searchEngine = home.create();
      } catch (Exception e) {
        throw new PdcException(
            "MixedSearchServiceImpl.getSearchEngineBm()",
            SilverpeasException.ERROR, "pdcPeas.EX_CANT_GET_SEARCH_ENGINE", e);
      }
    }
    return searchEngine;
  }
  
  private GlobalSilverContent matchingIndexEntry2SilverContent(MatchingIndexEntry mie, String language) throws Exception
  {
    SilverTrace.info("searchEngine", "MixedSearchServiceImpl.matchingIndexEntry2SilverContent()", "root.MSG_GEN_PARAM_VALUE", "mie = "+mie.toString());
    GlobalSilverContent silverContent = null;
    if (mie != null)
    {
      silverContent = new GlobalSilverContent(mie.getTitle(language), mie.getPreview(language), mie.getObjectId(), null, mie.getComponent(), mie.getCreationDate(), mie.getCreationUser());
      silverContent.setScore(mie.getScore());
      silverContent.setType(mie.getObjectType());
    }
    return silverContent;
  }
  
  /**
   * Get translated Publication in current site lang or lang as parameter
   * @param gsc
   * @param language
   * @return GlobalSilverContent
   */
  public GlobalSilverContent getTranslatedGlobalSilverContent(GlobalSilverContent gsc, String language)
  {
    if (StringUtil.isDefined(language))
    {
      GlobalSilverContentI18N gsci18n = (GlobalSilverContentI18N) gsc.getTranslation(language);
      if (gsci18n != null)
        gsc.setTitle(gsci18n.getName());
    }
    return gsc;
  }
  
  private MatchingIndexEntry[] mixedSearch(MatchingIndexEntry[] ie, List<String> objectIds) throws Exception {
    SilverTrace.info("searchEngine", "MixedSearchServiceImpl.mixedSearch()", "root.MSG_GEN_PARAM_VALUE", "objectIds = "+objectIds.toString());

    // la liste basicSearchList ne contient maintenant que les silverContentIds des documents trouvés
    // mais ces documents sont également dans le tableau résultat de la recherche classique
    // il faut donc créer un tableau de MatchingIndexEntry pour afficher le resultat
    ArrayList<MatchingIndexEntry> result = new ArrayList<MatchingIndexEntry>();

    String        objectId  = null;
    MatchingIndexEntry  mie     = null;
    for (int i=0; i<objectIds.size(); i++)
    {
      objectId = (String) objectIds.get(i);
      mie = getMatchingIndexEntry(ie, objectId);
      if (mie != null) {
        result.add(mie);
        SilverTrace.info("searchEngine", "MixedSearchServiceImpl.mixedSearch()", "root.MSG_GEN_PARAM_VALUE", "common objectId = "+mie.getObjectId());
      }
    }

    Collections.sort(result, ScoreComparator.comparator);
        return (MatchingIndexEntry[]) result.toArray(new MatchingIndexEntry[0]);
    }
  
  /**
   * Dans un tableau de MatchingIndexEntry, on recherche l'objet MatchingIndexEntry
   * qui a comme objectId l'internalContentId
   */
  private MatchingIndexEntry getMatchingIndexEntry(MatchingIndexEntry[] ie, String internalContentId) throws Exception {
      MatchingIndexEntry res = null;
      for (int i = 0; i < ie.length; i++) {
          // on parcourt le tableau résultats de la recherche classique
          // et on retourne le MatchingIndexEntry correspondant à l'internalContentId
          if ((ie[i].getObjectId()).equals(internalContentId)) {
              res = ie[i];
              break;
          }
      }
      return res;
  }
  
}
