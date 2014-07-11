/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery;

import com.silverpeas.gallery.constant.MediaMimeType;
import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.media.DrewMediaMetadataExtractor;
import com.silverpeas.gallery.media.MediaMetadataException;
import com.silverpeas.gallery.media.MediaMetadataExtractor;
import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.model.Sound;
import com.silverpeas.gallery.model.Video;
import com.silverpeas.gallery.processing.ImageResizer;
import com.silverpeas.gallery.processing.ImageUtility;
import com.silverpeas.gallery.processing.Size;
import com.silverpeas.gallery.processing.Watermarker;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.MetadataExtractor;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.silverpeas.media.Definition;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.io.file.HandledFile;
import org.silverpeas.util.ImageLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import static com.silverpeas.gallery.constant.MediaResolution.*;

public class MediaHelper {

  final static ResourceLocator gallerySettings =
      new ResourceLocator("org.silverpeas.gallery.settings.gallerySettings", "");

  /**
   * Saves uploaded sound file on file system
   * @param fileHandler
   * @param sound the current sound media
   * @param fileItem the current uploaded sound
   * @throws Exception
   */
  public static void processSound(final FileHandler fileHandler, Sound sound,
      final FileItem fileItem) throws Exception {
    if (fileItem != null) {
      String name = fileItem.getName();
      if (name != null) {
        sound.setFileName(FileUtil.getFilename(name));
        final HandledFile handledSoundFile = getHandledFile(fileHandler, sound);
        handledSoundFile.writeByteArrayToFile(fileItem.get());
        setInternalMetadata(handledSoundFile, sound, MediaMimeType.SOUNDS);
      }
    }
  }

  /**
   * Saves uploaded video file on file system
   * @param fileHandler
   * @param video the current video media
   * @param fileItem the current uploaded video
   * @throws Exception
   */
  public static void processVideo(final FileHandler fileHandler, Video video,
      final FileItem fileItem) throws Exception {
    if (fileItem != null) {
      String name = fileItem.getName();
      if (name != null) {
        video.setFileName(FileUtil.getFilename(name));
        final HandledFile handledVideoFile = getHandledFile(fileHandler, video);
        handledVideoFile.writeByteArrayToFile(fileItem.get());
        setInternalMetadata(handledVideoFile, video, MediaMimeType.VIDEOS);
      }
    }
  }

  /**
   * Saves uploaded video file on file system
   * (In case of drag And Drop upload)
   * @param fileHandler
   * @param video the current video media
   * @param uploadedFile the current uploaded video
   * @throws Exception
   */
  public static void processVideo(final FileHandler fileHandler, Video video,
      final File uploadedFile) throws Exception {
    if (uploadedFile != null) {
      video.setFileName(uploadedFile.getName());
      final HandledFile handledVideoFile = getHandledFile(fileHandler, video);
      fileHandler.copyFile(uploadedFile, handledVideoFile);
      setInternalMetadata(handledVideoFile, video, MediaMimeType.VIDEOS);
    }
  }

  /**
   * Saves uploaded photo file on file system with associated thumbnails and watermarks.
   * @param fileHandler
   * @param photo
   * @param image
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @throws Exception
   */
  public static void processPhoto(final FileHandler fileHandler, final Photo photo,
      final FileItem image, final boolean watermark, final String watermarkHD,
      final String watermarkOther) throws Exception {
    if (image != null) {
      String name = image.getName();
      if (name != null) {
        photo.setFileName(FileUtil.getFilename(name));
        final HandledFile handledImageFile = getHandledFile(fileHandler, photo);
        handledImageFile.writeByteArrayToFile(image.get());
        if (setInternalMetadata(handledImageFile, photo, MediaMimeType.PHOTOS)) {
          createPhoto(handledImageFile, photo, watermark, watermarkHD, watermarkOther);
        }
      }
    }
  }

