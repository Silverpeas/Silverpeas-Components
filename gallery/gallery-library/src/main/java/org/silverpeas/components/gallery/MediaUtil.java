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
package org.silverpeas.components.gallery;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.components.gallery.constant.MediaMimeType;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.media.DrewMediaMetadataExtractor;
import org.silverpeas.components.gallery.media.MediaMetadataException;
import org.silverpeas.components.gallery.media.MediaMetadataExtractor;
import org.silverpeas.components.gallery.model.GalleryRuntimeException;
import org.silverpeas.components.gallery.model.InternalMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.model.MetaData;
import org.silverpeas.components.gallery.model.Photo;
import org.silverpeas.components.gallery.model.Sound;
import org.silverpeas.components.gallery.model.Video;
import org.silverpeas.components.gallery.processing.ImageResizer;
import org.silverpeas.components.gallery.processing.ImageUtility;
import org.silverpeas.components.gallery.processing.Size;
import org.silverpeas.components.gallery.processing.Watermarker;
import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.io.media.video.VideoThumbnailExtractor;
import org.silverpeas.core.io.media.video.VideoThumbnailExtractorProvider;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.core.process.io.file.HandledFile;
import org.silverpeas.util.FileUtil;
import org.silverpeas.core.io.media.image.ImageLoader;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.apache.commons.io.filefilter.FileFilterUtils.*;
import static org.silverpeas.core.io.media.video.ThumbnailPeriod.VIDEO_THUMBNAIL_FILE_EXTENSION;
import static org.silverpeas.core.io.media.video.ThumbnailPeriod.VIDEO_THUMBNAIL_FILE_PREFIX;

public class MediaUtil {

  final static SettingBundle gallerySettings =
      ResourceLocator.getSettingBundle("org.silverpeas.gallery.settings.gallerySettings");

  /**
   * Saves uploaded sound file on file system
   * @param fileHandler the current session file handler
   * @param sound the current sound media
   * @param fileItem the current uploaded sound
   * @throws Exception
   */
  public static void processSound(final FileHandler fileHandler, Sound sound,
      final FileItem fileItem) throws Exception {
    if (fileItem != null) {
      String name = fileItem.getName();
      if (name != null) {
        try {
          sound.setFileName(FileUtil.getFilename(name));
          final HandledFile handledSoundFile = getHandledFile(fileHandler, sound);
          handledSoundFile.copyInputStreamToFile(fileItem.getInputStream());
          setInternalMetadata(handledSoundFile, sound, MediaMimeType.SOUNDS);
        } finally {
          fileItem.delete();
        }
      }
    }
  }

  /**
   * Saves uploaded sound file on file system (In case of drag And Drop upload)
   * @param fileHandler the current session file handler
   * @param sound the current sound media
   * @param uploadedFile the current uploaded sound
   * @throws Exception
   */
  public static void processSound(final FileHandler fileHandler, Sound sound,
      final File uploadedFile) throws Exception {
    if (uploadedFile != null) {
      try {
        sound.setFileName(uploadedFile.getName());
        final HandledFile handledSoundFile = getHandledFile(fileHandler, sound);
        fileHandler.copyFile(uploadedFile, handledSoundFile);
        setInternalMetadata(handledSoundFile, sound, MediaMimeType.SOUNDS);
      } finally {
        FileUtils.deleteQuietly(uploadedFile);
      }
    }
  }

  /**
   * Saves uploaded video file on file system
   * @param fileHandler the current session file handler
   * @param video the current video media
   * @param fileItem the current uploaded video
   * @throws Exception
   */
  public static void processVideo(final FileHandler fileHandler, Video video,
      final FileItem fileItem) throws Exception {
    if (fileItem != null) {
      String name = fileItem.getName();
      if (name != null) {
        try {
          video.setFileName(FileUtil.getFilename(name));
          final HandledFile handledVideoFile = getHandledFile(fileHandler, video);
          handledVideoFile.copyInputStreamToFile(fileItem.getInputStream());
          setInternalMetadata(handledVideoFile, video, MediaMimeType.VIDEOS);
          generateVideoThumbnails(handledVideoFile.getFile());
        } finally {
          fileItem.delete();
        }
      }
    }
  }

