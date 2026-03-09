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
package org.silverpeas.components.gallery;

import jakarta.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.silverpeas.components.gallery.constant.MediaMimeType;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.media.DrewMediaMetadataExtractor;
import org.silverpeas.components.gallery.media.MediaMetadataException;
import org.silverpeas.components.gallery.media.MediaMetadataExtractor;
import org.silverpeas.components.gallery.model.*;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.io.media.image.ImageTool;
import org.silverpeas.core.io.media.image.option.*;
import org.silverpeas.core.io.media.video.VideoThumbnailExtractor;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.core.process.io.file.HandledFile;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.file.FileItem;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.filefilter.FileFilterUtils.*;
import static org.silverpeas.components.gallery.constant.MediaResolution.*;
import static org.silverpeas.core.io.media.image.ImageInfoType.HEIGHT_IN_PIXEL;
import static org.silverpeas.core.io.media.image.ImageInfoType.WIDTH_IN_PIXEL;
import static org.silverpeas.core.io.media.image.ImageToolDirective.GEOMETRY_SHRINK;
import static org.silverpeas.core.io.media.image.ImageToolDirective.PREVIEW_WORK;
import static org.silverpeas.core.io.media.video.ThumbnailPeriod.VIDEO_THUMBNAIL_FILE_EXTENSION;
import static org.silverpeas.core.io.media.video.ThumbnailPeriod.VIDEO_THUMBNAIL_FILE_PREFIX;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

@Service
public class MediaProcessor {

  @Inject
  private ImageTool imageTool;

  @Inject
  private VideoThumbnailExtractor thumbnailExtractor;

  private static void pasteFile(final HandledFile fromFile, final HandledFile toFile,
      final boolean cut) {
    if (fromFile.exists()) {
      try {
        if (cut) {
          fromFile.moveFile(toFile);
        } else {
          fromFile.copyFile(toFile);
        }
      } catch (final Exception e) {
        SilverLogger.getLogger(MediaProcessor.class).error(
            "Unable to copy file : fromImage = " + fromFile.getFile().getPath() + ", toImage = " +
                toFile.getFile().getPath(), e);
      }
    }
  }

  /**
   * Gets a handled file.
   *
   * @param fileHandler the current session file handler
   * @param media the original media file to get.
   * @return the handled file
   */
  private static HandledFile getHandledFile(FileHandler fileHandler, InternalMedia media) {
    if (StringUtil.isNotDefined(media.getFileName())) {
      throw new IllegalArgumentException("media.getFilename() must return a defined name");
    }
    return fileHandler.getHandledFile(Media.BASE_PATH, media.getInstanceId(),
        media.getWorkspaceSubFolderName(), media.getFileName());
  }

  public static MediaProcessor get() {
    return ServiceProvider.getService(MediaProcessor.class);
  }

  /**
   * Saves uploaded sound file on file system
   *
   * @param fileHandler the current session file handler
   * @param sound the current sound media
   * @param fileItem the current uploaded sound
   * @throws Exception if the process fails.
   */
  public synchronized void processSound(final FileHandler fileHandler, Sound sound,
      final FileItem fileItem) throws Exception {
    if (fileItem != null) {
      String name = fileItem.getFileName();
      if (name != null) {
        try {
          sound.setFileName(StringUtil.normalize(FileUtil.getFilename(name)));
          final HandledFile handledSoundFile = getHandledFile(fileHandler, sound);
          handledSoundFile.copyInputStreamToFile(fileItem.getInputStream());
          new SoundProcess(handledSoundFile, sound).process();
        } finally {
          fileItem.delete();
        }
      }
    }
  }