  /**
   * Saves uploaded photo file on file system with associated thumbnails and watermarks.
   * (In case of drag And Drop upload)
   * @param fileHandler
   * @param photo
   * @param image
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @throws Exception
   */
  public static void processPhoto(final FileHandler fileHandler, final Photo photo,
      final File image, final boolean watermark, final String watermarkHD,
      final String watermarkOther) throws Exception {
    if (image != null) {
      photo.setFileName(image.getName());
      final HandledFile handledImageFile = getHandledFile(fileHandler, photo);
      fileHandler.copyFile(image, handledImageFile);
      if (setInternalMetadata(handledImageFile, photo, MediaMimeType.PHOTOS)) {
        createPhoto(handledImageFile, photo, watermark, watermarkHD, watermarkOther);
      }
    }
  }

  /**
   * Gets a handled file.
   * @param fileHandler
   * @param media
   * @return
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
   * @param handledImageFile
   * @param iMedia
   * @param supportedMimeTypes
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
      com.silverpeas.util.MetaData metaData =
          MetadataExtractor.getInstance().extractMetadata(handledImageFile.getFile());
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
      handledImageFile.delete();
      return false;
    }
  }

  /**
   * Creation treatment of all the preview image around a photo.
   * @param handledImageFile
   * @param photo
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
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
        SilverTrace
            .error("gallery", "ImageHelper.createImage", "gallery.ERR_CANT_CREATE_THUMBNAILS",
                "image = " + photo.getTitle() + " (#" + photo.getId() + ")");
      }
    }
  }

  public static void setMetaData(final FileHandler fileHandler, final Photo photo)
      throws IOException, MediaMetadataException {
    setMetaData(fileHandler, photo, I18NHelper.defaultLanguage);
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
          SilverTrace.error("gallery", "ImageHelper.computeWatermarkText", "root.MSG_BAD_ENCODING",
              "Bad metadata encoding in image " + photo.getTitle() + ": " + e.getMessage());
        }
      }
    }
  }

  /**
   * Sets the resolution of a photo.
   * @param image
   * @param photo
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
   * @param originalHandedImageFile
   * @param photo
   * @param originalImage
   * @param watermark
   * @param nameWatermark
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
        new MediaResolution[]{LARGE, PREVIEW, MEDIUM, SMALL, TINY};
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
   * @param image
   * @param outputFile
   * @param widthParam
   * @param watermark
   * @param nameWatermark
   * @param sizeWatermark
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

  public static void pasteImage(final FileHandler fileHandler, final MediaPK fromPK,
      final Photo image, final boolean cut) {
    final MediaPK toPK = image.getMediaPK();
    final String subDirectory = MediaType.Photo.getTechnicalFolder();
    final HandledFile fromDir = fileHandler
        .getHandledFile(Media.BASE_PATH, fromPK.getInstanceId(), subDirectory + fromPK.getId());
    final HandledFile toDir = fileHandler
        .getHandledFile(Media.BASE_PATH, toPK.getInstanceId(), subDirectory + toPK.getId());

    // copier et renommer chaque image présente dans le répertoire d'origine
    if (fromDir.exists()) {

      // copy thumbnails & watermark (only if it does exist)
      for (final MediaResolution mediaResolution : new MediaResolution[]{MEDIUM, SMALL, TINY,
          PREVIEW, LARGE, WATERMARK}) {
        pasteFile(fromDir.getHandledFile(fromPK.getId() + mediaResolution.getThumbnailSuffix()),
            toDir.getHandledFile(toPK.getId() + mediaResolution.getThumbnailSuffix()), cut);
      }
      // copy original image
      pasteFile(fromDir.getHandledFile(image.getFileName()), toDir.getHandledFile(image.
          getFileName()), cut);
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
        SilverTrace.error("gallery", "ImageHelper.pasteFile", "root.MSG_GEN_PARAM_VALUE",
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
      } catch (UnsupportedEncodingException e) {
        SilverTrace.error("gallery", "ImageHelper.computeWatermarkText", "root.MSG_BAD_ENCODING",
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

  private MediaHelper() {
  }
}
