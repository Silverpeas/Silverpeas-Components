/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import org.silverpeas.util.StringUtil;
import org.silverpeas.servlet.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedCreationHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC, HttpRequest request)
      throws Exception {

    ClassifiedsRole highestRole = (isAnonymousAccess(request)) ? ClassifiedsRole.ANONYMOUS :
        ClassifiedsRole.getRole(classifiedsSC.getUserRoles());

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
      if (price != null && !price.isEmpty()) {
        classified.setPrice(Integer.parseInt(price));
      }

      //Images of the classified
      Collection<FileItem> listImage = new ArrayList<>();
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
        PagesContext context = new PagesContext("myForm", "0", classifiedsSC.getLanguage(), false,
            classifiedsSC.getComponentId(), classifiedsSC.getUserId());
        context.setObjectId(classifiedId);

        // save data record
        form.update(items, data, context);
        set.save(data);
        classifiedsSC.updateClassified(classified, false, false);
      }
    }

    return HandlerProvider.getHandler("ViewMyClassifieds")
        .computeDestination(classifiedsSC, request);
  }
}
