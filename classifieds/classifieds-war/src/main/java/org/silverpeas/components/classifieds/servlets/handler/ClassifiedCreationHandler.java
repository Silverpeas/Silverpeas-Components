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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.classifieds.control.ClassifiedsRole;
import org.silverpeas.components.classifieds.control.ClassifiedsSessionController;
import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.components.classifieds.servlets.FunctionHandler;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
      boolean publish = request.getParameterAsBoolean("Publish");

      //Classified
      ClassifiedDetail classified = new ClassifiedDetail(title, description);
      if (StringUtil.isDefined(price)) {
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

      String classifiedId =
          classifiedsSC.createClassified(classified, listImage, highestRole, publish);

      PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
      if (pubTemplate != null) {
        // populate data record
        setDataRecord(classifiedsSC, pubTemplate, classifiedId, items);
        classifiedsSC.updateClassified(classified, false, false, false);
      }
    }

    return HandlerProvider.getHandler("ViewMyClassifieds")
        .computeDestination(classifiedsSC, request);
  }
}