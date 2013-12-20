package com.silverpeas.classifieds.servlets.handler;

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.search.indexEngine.model.FieldDescription;
import org.silverpeas.search.searchEngine.model.QueryDescription;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.control.SearchContext;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.form.XmlSearchForm;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import org.silverpeas.servlet.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class SearchHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception {

      QueryDescription query = buildQuery(classifiedsSC, request);
      
      GraphicElementFactory gef = (GraphicElementFactory) request.getSession().getAttribute(
          GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      classifiedsSC.setPagination(gef.getPagination());

      // Performs search
      classifiedsSC.search(query);
      
      // Display first page
      request.setAttribute("Index", "0");

      return HandlerProvider.getHandler("Pagination").computeDestination(classifiedsSC, request);
  }
  
  private QueryDescription buildQuery(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws PublicationTemplateException, FormException {
    // Parse request to retrieve search parameters
    if (request.isContentInMultipart()) {
      List<FileItem> items = request.getFileItems();
      QueryDescription query = new QueryDescription();

      DataRecord data = null;
      XmlSearchForm searchForm = null;
      PublicationTemplateImpl template = (PublicationTemplateImpl) classifiedsSC.getPublicationTemplate();
      if (template != null) {
        // Template Name
        String templateFileName = template.getFileName();
        String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));

        // Build search data record and fill it with search parameters
        RecordTemplate searchTemplate = template.getSearchTemplate();
        data = searchTemplate.getEmptyRecord();
        PagesContext context = new PagesContext("XMLSearchForm", "2", classifiedsSC.getLanguage(), classifiedsSC.getUserId());
        searchForm = (XmlSearchForm) template.getSearchForm();
        searchForm.update(items, data, context);
        
        classifiedsSC.setSearchContext(new SearchContext(searchForm, data));

        // Build query
        String[] fieldNames = searchTemplate.getFieldNames();
        for (String fieldName : fieldNames) {
          Field field = data.getField(fieldName);
          String fieldValue = field.getStringValue();
          if (fieldValue != null && fieldValue.trim().length() > 0) {
            String fieldQuery = fieldValue.trim().replaceAll("##", " AND "); // multiple checkbox
            query.addFieldQuery(new FieldDescription(templateName + "$$" + fieldName, fieldQuery,
                null));
          }
        }
        
        return query;
      }
    }
    return null;
  }

}