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
      String idImage1 = FileUploadUtil.getParameter(items, "IdImage1");
      String idImage2 = FileUploadUtil.getParameter(items, "IdImage2");
      String idImage3 = FileUploadUtil.getParameter(items, "IdImage3");
      String removeImageFile1 = FileUploadUtil.getParameter(items, "RemoveImageFile1"); //yes | no
      String removeImageFile2 = FileUploadUtil.getParameter(items, "RemoveImageFile2"); //yes | no
      String removeImageFile3 = FileUploadUtil.getParameter(items, "RemoveImageFile3"); //yes | no
      FileItem fileImage1 = FileUploadUtil.getFile(items, "Image1");
      FileItem fileImage2 = FileUploadUtil.getFile(items, "Image2");
      FileItem fileImage3 = FileUploadUtil.getFile(items, "Image3");

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
      
      //Images
      if(idImage1 == null && fileImage1 != null && StringUtil.isDefined(fileImage1.getName())) {//Create Image
        classifiedsSC.createClassifiedImage(fileImage1, classifiedId);
      } else if(idImage1 != null && fileImage1 != null && StringUtil.isDefined(fileImage1.getName())) {//Update Image
        classifiedsSC.updateClassifiedImage(fileImage1, idImage1, classified.getId());
      } else if(idImage1 != null && fileImage1 != null && ! StringUtil.isDefined(fileImage1.getName()) && "yes".equals(removeImageFile1)) {//Delete Image
        classifiedsSC.deleteClassifiedImage(idImage1);
      }
      
      if(idImage2 == null && fileImage2 != null && StringUtil.isDefined(fileImage2.getName())) {//Create Image
        classifiedsSC.createClassifiedImage(fileImage2, classifiedId);
      } else if(idImage2 != null && fileImage2 != null && StringUtil.isDefined(fileImage2.getName())) {//Update Image
        classifiedsSC.updateClassifiedImage(fileImage2, idImage2, classified.getId());
      } else if(idImage2 != null && fileImage2 != null && ! StringUtil.isDefined(fileImage2.getName()) && "yes".equals(removeImageFile2)) {//Delete Image
        classifiedsSC.deleteClassifiedImage(idImage2);
      }
      
      if(idImage3 == null && fileImage3 != null && StringUtil.isDefined(fileImage3.getName())) {//Create Image
        classifiedsSC.createClassifiedImage(fileImage3, classifiedId);
      } else if(idImage3 != null && fileImage3 != null && StringUtil.isDefined(fileImage3.getName())) {//Update Image
        classifiedsSC.updateClassifiedImage(fileImage3, idImage3, classified.getId());
      } else if(idImage3 != null && fileImage3 != null && ! StringUtil.isDefined(fileImage3.getName()) && "yes".equals(removeImageFile3)) {//Delete Image
        classifiedsSC.deleteClassifiedImage(idImage3);
      }
    }

    return HandlerProvider.getHandler("ViewMyClassifieds").computeDestination(classifiedsSC, request);
  }
}