  /**
   * Saves uploaded sound file on file system (In case of drag And Drop upload)
   *
   * @param fileHandler the current session file handler
   * @param sound the current sound media
   * @param uploadedFile the current uploaded sound
   * @throws Exception if an error occurs while saving the sound file.
   */
  public synchronized void processSound(final FileHandler fileHandler, Sound sound,
      final File uploadedFile) throws Exception {
    if (uploadedFile != null) {
      try {
        sound.setFileName(StringUtil.normalize(uploadedFile.getName()));
        final HandledFile handledSoundFile = getHandledFile(fileHandler, sound);
        fileHandler.copyFile(uploadedFile, handledSoundFile);
        new SoundProcess(handledSoundFile, sound).process();
      } finally {
        FileUtils.deleteQuietly(uploadedFile);
      }
    }
  }

  /**
   * Saves uploaded video file on file system and generates a thumbnail for the video.
   *
   * @param fileHandler the current session file handler
   * @param video the current video media
   * @param fileItem the current uploaded video
   * @throws Exception if an error occurs while saving the video file.
   */
  public synchronized void processVideo(final FileHandler fileHandler, Video video,
      final FileItem fileItem) throws Exception {
    if (fileItem != null) {
      String name = fileItem.getFileName();
      if (name != null) {
        try {
          video.setFileName(StringUtil.normalize(FileUtil.getFilename(name)));
          final HandledFile handledVideoFile = getHandledFile(fileHandler, video);
          handledVideoFile.copyInputStreamToFile(fileItem.getInputStream());
          new VideoProcess(handledVideoFile, video, thumbnailExtractor).process();
        } finally {
          fileItem.delete();
        }
      }
    }
  }

  /**
   * Saves uploaded video file on file system (In case of drag And Drop upload)
   *
   * @param fileHandler the current session file handler
   * @param video the current video media
   * @param uploadedFile the current uploaded video
   * @throws Exception if an error occurs while saving the video file.
   */
  public synchronized void processVideo(final FileHandler fileHandler, Video video,
      final File uploadedFile) throws Exception {
    if (uploadedFile != null) {
      try {
        video.setFileName(StringUtil.normalize(uploadedFile.getName()));
        final HandledFile handledVideoFile = getHandledFile(fileHandler, video);
        fileHandler.copyFile(uploadedFile, handledVideoFile);
        new VideoProcess(handledVideoFile, video, thumbnailExtractor).process();
      } finally {
        FileUtils.deleteQuietly(uploadedFile);
      }
    }
  }

  /**
   * Saves uploaded photo file on file system with associated thumbnails and watermarks.
   *
   * @param fileHandler the current session file handler
   * @param photo the photo media
   * @param image the image to register
   * @param watermark if watermark must be handled, the data are represented by this
   * {@link Watermark} instance.
   * @throws Exception on technical error.
   */
  public synchronized void processPhoto(final FileHandler fileHandler, final Photo photo,
      final FileItem image, final Watermark watermark) throws Exception {
    if (image != null) {
      String name = image.getFileName();
      if (name != null) {
        try {
          photo.setFileName(StringUtil.normalize(image.getFileName()));
          final HandledFile handledImageFile = getHandledFile(fileHandler, photo);
          handledImageFile.copyInputStreamToFile(image.getInputStream());
          new PhotoProcess(handledImageFile, photo, watermark, imageTool).process();
        } finally {
          image.delete();
        }
      }
    }
  }

  /**
   * Saves uploaded photo file on file system with associated thumbnails and watermarks. (In case of
   * drag And Drop upload)
   *
   * @param fileHandler the current session file handler
   * @param photo the photo media
   * @param image the image to register
   * @param watermark if watermark must be handled, the data are represented by this
   * {@link Watermark} instance.
   * @throws Exception on technical error.
   */
  public synchronized void processPhoto(final FileHandler fileHandler, final Photo photo,
      final File image, final Watermark watermark) throws Exception {
    if (image != null) {
      try {
        photo.setFileName(StringUtil.normalize(image.getName()));
        final HandledFile handledImageFile = getHandledFile(fileHandler, photo);
        fileHandler.copyFile(image, handledImageFile);
        new PhotoProcess(handledImageFile, photo, watermark, imageTool).process();
      } finally {
        FileUtils.deleteQuietly(image);
      }
    }
  }

