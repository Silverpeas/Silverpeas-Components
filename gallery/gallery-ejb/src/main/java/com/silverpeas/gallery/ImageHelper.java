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

import static com.silverpeas.gallery.constant.MediaResolution.LARGE;
import static com.silverpeas.gallery.constant.MediaResolution.MEDIUM;
import static com.silverpeas.gallery.constant.MediaResolution.PREVIEW;
import static com.silverpeas.gallery.constant.MediaResolution.SMALL;
import static com.silverpeas.gallery.constant.MediaResolution.TINY;
import static com.silverpeas.gallery.constant.MediaResolution.WATERMARK;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.process.io.file.FileBasePath;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.io.file.HandledFile;
import org.silverpeas.util.ImageLoader;

import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.image.DrewImageMetadataExtractor;
import com.silverpeas.gallery.image.ImageMetadataException;
import com.silverpeas.gallery.image.ImageMetadataExtractor;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.processing.ImageResizer;
import com.silverpeas.gallery.processing.ImageUtility;
import com.silverpeas.gallery.processing.Size;
import com.silverpeas.gallery.processing.Watermarker;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

public class ImageHelper {

  private final static FileBasePath BASE_PATH = FileBasePath.UPLOAD_PATH;
  final static ResourceLocator gallerySettings =
      new ResourceLocator("org.silverpeas.gallery.settings.gallerySettings", "");

  /**
   * Open an output stream of an image according to given details of a photo.
   * @param photo
   * @param isOriginalRequired
   * @return
   * @throws IOException
   */
  public static InputStream openInputStream(final Photo photo, final boolean isOriginalRequired) {
    final String photoId = photo.getMediaPK().getId();
    final String instanceId = photo.getMediaPK().getInstanceId();
    if (StringUtil.isDefined(photoId) && StringUtil.isDefined(instanceId)) {
      String fileName = photoId + PREVIEW.getThumbnailSuffix();
      if (isOriginalRequired) {
        fileName = photo.getFileName();
      }
      try {
        return FileUtils.openInputStream(FileUtils
            .getFile(new File(BASE_PATH.getPath()), instanceId,
                MediaType.Photo.getTechnicalFolder() + photoId, fileName));
      } catch (IOException e) {
        SilverTrace.error("gallery", "ImageHelper.getBytes", "gallery.ERR_CANT_GET_IMAGE_BYTES",
            "image = " + photo.getTitle() + " (#" + photo.getId() + ")");
        return null;
      }
    }
    return null;
  }

  /**
   * @param fileHandler
   * @param photo
   * @param image
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @throws Exception
   */
  public static void processImage(final FileHandler fileHandler, final Photo photo,
      final FileItem image, final boolean watermark, final String watermarkHD,
      final String watermarkOther) throws Exception {
    final String photoId = photo.getMediaPK().getId();
    final String instanceId = photo.getMediaPK().getInstanceId();

    if (image != null) {
      String name = image.getName();
      if (name != null) {
        name = FileUtil.getFilename(name);
        if (ImageType.isImage(name)) {
          final String subDirectory = MediaType.Photo.getTechnicalFolder();
          final HandledFile handledImageFile =
              fileHandler.getHandledFile(BASE_PATH, instanceId, subDirectory + photoId, name);
          handledImageFile.writeByteArrayToFile(image.get());
          photo.setFileName(name);
          photo.setFileMimeType(image.getContentType());
          photo.setFileSize(image.getSize());
          createImage(name, handledImageFile, photo, watermark, watermarkHD, watermarkOther);
        }
      }
    }
  }

  /**
   * In case of drag And Drop upload.
   * @param fileHandler
   * @param photo
   * @param image
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @throws Exception
   */
  public static void processImage(final FileHandler fileHandler, final Photo photo,
      final File image, final boolean watermark, final String watermarkHD,
      final String watermarkOther) throws Exception {
    final String photoId = photo.getMediaPK().getId();
    final String instanceId = photo.getMediaPK().getInstanceId();

    if (image != null) {
      String name = image.getName();
      name = name.substring(name.lastIndexOf(File.separator) + 1, name.length());
      if (ImageType.isImage(name)) {
        String subDirectory = MediaType.Photo.getTechnicalFolder();
        final HandledFile handledImageFile =
            fileHandler.getHandledFile(BASE_PATH, instanceId, subDirectory + photoId, name);
        fileHandler.copyFile(image, handledImageFile);
        photo.setFileName(name);
        photo.setFileMimeType(FileUtil.getMimeType(name));
        photo.setFileSize(image.length());
        createImage(name, handledImageFile, photo, watermark, watermarkHD, watermarkOther);
      }
    }
  }

