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
      
      // Create classified
      ClassifiedDetail classified = new ClassifiedDetail(title, description);
      if (price != null && ! price.isEmpty()) {
        classified.setPrice(Integer.parseInt(price));
      }
      String classifiedId = classifiedsSC.createClassified(classified, highestRole);
      
      //Create Image
      if (fileImage != null && StringUtil.isDefined(fileImage.getName())) {
        classified = classifiedsSC.getClassified(classifiedId);
        String imageSubDirectory = classifiedsSC.getResources().getSetting("imagesSubDirectory");
        String fullFileName = fileImage.getName();
        String fileName = fullFileName.substring(
                          fullFileName.lastIndexOf(File.separator) + 1,
                          fullFileName.length());
        String type = null;
        if (fileName.lastIndexOf(".") != -1) {
          type = fileName.substring(fileName.lastIndexOf(".") + 1,
              fileName.length());
        }
        
        String physicalName = new Long(new Date().getTime()).toString()
            + "." + type;
        
        String mimeType = AttachmentController.getMimeType(fileName);

        //save picture file in the fileServer
        String filePath = FileRepositoryManager
            .getAbsolutePath(classified.getComponentInstanceId())
            + imageSubDirectory + File.separator + physicalName;
        File file = new File(filePath);
        if (!file.exists()) {
          FileFolderManager.createFolder(file.getParentFile());
          file.createNewFile();
        }
        fileImage.write(file);
        
        //Object Image
        Image classifiedImage = new Image(Integer.parseInt(classifiedId), physicalName, mimeType);
     
        //save the picture in the data base
        classifiedsSC.createClassifiedImage(classifiedImage);
        
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
