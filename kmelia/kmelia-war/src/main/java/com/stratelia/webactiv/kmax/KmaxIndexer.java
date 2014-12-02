/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.webactiv.kmax;

import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexation;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.publication.control.PublicationService;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.silverpeas.attachment.AttachmentService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
@Named("KmaxComponentIndexation")
public class KmaxIndexer implements ComponentIndexation {

  @Inject
  private AttachmentService attachmentService;
  @Inject
  private PublicationService publicationService;
  @Inject
  private KmeliaBm kmeliaBm;

  @Override
  public void index(ComponentInst componentInst) throws Exception {
    kmeliaBm.indexKmax(componentInst.getId());

    Collection<PublicationDetail> publications =
        publicationService.getAllPublications(new PublicationPK("useless", componentInst.getId()),
            "pubId desc");
    for (PublicationDetail aPublication : publications) {
      attachmentService.indexAllDocuments(aPublication.getPK(), null, null);
    }
  }
}