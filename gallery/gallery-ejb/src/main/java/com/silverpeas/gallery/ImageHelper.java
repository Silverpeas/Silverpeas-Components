/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import java.awt.Font;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.silverpeas.process.io.file.FileBasePath;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.io.file.HandledFile;

import com.silverpeas.gallery.image.DrewImageMetadataExtractor;
import com.silverpeas.gallery.image.ImageMetadataException;
import com.silverpeas.gallery.image.ImageMetadataExtractor;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
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
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.util.ImageLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.ws.rs.HEAD;

import com.stratelia.webactiv.util.attachment.control.AttachmentController;

public class ImageHelper {

  private final static FileBasePath BASE_PATH = FileBasePath.UPLOAD_PATH;
  final static ResourceLocator gallerySettings = new ResourceLocator(
      "com.silverpeas.gallery.settings.gallerySettings", "");
  final static ResourceLocator settings = new ResourceLocator(
      "com.silverpeas.gallery.settings.metadataSettings", "");
  static final String thumbnailSuffix_small = "_66x50.jpg";
  static final String thumbnailSuffix_medium = "_133x100.jpg";
  static final String thumbnailSuffix_large = "_266x150.jpg";
  static final String previewSuffix = "_preview.jpg";
  static final String thumbnailSuffix_Xlarge = "_600x400.jpg";
  static final String watermarkSuffix = "_watermark.jpg";

