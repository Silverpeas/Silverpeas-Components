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
import java.io.InputStream;
import java.io.IOException;
import java.text.Normalizer;
import org.silverpeas.core.util.file.FileItem;

/** Bounded binary upload with a display-safe basename. Files are served as downloads. */
public final class DiscussionFileUpload {
  public static final int MAX_FILES = 4;
  public static final int MAX_BYTES = 20 * 1024 * 1024;
  private final byte[] content;
  private final String name;

  private DiscussionFileUpload(byte[] content, String name) {
    this.content = content;
    this.name = name;
  }

  public static DiscussionFileUpload read(FileItem file) {
    if (file.getSize() <= 0 || file.getSize() > MAX_BYTES) {
      throw new IllegalArgumentException("Invalid attachment size");
    }
    String original = file.getFileName();
    if (original == null) {
      throw new IllegalArgumentException("Missing attachment name");
    }
    String name = Normalizer.normalize(original.replace('\\', '/'), Normalizer.Form.NFC);
    name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}<>:\"|?*]", "_").strip();
    if (name.isBlank() || name.equals(".") || name.equals("..") || name.length() > 180) {
      throw new IllegalArgumentException("Invalid attachment name");
    }
    try (var input = file.getInputStream()) {
      byte[] bytes = input.readNBytes(MAX_BYTES + 1);
      if (bytes.length == 0 || bytes.length > MAX_BYTES) {
        throw new IllegalArgumentException("Invalid attachment size");
      }
      return new DiscussionFileUpload(bytes, name);
    } catch (IOException error) {
      throw new IllegalArgumentException("Cannot read attachment", error);
    }
  }

  public InputStream openStream() { return new ByteArrayInputStream(content); }
  public long size() { return content.length; }
  public String filename() { return name; }
}
