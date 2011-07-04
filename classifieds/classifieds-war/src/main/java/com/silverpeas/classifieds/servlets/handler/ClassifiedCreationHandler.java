package com.silverpeas.classifieds.servlets.handler;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.classifieds.control.ClassifiedsRole;
import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.util.web.servlet.FileUploadUtil;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedCreationHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    ClassifiedsRole highestRole = (isAnonymousAccess(request)) ? ClassifiedsRole.ANONYMOUS : ClassifiedsRole.getRole(classifiedsSC.getUserRoles());

    if (FileUploadUtil.isRequestMultipart(request)) {
      // Retrieves parameters
      List<FileItem> items = FileUploadUtil.parseRequest(request);
      String title = FileUploadUtil.getParameter(items, "Title");

      // Create classified
      ClassifiedDetail classified = new ClassifiedDetail(title);
      String classifiedId = classifiedsSC.createClassified(classified, highestRole);

      PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
      if (pubTemplate != null) {
        // populate data record
        RecordSet set = pubTemplate.getRecordSet();
        Form form = pubTemplate.getUpdateForm();
        DataRecord data = set.getRecord(classifiedId);
        if (data == null) {
          data = set.getEmptyRecord();
          data.setId(classifiedId);
        }
        PagesContext context = new PagesContext("myForm", "0", classifiedsSC.getLanguage(),
            false, classifiedsSC.getComponentId(), classifiedsSC.getUserId());
        context.setObjectId(classifiedId);

        // save data record
        form.update(items, data, context);
        set.save(data);
        classifiedsSC.updateClassified(classified, false, false);
      }
    }

    return HandlerProvider.getHandler("ViewMyClassifieds").computeDestination(classifiedsSC, request);
  }
}
