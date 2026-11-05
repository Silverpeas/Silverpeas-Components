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
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionUploadSettingsTest {
  @TestManagedMock
  private OrganizationController organization;
  @TestedBean
  private DiscussionUploadSettings settings;

  @Test
  void existingInstancesRetainDefaultsAndInvalidParametersFallBackIndividually() {
    assertEquals(DiscussionUploadSettings.Limits.defaults(), settings.forInstance("discussion1"));
    for (String invalid : List.of("", " ", "0", "-2", "abc", "1.5", "2147483648")) {
      when(organization.getComponentParameterValue("discussion1", DiscussionUploadSettings.MAX_IMAGES)).thenReturn(invalid);
      when(organization.getComponentParameterValue("discussion1", DiscussionUploadSettings.IMAGE_SIZE)).thenReturn(invalid);
      when(organization.getComponentParameterValue("discussion1", DiscussionUploadSettings.FILE_SIZE)).thenReturn(invalid);
      assertEquals(DiscussionUploadSettings.Limits.defaults(), settings.forInstance("discussion1"));
    }
    when(organization.getComponentParameterValue("discussion1", DiscussionUploadSettings.IMAGE_SIZE)).thenReturn("2048");
    assertEquals(5, settings.forInstance("discussion1").imageSizeMb());
  }

  @Test
  void limitsAreIsolatedPerInstanceAndReadAgainAfterAdministrationChanges() {
    when(organization.getComponentParameterValue("discussion1", DiscussionUploadSettings.MAX_IMAGES)).thenReturn(" 7 ");
    when(organization.getComponentParameterValue("discussion1", DiscussionUploadSettings.IMAGE_SIZE)).thenReturn("8");
    when(organization.getComponentParameterValue("discussion1", DiscussionUploadSettings.FILE_SIZE)).thenReturn("50");
    var limits = settings.forInstance("discussion1");
    assertEquals(new DiscussionUploadSettings.Limits(7, 8, 50), limits);
    assertEquals(8 * 1024 * 1024, limits.getImageBytes());
    assertEquals(50 * 1024 * 1024, limits.getFileBytes());
    assertEquals(DiscussionUploadSettings.Limits.defaults(), settings.forInstance("discussion2"));
    when(organization.getComponentParameterValue("discussion1", DiscussionUploadSettings.IMAGE_SIZE)).thenReturn("2");
    assertEquals(2, settings.forInstance("discussion1").imageSizeMb());
  }

  @Test
  void theBusinessPolicyRejectsExcessCountsAndUploadsCreatedWithAnotherInstancesLargerLimit() throws Exception {
    var image = DiscussionImageUpload.read(DiscussionImageUploadTest.image("png"));
    var file = DiscussionFileUpload.read(DiscussionFileUploadTest.file("data.bin", new byte[1024 * 1024 + 1]));
    var limits = new DiscussionUploadSettings.Limits(1, 1, 1);
    limits.validate(List.of(image), List.of());
    assertThrows(IllegalArgumentException.class, () -> limits.validate(List.of(image, image), List.of()));
    assertThrows(IllegalArgumentException.class, () -> limits.validate(List.of(), List.of(file)));
    var tooLargeImage = mock(DiscussionImageUpload.class);
    when(tooLargeImage.size()).thenReturn(1024 * 1024 + 1L);
    assertThrows(IllegalArgumentException.class, () -> limits.validate(List.of(tooLargeImage), List.of()));
    assertThrows(IllegalArgumentException.class, () -> limits.validate(null, List.of()));
    assertThrows(IllegalArgumentException.class, () -> limits.validate(List.of(), null));
  }
  @Test
  void nativeDescriptorDeclaresTheThreeEditableInstanceParameters() throws Exception {
    var descriptor = java.nio.file.Path.of(System.getProperty("basedir"), "..", "discussion-configuration",
        "src", "main", "config", "xmlcomponents", "discussion.xml");
    var component = jakarta.xml.bind.JAXBContext.newInstance(org.silverpeas.core.admin.component.model.WAComponent.class)
        .createUnmarshaller().unmarshal(new javax.xml.transform.stream.StreamSource(descriptor.toFile()),
            org.silverpeas.core.admin.component.model.WAComponent.class).getValue();
    var parameters = component.getParameters();
    assertEquals(3, parameters.size());
    assertEquals(List.of(DiscussionUploadSettings.MAX_IMAGES, DiscussionUploadSettings.IMAGE_SIZE,
        DiscussionUploadSettings.FILE_SIZE), parameters.stream().map(org.silverpeas.core.admin.component.model.Parameter::getName).toList());
    assertEquals(List.of("4", "5", "20"), parameters.stream().map(org.silverpeas.core.admin.component.model.Parameter::getValue).toList());
    assertTrue(parameters.stream().allMatch(parameter -> "always".equals(parameter.getUpdatable())));
  }

}