  /**
   * Pastes media from a source to a destination.
   *
   * @param fileHandler the file handler (space quota management).
   * @param fromPK the source.
   * @param media the destination.
   * @param cut true if it is a cut operation, false if it is a copy one.
   */
  public synchronized void pasteInternalMedia(final FileHandler fileHandler,
      final MediaPK fromPK, final InternalMedia media, final boolean cut) {
    InternalMedia fromMedia = media.getType().newInstance();
    Objects.requireNonNull(fromMedia);
    fromMedia.setMediaPK(fromPK);
    fromMedia.setFileName(media.getFileName());
    final HandledFile fromDir = getHandledFile(fileHandler, fromMedia).getParentHandledFile();
    final HandledFile toDir = getHandledFile(fileHandler, media).getParentHandledFile();

    // Copy and rename all media that exist into source folder
    if (fromDir.exists()) {

      // Copy all files which the name starts with the media identifier
      Collection<HandledFile> srcFiles =
          fromDir.listFiles(
              or(
                  prefixFileFilter(fromPK.getId()),
                  asFileFilter(file -> file.getName().matches(
                      "^" + VIDEO_THUMBNAIL_FILE_PREFIX + "[0-9]+" +
                          VIDEO_THUMBNAIL_FILE_EXTENSION + "$"))
              ),
              falseFileFilter());
      int substringIndex = fromPK.getId().length();
      for (HandledFile srcFile : srcFiles) {
        String srcFileName = srcFile.getFile().getName();
        String dstFileName = (srcFileName.startsWith(fromPK.getId())) ?
            (media.getId() + srcFileName.substring(substringIndex)) : srcFileName;
        pasteFile(srcFile, toDir.getHandledFile(dstFileName), cut);
      }

      // Copy original image
      pasteFile(fromDir.getHandledFile(media.getFileName()), toDir.getHandledFile(media.
          getFileName()), cut);

      // On cut operation, deleting the source repo
      if (cut && !fromPK.getInstanceId().equals(media.getInstanceId())) {
        try {
          fromDir.delete();
        } catch (Exception e) {
          SilverLogger.getLogger(MediaProcessor.class).error(
              "Unable to delete source folder : folder path = " + fromDir.getFile().getPath(), e);
        }
      }
    }
  }

  /**
   * In charge of processing an internal media.
   *
   * @param <M>
   */
  private abstract static class MediaProcess<M extends InternalMedia> {

    private final HandledFile handledFile;
    private final M media;
    private final Set<MediaMimeType> supportedMimeTypes;

    private MediaMimeType physicalFileMimeType = null;
    private org.silverpeas.core.io.media.MetaData physicalFileMetaData = null;

    private MediaProcess(final HandledFile handledFile, final M media) {
      this.handledFile = handledFile;
      this.media = media;
      this.supportedMimeTypes = MediaMimeType.getSupportedMimeTypes(media.getType());
    }

    /**
     * Processes the media files.
     *
     * @throws Exception if the processing fails.
     */
    public void process() throws Exception {
      setInternalMetadata();
      generateFiles();
    }

    /**
     * Generates specific media files.
     */
    protected abstract void generateFiles();

    /**
     * Sets the internal metadata.
     *
     * @throws Exception if no supported mime type.
     */
    private void setInternalMetadata() throws Exception {
      File fileForData = getHandledFile().getFile();
      MediaMimeType mediaMimeType = getPhysicalFileMimeType();
      if (supportedMimeTypes.contains(mediaMimeType)) {
        getMedia().setFileName(fileForData.getName());
        getMedia().setFileMimeType(mediaMimeType);
        getMedia().setFileSize(fileForData.length());
        final MediaType mediaType = getMedia().getType();
        if (mediaType.isPhoto()) {
          getMedia().getPhoto().setDefinition(getPhysicalFileMetaData().getDefinition());
        } else if (mediaType.isVideo()) {
          getMedia().getVideo().setDefinition(getPhysicalFileMetaData().getDefinition());
        }
        if (getPhysicalFileMetaData().getDuration() != null) {
          if (mediaType.isVideo()) {
            getMedia().getVideo()
                .setDuration(getPhysicalFileMetaData().getDuration().getTimeAsLong());
          } else if (mediaType.isSound()) {
            getMedia().getSound()
                .setDuration(getPhysicalFileMetaData().getDuration().getTimeAsLong());
          }
        }
        if (StringUtil.isNotDefined(getMedia().getTitle()) &&
            isDefined(getPhysicalFileMetaData().getTitle())) {
          getMedia().setTitle(getPhysicalFileMetaData().getTitle());
        }
      } else {
        getMedia().setFileName(null);
        try {
          throw new GalleryRuntimeException(
              "Mime-Type of " + fileForData.getName() + " is not supported (" +
                  FileUtil.getMimeType(fileForData.getPath()) + ")");
        } finally {
          getHandledFile().delete();
        }
      }
    }

