/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.discussion.service;

import java.util.List;
import jakarta.inject.Inject;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.annotation.Service;

/** Instance-level upload policy, read afresh so administration changes apply to new messages. */
@Service
public class DiscussionUploadSettings {
  public static final String MAX_IMAGES = "maxImagesPerMessage";
  public static final String IMAGE_SIZE = "maxImageSizeMb";
  public static final String FILE_SIZE = "maxFileSizeMb";
  private static final int MEBIBYTE = 1024 * 1024;
  @Inject
  private OrganizationController organization;

  public record Limits(int maxImages, int imageSizeMb, int fileSizeMb) {
    public Limits {
      if (maxImages < 1 || imageSizeMb < 1 || fileSizeMb < 1
          || imageSizeMb > (Integer.MAX_VALUE - 1) / MEBIBYTE
          || fileSizeMb > (Integer.MAX_VALUE - 1) / MEBIBYTE) {
        throw new IllegalArgumentException("Invalid attachment limits");
      }
    }
    public static Limits defaults() { return new Limits(4, 5, 20); }
    public int getMaxImages() { return maxImages; }
    public int getImageSizeMb() { return imageSizeMb; }
    public int getFileSizeMb() { return fileSizeMb; }
    public int getMaxFiles() { return DiscussionFileUpload.MAX_FILES; }
    public int getImageBytes() { return imageSizeMb * MEBIBYTE; }
    public int getFileBytes() { return fileSizeMb * MEBIBYTE; }

    public void validate(List<DiscussionImageUpload> images, List<DiscussionFileUpload> files) {
      if (images == null || images.size() > maxImages
          || images.stream().anyMatch(image -> image == null || image.size() > getImageBytes())) {
        throw new IllegalArgumentException("Invalid images for this instance");
      }
      if (files == null || files.size() > getMaxFiles()
          || files.stream().anyMatch(file -> file == null || file.size() > getFileBytes())) {
        throw new IllegalArgumentException("Invalid attachments for this instance");
      }
    }
  }

  public Limits forInstance(String instanceId) {
    return new Limits(parameter(instanceId, MAX_IMAGES, 4, Integer.MAX_VALUE),
        parameter(instanceId, IMAGE_SIZE, 5, (Integer.MAX_VALUE - 1) / MEBIBYTE),
        parameter(instanceId, FILE_SIZE, 20, (Integer.MAX_VALUE - 1) / MEBIBYTE));
  }

  private int parameter(String instanceId, String name, int fallback, int maximum) {
    String value = organization.getComponentParameterValue(instanceId, name);
    if (value == null || value.isBlank()) { return fallback; }
    try {
      int parsed = Integer.parseInt(value.strip());
      return parsed > 0 && parsed <= maximum ? parsed : fallback;
    } catch (NumberFormatException invalid) {
      return fallback;
    }
  }
}
