/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.classifieds.servlets.handler;

import org.silverpeas.components.classifieds.control.ClassifiedsSessionController;
import org.silverpeas.components.classifieds.model.Category;
import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.components.classifieds.servlets.FunctionHandler;
import org.silverpeas.components.classifieds.servlets.SubscriptionField;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default use case : show all categories and for each one, list last published adds
 *
 * @author Ludovic Bertin
 */
public class DefaultHandler extends FunctionHandler {

  private static final int NB_ADDS_BY_CATEGORY = 5;

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      @NonNull HttpRequest request) throws Exception {

    boolean portletMode = request.getParameterAsBoolean("PortletMode");

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
    request.setAttribute("InstanceSettings", classifiedsSC.getInstanceSettings());
    request.setAttribute("Form", formUpdate);
    request.setAttribute("Data", data);
    request.setAttribute("NbTotal", nbTotalClassifieds);
    request.setAttribute("Validation", classifiedsSC.isValidationEnabled());

    //Affichage page d'accueil annonces par catégorie
    if (classifiedsSC.isHomePageDisplayCategorized() && !portletMode) {

      Collection<Category> categories = null;

      if (pubTemplate != null) {
        // subscription fieds
        var fields = getSubscriptionFields(pubTemplate, request.getUserLanguage());
        request.setAttribute("Fields", fields);

        // Template Name
        String templateFileName = pubTemplate.getFileName();
        String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));

        // Category list based on a listbox field
        String field = classifiedsSC.getSearchFields1();
        FieldTemplate fieldTemplate = pubTemplate.getRecordTemplate().getFieldTemplate(field);
        categories = createCategory(templateName, fieldTemplate, classifiedsSC);
      }
      request.setAttribute("Categories", categories);

      // Returns jsp to redirect to
      return "accueil.jsp";
    } else {
      //Affichage page d'accueil annonces listées
      List<SubscriptionField> fields = pubTemplate != null ?
          getSubscriptionFields(pubTemplate, request.getUserLanguage()) : List.of();
      request.setAttribute("Fields", fields);
      request.setAttribute("Classifieds", classifiedsSC.getAllValidClassifieds());

      // Returns jsp to redirect to
      return "accueilNotCategorized.jsp";
    }

  }

  /**
   * Build collection of categories from the definition of the different values of the specified
   * field template. Each value definition of the field will serve as a category.
   *
   * @param templateName XML form template name
   * @param fieldTemplate the template of the field to use to build the different categories of
   * the classifieds.
   * @param classifiedsSC Classified Session Controller
   * @return a collection with the classifieds categories
   */
  @NonNull
  private Collection<Category> createCategory(String templateName, FieldTemplate fieldTemplate,
      ClassifiedsSessionController classifiedsSC) {
    List<Category> categories = new ArrayList<>();
    fieldTemplate.getFieldValuesTemplate(classifiedsSC.getLanguage())
        .apply(v -> {
          Category category = new Category(v.getKey(), v.getLabel());
          var classifieds =
              searchCategorizedClassifieds(category.getKey(), templateName,
                  fieldTemplate.getFieldName(), classifiedsSC);
          category.setClassifieds(classifieds);
          categories.add(category);
        });
    return categories;
  }

  @NonNull
  private Collection<ClassifiedDetail> searchCategorizedClassifieds(String category,
      String templateName, String fieldName, ClassifiedsSessionController classifiedsSC) {
    // search for classified inside this category
    QueryDescription query = new QueryDescription();
    query.addFieldQuery(new FieldDescription(templateName + "$$" + fieldName, category, null));
    Collection<ClassifiedDetail> classifieds;
    try {
      classifieds = classifiedsSC.getClassifieds(query, NB_ADDS_BY_CATEGORY);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      classifieds = new ArrayList<>();
    }
    return classifieds;
  }
}