    /**
     * Gets the metadata of the physical file.
     *
     * @return meta data.
     */
    org.silverpeas.core.io.media.MetaData getPhysicalFileMetaData() {
      if (physicalFileMetaData == null) {
        physicalFileMetaData = MetadataExtractor.get().extractMetadata(getHandledFile().getFile());
      }
      return physicalFileMetaData;
    }

    /**
     * Gets lazily the mime type from the physical file which represents the media file.
     *
     * @return the mime type of the physical file.
     */
    private MediaMimeType getPhysicalFileMimeType() {
      if (physicalFileMimeType == null) {
        physicalFileMimeType = MediaMimeType.fromFile(getHandledFile().getFile());
      }
      return physicalFileMimeType;
    }

    /**
     * Gets the handled physical file.
     *
     * @return the handled file
     */
    HandledFile getHandledFile() {
      return handledFile;
    }

    /**
     * Gets the representation of the handled media.
     *
     * @return the media instance.
     */
    public M getMedia() {
      return media;
    }
  }

  private static class SoundProcess extends MediaProcess<Sound> {

    private SoundProcess(final HandledFile handledFile, final Sound media) {
      super(handledFile, media);
    }

    @Override
    protected void generateFiles() {
      // No generation.
    }
  }

  private static class VideoProcess extends MediaProcess<Video> {
    private final VideoThumbnailExtractor vte;

    private VideoProcess(final HandledFile handledFile, final Video media, final
        VideoThumbnailExtractor extractor) {
      super(handledFile, media);
      this.vte = extractor;
    }

    @Override
    protected void generateFiles() {
      if (vte.isActivated()) {
        vte.generateThumbnailsFrom(getPhysicalFileMetaData(), super.getHandledFile().getFile());
      }
    }
  }

  private static class PhotoProcess extends MediaProcess<Photo> {
    private Set<AbstractImageToolOption> watermarkNormalOptions = Collections.emptySet();
    private Set<AbstractImageToolOption> watermarkThumbnailOptions = Collections.emptySet();
    private final HandledFile[] thumbnailSrc = new HandledFile[2];

    private List<MetaData> cachedIptcMetadata = null;
    private final ImageTool imageTool;

    private PhotoProcess(final HandledFile handledFile, final Photo photo,
        final Watermark watermark, final ImageTool imageTool) {
      super(handledFile, photo);
      this.imageTool = imageTool;
      computeWatermarks(watermark);
    }

    private ImageTool getImageTool() {
      return imageTool;
    }

    @Override
    protected void generateFiles() {

      final Photo photo = getMedia();
      if (photo.isPreviewable()) {

        // Registering the size of the image
        registerResolutionData();

        // Creating normal pictures (normal and watermark)
        createNormals(photo);

        // Creating preview and thumbnails
        try {
          createThumbnails();
        } catch (final Exception e) {
          SilverLogger.getLogger(MediaProcessor.class)
              .error("image = " + photo.getTitle() + " (#" + photo.getId() + ")", e);
        }
      }
    }

