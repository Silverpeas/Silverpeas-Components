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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Verifies the watermark image URL, an instance parameter a space manager can set, cannot make the
 * server request the services it deliberately keeps for itself.
 * @author mmoquillon
 */
class WatermarkTest {

  @Test
  void theServicesBoundToTheServerItselfArentReachable() {
    assertThat(Watermark.isFetchable("http://127.0.0.1:9990/management"), is(false));
    assertThat(Watermark.isFetchable("http://127.15.42.7/"), is(false));
    assertThat(Watermark.isFetchable("https://localhost/logo.png"), is(false));
    assertThat(Watermark.isFetchable("http://[::1]/logo.png"), is(false));
    assertThat(Watermark.isFetchable("http://0.0.0.0/logo.png"), is(false));
  }

  @Test
  void theMetadataEndpointOfACloudProviderIsntReachable() {
    assertThat(Watermark.isFetchable(
        "http://169.254.169.254/latest/meta-data/iam/security-credentials/"), is(false));
  }

  /**
   * The internal resources of an organization legitimately live in its private networks, and no
   * address range can tell them from the internal services one would rather protect.
   */
  @Test
  void theResourcesOfThePrivateNetworksOfAnOrganizationRemainReachable() {
    assertThat(Watermark.isFetchable("http://10.0.3.14/watermark.png"), is(true));
    assertThat(Watermark.isFetchable("https://172.16.5.4/watermark.png"), is(true));
    assertThat(Watermark.isFetchable("http://192.168.1.20:8080/watermark.png"), is(true));
  }

  @Test
  void onlyTheHttpSchemesAreHandled() {
    assertThat(Watermark.isFetchable("file:///etc/passwd"), is(false));
    assertThat(Watermark.isFetchable("ftp://10.0.3.14/watermark.png"), is(false));
    assertThat(Watermark.isFetchable("jar:http://10.0.3.14/a.jar!/watermark.png"), is(false));
    assertThat(Watermark.isFetchable("/var/lib/silverpeas/watermark.png"), is(false));
    assertThat(Watermark.isFetchable("http://"), is(false));
    assertThat(Watermark.isFetchable("not an url at all"), is(false));
  }
}