  /**
   * @param photo
   * @param image
   * @param subDirectory
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @throws Exception
   */
  public static void processImage(final FileHandler fileHandler, final PhotoDetail photo,
      final FileItem image, final boolean watermark, final String watermarkHD,
      final String watermarkOther) throws Exception {
    final String photoId = photo.getPhotoPK().getId();
    final String instanceId = photo.getPhotoPK().getInstanceId();

    if (image != null) {
      String name = image.getName();
      if (name != null) {
        name = FileUtil.getFilename(name);

        if (ImageType.isImage(name)) {

          final String subDirectory = gallerySettings.getString("imagesSubDirectory");
          final HandledFile handledImageFile =
              fileHandler.getHandledFile(BASE_PATH, instanceId, subDirectory + photoId, name);
          handledImageFile.writeByteArrayToFile(image.get());

          photo.setImageName(name);
          photo.setImageMimeType(image.getContentType());
          photo.setImageSize(image.getSize());

          createImage(name, handledImageFile, photo, subDirectory, watermark, watermarkHD,
              watermarkOther);
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
  public static void processImage(final FileHandler fileHandler, final PhotoDetail photo,
      final File image, final boolean watermark, final String watermarkHD,
      final String watermarkOther) throws Exception {
    final String photoId = photo.getPhotoPK().getId();
    final String instanceId = photo.getPhotoPK().getInstanceId();

    if (image != null) {
      String name = image.getName();
      if (name != null) {
        name = name.substring(name.lastIndexOf(File.separator) + 1, name.length());
        if (ImageType.isImage(name)) {
          String subDirectory = gallerySettings.getString("imagesSubDirectory");
          final HandledFile handledImageFile =
              fileHandler.getHandledFile(BASE_PATH, instanceId, subDirectory + photoId, name);
          fileHandler.copyFile(image, handledImageFile);

          photo.setImageName(name);
          photo.setImageMimeType(AttachmentController.getMimeType(name));
          photo.setImageSize(image.length());

          createImage(name, handledImageFile, photo, subDirectory, watermark, watermarkHD,
              watermarkOther);
        }
      }
    }
  }

  private static void createImage(final String name, final HandledFile handledImageFile,
      final PhotoDetail photo, final String subDirectory, final boolean watermark,
      final String watermarkHD, final String watermarkOther) throws Exception,
      ImageMetadataException {

    // Getting the percent size parameter value of watermark
    String percent = gallerySettings.getString("percentSizeWatermark");
    if (!StringUtil.isDefined(percent)) {
      percent = "1";
    }
    int percentSize = Integer.parseInt(percent);
    if (percentSize <= 0) {
      percentSize = 1;
    }

    if (ImageType.isValidExtension(name)) {

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
        SilverTrace.error("gallery", "ImageHelper.createImage",
            "gallery.ERR_CANT_CREATE_THUMBNAILS",
            "image = " + photo.getTitle() + " (#" + photo.getId() + ")");
      }
    }
  }

  public static void setMetaData(final FileHandler fileHandler, final PhotoDetail photo)
      throws IOException, ImageMetadataException {
    setMetaData(fileHandler, photo, I18NHelper.defaultLanguage);
  }

  public static void setMetaData(final FileHandler fileHandler, final PhotoDetail photo,
      final String lang) throws ImageMetadataException, IOException {
    final String photoId = photo.getPhotoPK().getId();
    final String name = photo.getImageName();
    final String mimeType = photo.getImageMimeType();

    if ("image/jpeg".equals(mimeType) || "image/pjpeg".equals(mimeType)) {
      final HandledFile handledFile =
          fileHandler.getHandledFile(BASE_PATH, photo.getInstanceId(),
          settings.getString("imagesSubDirectory") + photoId, name);
      if (handledFile.exists()) {
        final ImageMetadataExtractor extractor =
            new DrewImageMetadataExtractor(photo.getInstanceId());
        for (final MetaData meta : extractor.extractImageExifMetaData(handledFile.getFile(), lang)) {
          photo.addMetaData(meta);
        }
        for (final MetaData meta : extractor.extractImageIptcMetaData(handledFile.getFile(), lang)) {
          photo.addMetaData(meta);
        }
      }
    }
  }

  private static void getDimension(final BufferedImage image, final PhotoDetail photo)
      throws IOException {
    if (image == null) {
      photo.setSizeL(0);
      photo.setSizeH(0);
    } else {
      photo.setSizeL(image.getWidth());
      photo.setSizeH(image.getHeight());
    }
  }

  private static void createThumbnails(final PhotoDetail photo,
      final HandledFile originalHandedImageFile, final BufferedImage originalImage,
      final boolean watermark, final String nameWatermark) throws Exception {

    // File name
    final String photoId = photo.getId();

    // Preview image resizing only if the original image is larger than the preview
    int originalImageWidth = photo.getSizeL();
    if (photo.getSizeH() > photo.getSizeL()) {
      originalImageWidth = photo.getSizeH();
    }

    // Processing order :
    // XLarge (preview without watermark)
    // Preview
    // Large
    // Medium
    // Small
    final int[] imageSize = new int[]{600, 600, 266, 133, 66};
    final boolean[] isWatermark =
        new boolean[]{false, watermark, watermark, watermark, watermark};
    final int[] imageWatermarkSize =
        new int[]{0, Integer.parseInt(gallerySettings.getString("sizeWatermark600x400")),
      Integer.parseInt(gallerySettings.getString("sizeWatermark266x150")),
      Integer.parseInt(gallerySettings.getString("sizeWatermark133x100")),
      Integer.parseInt(gallerySettings.getString("sizeWatermark66x50"))};
    final String[] imageSuffixName =
        new String[]{thumbnailSuffix_Xlarge, previewSuffix, thumbnailSuffix_large,
      thumbnailSuffix_medium, thumbnailSuffix_small};

    // Image creation
    HandledFile currentThumblail = null;
    BufferedImage previewImage = null;
    for (int i = 0; i < imageSize.length; i++) {

      // Current thumbnail
      currentThumblail =
          originalHandedImageFile.getParentHandledFile().
          getHandledFile(photoId + imageSuffixName[i]);

      // Thumbnail creation
      redimPhoto((previewImage != null ? previewImage : originalImage), currentThumblail,
          (originalImageWidth > imageSize[i] ? imageSize[i] : originalImageWidth), isWatermark[i],
          nameWatermark, imageWatermarkSize[i]);

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

  public static Size getWidthAndHeight(final BufferedImage image, final int widthParam) {
    return ImageUtility.getWidthAndHeight(image, widthParam);
  }

  /**
   * Return the written file
   *
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
    watermarker.addWatermark(image, outputBuf, new Font("Arial", Font.BOLD, size), watermarkLabel,
        size);

    ImageIO.write(outputBuf, "JPEG", watermarkedTargetStream);
  }

  public static void pasteImage(final FileHandler fileHandler, final PhotoPK fromPK,
      final PhotoDetail image, final boolean cut) {
    final PhotoPK toPK = image.getPhotoPK();
    final String subDirectory = gallerySettings.getString("imagesSubDirectory");
    final HandledFile fromDir =
        fileHandler
        .getHandledFile(BASE_PATH, fromPK.getInstanceId(), subDirectory + fromPK.getId());
    final HandledFile toDir =
        fileHandler.getHandledFile(BASE_PATH, toPK.getInstanceId(), subDirectory + toPK.getId());

    // copier et renommer chaque image présente dans le répertoire d'origine
    if (fromDir.exists()) {

      // copy thumbnails & watermark (only if it does exist)
      for (final String thumbnailSuffix : new String[]{thumbnailSuffix_large,
            thumbnailSuffix_medium, thumbnailSuffix_small, previewSuffix, thumbnailSuffix_Xlarge,
            watermarkSuffix}) {
        pasteFile(fromDir.getHandledFile(fromPK.getId() + thumbnailSuffix),
            toDir.getHandledFile(toPK.getId() + thumbnailSuffix), cut);
      }

      // copy original image
      pasteFile(fromDir.getHandledFile(image.getImageName()), toDir.getHandledFile(image.
          getImageName()), cut);
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
            "Unable to copy file : fromImage = " + fromFile.getFile().getPath() + ", toImage = "
            + toFile.getFile().getPath(), e);
      }
    }
  }

  private static String computeWatermarkText(final String watermarkHD, final boolean watermark,
      final String type, final HandledFile image, final PhotoDetail photo, final int percentSize,
      final String watermarkOther) throws Exception, NumberFormatException, ImageMetadataException {
    String nameAuthor = "";
    String nameForWatermark = "";
    if (ImageType.isJpeg(type) && watermark) {
      final ImageMetadataExtractor extractor =
          new DrewImageMetadataExtractor(photo.getInstanceId());
      final List<MetaData> iptcMetadata = extractor.extractImageIptcMetaData(image.getFile());
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
    }
    return nameForWatermark;
  }

  private static String getWatermarkValue(final String property, final List<MetaData> iptcMetadata) {
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
