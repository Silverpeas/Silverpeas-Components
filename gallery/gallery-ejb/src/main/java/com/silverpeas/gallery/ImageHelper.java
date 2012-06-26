/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery;

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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageHelper {

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
  public static void processImage(PhotoDetail photo, FileItem image, String subDirectory,
    boolean watermark, String watermarkHD, String watermarkOther) throws Exception {
    String photoId = photo.getPhotoPK().getId();
    String instanceId = photo.getPhotoPK().getInstanceId();

    if (image != null) {
      String name = image.getName();
      if (name != null) {
        if (!FileUtil.isWindows()) {
          name = name.replace('\\', File.separatorChar);
          SilverTrace.info("gallery", "ImageHelper.processImage",
            "root.MSG_GEN_PARAM_VALUE", "fileName on Unix = " + name);
        }

        name = name.substring(name.lastIndexOf(File.separator) + 1, name.length());
        if (ImageType.isImage(name)) {
          File dir = new File(FileRepositoryManager.getAbsolutePath(instanceId)
            + subDirectory + photoId + File.separator + name);
          String mimeType = image.getContentType();
          long size = image.getSize();
          // création du répertoire pour mettre la photo
          String nameRep = subDirectory + photoId;
          FileRepositoryManager.createAbsolutePath(instanceId, nameRep);
          image.write(dir);
          photo.setImageName(name);
          photo.setImageMimeType(mimeType);
          photo.setImageSize(size);
          createImage(name, dir, photo, subDirectory, watermark, watermarkHD, watermarkOther);
        }
      }
    }
  }

  /**
   * In case of drag And Drop upload
   *
   * @param photo
   * @param image
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @throws Exception
   */
  public static void processImage(PhotoDetail photo, File image, boolean watermark,
    String watermarkHD, String watermarkOther) throws Exception {
    String photoId = photo.getPhotoPK().getId();
    String instanceId = photo.getPhotoPK().getInstanceId();

    String percent = gallerySettings.getString("percentSizeWatermark");
    if (!StringUtil.isDefined(percent)) {
      percent = "1";
    }
    int percentSize = Integer.parseInt(percent);
    if (percentSize <= 0) {
      percentSize = 1;
    }

    if (image != null) {
      String name = image.getName();
      if (name != null) {
        name = name.substring(name.lastIndexOf(File.separator) + 1, name.length());
        if (ImageType.isImage(name)) {
          String subDirectory = gallerySettings.getString("imagesSubDirectory");

          String dir = FileRepositoryManager.getAbsolutePath(instanceId) + subDirectory + photoId
            + File.separator + name;

          String mimeType = FileUtil.getMimeType(name);
          long size = image.length();

          // création du répertoire pour mettre la photo
          String nameRep = subDirectory + photoId;
          FileRepositoryManager.createAbsolutePath(instanceId, nameRep);

          FileRepositoryManager.copyFile(image.getAbsolutePath(), dir);

          photo.setImageName(name);
          photo.setImageMimeType(mimeType);
          photo.setImageSize(size);

          createImage(name, image, photo, subDirectory, watermark, watermarkHD, watermarkOther);
        }
      }
    }
  }

  private static void createImage(String name, File imageFile, PhotoDetail photo,
    String subDirectory,
    boolean watermark, String watermarkHD, String watermarkOther) throws IOException,
    ImageMetadataException {
    String type = FileRepositoryManager.getFileExtension(name);
    String photoId = photo.getPhotoPK().getId();
    String instanceId = photo.getPhotoPK().getInstanceId();

    // recherche du paramètre du pourcentage de la taille du watermark
    String percent = gallerySettings.getString("percentSizeWatermark");
    if (!StringUtil.isDefined(percent)) {
      percent = "1";
    }
    int percentSize = Integer.parseInt(percent);
    if (percentSize <= 0) {
      percentSize = 1;
    }

    if (ImageType.isValidExtension(name)) {
      // recherche de la taille de l'image
      BufferedImage image = ImageLoader.loadImage(imageFile);
      getDimension(image, photo);
      String pathFile = FileRepositoryManager.getAbsolutePath(instanceId)
        + subDirectory + photoId + File.separator;
      String nameForWatermark = computeWatermarkText(watermarkHD, watermark, type, imageFile, photo,
        pathFile, percentSize, watermarkOther);
      // création de la preview et des vignettes
      createVignettes(photo, pathFile, image, watermark, nameForWatermark);
    }
  }

  public static void setMetaData(PhotoDetail photo) throws IOException, ImageMetadataException {
    setMetaData(photo, I18NHelper.defaultLanguage);
  }

  public static void setMetaData(PhotoDetail photo, String lang) throws ImageMetadataException,
    IOException {
    String photoId = photo.getPhotoPK().getId();
    String name = photo.getImageName();
    String mimeType = photo.getImageMimeType();

    if ("image/jpeg".equals(mimeType) || "image/pjpeg".equals(mimeType)) {
      File file = new File(FileRepositoryManager.getAbsolutePath(photo.getInstanceId())
        + settings.getString("imagesSubDirectory") + photoId + File.separator + name);
      if (file != null && file.exists()) {
        ImageMetadataExtractor extractor = new DrewImageMetadataExtractor(photo.getInstanceId());
        List<MetaData> metadata = extractor.extractImageExifMetaData(file, lang);
        for (MetaData meta : metadata) {
          photo.addMetaData(meta);
        }
        metadata = extractor.extractImageIptcMetaData(file, lang);
        for (MetaData meta : metadata) {
          photo.addMetaData(meta);
        }
      }
    }
  }

  private static void getDimension(BufferedImage image, PhotoDetail photo) throws IOException {
    if (image == null) {
      photo.setSizeL(0);
      photo.setSizeH(0);
    } else {
      photo.setSizeL(image.getWidth());
      photo.setSizeH(image.getHeight());
    }
  }

  private static void createVignettes(PhotoDetail photo, String path, BufferedImage originalImage,
    boolean watermark, String nameWatermark) throws IOException {
    String fileId = photo.getId();

    // création d'une preview sans watermark (pour être utilisée pour créer les
    // vignettes)
    String previewFile = path + fileId + thumbnailSuffix_Xlarge;
    int vignetteFile = 600;
    // on ne redimensionne l'image en preview que si l'image est plus grande que
    // la preview
    int largeWidth = photo.getSizeL();
    if (photo.getSizeH() > photo.getSizeL()) {
      largeWidth = photo.getSizeH();
    }
    if (largeWidth > vignetteFile) {
      redimPhoto(originalImage, previewFile, vignetteFile, false, nameWatermark, 0);
    } else {
      redimPhoto(originalImage, previewFile, largeWidth, false, nameWatermark, 0);
    }

    BufferedImage previewImage = ImageLoader.loadImage(new File(previewFile));

    // 1/ création de la preview
    int sizeWatermarkPreview = Integer.parseInt(gallerySettings.getString("sizeWatermark600x400"));
    String previewFileWatermark = path + fileId + previewSuffix;
    int previewWidth = 600;
    // on ne redimensionne l'image en preview que si l'image est plus grande que
    // la preview
    if (photo.getSizeH() > photo.getSizeL()) {
      largeWidth = photo.getSizeH();
    }
    if (largeWidth > previewWidth) {
      redimPhoto(previewImage, previewFileWatermark, previewWidth, watermark,
        nameWatermark, sizeWatermarkPreview);
    } else {
      redimPhoto(previewImage, previewFileWatermark, largeWidth, watermark,
        nameWatermark, sizeWatermarkPreview);
    }

    // 2/ création de la vignette 266x150
    int sizeWatermark266x150 = Integer.parseInt(gallerySettings.getString("sizeWatermark266x150"));
    String vignetteFile1 = path + fileId + thumbnailSuffix_large;
    int vignetteWidth1 = 266;
    if (largeWidth > vignetteWidth1) {
      redimPhoto(previewImage, vignetteFile1, vignetteWidth1, watermark, nameWatermark,
        sizeWatermark266x150);
    } else {
      redimPhoto(previewImage, vignetteFile1, largeWidth, watermark, nameWatermark,
        sizeWatermark266x150);
    }

    // création de la vignette 133x100
    int sizeWatermark133x100 = Integer.parseInt(gallerySettings.getString("sizeWatermark133x100"));
    String vignetteFile2 = path + fileId + thumbnailSuffix_medium;
    int vignetteWidth2 = 133;
    if (largeWidth > vignetteWidth2) {
      redimPhoto(previewImage, vignetteFile2, vignetteWidth2, watermark, nameWatermark,
        sizeWatermark133x100);
    } else {
      redimPhoto(previewImage, vignetteFile2, largeWidth, watermark, nameWatermark,
        sizeWatermark133x100);
    }

    // création de la vignette 50x66
    int sizeWatermark50x66 = Integer.parseInt(gallerySettings.getString("sizeWatermark66x50"));
    String vignetteFile3 = path + fileId + thumbnailSuffix_small;
    int vignetteWidth3 = 66;
    if (largeWidth > vignetteWidth3) {
      redimPhoto(previewImage, vignetteFile3, vignetteWidth3, watermark, nameWatermark,
        sizeWatermark50x66);
    } else {
      redimPhoto(previewImage, vignetteFile3, largeWidth, watermark, nameWatermark,
        sizeWatermark50x66);
    }
  }

  public static Size getWidthAndHeight(String instanceId, String subDir,
    String imageName, int baseWidth) throws IOException {
    return ImageUtility.getWidthAndHeight(instanceId, subDir, imageName, baseWidth);
  }

  public static Size getWidthAndHeight(BufferedImage image, int widthParam) {
    return ImageUtility.getWidthAndHeight(image, widthParam);
  }

  private static void redimPhoto(BufferedImage image, String outputFile,
    int widthParam, boolean watermark, String nameWatermark, int sizeWatermark)
    throws IOException {

    ImageResizer resizer = new ImageResizer(image, widthParam);
    if (watermark) {
      resizer.resizeImageWithWatermark(outputFile, nameWatermark, sizeWatermark);
    } else {
      resizer.resizeImage(outputFile);
    }
  }

  private static void createWatermark(String watermarkedTargetFile, String watermarkLabel,
    BufferedImage image, int percentSizeWatermark) throws IOException {

    

    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();

    // création du buffer a la même taille
    BufferedImage outputBuf = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

    double max = Math.max(imageWidth, imageHeight);

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
    Watermarker watermarker = new Watermarker(imageWidth, imageHeight);
    watermarker.addWatermark(image, outputBuf, new Font("Arial", Font.BOLD, size), watermarkLabel, size);

    File fileWatermark = new File(watermarkedTargetFile);
    ImageIO.write(outputBuf, "JPEG", fileWatermark);
  }

  public static void pasteImage(PhotoPK fromPK, PhotoDetail image, boolean cut) {
    PhotoPK toPK = image.getPhotoPK();
    String toAbsolutePath = FileRepositoryManager.getAbsolutePath(toPK.getInstanceId());
    String fromAbsolutePath = FileRepositoryManager.getAbsolutePath(fromPK.getInstanceId());
    String subDirectory = gallerySettings.getString("imagesSubDirectory");
    String fromDir = fromAbsolutePath + subDirectory + fromPK.getId() + File.separator;
    String toDir = toAbsolutePath + subDirectory + toPK.getId() + File.separator;
    // création du répertoire pour mettre la photo
    String nameRep = subDirectory + toPK.getId();
    try {
      FileRepositoryManager.createAbsolutePath(toPK.getInstanceId(), nameRep);
    } catch (Exception e) {
      SilverTrace.error("gallery", "ImageHelper.pasteImage", "root.MSG_GEN_PARAM_VALUE",
        "Unable to create dir : " + toAbsolutePath + nameRep, e);
    }

    // copier et renommer chaque image présente dans le répertoire d'origine
    File dirToCopy = new File(fromDir);
    if (dirToCopy.exists()) {
      // copier vignettes
      String fromImage = fromDir + fromPK.getId() + thumbnailSuffix_large;
      String toImage = toDir + toPK.getId() + thumbnailSuffix_large;
      pasteFile(fromImage, toImage, cut);

      fromImage = fromDir + fromPK.getId() + thumbnailSuffix_medium;
      toImage = toDir + toPK.getId() + thumbnailSuffix_medium;
      pasteFile(fromImage, toImage, cut);

      fromImage = fromDir + fromPK.getId() + thumbnailSuffix_small;
      toImage = toDir + toPK.getId() + thumbnailSuffix_small;
      pasteFile(fromImage, toImage, cut);

      // copier preview
      fromImage = fromDir + fromPK.getId() + previewSuffix;
      toImage = toDir + toPK.getId() + previewSuffix;
      pasteFile(fromImage, toImage, cut);

      // copier preview sans watermark
      fromImage = fromDir + fromPK.getId() + thumbnailSuffix_Xlarge;
      toImage = toDir + toPK.getId() + thumbnailSuffix_Xlarge;
      pasteFile(fromImage, toImage, cut);

      // copier originale
      fromImage = fromDir + image.getImageName();
      toImage = toDir + image.getImageName();
      pasteFile(fromImage, toImage, cut);

      // copie original avec Watermark si elle existe
      fromImage = fromDir + fromPK.getId() + watermarkSuffix;
      File fromFile = new File(fromImage);
      // original with watermark does not exist (if watermark is not used)
      // copy file only if original with watermark exists
      if (fromFile != null && fromFile.exists()) {
        toImage = toDir + toPK.getId() + watermarkSuffix;
        pasteFile(fromImage, toImage, cut);
      }
    }
  }

  private static void pasteFile(String fromImage, String toImage, boolean cut) {
    File fromFile = new File(fromImage);
    File toFile = new File(toImage);
    if (fromFile.exists()) {
      if (cut && fromFile.renameTo(toFile)) {
        return;
      }
      try {
        FileRepositoryManager.copyFile(fromImage, toImage);
      } catch (Exception e) {
        SilverTrace.error("gallery", "ImageHelper.pasteFile", "root.MSG_GEN_PARAM_VALUE",
          "Unable to copy file : fromImage = " + fromImage + ", toImage = " + toImage, e);
      }
    }
  }

  private static String computeWatermarkText(String watermarkHD, boolean watermark, String type,
    File image, PhotoDetail photo, String pathFile, int percentSize, String watermarkOther) throws
    IOException, NumberFormatException, ImageMetadataException {
    String nameAuthor = "";
    String nameForWatermark = "";
    if (ImageType.isJpeg(type) && watermark) {
      ImageMetadataExtractor extractor = new DrewImageMetadataExtractor(photo.getInstanceId());
      List<MetaData> iptcMetadata = extractor.extractImageIptcMetaData(image);
      BufferedImage bufferedImage = ImageLoader.loadImage(image);
      if (StringUtil.isDefined(watermarkHD)) {
        // création d'un duplicata de l'image originale avec intégration du
        // watermark
        String value = getWatermarkValue(watermarkHD, iptcMetadata);
        if (value != null) {
          nameAuthor = value;
        }
        if (!nameAuthor.isEmpty()) {
          String watermarkFile = pathFile + photo.getId() + "_watermark.jpg";
          createWatermark(watermarkFile, nameAuthor, bufferedImage, percentSize);
        }
      }
      if (StringUtil.isDefined(watermarkOther)) {
        String value = getWatermarkValue(watermarkOther, iptcMetadata);
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

  private static String getWatermarkValue(String property, List<MetaData> iptcMetadata) {
    String value = null;
    for (MetaData metadata : iptcMetadata) {
      if (property.equalsIgnoreCase(metadata.getProperty())) {
        value = metadata.getValue();
      }
    }
    return value;
  }

  private ImageHelper() {
  }
}
