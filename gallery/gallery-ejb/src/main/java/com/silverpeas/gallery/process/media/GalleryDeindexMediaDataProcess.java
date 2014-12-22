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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.gallery.process.media;

import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.process.session.ProcessSession;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;

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
   * @see
   * com.silverpeas.gallery.process.AbstractGalleryDataProcess#processData(com.silverpeas.gallery
   * .process.GalleryProcessExecutionContext, org.silverpeas.process.session.ProcessSession)
   */
  @Override
  protected void processData(final GalleryProcessExecutionContext context,
      final ProcessSession session) throws Exception {
    // Nothing to do
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.AbstractProcess#onSuccessful()
   */
  @Override
  public void onSuccessful() throws Exception {
    super.onSuccessful();

    SilverTrace.info("gallery", "GalleryDeindexMediaDataProcess.onSuccessful()",
        "root.MSG_GEN_ENTER_METHOD", "MediaPK = " + getMedia().getMediaPK().toString());

    final IndexEntryPK indexEntry = new IndexEntryPK(getMedia().getMediaPK().getComponentName(),
        getMedia().getContributionType(), getMedia().getMediaPK().getId());

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }
}