    /**
     * Registers the resolution of a photo.
     */
    private void registerResolutionData() {
      if (getMedia().getDefinition().getWidth() != 0 &&
          getMedia().getDefinition().getHeight() != 0) {
        // definition already set.
        return;
      }
      String[] widthAndHeight = null;
      try {
        widthAndHeight = getImageTool().getImageInfo(super.getHandledFile().getFile(),
            WIDTH_IN_PIXEL, HEIGHT_IN_PIXEL);
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("impossible to read the width and height of file ''{0}''",
                new Object[]{super.getHandledFile().getFile().getName()}, e);
      }
      final int widthAndHeightSize = 2;
      if (widthAndHeight == null || widthAndHeight.length != widthAndHeightSize) {
        getMedia().setDefinition(Definition.fromZero());
      } else {
        getMedia().setDefinition(
            Definition.of(Integer.parseInt(widthAndHeight[0]), Integer.parseInt(widthAndHeight[1])));
      }
    }

    /**
     * Creates all the thumbnails around a photo.
     *
     * @throws Exception on technical error.
     */
    private void createThumbnails() throws Exception {
      Photo photo = getMedia();

      // File name
      final String photoId = photo.getId();

      // Processing order :
      // Large (preview without watermark)
      // Preview
      // Medium
      // Small
      // Tiny
      final MediaResolution[] mediaResolutions =
          new MediaResolution[]{LARGE, PREVIEW, MEDIUM, SMALL, TINY};
      final HandledFile originalFile = super.getHandledFile();
      final HandledFile[] sources = Stream.of(thumbnailSrc).toArray(HandledFile[]::new);
      final String originalFileExt = "." + getExtension(photo.getFileName());
      for (MediaResolution mediaResolution : mediaResolutions) {
        final boolean watermarkApplicable = mediaResolution.isWatermarkApplicable();
        final int index = watermarkApplicable && thumbnailSrc[1] != null ? 1 : 0;
        final HandledFile currentThumbnail = originalFile.getParentHandledFile()
            .getHandledFile(photoId + mediaResolution.getThumbnailSuffix() + originalFileExt);
        generateThumbnail(thumbnailSrc[index], currentThumbnail, mediaResolution);
        // The first thumbnail that has to be created must be the larger one and without watermark.
        // This first thumbnail is cached and reused for the following thumbnail creation.
        if (sources[index] == thumbnailSrc[index]) {
          thumbnailSrc[index] = currentThumbnail;
        }
      }
      if (sources[1] != null) {
        sources[1].delete();
      }
    }

    private void generateThumbnail(final HandledFile sourceFile, final HandledFile outputFile,
        MediaResolution mediaResolution) throws Exception {
      final boolean watermarkToApply =
          mediaResolution.isWatermarkApplicable() && !watermarkThumbnailOptions.isEmpty();
      final Definition definition = getMedia().getDefinition();
      final boolean resizeToPerform = definition.getWidth() > mediaResolution.getWidth() ||
          definition.getHeight() > mediaResolution.getHeight();
      if (!resizeToPerform && !watermarkToApply) {
        // Simple copy
        sourceFile.copyFile(outputFile);
        return;
      }

      // Optimized media processing
      final Set<AbstractImageToolOption> options = new HashSet<>();
      options.add(OrientationOption.auto());
      if (resizeToPerform) {
        options.add(DimensionOption
            .widthAndHeight(mediaResolution.getWidth(), mediaResolution.getHeight()));
      }
      getImageTool().convert(sourceFile.getFile(), outputFile.getFile(), options, PREVIEW_WORK,
          GEOMETRY_SHRINK);
    }

