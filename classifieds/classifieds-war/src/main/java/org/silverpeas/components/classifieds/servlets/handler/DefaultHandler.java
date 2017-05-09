/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.components.classifieds.servlets.handler;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.components.classifieds.control.ClassifiedsSessionController;
import org.silverpeas.components.classifieds.model.Category;
import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.components.classifieds.servlets.FunctionHandler;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Default use case : show all categories and for each one, list last published adds
 * @author Ludovic Bertin
 */
public class DefaultHandler extends FunctionHandler {

  private static final int NB_ADDS_BY_CATEGORY = 5;

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC, HttpRequest request)
      throws Exception {

    Form formUpdate = null;
    DataRecord data = null;
    String nbTotalClassifieds;

    PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
    if (pubTemplate != null) {
      // Update form
      formUpdate = pubTemplate.getSearchForm();

      // Empty data record
      RecordSet recordSet = pubTemplate.getRecordSet();
      data = recordSet.getEmptyRecord();
    }

    // Stores objects in request
    nbTotalClassifieds = classifiedsSC.getNbTotalClassifieds();
    request.setAttribute("Form", formUpdate);
    request.setAttribute("Data", data);
    request.setAttribute("NbTotal", nbTotalClassifieds);
    request.setAttribute("Validation", classifiedsSC.isValidationEnabled());
    request.setAttribute("wysiwygHeader", classifiedsSC.getWysiwygHeader());

    //Affichage page d'accueil annonces par catégorie
    if (classifiedsSC.isHomePageDisplayCategorized()) {

      Collection<Category> categories = null;

      if (pubTemplate != null) {
        // Template Name
        String templateFileName = pubTemplate.getFileName();
        String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));

        // Category list based on a listbox field
        String field = classifiedsSC.getSearchFields1();
        FieldTemplate fieldTemplate = pubTemplate.getRecordTemplate().getFieldTemplate(field);
        String keys = fieldTemplate.getParameters(classifiedsSC.getLanguage()).get("keys");
        String values = fieldTemplate.getParameters(classifiedsSC.getLanguage()).get("values");
        String label = fieldTemplate.getFieldName();
        categories = createCategory(templateName, label, keys, values, classifiedsSC);
      }
      request.setAttribute("Categories", categories);

      // Returns jsp to redirect to
      return "accueil.jsp";
    } else {
      //Affichage page d'accueil annonces listées
      request.setAttribute("Classifieds", classifiedsSC.getAllValidClassifieds());

      // Returns jsp to redirect to
      return "accueilNotCategorized.jsp";
    }

  }

  /**
   * Build collection of categories filled with the collection of corresponding classifieds.
   * @param templateName XML form template name
   * @param label field label
   * @param stringKeys listbox key list
   * @param stringValues listbox value list
   * @param classifiedsSC Classified Session Controller
   * @return
   */
  private Collection<Category> createCategory(String templateName, String label, String stringKeys,
      String stringValues, ClassifiedsSessionController classifiedsSC) {

    Collection<Category> categories = new ArrayList<>();
    String[] keys = stringKeys.split("##");
    String[] values = stringValues.split("##");

    // Populate categories
    for (int i = 0; i < keys.length; i++) {
      // build Category object
      Category category = new Category(keys[i], values[i]);

      // search for classified inside this category
      QueryDescription query = new QueryDescription();
      query.addFieldQuery(new FieldDescription(templateName + "$$" + label, keys[i], null));
      Collection<ClassifiedDetail> classifieds;
      try {
        classifieds = classifiedsSC.getClassifieds(query, NB_ADDS_BY_CATEGORY);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
        classifieds = new ArrayList<>();
      }
      category.setClassifieds(classifieds);

      // add category to collection
      categories.add(category);
    }

    return categories;
  }
}