  /**
   * Saves uploaded video file on file system (In case of drag And Drop upload)
   * @param fileHandler the current session file handler
   * @param video the current video media
   * @param uploadedFile the current uploaded video
   * @throws Exception
   */
  public static void processVideo(final FileHandler fileHandler, Video video,
      final File uploadedFile) throws Exception {
    if (uploadedFile != null) {
      try {
        video.setFileName(uploadedFile.getName());
        final HandledFile handledVideoFile = getHandledFile(fileHandler, video);
        fileHandler.copyFile(uploadedFile, handledVideoFile);
        setInternalMetadata(handledVideoFile, video, MediaMimeType.VIDEOS);
        generateVideoThumbnails(handledVideoFile.getFile());
      } finally {
        FileUtils.deleteQuietly(uploadedFile);
      }
    }
  }

  /**
   * Saves uploaded photo file on file system with associated thumbnails and watermarks.
   * @param fileHandler the current session file handler
   * @param photo the photo media
   * @param image the image to register
   * @param watermark true if watermark must be handled
   * @param watermarkHD the primary metadata retrieved to compute the watermark
   * @param watermarkOther the secondary metadata retrieved to compute the watermark
   * @throws Exception
   */
  public static void processPhoto(final FileHandler fileHandler, final Photo photo,
      final FileItem image, final boolean watermark, final String watermarkHD,
      final String watermarkOther) throws Exception {
    if (image != null) {
      String name = image.getName();
      if (name != null) {
        try {
          photo.setFileName(FileUtil.getFilename(name));
          final HandledFile handledImageFile = getHandledFile(fileHandler, photo);
          handledImageFile.copyInputStreamToFile(image.getInputStream());
          if (setInternalMetadata(handledImageFile, photo, MediaMimeType.PHOTOS)) {
            createPhoto(handledImageFile, photo, watermark, watermarkHD, watermarkOther);
          }
        } finally {
          image.delete();
        }
      }
    }
  }

  /**
   * Saves uploaded photo file on file system with associated thumbnails and watermarks. (In case
   * of
   * drag And Drop upload)
   * @param fileHandler the current session file handler
   * @param photo the photo media
   * @param image the image to register
   * @param watermark true if watermark must be handled
   * @param watermarkHD the primary metadata retrieved to compute the watermark
   * @param watermarkOther the secondary metadata retrieved to compute the watermark
   * @throws Exception
   */
  public static void processPhoto(final FileHandler fileHandler, final Photo photo,
      final File image, final boolean watermark, final String watermarkHD,
      final String watermarkOther) throws Exception {
    if (image != null) {
      try {
        photo.setFileName(image.getName());
        final HandledFile handledImageFile = getHandledFile(fileHandler, photo);
        fileHandler.copyFile(image, handledImageFile);
        if (setInternalMetadata(handledImageFile, photo, MediaMimeType.PHOTOS)) {
          createPhoto(handledImageFile, photo, watermark, watermarkHD, watermarkOther);
        }
      } finally {
        FileUtils.deleteQuietly(image);
      }
    }
  }

  /**
   * Gets a handled file.
   * @param fileHandler the current session file handler
   * @param media the original media file to get.
   * @return the handled file
   */
  private static HandledFile getHandledFile(FileHandler fileHandler, InternalMedia media) {
    if (StringUtil.isNotDefined(media.getFileName())) {
      throw new IllegalArgumentException("media.getFilename() must return a defined name");
    }
    return fileHandler.getHandledFile(Media.BASE_PATH, media.getComponentInstanceId(),
        media.getWorkspaceSubFolderName(), media.getFileName());
  }

