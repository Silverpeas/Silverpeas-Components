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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.util.file.FileItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscussionImageUploadTest {
  static FileItem image(String format) throws Exception {
    var output = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), format, output));
    return file(output.toByteArray());
  }

  static FileItem file(byte[] content) throws Exception {
    var file = mock(FileItem.class);
    when(file.getSize()).thenReturn((long) content.length);
    when(file.getInputStream()).thenAnswer(call -> new ByteArrayInputStream(content));
    when(file.getFieldName()).thenReturn("images");
    when(file.getFileName()).thenReturn("../../fake.svg");
    return file;
  }

  @Test
  void fileContentDeterminesTheTypeAndGeneratedStorageName() throws Exception {
    for (String format : java.util.List.of("jpeg", "png", "gif")) {
      var upload = DiscussionImageUpload.read(image(format));
      assertEquals("image/" + format, upload.contentType());
      assertTrue(upload.filename().endsWith("." + format));
      assertFalse(upload.filename().contains("/"));
      try (var input = upload.openStream()) {
        assertNotNull(ImageIO.read(input));
      }
    }
  }

  @Test
  void invalidActiveEmptyOrOversizedContentIsRejected() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> DiscussionImageUpload.read(file("<svg onload='alert(1)'/>".getBytes())));
    assertThrows(IllegalArgumentException.class, () -> DiscussionImageUpload.read(file(new byte[0])));
    var oversized = mock(FileItem.class);
    when(oversized.getSize()).thenReturn((long) DiscussionImageUpload.MAX_BYTES + 1);
    assertThrows(IllegalArgumentException.class, () -> DiscussionImageUpload.read(oversized));
    verify(oversized, never()).getInputStream();
    var misleading = file(new byte[DiscussionImageUpload.MAX_BYTES + 1]);
    when(misleading.getSize()).thenReturn(1L);
    assertThrows(IllegalArgumentException.class, () -> DiscussionImageUpload.read(misleading));
    byte[] truncated = {(byte)0x89, 'P', 'N', 'G', 13, 10, 26, 10};
    assertThrows(IllegalArgumentException.class, () -> DiscussionImageUpload.read(file(truncated)));
  }
}
