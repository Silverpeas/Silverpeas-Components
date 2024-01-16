/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.classifieds.servlets.handler;

import java.util.List;

import org.apache.commons.fileupload.FileItem;

import org.silverpeas.components.classifieds.control.ClassifiedsRole;
import org.silverpeas.components.classifieds.control.ClassifiedsSessionController;
import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.components.classifieds.servlets.FunctionHandler;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.web.http.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedUpdateHandler extends FunctionHandler {

  private static final int NB_IMAGES = 4;

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC, HttpRequest request)
      throws Exception {

    ClassifiedsRole highestRole = isAnonymousAccess(request) ? ClassifiedsRole.ANONYMOUS :
        ClassifiedsRole.getRole(classifiedsSC.getUserRoles());

    if (request.isContentInMultipart()) {
      // Retrieves parameters
      List<FileItem> items = request.getFileItems();
      String title = request.getParameter("Title");
      String classifiedId = request.getParameter("ClassifiedId");
      String description = request.getParameter("Description");
      String price = request.getParameter("Price");
      boolean publish = request.getParameterAsBoolean("Publish");

      ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);
      classified.setTitle(title);
      classified.setDescription(description);
      if (price != null && !price.isEmpty()) {
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
        PagesContext context = new PagesContext("myForm", "0", classifiedsSC.getLanguage(), false,
            classifiedsSC.getComponentId(), classifiedsSC.getUserId());
        context.setObjectId(classifiedId);
        // mise à jour des données saisies
        form.update(items, data, context);
        set.save(data);
      }
      //Update classified
      classifiedsSC
          .updateClassified(classified, true, SilverpeasRole.admin.isInRole(highestRole.getName()),
              publish);
      request.setAttribute("ClassifiedId", classifiedId);

      //Images
      processImages(classified, classifiedsSC, request);
    }

    return HandlerProvider.getHandler("ViewClassified")
        .computeDestination(classifiedsSC, request);
  }

  private void processImages(ClassifiedDetail classified,
      ClassifiedsSessionController classifiedsSC, HttpRequest request) {
    for (int i = 1; i <= NB_IMAGES; i++) {
      String idImage = request.getParameter("IdImage"+i);
      boolean removeImageFile = request.getParameterAsBoolean("RemoveImageFile"+i);
      FileItem fileImage = request.getFile("Image"+i);
      processImage(idImage, removeImageFile, fileImage, classified.getId(), classifiedsSC);
    }
  }

  private void processImage(String imageId, boolean removeIt, FileItem fileImage,
      String classifiedId, ClassifiedsSessionController classifiedsSC) {
    if (fileImage != null) {
      if (imageId == null && StringUtil.isDefined(fileImage.getName())) {
        classifiedsSC.createClassifiedImage(fileImage, classifiedId);
      } else if (imageId != null && StringUtil.isDefined(fileImage.getName())) {
        classifiedsSC.updateClassifiedImage(fileImage, imageId, classifiedId);
      } else if (imageId != null && !StringUtil.isDefined(fileImage.getName()) && removeIt) {
        classifiedsSC.deleteClassifiedImage(imageId);
      }
    }
  }
}