  /**
   * Sets the internal metadata. If metadata
   * @param handledImageFile the handled file that represents the image
   * @param iMedia the internal media
   * @param supportedMimeTypes the set of supported media types
   * @return true if internal data have been set, false otherwise.
   */
  private static boolean setInternalMetadata(HandledFile handledImageFile, InternalMedia iMedia,
      final Set<MediaMimeType> supportedMimeTypes) throws Exception {
    File fileForData = handledImageFile.getFile();
    MediaMimeType mediaMimeType = MediaMimeType.fromFile(handledImageFile.getFile());
    if (supportedMimeTypes.contains(mediaMimeType)) {
      iMedia.setFileName(fileForData.getName());
      iMedia.setFileMimeType(mediaMimeType);
      iMedia.setFileSize(fileForData.length());
      MetadataExtractor metadataExtractor = MetadataExtractor.get();
      org.silverpeas.core.io.media.MetaData metaData =
          metadataExtractor.extractMetadata(handledImageFile.getFile());
      switch (iMedia.getType()) {
        case Photo:
          iMedia.getPhoto().setDefinition(metaData.getDefinition());
          break;
        case Video:
          iMedia.getVideo().setDefinition(metaData.getDefinition());
          break;
      }
      if (metaData.getDuration() != null) {
        switch (iMedia.getType()) {
          case Video:
            iMedia.getVideo().setDuration(metaData.getDuration().getTimeAsLong());
            break;
          case Sound:
            iMedia.getSound().setDuration(metaData.getDuration().getTimeAsLong());
            break;
        }
      }
      if (StringUtil.isNotDefined(iMedia.getTitle()) && StringUtil.isDefined(metaData.getTitle())) {
        iMedia.setTitle(metaData.getTitle());
      }
      return true;
    } else {
      iMedia.setFileName(null);
      try {
        throw new GalleryRuntimeException("MediaHelper.setInternalMetadata",
            SilverpeasRuntimeException.ERROR,
            "Mime-Type of " + handledImageFile.getFile().getName() + " is not supported (" +
                FileUtil.getMimeType(handledImageFile.getFile().getPath()) + ")");
      } finally {
        handledImageFile.delete();
      }
    }
  }

  /**
   * Creation treatment of all the preview image around a photo.
   * @param handledImageFile the handled file that represents the image
   * @param photo the photo media
   * @param watermark true if watermark must be handled
   * @param watermarkHD the primary metadata retrieved to compute the watermark
   * @param watermarkOther the secondary metadata retrieved to compute the watermark
   * @throws Exception
   */
  private static void createPhoto(final HandledFile handledImageFile, final Photo photo,
      final boolean watermark, final String watermarkHD, final String watermarkOther)
      throws Exception {
    String percent = gallerySettings.getString("percentSizeWatermark");
    if (!StringUtil.isDefined(percent)) {
      percent = "1";
    }
    int percentSize = Integer.parseInt(percent);
    if (percentSize <= 0) {
      percentSize = 1;
    }

    if (photo.isPreviewable()) {

      // Getting the size of the image
      final BufferedImage image = ImageLoader.loadImage(handledImageFile.getFile());
      setResolution(image, photo);

      // Computing watermark data
      final String nameForWatermark =
          computeWatermarkText(handledImageFile, photo, watermark, watermarkHD, watermarkOther,
              percentSize);

      // Creating preview and thumbnails
      try {
        createThumbnails(handledImageFile, photo, image, watermark, nameForWatermark);
      } catch (final Exception e) {
        SilverLogger.getLogger("gallery")
            .error("image = " + photo.getTitle() + " (#" + photo.getId() + ")");
      }
    }
  }

  public static void setMetaData(final FileHandler fileHandler, final Photo photo)
      throws IOException, MediaMetadataException {
    setMetaData(fileHandler, photo, MessageManager.getLanguage());
  }

  public static void setMetaData(final FileHandler fileHandler, final Photo photo,
      final String lang) throws MediaMetadataException, IOException {
    if (MediaMimeType.JPG == photo.getFileMimeType()) {
      final HandledFile handledFile = fileHandler
          .getHandledFile(Media.BASE_PATH, photo.getInstanceId(), photo.getWorkspaceSubFolderName(),
              photo.getFileName());
      if (handledFile.exists()) {
        try {
          final MediaMetadataExtractor extractor = new DrewMediaMetadataExtractor(photo.
              getInstanceId());
          for (final MetaData meta : extractor
              .extractImageExifMetaData(handledFile.getFile(), lang)) {
            photo.addMetaData(meta);
          }
          for (final MetaData meta : extractor
              .extractImageIptcMetaData(handledFile.getFile(), lang)) {
            photo.addMetaData(meta);
          }
        } catch (UnsupportedEncodingException e) {
          SilverLogger.getLogger("gallery")
              .error("Bad metadata encoding in image " + photo.getTitle() + ": " + e.getMessage());
        }
      }
    }
  }

