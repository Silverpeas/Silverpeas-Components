package com.silverpeas.classifieds.servlets.handler;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.indexEngine.model.FieldDescription;

/**
 * Use Case : for all users, show all adds of given category
 *
 * @author Ludovic Bertin
 *
 */
public class ClassifiedsListByCategoryHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception{

    // retrieve requested category
    String fieldKey = request.getParameter("FieldKey");
    String categoryName = request.getParameter("CategoryName");

    // builds classifieds collection
    Collection<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();

    // As search is based on XML Forms, publication template is needed
    PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
    if (pubTemplate != null) {
      // Template Name
      String templateFileName = pubTemplate.getFileName();
      String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));

      // Field to add criteria on
      String field = classifiedsSC.getSearchFields1();
      String label = pubTemplate.getRecordTemplate().getFieldTemplate(field).getFieldName();

      // Performs search
      QueryDescription query = new QueryDescription();
      query.addFieldQuery(new FieldDescription(templateName + "$$" + label, fieldKey, null));
      try {
        classifieds = classifiedsSC.search(query);
      } catch (Exception e) {
        classifieds = new ArrayList<ClassifiedDetail>();
      }
    }

    // if no template found, returns an empty list
    else {
      classifieds = new ArrayList<ClassifiedDetail>();
    }

    // Stores objects in request
    request.setAttribute("Classifieds", classifieds);
    request.setAttribute("TitlePath", "classifieds.viewByCategorie");
    request.setAttribute("Extra", categoryName);

    // Returns jsp to redirect to
    return "classifieds.jsp";
   }

}

