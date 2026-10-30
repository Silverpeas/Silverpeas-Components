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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.silverpeas.core.util.file.FileItem;

/** Validated raster upload. MIME and extension are derived from the file, not the browser. */
public final class DiscussionImageUpload {
  public static final int MAX_IMAGES = 4;
  public static final int MAX_BYTES = 5 * 1024 * 1024;
  private final byte[] content;
  private final String format;

  private DiscussionImageUpload(byte[] content, String format) {
    this.content = content;
    this.format = format;
  }

  public static DiscussionImageUpload read(FileItem file) {
    if (file.getSize() <= 0 || file.getSize() > MAX_BYTES) {
      throw new IllegalArgumentException("Invalid image size");
    }
    try (var input = file.getInputStream()) {
      byte[] content = input.readNBytes(MAX_BYTES + 1);
      if (content.length == 0 || content.length > MAX_BYTES) {
        throw new IllegalArgumentException("Invalid image size");
      }
      try (var imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
        var readers = ImageIO.getImageReaders(imageInput);
        if (!readers.hasNext()) {
          throw new IllegalArgumentException("Unsupported image");
        }
        var reader = readers.next();
        try {
          reader.setInput(imageInput);
          String format = reader.getFormatName().toLowerCase(Locale.ROOT);
          if (!java.util.Set.of("jpeg", "png", "gif").contains(format)
              || reader.getWidth(0) <= 0 || reader.getHeight(0) <= 0
              || (long) reader.getWidth(0) * reader.getHeight(0) > 40000000L) {
            throw new IllegalArgumentException("Unsupported image");
          }
          // Decode the first frame too: a valid header alone is not an image.
          if (reader.read(0) == null) {
            throw new IllegalArgumentException("Invalid image");
          }
          return new DiscussionImageUpload(content, format);
        } finally {
          reader.dispose();
        }
      }
    } catch (IOException failure) {
      throw new IllegalArgumentException("Invalid image", failure);
    }
  }

  public InputStream openStream() { return new ByteArrayInputStream(content); }
  public long size() { return content.length; }
  public String contentType() { return "image/" + format; }
  public String filename() { return "image-" + java.util.UUID.randomUUID() + "." + format; }
}
