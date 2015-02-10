/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.whitePages.service;

import com.silverpeas.annotation.Service;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.contentManager.GlobalSilverContentI18N;
import com.stratelia.silverpeas.pdc.control.PdcManager;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.search.SearchEngineProvider;
import org.silverpeas.search.indexEngine.model.FieldDescription;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.QueryDescription;
import org.silverpeas.search.searchEngine.model.ScoreComparator;
import org.silverpeas.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class MixedSearchServiceImpl implements MixedSearchService {

  @Inject
  private PdcManager pdcManager = null;

  @Override
  public Collection<GlobalSilverContent> search(String spaceId, String componentId, String userId,
      String queryString, SearchContext pdcContext, Map<String, String> xmlFields,
      String xmlTemplate, List<FieldDescription> fieldsQuery, String language) throws Exception {
    //build the search
    QueryDescription query = new QueryDescription(queryString);

    //Set the identity of the user who processing the search
    query.setSearchingUser(userId);

    //Set the list of all components which are available for the user
    query.addSpaceComponentPair(spaceId, componentId);

    List<String> alSilverContentIds = new ArrayList<>();
    ArrayList<GlobalSilverContent> silverContents = new ArrayList<>();

    if (pdcContext != null && !pdcContext.isEmpty()) {
      SilverTrace
          .info("searchEngine", "MixedSearchServiceImpl.search()", "root.MSG_GEN_PARAM_VALUE",
              "pdc search start !");
      //the pdc context is not empty. We have to search all silvercontentIds according to query
      // settings

      ArrayList<String> alComponentIds = new ArrayList<>();
      alComponentIds.add(componentId);

      List<GlobalSilverContent> alSilverContents =
          pdcManager.findGlobalSilverContents(pdcContext, alComponentIds, true, true);

      if (queryString != null && queryString.length() > 0) {
        //extract the silvercontent ids
        for (GlobalSilverContent silverContent : alSilverContents) {
          alSilverContentIds.add(silverContent.getId());
        }
      } else {
        // no econd request necessary -> return directy the contents
        for (GlobalSilverContent silverContent : alSilverContents) {
          silverContents.add(getTranslatedGlobalSilverContent(silverContent, language));
        }
        return silverContents;
      }
      SilverTrace
          .info("searchEngine", "MixedSearchServiceImpl.search()", "root.MSG_GEN_PARAM_VALUE",
              "pdc search done !");
    }

    // XML search
    if (xmlFields != null && !xmlFields.isEmpty() && xmlTemplate != null) {
      Map<String, String> newXmlQuery = new HashMap<>();

      Set<String> keys = xmlFields.keySet();
      for (final String key : keys) {
        String value = xmlFields.get(key);
        value = value.trim().replaceAll("##", " AND ");
        newXmlQuery.put(xmlTemplate + "$$" + key, value);

        SilverTrace
            .info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE",
                "newXmlQuery.put(" + xmlTemplate + "$$" + key + "," + value + ")");
      }

      query.setXmlQuery(newXmlQuery);
    }

    // LDAP and classicals Silverpeas fields search
    if (fieldsQuery != null && fieldsQuery.size() > 0) {
      query.setFieldQueries(fieldsQuery);
    }

    List<MatchingIndexEntry> result = null;
    if (StringUtil.isDefined(query.getQuery()) || query.getXmlQuery() != null ||
        query.getMultiFieldQuery() != null) {


      SilverTrace
          .info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE",
              "search processed !");

      //retrieve results
      List<MatchingIndexEntry> fullTextResult =
          SearchEngineProvider.getSearchEngine().search(query).
              getEntries();
      SilverTrace
          .info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE",
              "results retrieved !");

      if (pdcContext != null && !pdcContext.isEmpty()) {
        // We retain only objects which are presents in the both search result list
        result = mixedSearch(fullTextResult, alSilverContentIds);
        SilverTrace
            .info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE",
                "both searches have been mixed !");
      } else {
        result = fullTextResult;
        SilverTrace
            .info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE",
                "results are full text results !");
      }
    }

    if (result != null && !result.isEmpty()) {
      //get each result according to result's list
      GlobalSilverContent silverContent;
      LinkedList<String> returnedObjects = new LinkedList<>();
      for (MatchingIndexEntry mie : result) {
        SilverTrace
            .info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE",
                "mie.getTitle() = " + mie.getTitle() + ", mie.getObjectType() = " +
                    mie.getObjectType());
        if (mie.getTitle().endsWith("wysiwyg.txt")) {
          //we don't show it as result.
        } else {
          //Added by NEY - 22/01/2004
          //Some explanations to lines below
          //If a publication have got the word "truck" in its title and an associated wysiwyg
          // which content the same word
          //The search engine will return 2 same lines (One for the publication and the other for
          // the wysiwyg)
          //Following lines filters one and only one line. The choice between both lines is not
          // important.
          if ("Wysiwyg".equals(mie.getObjectType())) {
            //We must search if the eventual associated Publication have not been already added
            // to the result
            String objectIdAndObjectType =
                mie.getObjectId() + "&&Publication&&" + mie.getComponent();
            if (returnedObjects.contains(objectIdAndObjectType)) {
              //the Publication have already been added
              continue;
            } else {
              objectIdAndObjectType = mie.getObjectId() + "&&Wysiwyg&&" + mie.getComponent();
              returnedObjects.add(objectIdAndObjectType);
            }
          } else if ("Publication".equals(mie.getObjectType())) {
            //We must search if the eventual associated Wysiwyg have not been already added to
            // the result
            String objectIdAndObjectType = mie.getObjectId() + "&&Wysiwyg&&" + mie.getComponent();
            if (returnedObjects.contains(objectIdAndObjectType)) {
              //the Wysiwyg have already been added
              continue;
            } else {
              objectIdAndObjectType = mie.getObjectId() + "&&Publication&&" + mie.getComponent();
              returnedObjects.add(objectIdAndObjectType);
            }
          }

          silverContent = matchingIndexEntry2SilverContent(mie, language);
          if (silverContent != null) {
            silverContents.add(silverContent);
          }
        }
      }
      SilverTrace
          .info("searchEngine", "SearchEngineTagUtil.getResults()", "root.MSG_GEN_PARAM_VALUE",
              "results transformed !");
    }

    return silverContents;
  }

  private GlobalSilverContent matchingIndexEntry2SilverContent(MatchingIndexEntry mie,
      String language) throws Exception {
    SilverTrace.info("searchEngine", "MixedSearchServiceImpl.matchingIndexEntry2SilverContent()",
        "root.MSG_GEN_PARAM_VALUE", "mie = " + mie.toString());
    GlobalSilverContent silverContent;
    silverContent =
        new GlobalSilverContent(mie.getTitle(language), mie.getPreview(language), mie.getObjectId(),
            null, mie.getComponent(), mie.getCreationDate(), mie.getCreationUser());
    silverContent.setScore(mie.getScore());
    silverContent.setType(mie.getObjectType());
    return silverContent;
  }

  /**
   * Get translated Publication in current site lang or lang as parameter
   * @param gsc
   * @param language
   * @return GlobalSilverContent
   */
  public GlobalSilverContent getTranslatedGlobalSilverContent(GlobalSilverContent gsc,
      String language) {
    if (StringUtil.isDefined(language)) {
      GlobalSilverContentI18N gsci18n = gsc.getTranslation(language);
      if (gsci18n != null) {
        gsc.setTitle(gsci18n.getName());
      }
    }
    return gsc;
  }

  private List<MatchingIndexEntry> mixedSearch(List<MatchingIndexEntry> ie, List<String> objectIds)
      throws Exception {
    SilverTrace
        .info("searchEngine", "MixedSearchServiceImpl.mixedSearch()", "root.MSG_GEN_PARAM_VALUE",
            "objectIds = " + objectIds.toString());

    // la liste basicSearchList ne contient maintenant que les silverContentIds des documents
    // trouvés mais ces documents sont également dans le tableau résultat de la recherche classique
    // il faut donc créer un tableau de MatchingIndexEntry pour afficher le resultat
    ArrayList<MatchingIndexEntry> result = new ArrayList<>();
    for (String objectId : objectIds) {
      MatchingIndexEntry mie = getMatchingIndexEntry(ie, objectId);
      if (mie != null) {
        result.add(mie);
        SilverTrace.info("searchEngine", "MixedSearchServiceImpl.mixedSearch()",
            "root.MSG_GEN_PARAM_VALUE", "common objectId = " + mie.getObjectId());
      }
    }
    Collections.sort(result, ScoreComparator.comparator);
    return result;
  }

  /**
   * Dans un tableau de MatchingIndexEntry, on recherche l'objet MatchingIndexEntry qui a comme
   * objectId l'internalContentId
   */
  private MatchingIndexEntry getMatchingIndexEntry(List<MatchingIndexEntry> ie,
      String internalContentId) throws Exception {
    for (MatchingIndexEntry entry : ie) {
      // on parcourt le tableau résultats de la recherche classique
      // et on retourne le MatchingIndexEntry correspondant à l'internalContentId
      if ((entry.getObjectId()).equals(internalContentId)) {
        return entry;
      }
    }
    return null;
  }
}
