/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
import java.util.EnumSet;
import java.util.Set;

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

  /**
   * The operations reading something and which can therefore be answered to a GET. Every other
   * operation is taken as writing something, so that adding one to this enumeration doesn't expose
   * it by mistake.
   */
  private static final Set<AjaxOperation> READ_ONLY =
      EnumSet.of(GetProfile, GetTopicWysiwyg, GetClipboardState, GetPublicationAuthorizations);

  private AjaxHandler handler;
  private boolean controllerRequired;

  AjaxOperation(AjaxHandler handler, boolean controllerRequired) {
    this.handler = handler;
    this.controllerRequired = controllerRequired;
  }

  public boolean requiresController() {
    return this.controllerRequired;
  }

  /**
   * Does this operation write something, and has therefore to be requested by POST? The
   * synchronizer token is required on a POST whatever its URL, whereas it is required on a GET
   * only when its URL holds one of a few keywords, which no URL of this servlet holds.
   * @return true if the operation writes something, false if it only reads.
   */
  public boolean isWriting() {
    return !READ_ONLY.contains(this);
  }

  public String handleRequest(HttpServletRequest request, KmeliaSessionController controller) {
    return this.handler.handleRequest(request, controller);
  }
}
