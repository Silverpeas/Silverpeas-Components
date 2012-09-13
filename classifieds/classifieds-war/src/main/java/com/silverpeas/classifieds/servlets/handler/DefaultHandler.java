package com.silverpeas.classifieds.servlets.handler;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.Category;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import org.silverpeas.search.searchEngine.model.QueryDescription;
import org.silverpeas.search.indexEngine.model.FieldDescription;

/**
 * Default use case : show all categories and for each one, list last published adds
 * @author Ludovic Bertin
 */
public class DefaultHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    Collection<Category> categories = null;
    Form formUpdate = null;
    DataRecord data = null;

    PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
    try {
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

        // Update form
        formUpdate = pubTemplate.getSearchForm();

        // Empty data record
        RecordSet recordSet = pubTemplate.getRecordSet();
        data = recordSet.getEmptyRecord();
      }

      // Stores objects in request
      request.setAttribute("Form", formUpdate);
      request.setAttribute("Data", data);
      request.setAttribute("NbTotal", classifiedsSC.getNbTotalClassifieds());
      request.setAttribute("Validation", classifiedsSC.isValidationEnabled());
      request.setAttribute("Categories", categories);
      request.setAttribute("wysiwygHeader", classifiedsSC.getWysiwygHeader());

      // Returns jsp to redirect to
      return "accueil.jsp";
    } catch (Exception e) {
      // form error
      request.setAttribute("ErrorType", "labelErrorForm");
      return "error.jsp";
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
  private Collection<Category> createCategory(String templateName,
      String label,
      String stringKeys,
      String stringValues,
      ClassifiedsSessionController classifiedsSC) {

    Collection<Category> categories = new ArrayList<Category>();
    String[] keys = stringKeys.split("##");
    String[] values = stringValues.split("##");

    // Populate categories
    for (int i = 0; i < keys.length; i++) {
      // build Category object
      Category category = new Category(keys[i], values[i]);

      // search for classified inside this category
      QueryDescription query = new QueryDescription();
      query.addFieldQuery(new FieldDescription(templateName + "$$" + label, keys[i], null));
      Collection<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
      try {
        classifieds = classifiedsSC.search(query);
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
