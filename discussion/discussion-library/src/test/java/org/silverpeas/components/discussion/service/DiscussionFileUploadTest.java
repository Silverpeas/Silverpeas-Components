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
import org.junit.jupiter.api.Test;
import org.silverpeas.core.util.file.FileItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscussionFileUploadTest {
  static FileItem file(String name, byte[] data) throws Exception {
    var file = mock(FileItem.class);
    when(file.getFileName()).thenReturn(name);
    when(file.getSize()).thenReturn((long) data.length);
    when(file.getInputStream()).thenAnswer(call -> new ByteArrayInputStream(data));
    when(file.getFieldName()).thenReturn("files");
    return file;
  }

  @Test
  void uploadsKeepTheirBasenameAndNeverUseClientPaths() throws Exception {
    var upload = DiscussionFileUpload.read(file("C:\\private\\budget <2026>.pdf", new byte[]{1,2,3}));
    assertEquals("budget _2026_.pdf", upload.filename());
    assertEquals(3, upload.size());
    assertArrayEquals(new byte[]{1,2,3}, upload.openStream().readAllBytes());
    assertEquals("note.txt", DiscussionFileUpload.read(file("../../note.txt", new byte[]{1})).filename());
  }

  @Test
  void emptyOversizedAndInvalidNamesAreRejectedBeforeStorage() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> DiscussionFileUpload.read(file("empty", new byte[0])));
    assertThrows(IllegalArgumentException.class, () -> DiscussionFileUpload.read(file("..", new byte[]{1})));
    var huge = file("big.zip", new byte[]{1});
    when(huge.getSize()).thenReturn((long) DiscussionFileUpload.MAX_BYTES + 1);
    assertThrows(IllegalArgumentException.class, () -> DiscussionFileUpload.read(huge));
    verify(huge, never()).getInputStream();
  }
}