    private void computeWatermarks(Watermark watermark) {
      String wText = null;
      File wImage = null;
      String thumbnailWText = null;
      File thumbnailWImage = null;
      final Photo photo = getMedia();
      if (watermark != null && watermark.isEnabled()) {
        try {
          wText = defaultStringIfNotDefined(
              getIptcWatermarkValue(watermark, photo, watermark.getIPTCPropertyForHD()),
              watermark.getTextForHD());
          wImage = watermark.getImageForHD();
          if (watermark.isDefinedForThumbnails()) {
            thumbnailWText = defaultStringIfNotDefined(
                getIptcWatermarkValue(watermark, photo, watermark.getIPTCPropertyForThumbnails()),
                watermark.getTextForThumbnails());
            thumbnailWImage = watermark.getImageForThumbnails();
          }
        } catch (MediaMetadataException e) {
          SilverLogger.getLogger(MediaProcessor.class).silent(e).error(
              "Bad image file format " + super.getHandledFile().getFile().getPath() + ": " +
                  e.getMessage());
        } catch (IOException e) {
          SilverLogger.getLogger(MediaProcessor.class).silent(e).error(
              "Bad metadata encoding in image " + super.getHandledFile().getFile().getPath() +
                  ": " + e.getMessage());
        }
      }
      watermarkNormalOptions = computeWatermarkOptions(wText, wImage);
      watermarkThumbnailOptions = computeWatermarkOptions(thumbnailWText, thumbnailWImage);
    }

    private void createNormals(final Photo photo) {
      final String originalFileExt = "." + getExtension(photo.getFileName());
      // Normal duplication (for orientation)
      final HandledFile normalFile = super.getHandledFile()
          .getParentHandledFile()
          .getHandledFile(photo.getId() + "_normal" + originalFileExt);
      getImageTool().convert(super.getHandledFile().getFile(), normalFile.getFile(),
          OrientationOption.auto());
      thumbnailSrc[0] = normalFile;
      // Photo duplication that is stamped with a Watermark.
      if (!watermarkNormalOptions.isEmpty()) {
        final HandledFile watermarkFile = super.getHandledFile()
            .getParentHandledFile()
            .getHandledFile(photo.getId() + "_watermark" + originalFileExt);
        getImageTool().convert(super.getHandledFile().getFile(), watermarkFile.getFile(),
            watermarkNormalOptions);
      }
      if (!watermarkThumbnailOptions.isEmpty()) {
        final HandledFile watermarkFileForThumbnails = super.getHandledFile().getParentHandledFile()
            .getHandledFile(photo.getId() + "_watermark_for_thumbnails" + originalFileExt);
        getImageTool()
            .convert(super.getHandledFile().getFile(), watermarkFileForThumbnails.getFile(),
                watermarkThumbnailOptions);
        thumbnailSrc[1] = watermarkFileForThumbnails;
      }
    }

    private Set<AbstractImageToolOption> computeWatermarkOptions(final String textToWatermark,
        final File imageToWatermark) {
      final Set<AbstractImageToolOption> options = new HashSet<>();
      if (isDefined(textToWatermark)) {
        final WatermarkTextOption wText = WatermarkTextOption.text(textToWatermark);
        if (imageToWatermark != null) {
          wText.withAnchoringPosition(AnchoringPosition.SOUTH_WEST);
        }
        options.add(wText);
      }
      if (imageToWatermark != null) {
        options.add(WatermarkImageOption.image(imageToWatermark));
      }
      if (!options.isEmpty()) {
        options.add(OrientationOption.auto());
      }
      return options;
    }

    private List<MetaData> getIptcMetaData() throws MediaMetadataException, IOException {
      if (cachedIptcMetadata == null) {
        final MediaMetadataExtractor extractor =
            new DrewMediaMetadataExtractor(getMedia().getInstanceId());
        cachedIptcMetadata = extractor.extractImageIptcMetaData(super.getHandledFile().getFile());
      }
      return cachedIptcMetadata;
    }

    private String getIptcWatermarkValue(final Watermark watermark, final Photo photo,
        final String property)
        throws MediaMetadataException, IOException {
      String value = null;
      if (watermark.isBasedOnIPTC() && photo.getFileMimeType().isIPTCCompliant()) {
        value = getIptcMetaData().stream()
            .filter(i -> property.equalsIgnoreCase(i.getProperty()))
            .map(MetaData::getValue)
            .findFirst()
            .orElse(null);
      }
      return value;
    }
  }
}
