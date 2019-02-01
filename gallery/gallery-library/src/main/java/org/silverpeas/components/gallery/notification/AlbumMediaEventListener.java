/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.notification;

import org.silverpeas.components.gallery.model.AlbumMedia;
import org.silverpeas.components.gallery.notification.user.AlbumMediaNotificationManager;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

/**
 * A listener about some lifecycle events triggered by the Media engine.
 * @author silveryocha
 */
public class AlbumMediaEventListener extends CDIResourceEventListener<AlbumMediaEvent> {

  @Override
  public void onCreation(final AlbumMediaEvent event) {
    final AlbumMedia albumMedia = event.getTransition().getAfter();
    final User modifier = getModifier(albumMedia);
    AlbumMediaNotificationManager.get().putCreationOf(albumMedia, modifier);
  }

  @Override
  public void onDeletion(final AlbumMediaEvent event) {
    final AlbumMedia albumMedia = event.getTransition().getBefore();
    final User modifier = getModifier(albumMedia);
    AlbumMediaNotificationManager.get().putDeletionOf(albumMedia, modifier);
  }

  private User getModifier(final AlbumMedia albumMedia) {
    User sender = User.getCurrentRequester();
    if (sender == null) {
      sender = albumMedia.getMedia().getLastUpdater();
    }
    return sender;
  }
}