  /**
   * Sets the resolution of a photo.
   * @param image the image from which the resolution must be red
   * @param photo the photo media into which the resolution must be saved
   */
  private static void setResolution(final BufferedImage image, final Photo photo) {
    if (image == null) {
      photo.setDefinition(Definition.fromZero());
    } else {
      photo.setDefinition(Definition.of(image.getWidth(), image.getHeight()));
    }
  }

  /**
   * Creates all the thumbnails around a photo.
   * @param originalHandedImageFile the handled file that represents the original media file
   * @param photo the photo media
   * @param originalImage the original image of the original media
   * @param watermark true if watermark must be handled
   * @param nameWatermark the watermark to register
   * @throws Exception
   */
  private static void createThumbnails(final HandledFile originalHandedImageFile, final Photo photo,
      final BufferedImage originalImage, final boolean watermark, final String nameWatermark)
      throws Exception {

    // File name
    final String photoId = photo.getId();

    // Preview image resizing only if the original image is larger than the preview
    int originalImageWidth =
        Math.max(photo.getDefinition().getWidth(), photo.getDefinition().getHeight());

    // Processing order :
    // Large (preview without watermark)
    // Preview
    // Medium
    // Small
    // Tiny
    final MediaResolution[] mediaResolutions =
        new MediaResolution[]{MediaResolution.LARGE, MediaResolution.PREVIEW,
            MediaResolution.MEDIUM, MediaResolution.SMALL, MediaResolution.TINY};
    BufferedImage previewImage = null;
    for (MediaResolution mediaResolution : mediaResolutions) {
      // Current thumbnail
      HandledFile currentThumbnail = originalHandedImageFile.getParentHandledFile()
          .getHandledFile(photoId + mediaResolution.getThumbnailSuffix());
      // Thumbnail creation
      resizePhoto((previewImage != null ? previewImage : originalImage), currentThumbnail,
          (originalImageWidth > mediaResolution.getWidth() ? mediaResolution.getWidth() :
              originalImageWidth), (watermark && mediaResolution.isWatermarkApplicable()),
          nameWatermark,
          (mediaResolution.getWatermarkSize() != null ? mediaResolution.getWatermarkSize() : 0));
      // The first thumbnail that has to be created must be the larger one and without watermark.
      // This first thumbnail is cached and reused for the following thumbnail creation.
      if (previewImage == null) {
        previewImage = ImageLoader.loadImage(currentThumbnail.getFile());
      }
    }
  }

  public static Size getWidthAndHeight(final String instanceId, final String subDir,
      final String imageName, final int baseWidth) throws IOException {
    return ImageUtility.getWidthAndHeight(instanceId, subDir, imageName, baseWidth);
  }

  /**
   * Return the written file
   * @param image the image from which the resolution must be red
   * @param outputFile the output file
   * @param widthParam the width parameter
   * @param watermark the height parameter
   * @param nameWatermark the watermark to write on the output file
   * @param sizeWatermark the size of the watermark
   * @throws Exception
   */
  private static void resizePhoto(final BufferedImage image, final HandledFile outputFile,
      final int widthParam, final boolean watermark, final String nameWatermark,
      final int sizeWatermark) throws Exception {
    OutputStream outputStream = null;
    try {
      outputStream = outputFile.openOutputStream();
      final ImageResizer resizer = new ImageResizer(image, widthParam);
      if (watermark) {
        resizer.resizeImageWithWatermark(outputStream, nameWatermark, sizeWatermark);
      } else {
        resizer.resizeImage(outputStream);
      }
    } finally {
      if (outputStream != null) {
        IOUtils.closeQuietly(outputStream);
      }
    }
  }

