package com.silverpeas.classifieds.servlets.handler;

import java.util.ArrayList;
import java.util.Collection;
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
import org.silverpeas.servlet.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedCreationHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception {

    ClassifiedsRole highestRole = (isAnonymousAccess(request)) ? ClassifiedsRole.ANONYMOUS : ClassifiedsRole.getRole(classifiedsSC.getUserRoles());


    if (request.isContentInMultipart()) {
      // Retrieves parameters from the multipart stream
      List<FileItem> items = request.getFileItems();
      String title = request.getParameter("Title");
      String description = request.getParameter("Description");
      String price = request.getParameter("Price");
      FileItem fileImage1 = request.getFile("Image1");
      FileItem fileImage2 = request.getFile("Image2");
      FileItem fileImage3 = request.getFile("Image3");
      FileItem fileImage4 = request.getFile("Image4");

      //Classified
      ClassifiedDetail classified = new ClassifiedDetail(title, description);
      if (price != null && ! price.isEmpty()) {
        classified.setPrice(Integer.parseInt(price));
      }
      
      //Images of the classified
      Collection<FileItem> listImage = new ArrayList<FileItem>();
      if (fileImage1 != null && StringUtil.isDefined(fileImage1.getName())) {
        listImage.add(fileImage1);
      }
      if (fileImage2 != null && StringUtil.isDefined(fileImage2.getName())) {
        listImage.add(fileImage2);
      }
      if (fileImage3 != null && StringUtil.isDefined(fileImage3.getName())) {
        listImage.add(fileImage3);
      }
      if (fileImage4 != null && StringUtil.isDefined(fileImage4.getName())) {
        listImage.add(fileImage4);
      }
      String classifiedId = classifiedsSC.createClassified(classified, listImage, highestRole);
      
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
