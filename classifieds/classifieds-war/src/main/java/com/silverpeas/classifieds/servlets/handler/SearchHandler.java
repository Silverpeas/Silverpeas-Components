package com.silverpeas.classifieds.servlets.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.form.XmlSearchForm;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.indexEngine.model.FieldDescription;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class SearchHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    // Parse request to retrieve search parameters
    if (FileUploadUtil.isRequestMultipart(request)) {

      List<FileItem> items = FileUploadUtil.parseRequest(request);
      QueryDescription query = new QueryDescription();

      DataRecord data = null;
      XmlSearchForm searchForm = null;
      PublicationTemplateImpl template = (PublicationTemplateImpl) getPublicationTemplate(classifiedsSC);
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

        // Build query
        String[] fieldNames = searchTemplate.getFieldNames();
        for (String fieldName : fieldNames) {
          Field field = data.getField(fieldName);
          String fieldValue = field.getStringValue();
          if (fieldValue != null && fieldValue.trim().length() > 0) {
            String fieldQuery = fieldValue.trim().replaceAll("##", " AND "); // multiple checkbox
            query.addFieldQuery(new FieldDescription(templateName + "$$" + fieldName,
                fieldQuery,
                null));
          }
        }
      }

      // Performs search
      Collection<ClassifiedDetail> classifieds;
      try {
        classifieds = classifiedsSC.search(query);
      } catch (Exception e) {
        classifieds = new ArrayList<ClassifiedDetail>();
      }

      // Stores objects in request
      request.setAttribute("Form", searchForm);
      request.setAttribute("Data", data);
      request.setAttribute("NbTotal", (classifiedsSC.getNbTotalClassifieds()));
      request.setAttribute("Classifieds", classifieds);
    }
    // Returns jsp to redirect to
    return "classifiedsResult.jsp";
  }

}
