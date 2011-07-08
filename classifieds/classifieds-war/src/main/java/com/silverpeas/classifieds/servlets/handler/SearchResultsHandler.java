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
public class SearchResultsHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    // retrieves parameters
    String id = request.getParameter("Id");
    String type = request.getParameter("Type");

    try {
      // Classified
      if (type.equals("Classified")) {
        request.setAttribute("ClassifiedId", id);
        return HandlerProvider.getHandler("ViewClassified").computeDestination(classifiedsSC,
            request);
      }

      // Comment
      else if (type.startsWith("Comment")) {
        request.setAttribute("ClassifiedId", id);
        return HandlerProvider.getHandler("Comments").computeDestination(classifiedsSC, request);
      }

      // Default : Main page
      else {
        return HandlerProvider.getHandler("Main").computeDestination(classifiedsSC, request);
      }
    } catch (Exception e) {
      request.setAttribute("ComponentId", classifiedsSC.getComponentId());
      return "/admin/jsp/documentNotFound.jsp";
    }
  }

}
