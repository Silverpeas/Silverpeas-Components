/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mailinglist.service.job;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class BetterMimeMessageTest {

  public BetterMimeMessageTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of isSpam method, of class BetterMimeMessage.
   * @throws Exception
   */
  @Test
  public void testIsSpam() throws Exception {
    MimeMessage automatic = mock(MimeMessage.class);
    when(automatic.getHeader("X-Spam-Flag")).thenReturn(new String[]{"YES"});
    BetterMimeMessage instance = new BetterMimeMessage(automatic);
    assertTrue(instance.isSpam());
    automatic = mock(MimeMessage.class);
    when(automatic.getHeader("X-Spam-Flag")).thenReturn(new String[]{"NO"});
    instance = new BetterMimeMessage(automatic);
    assertFalse(instance.isSpam());
     automatic = mock(MimeMessage.class);
    when(automatic.getHeader("X-Spam-Flag")).thenReturn(null);
    instance = new BetterMimeMessage(automatic);
    assertFalse(instance.isSpam());
  }

  /**
   * Test of getSpamLevel method, of class BetterMimeMessage.
   * @throws Exception
   */
  @Test
  public void testGetSpamLevel() throws Exception {
    MimeMessage automatic = mock(MimeMessage.class);
    when(automatic.getHeader("X-Spam-Score")).thenReturn(new String[]{"4.32"});
    BetterMimeMessage instance = new BetterMimeMessage(automatic);
    assertEquals(4.32, instance.getSpamLevel(), 0.01f);
    automatic = mock(MimeMessage.class);
    when(automatic.getHeader("X-Spam-Score")).thenReturn(null);
    instance = new BetterMimeMessage(automatic);
    assertEquals(0.0f, instance.getSpamLevel(), 0.01f);

  }

  /**
   * Test of isBounced method, of class BetterMimeMessage.
   */
  @Test
  public void testIsBounced() throws Exception {
    MimeMessage automatic = mock(MimeMessage.class);
    when(automatic.getHeader("Auto-Submitted")).thenReturn(new String[]{"bart", "auto-replied"});
    BetterMimeMessage instance = new BetterMimeMessage(automatic);
    assertTrue(instance.isBounced());

    automatic = mock(MimeMessage.class);
    when(automatic.getHeader("Auto-Submitted")).thenReturn(new String[]{"bart", "auto-generated"});
    instance = new BetterMimeMessage(automatic);
    assertTrue(instance.isBounced());

    automatic = mock(MimeMessage.class);
    when(automatic.getHeader("Auto-Submitted")).thenReturn(new String[]{"bart", "auto-notified; "
          + "owner-email=\"me@example.com\""});
    instance = new BetterMimeMessage(automatic);
    assertTrue(instance.isBounced());

    automatic = mock(MimeMessage.class);
    when(automatic.getHeader("Auto-Submitted")).thenReturn(new String[]{"bart", "auto-notified; "
          + "owner-token=af3NN2pK5dDXI0W"});
    instance = new BetterMimeMessage(automatic);
    assertTrue(instance.isBounced());


    automatic = mock(MimeMessage.class);
    when(automatic.getContent()).thenReturn("Hello World");
    instance = new BetterMimeMessage(automatic);
    when(automatic.getContentType()).thenReturn("multipart/report; report-type=delivery-status;\n\t"
        + "boundary=\"A3DB01912007.1281602599/zimbra.oevo.com\"");
    assertTrue(instance.isBounced());
    when(automatic.getContentType()).thenReturn("message/delivery-status");
    assertTrue(instance.isBounced());
    when(automatic.getContentType()).thenReturn("message/rfc822");
    assertFalse(instance.isBounced());
    when(automatic.getContentType()).thenReturn("text/plain; charset=utf-8");
    assertFalse(instance.isBounced());
  }

  /**
   * Test of isAutomaticMessage method, of class BetterMimeMessage.
   * @throws MessagingException
   */
  @Test
  public void testIsAutomaticMessage() throws MessagingException {
    MimeMessage automatic = mock(MimeMessage.class);
    when(automatic.getHeader("Auto-Submitted")).thenReturn(new String[]{"bart", "auto-replied"});
    BetterMimeMessage instance = new BetterMimeMessage(automatic);
    assertTrue(instance.isAutomaticMessage());

    automatic = mock(MimeMessage.class);
    when(automatic.getHeader("Auto-Submitted")).thenReturn(new String[]{"bart", "auto-generated"});
    instance = new BetterMimeMessage(automatic);
    assertTrue(instance.isAutomaticMessage());

    automatic = mock(MimeMessage.class);
    when(automatic.getHeader("Auto-Submitted")).thenReturn(new String[]{"bart", "auto-notified; "
          + "owner-email=\"me@example.com\""});
    instance = new BetterMimeMessage(automatic);
    assertTrue(instance.isAutomaticMessage());

    automatic = mock(MimeMessage.class);
    when(automatic.getHeader("Auto-Submitted")).thenReturn(new String[]{"bart", "auto-notified; "
          + "owner-token=af3NN2pK5dDXI0W"});
    instance = new BetterMimeMessage(automatic);
    assertTrue(instance.isAutomaticMessage());
  }

  /**
   * Test of isNotification method, of class BetterMimeMessage.
   * @throws MessagingException
   */
  @Test
  public void testIsNotification() throws MessagingException {
    MimeMessage report = mock(MimeMessage.class);
    BetterMimeMessage instance = new BetterMimeMessage(report);
    String contentType = "multipart/report; report-type=delivery-status;\n\t"
        + "boundary=\"A3DB01912007.1281602599/zimbra.oevo.com\"";
    assertTrue(instance.isNotification(contentType));
    contentType = "message/delivery-status";
    assertTrue(instance.isNotification(contentType));
    contentType = "message/rfc822";
    assertFalse(instance.isNotification(contentType));
    contentType = "text/plain; charset=utf-8";
    assertFalse(instance.isNotification(contentType));
  }

  /**
   * Test of isDeliveryStatus method, of class BetterMimeMessage.
   * @throws MessagingException
   */
  @Test
  public void testIsDeliveryStatus() throws MessagingException {
    MimeMessage report = mock(MimeMessage.class);
    BetterMimeMessage instance = new BetterMimeMessage(report);
    String contentType = "multipart/report; report-type=delivery-status;\n\t"
        + "boundary=\"A3DB01912007.1281602599/zimbra.oevo.com\"";
    assertFalse(instance.isDeliveryStatus(contentType));
    contentType = "message/delivery-status";
    assertTrue(instance.isDeliveryStatus(contentType));
    contentType = "message/rfc822";
    assertFalse(instance.isDeliveryStatus(contentType));
    contentType = "text/plain; charset=utf-8";
    assertFalse(instance.isDeliveryStatus(contentType));
  }

  /**
   * Test of isMessageReport method, of class BetterMimeMessage.
   * @throws MessagingException
   */
  @Test
  public void testIsMessageReport() throws MessagingException {
    MimeMessage report = mock(MimeMessage.class);
    BetterMimeMessage instance = new BetterMimeMessage(report);
    String contentType = "multipart/report; report-type=delivery-status;\n\t"
        + "boundary=\"A3DB01912007.1281602599/zimbra.oevo.com\"";
    assertTrue(instance.isMessageReport(contentType));
    contentType = "message/delivery-status";
    assertFalse(instance.isMessageReport(contentType));
    contentType = "message/rfc822";
    assertFalse(instance.isMessageReport(contentType));
    contentType = "text/plain; charset=utf-8";
    assertFalse(instance.isMessageReport(contentType));

  }
}
