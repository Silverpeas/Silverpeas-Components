package com.silverpeas.classifieds.servlets.handler;

import java.util.List;

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
import com.stratelia.webactiv.SilverpeasRole;
import org.silverpeas.servlet.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedUpdateHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception {

    ClassifiedsRole highestRole = (isAnonymousAccess(request)) ? ClassifiedsRole.ANONYMOUS : ClassifiedsRole.getRole(classifiedsSC.getUserRoles());

    if (request.isContentInMultipart()) {
      // Retrieves parameters
      List<FileItem> items = request.getFileItems();
      String title = request.getParameter("Title");
      String classifiedId = request.getParameter("ClassifiedId");
      String description = request.getParameter("Description");
      String price = request.getParameter("Price");
      String idImage1 = request.getParameter("IdImage1");
      String idImage2 = request.getParameter("IdImage2");
      String idImage3 = request.getParameter("IdImage3");
      String idImage4 = request.getParameter("IdImage4");
      String removeImageFile1 = request.getParameter("RemoveImageFile1"); //yes | no
      String removeImageFile2 = request.getParameter("RemoveImageFile2"); //yes | no
      String removeImageFile3 = request.getParameter("RemoveImageFile3"); //yes | no
      String removeImageFile4 = request.getParameter("RemoveImageFile4"); //yes | no
      FileItem fileImage1 = request.getFile("Image1");
      FileItem fileImage2 = request.getFile("Image2");
      FileItem fileImage3 = request.getFile("Image3");
      FileItem fileImage4 = request.getFile("Image4");

      ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);
      classified.setTitle(title);
      classified.setDescription(description);
      if (price != null && ! price.isEmpty()) {
        classified.setPrice(Integer.parseInt(price));
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
      
      if(idImage4 == null && fileImage4 != null && StringUtil.isDefined(fileImage4.getName())) {//Create Image
        classifiedsSC.createClassifiedImage(fileImage4, classifiedId);
      } else if(idImage4 != null && fileImage4 != null && StringUtil.isDefined(fileImage4.getName())) {//Update Image
        classifiedsSC.updateClassifiedImage(fileImage4, idImage4, classified.getId());
      } else if(idImage4 != null && fileImage4 != null && ! StringUtil.isDefined(fileImage4.getName()) && "yes".equals(removeImageFile4)) {//Delete Image
        classifiedsSC.deleteClassifiedImage(idImage4);
      }
    }

    return HandlerProvider.getHandler("ViewMyClassifieds").computeDestination(classifiedsSC, request);
  }
}
