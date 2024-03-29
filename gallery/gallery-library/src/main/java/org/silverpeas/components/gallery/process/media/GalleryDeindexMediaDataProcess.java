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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.gallery.process.media;

import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.process.AbstractGalleryDataProcess;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;

public class GalleryDeindexMediaDataProcess extends AbstractGalleryDataProcess {

  /**
   * Process to deindex a media in Database
   * @param media
   * @return
   */
  public static GalleryDeindexMediaDataProcess getInstance(final Media media) {
    return new GalleryDeindexMediaDataProcess(media);
  }

  /**
   * Default hidden conctructor
   * @param media
   */
  protected GalleryDeindexMediaDataProcess(final Media media) {
    super(media);
  }

  /*
   * (non-Javadoc)
   *
   * AbstractGalleryDataProcess#processData(com.silverpeas.gallery
   * .process.GalleryProcessExecutionContext, ProcessSession)
   */
  @Override
  protected void processData(final ProcessExecutionContext context, final ProcessSession session)
      throws Exception {
    // Nothing to do
  }

  /*
   * (non-Javadoc)
   * @see AbstractProcess#onSuccessful()
   */
  @Override
  public void onSuccessful() throws Exception {
    super.onSuccessful();



    final IndexEntryKey indexEntry = new IndexEntryKey(getMedia().getMediaPK().getComponentName(),
        getMedia().getContributionType(), getMedia().getMediaPK().getId());

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }
}
