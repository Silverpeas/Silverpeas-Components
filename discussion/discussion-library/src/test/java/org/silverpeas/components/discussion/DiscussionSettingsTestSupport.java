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
package org.silverpeas.components.discussion;

import java.util.Map;
import org.mockito.MockedStatic;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Simulates customer overrides without changing the developer's installed configuration. */
public final class DiscussionSettingsTestSupport {
  private DiscussionSettingsTestSupport() { }

  public static MockedStatic<ResourceLocator> configured(Map<String, String> values) {
    var bundle = mock(SettingBundle.class);
    when(bundle.getInteger(anyString(), anyInt())).thenAnswer(call -> {
      String value = values.get(call.getArgument(0));
      return value == null ? call.getArgument(1) : Integer.parseInt(value.strip());
    });
    var locator = mockStatic(ResourceLocator.class, CALLS_REAL_METHODS);
    locator.when(() -> ResourceLocator.getSettingBundle(DiscussionSettings.BUNDLE)).thenReturn(bundle);
    return locator;
  }
}
