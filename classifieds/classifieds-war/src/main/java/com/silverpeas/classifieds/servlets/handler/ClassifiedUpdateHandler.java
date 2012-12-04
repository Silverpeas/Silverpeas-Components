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
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.webactiv.SilverpeasRole;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedUpdateHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    ClassifiedsRole highestRole = (isAnonymousAccess(request)) ? ClassifiedsRole.ANONYMOUS : ClassifiedsRole.getRole(classifiedsSC.getUserRoles());

    if (FileUploadUtil.isRequestMultipart(request)) {
      // Retrieves parameters
      List<FileItem> items = FileUploadUtil.parseRequest(request);
      String title = FileUploadUtil.getParameter(items, "Title");
      String classifiedId = FileUploadUtil.getParameter(items, "ClassifiedId");
      String description = FileUploadUtil.getParameter(items, "Description");
      String price = FileUploadUtil.getParameter(items, "Price");
      String removeImageFile = FileUploadUtil.getParameter(items, "removeImageFile"); //yes | no
      FileItem fileImage = FileUploadUtil.getFile(items, "Image");

      ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);
      classified.setTitle(title);
      classified.setDescription(description);
      if (price != null && ! price.isEmpty()) {
        classified.setPrice(Integer.parseInt(price));
      } else {
        classified.setPrice(null);
      }
     
      // Populate data record
      PublicationTemplate pub = getPublicationTemplate(classifiedsSC);
      if (pub != null) {
        RecordSet set = pub.getRecordSet();
        Form form = pub.getUpdateForm();
        DataRecord data = set.getRecord(classifiedId);
        if (data == null) {
          data = set.getEmptyRecord();
          data.setId(classifiedId);
        }
        PagesContext context = new PagesContext("myForm", "0", classifiedsSC.getLanguage(),
            false, classifiedsSC.getComponentId(), classifiedsSC.getUserId());
        context.setObjectId(classifiedId);
        // mise à jour des données saisies
        form.update(items, data, context);
        set.save(data);
      }
      //Update classified
      classifiedsSC.updateClassified(classified, true, SilverpeasRole.admin.isInRole(highestRole.getName()));
      request.setAttribute("ClassifiedId", classifiedId);
      
      //Image
      if (fileImage != null && StringUtil.isDefined(fileImage.getName())) {//Update image
        //classifiedsSC.updateClassifiedImage(fileImage, classifiedId);
      } else if ("yes".equals(removeImageFile)) {//Remove image
         
      }
    }

    return HandlerProvider.getHandler("ViewMyClassifieds").computeDestination(classifiedsSC, request);
  }
}
