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
package org.silverpeas.components.gallery;

import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaCriteria;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.core.silverstatistics.volume.service.ComponentStatisticsProvider;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.silverpeas.components.gallery.constant.MediaType.*;
import static org.silverpeas.core.util.file.FileRepositoryManager.getAbsolutePath;

@Singleton
@Named("gallery" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class GalleryStatistics implements ComponentStatisticsProvider {

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId) {
    Collection<Media> media = getGalleryService().getAllMedia(componentId,
        MediaCriteria.VISIBILITY.FORCE_GET_ALL);
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<>(media.size());
    for (Media aMedia : media) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(aMedia.getCreatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }
    return myArrayList;
  }

  @Override
  public long memorySizeOfSpecificFiles(final String componentId) {
    final GalleryFileSizeCounter fileSizeCounter = new GalleryFileSizeCounter();
    long result = 0L;
    try {
      Files.walkFileTree(Paths.get(getAbsolutePath(componentId)), fileSizeCounter);
      result = fileSizeCounter.getSize();
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return result;
  }

  @Override
  public long countSpecificFiles(final String componentId) {
    final GalleryFileCounter fileCounter = new GalleryFileCounter();
    long result = 0L;
    try {
      Files.walkFileTree(Paths.get(getAbsolutePath(componentId)), fileCounter);
      result = fileCounter.getCount();
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return result;
  }

  private GalleryService getGalleryService() {
    return ServiceProvider.getService(GalleryService.class);
  }

  private class GalleryFileCounter extends GallerySpecificFileVisitor {

    private long count = 0L;

    private GalleryFileCounter() {
      super(false);
    }

    @Override
    protected void handleDirectory(final Path file, final BasicFileAttributes attrs) {
      count++;
    }

    @Override
    protected void handleFile(final Path file, final BasicFileAttributes attrs) {
      // handleDirectory is used here
    }

    long getCount() {
      return count;
    }
  }

  private class GalleryFileSizeCounter extends GallerySpecificFileVisitor {

    private long size = 0L;

    private GalleryFileSizeCounter() {
      super(true);
    }

    @Override
    protected void handleDirectory(final Path file, final BasicFileAttributes attrs) {
      // handleFile is used here
    }

    @Override
    protected void handleFile(final Path file, final BasicFileAttributes attrs) {
      size += attrs.size();
    }

    long getSize() {
      return size;
    }
  }

  private abstract static class GallerySpecificFileVisitor implements FileVisitor<Path> {

    private static final String PHOTO_PREFIX = Photo.getTechnicalFolder();
    private static final String VIDEO_PREFIX = Video.getTechnicalFolder();
    private static final String SOUND_PREFIX = Sound.getTechnicalFolder();
    private boolean firstAccess = true;
    private final FileVisitResult preVisitDirectoryResult;

    GallerySpecificFileVisitor(final boolean visitFiles) {
      this.preVisitDirectoryResult = visitFiles
          ? FileVisitResult.CONTINUE
          : FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public final FileVisitResult preVisitDirectory(final Path dir,
        final BasicFileAttributes attrs) {
      FileVisitResult result = preVisitDirectoryResult;
      if (isSpecificDirectory(dir)) {
        handleDirectory(dir, attrs);
      } else if (!firstAccess) {
        result = FileVisitResult.SKIP_SUBTREE;
      }
      if (firstAccess) {
        firstAccess = false;
        return FileVisitResult.CONTINUE;
      } else {
        return result;
      }
    }

    private boolean isSpecificDirectory(final Path dir) {
      final String fileName = dir.getFileName().toString();
      return fileName.startsWith(PHOTO_PREFIX) || fileName.startsWith(VIDEO_PREFIX) ||
          fileName.startsWith(SOUND_PREFIX);
    }

    @Override
    public final FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
      handleFile(file, attrs);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public final FileVisitResult visitFileFailed(final Path file, final IOException exc) {
      SilverLogger.getLogger(this).warn(exc);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public final FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
      return FileVisitResult.CONTINUE;
    }

    protected abstract void handleDirectory(final Path file, final BasicFileAttributes attrs);

    protected abstract void handleFile(final Path file, final BasicFileAttributes attrs);
  }
}
