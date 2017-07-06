/*
 * Copyright (C) 2000 - 2017 Silverpeas
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

package org.silverpeas.components.classifieds.servlets.handler;

import java.util.ArrayList;
import java.util.Collection;
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
import org.silverpeas.core.web.http.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedCreationHandler extends FunctionHandler {

  private static final int NB_IMAGES = 4;

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC, HttpRequest request)
      throws Exception {

    ClassifiedsRole highestRole = isAnonymousAccess(request) ? ClassifiedsRole.ANONYMOUS :
        ClassifiedsRole.getRole(classifiedsSC.getUserRoles());

    if (request.isContentInMultipart()) {
      // Retrieves parameters from the multipart stream
      List<FileItem> items = request.getFileItems();
      String title = request.getParameter("Title");
      String description = request.getParameter("Description");
      String price = request.getParameter("Price");

      //Classified
      ClassifiedDetail classified = new ClassifiedDetail(title, description);
      if (price != null && !price.isEmpty()) {
        classified.setPrice(Integer.parseInt(price));
      }

      //Images of the classified
      Collection<FileItem> listImage = new ArrayList<>();
      for (int i = 1; i <= NB_IMAGES; i++) {
        FileItem fileImage = request.getFile("Image"+i);
        if (fileImage != null && StringUtil.isDefined(fileImage.getName())) {
          listImage.add(fileImage);
        }
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