package com.silverpeas.classifieds.servlets.handler;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.classifieds.control.ClassifiedsRole;
import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.Image;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.thumbnail.ThumbnailRuntimeException;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

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
      String description = FileUploadUtil.getParameter(items, "Description");
      String price = FileUploadUtil.getParameter(items, "Price");
      FileItem fileImage = FileUploadUtil.getFile(items, "Image");
      
      //Create classified
      ClassifiedDetail classified = new ClassifiedDetail(title, description);
      if (price != null && ! price.isEmpty()) {
        classified.setPrice(Integer.parseInt(price));
      }
      String classifiedId = classifiedsSC.createClassified(classified, highestRole);
      
      //Create image
      if (fileImage != null && StringUtil.isDefined(fileImage.getName())) {
        classifiedsSC.createClassifiedImage(fileImage, classifiedId);
      }

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
