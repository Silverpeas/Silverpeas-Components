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

package com.silverpeas.classifieds.servlets.handler;

import java.util.ArrayList;
import java.util.Collection;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.Category;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import org.silverpeas.util.StringUtil;

import org.silverpeas.search.searchEngine.model.QueryDescription;
import org.silverpeas.search.indexEngine.model.FieldDescription;
import org.silverpeas.servlet.HttpRequest;

/**
 * Default use case : show all categories and for each one, list last published adds
 * @author Ludovic Bertin
 */
public class DefaultHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC, HttpRequest request)
      throws Exception {

    Form formUpdate = null;
    DataRecord data = null;
    String nbTotalClassifieds;

    PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
    try {
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

    } catch (Exception e) {
      // form error
      request.setAttribute("ErrorType",
          classifiedsSC.getResources().getString("classifieds.labelErrorForm"));
      return "error.jsp";
    }

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

    } else { //Affichage page d'accueil annonces listées

      String currentPage = request.getParameter("CurrentPage");
      if (!StringUtil.isDefined(currentPage)) {
        currentPage = "0";
      }
      int currentPageInt = Integer.parseInt(currentPage);
      classifiedsSC.setCurrentPage(currentPageInt);

      request.setAttribute("CurrentPage", currentPage);
      request.setAttribute("NbPages", classifiedsSC.getNbPages(nbTotalClassifieds));
      request.setAttribute("Classifieds", classifiedsSC.getAllValidClassifieds(currentPageInt));

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
        classifieds = classifiedsSC.getClassifieds(query, 5);
      } catch (Exception e) {
        classifieds = new ArrayList<ClassifiedDetail>();
      }
      category.setClassifieds(classifieds);

      // add category to collection
      categories.add(category);
    }

    return categories;
  }
}