  private static void createWatermark(final OutputStream watermarkedTargetStream,
      final String watermarkLabel, final BufferedImage image, final int percentSizeWatermark)
      throws IOException {

    final int imageWidth = image.getWidth();
    final int imageHeight = image.getHeight();

    // création du buffer a la même taille
    final BufferedImage outputBuf =
        new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

    final double max = Math.max(imageWidth, imageHeight);

    // recherche de la taille du watermark en fonction de la taille de la photo
    int size = 8;
    if (max < 600) {
      size = 8;
    }
    if (max >= 600 && max < 750) {
      size = 10;
    }
    if (max >= 750 && max < 1000) {
      size = 12;
    }
    if (max >= 1000 && max < 1250) {
      size = 14;
    }
    if (max >= 1250 && max < 1500) {
      size = 16;
    }
    if (max >= 1500 && max < 1750) {
      size = 18;
    }
    if (max >= 1750 && max < 2000) {
      size = 20;
    }
    if (max >= 2000 && max < 2250) {
      size = 22;
    }
    if (max >= 2250 && max < 2500) {
      size = 24;
    }
    if (max >= 2500 && max < 2750) {
      size = 26;
    }
    if (max >= 2750 && max < 3000) {
      size = 28;
    }
    if (max >= 3000) {
      size = (int) Math.rint(max * percentSizeWatermark / 100);
    }
    final Watermarker watermarker = new Watermarker(imageWidth, imageHeight);
    watermarker
        .addWatermark(image, outputBuf, new Font("Arial", Font.BOLD, size), watermarkLabel, size);
    ImageIO.write(outputBuf, "JPEG", watermarkedTargetStream);
  }

  public static void pasteInternalMedia(final FileHandler fileHandler, final MediaPK fromPK,
      final InternalMedia media, final boolean cut) {
    InternalMedia fromMedia = media.getType().newInstance();
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
                  asFileFilter((file) -> file.getName().matches(
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
          SilverLogger.getLogger("gallery").error(
              "Unable to delete source folder : folder path = " + fromDir.getFile().getPath(), e);
        }
      }
    }
  }

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
        SilverLogger.getLogger("gallery").error(
            "Unable to copy file : fromImage = " + fromFile.getFile().getPath() + ", toImage = " +
                toFile.getFile().getPath(), e);
      }
    }
  }

  private static String computeWatermarkText(final HandledFile image, final Photo photo,
      final boolean watermark, final String watermarkHD, final String watermarkOther,
      final int percentSize) throws Exception {
    String nameAuthor = "";
    String nameForWatermark = "";
    if (watermark && photo.getFileMimeType().isIPTCCompliant()) {
      final MediaMetadataExtractor extractor =
          new DrewMediaMetadataExtractor(photo.getInstanceId());
      final List<MetaData> iptcMetadata;
      try {
        iptcMetadata = extractor.extractImageIptcMetaData(image.getFile());
        final BufferedImage bufferedImage = ImageLoader.loadImage(image.getFile());
        if (StringUtil.isDefined(watermarkHD)) {
          // Photo duplication that is stamped with a Watermark.
          final String value = getWatermarkValue(watermarkHD, iptcMetadata);
          if (value != null) {
            nameAuthor = value;
          }
          if (!nameAuthor.isEmpty()) {
            OutputStream watermarkStream = null;
            try {
              watermarkStream =
                  image.getParentHandledFile().getHandledFile(photo.getId() + "_watermark.jpg")
                      .openOutputStream();
              createWatermark(watermarkStream, nameAuthor, bufferedImage, percentSize);
            } finally {
              IOUtils.closeQuietly(watermarkStream);
            }
          }
        }
        if (StringUtil.isDefined(watermarkOther)) {
          final String value = getWatermarkValue(watermarkOther, iptcMetadata);
          if (value != null) {
            nameAuthor = value;
          }
          if (!nameAuthor.isEmpty()) {
            nameForWatermark = nameAuthor;
          }
        }
      } catch (MediaMetadataException e) {
        SilverLogger.getLogger("gallery")
            .error("Bad image file format " + image.getFile().getPath() + ": " + e.getMessage());
      } catch (UnsupportedEncodingException e) {
        SilverLogger.getLogger("gallery").error(
            "Bad metadata encoding in image " + image.getFile().getPath() + ": " + e.getMessage());
      }
    }
    return nameForWatermark;
  }

  private static String getWatermarkValue(final String property,
      final List<MetaData> iptcMetadata) {
    String value = null;
    for (final MetaData metadata : iptcMetadata) {
      if (property.equalsIgnoreCase(metadata.getProperty())) {
        value = metadata.getValue();
      }
    }
    return value;
  }

  private static void generateVideoThumbnails(File videoFile) {
    VideoThumbnailExtractor vte = VideoThumbnailExtractorProvider.getVideoThumbnailExtractor();
    if (vte.isActivated()) {
      vte.generateThumbnailsFrom(videoFile);
    }
  }

  private MediaUtil() {
  }
}
