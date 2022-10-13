/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.servlets.ajax;

import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.servlets.ajax.handlers.*;

import javax.servlet.http.HttpServletRequest;

public enum AjaxOperation {

  Delete(new DeleteHandler(), true),
  GetProfile(new GetProfileHandler(), true),
  SortTopics(new SortTopicsHandler(), true),
  EmptyTrash(new EmptyTrashHandler(), true),
  UpdateTopicStatus(new UpdateTopicStatusHandler(), true),
  GetTopicWysiwyg(new GetTopicWysiwygHandler(), true),
  Rename(new RenameTopicHandler(), true),
  bindToPub(new BindToPubliHandler(), false),
  unbindToPub(new UnbindToPubliHandler(), false),
  Paste(new PasteHandler(), true),
  MovePublication(new MovePublicationHandler(), true),
  DeletePublications(new DeletePublicationsHandler(), true),
  CopyPublications(new CopyPublicationsHandler(), true),
  CutPublications(new CutPublicationsHandler(), true),
  GetClipboardState(new GetClipboardStateHandler(), true),
  GetPublicationAuthorizations(new GetPublicationAuthorizationsHandler(), true),
  SELECTALLPUBLICATIONS(new SelectAllPublicationsHandler(), true);

  private AjaxHandler handler;
  private boolean controllerRequired;

  AjaxOperation(AjaxHandler handler, boolean controllerRequired) {
    this.handler = handler;
    this.controllerRequired = controllerRequired;
  }

  public boolean requiresController() {
    return this.controllerRequired;
  }

  public String handleRequest(HttpServletRequest request, KmeliaSessionController controller) {
    return this.handler.handleRequest(request, controller);
  }
}