  private static void createImage(final String name, final HandledFile handledImageFile,
      final Photo photo, final boolean watermark, final String watermarkHD,
      final String watermarkOther) throws Exception {
    String percent = gallerySettings.getString("percentSizeWatermark");
    if (!StringUtil.isDefined(percent)) {
      percent = "1";
    }
    int percentSize = Integer.parseInt(percent);
    if (percentSize <= 0) {
      percentSize = 1;
    }

    if (ImageType.isReadable(name)) {

      // Getting the size of the image
      final String type = FileRepositoryManager.getFileExtension(name);
      final BufferedImage image = ImageLoader.loadImage(handledImageFile.getFile());
      getDimension(image, photo);

      // Computing watermark data
      final String nameForWatermark =
          computeWatermarkText(watermarkHD, watermark, type, handledImageFile, photo, percentSize,
              watermarkOther);

      // Creating preview and thumbnails
      try {
        createThumbnails(photo, handledImageFile, image, watermark, nameForWatermark);
      } catch (final Exception e) {
        SilverTrace
            .error("gallery", "ImageHelper.createImage", "gallery.ERR_CANT_CREATE_THUMBNAILS",
                "image = " + photo.getTitle() + " (#" + photo.getId() + ")");
      }
    }
  }

  public static void setMetaData(final FileHandler fileHandler, final Photo photo)
      throws IOException, ImageMetadataException {
    setMetaData(fileHandler, photo, I18NHelper.defaultLanguage);
  }

  public static void setMetaData(final FileHandler fileHandler, final Photo photo,
      final String lang) throws ImageMetadataException, IOException {
    final String photoId = photo.getMediaPK().getId();
    final String name = photo.getFileName();
    final String mimeType = photo.getFileMimeType();

    if ("image/jpeg".equals(mimeType) || "image/pjpeg".equals(mimeType)) {
      final HandledFile handledFile = fileHandler.getHandledFile(BASE_PATH, photo.getInstanceId(),
          MediaType.Photo.getTechnicalFolder() + photoId, name);
      if (handledFile.exists()) {
        try {
          final ImageMetadataExtractor extractor = new DrewImageMetadataExtractor(photo.
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

  private static void getDimension(final BufferedImage image, final Photo photo) {
    if (image == null) {
      photo.setResolutionW(0);
      photo.setResolutionH(0);
    } else {
      photo.setResolutionW(image.getWidth());
      photo.setResolutionH(image.getHeight());
    }
  }

  private static void createThumbnails(final Photo photo, final HandledFile originalHandedImageFile,
      final BufferedImage originalImage, final boolean watermark, final String nameWatermark)
      throws Exception {

    // File name
    final String photoId = photo.getId();

    // Preview image resizing only if the original image is larger than the preview
    int originalImageWidth = Math.max(photo.getResolutionW(), photo.getResolutionH());

    // Processing order :
    // XLarge (preview without watermark)
    // Preview
    // Large
    // Medium
    // Small
    final MediaResolution[] mediaResolutions =
        new MediaResolution[]{LARGE, PREVIEW, MEDIUM, SMALL, TINY};
    BufferedImage previewImage = null;
    for (MediaResolution mediaResolution : mediaResolutions) {
      // Current thumbnail
      HandledFile currentThumblail = originalHandedImageFile.getParentHandledFile()
          .getHandledFile(photoId + mediaResolution.getThumbnailSuffix());
      // Thumbnail creation
      redimPhoto((previewImage != null ? previewImage : originalImage), currentThumblail,
          (originalImageWidth > mediaResolution.getWidth() ? mediaResolution.getWidth() :
              originalImageWidth), (watermark && mediaResolution.isWatermarkApplicable()),
          nameWatermark,
          (mediaResolution.getWatermarkSize() != null ? mediaResolution.getWatermarkSize() : 0));
      // The first thumbnail that has to be created must be the larger one and without watermark.
      // This first thumbnail is cached and reused for the following thumbnail creation.
      if (previewImage == null) {
        previewImage = ImageLoader.loadImage(currentThumblail.getFile());
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
  private static void redimPhoto(final BufferedImage image, final HandledFile outputFile,
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
        .getHandledFile(BASE_PATH, fromPK.getInstanceId(), subDirectory + fromPK.getId());
    final HandledFile toDir =
        fileHandler.getHandledFile(BASE_PATH, toPK.getInstanceId(), subDirectory + toPK.getId());

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

  private static String computeWatermarkText(final String watermarkHD, final boolean watermark,
      final String type, final HandledFile image, final Photo photo, final int percentSize,
      final String watermarkOther) throws Exception {
    String nameAuthor = "";
    String nameForWatermark = "";
    if (ImageType.isIPTCCompliant(type) && watermark) {
      final ImageMetadataExtractor extractor =
          new DrewImageMetadataExtractor(photo.getInstanceId());
      final List<MetaData> iptcMetadata;
      try {
        iptcMetadata = extractor.extractImageIptcMetaData(image.getFile());
        final BufferedImage bufferedImage = ImageLoader.loadImage(image.getFile());
        if (StringUtil.isDefined(watermarkHD)) {
          // création d'un duplicata de l'image originale avec intégration du
          // watermark
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

  private ImageHelper() {
  }
}
