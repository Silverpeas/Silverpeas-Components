/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.components.saasmanager.service;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.silverpeas.components.saasmanager.exception.SaasManagerException;
import com.silverpeas.components.saasmanager.model.SaasAccess;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * SAAS mail service
 * @author ahedin
 */
public class SaasMailService {

  private static final String RESOURCE_SETTINGS =
    "com.silverpeas.saasmanager.settings.SaasManagerSettings";
  private static final String RESOURCE_SMTP =
    "com.stratelia.silverpeas.notificationserver.channel.smtp.smtpSettings";
  private static final String RESOURCE_MULTILANG =
    "com.silverpeas.saasmanager.multilang.SaasManagerBundle";

  private static final String TRANSPORT_SMTP = "smtp";
  private static final String TRANSPORT_SMTPS = "smtps";
  private static final String SUBJECT_ENCODING = "UTF-8";

  private static final int URL_CONTEXT_SILVERPEAS = 0;
  private static final int URL_CONTEXT_MYSILVERPEAS = 1;

  private static final String MAIL_ACTIVATION = "activation";
  private static final String MAIL_LOGIN = "login";
  private static final String MAIL_INVITATION = "invitation";

  private ResourceLocator managerSettings;

  // SMTP parameters
  private String smtpHost;
  private boolean smtpAuthentication;
  private boolean smtpSecure;
  private boolean smtpDebug;
  private int smtpPort;
  private String smtpUser;
  private String smtpPwd;
  private Session session;

  public SaasMailService() {
    managerSettings = new ResourceLocator(RESOURCE_SETTINGS, "");
    init();
  }

  /**
   * Initializes mail parameters.
   */
  private void init() {
    ResourceLocator smtpSettings = new ResourceLocator(RESOURCE_SMTP, "");
    smtpHost = smtpSettings.getString("SMTPServer");
    smtpAuthentication = smtpSettings.getBoolean("SMTPAuthentication", false);
    smtpDebug = smtpSettings.getBoolean("SMTPDebug", false);
    smtpPort = Integer.parseInt(smtpSettings.getString("SMTPPort"));
    smtpUser = smtpSettings.getString("SMTPUser");
    smtpPwd = smtpSettings.getString("SMTPPwd");
    smtpSecure = smtpSettings.getBoolean("SMTPSecure", false);

    Properties props = System.getProperties();
    props.put("mail.smtp.host", smtpHost);
    props.put("mail.smtp.auth", String.valueOf(smtpAuthentication));
    session = Session.getInstance(props, null);
    session.setDebug(smtpDebug); // print on the console all SMTP messages.    
  }

  /**
   * Sends a SAAS access activation mail.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  public void sendActivationMail(SaasAccess access)
    throws SaasManagerException {
    HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put("firstName", access.getFirstName());
    parameters.put("lastName", access.getLastName());
    parameters.put("link", getMailLink(URL_CONTEXT_MYSILVERPEAS, "accessActivation.jsp",
      new String[][] {{"uid", access.getUid()}, {"lang", access.getLang()}}));
    sendMail(access, MAIL_ACTIVATION, parameters);
  }

  /**
   * Sends a login mail.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  public void sendLoginMail(SaasAccess access)
  throws SaasManagerException {
    HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put("firstName", access.getFirstName());
    parameters.put("lastName", access.getLastName());
    parameters.put("login", access.getUserLogin());
    parameters.put("password", access.getUserPassword());
    parameters.put("link", getMailLink(URL_CONTEXT_SILVERPEAS, "defaultLogin.jsp",
      new String[][] {{"DomainId", access.getDomainId()}}));
    sendMail(access, MAIL_LOGIN, parameters);
  }

  /**
   * Sends an invitation mail to a new user of the SAAS domain.
   * @param access The SAAS access.
   * @param firstName The user's first name.
   * @param lastName The user's last name.
   * @param login The user's login.
   * @param password The user's password.
   * @throws SaasManagerException
   */
  public void sendInvitationMail(SaasAccess access, String firstName, String lastName,
    String login, String password)
  throws SaasManagerException {
    HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put("firstName", firstName);
    parameters.put("lastName", lastName);
    parameters.put("adminFirstName", access.getFirstName());
    parameters.put("adminLastName", access.getLastName());
    parameters.put("login", login);
    parameters.put("password", password);
    parameters.put("link", getMailLink(URL_CONTEXT_SILVERPEAS, "defaultLogin.jsp",
      new String[][] {{"DomainId", access.getDomainId()}}));
    sendMail(access, MAIL_INVITATION, parameters);
  }

  /**
   * Sends a mail.
   * @param access The SAAS access.
   * @param mailType The type of the mail.
   * @param parameters The specific parameters linked to the type of the mail.
   * @throws SaasManagerException
   */
  private void sendMail(SaasAccess access, String mailType, HashMap<String, String> parameters)
  throws SaasManagerException {
    String lang = access.getLang();
    ResourceLocator multilang = new ResourceLocator(RESOURCE_MULTILANG, lang);
    String subject = multilang.getString("mail." + mailType + ".subject");
    String content = getMailContent(mailType, parameters, lang);
    
    String fromAddress = managerSettings.getString("fromAddress." + mailType, "noreply@silverpeas.com");
    String fromName = managerSettings.getString("fromName." + mailType, "Silverpeas");

    Transport transport = null;
    try {
      MimeMessage msg = new MimeMessage(session);
      try {
        msg.setFrom(new InternetAddress(fromAddress, fromName, "UTF-8"));
      } catch (UnsupportedEncodingException e1) {
        msg.setFrom(new InternetAddress(fromAddress));
      }

      msg.setSubject(subject, SUBJECT_ENCODING);
      msg.setContent(content, MimeTypes.HTML_MIME_TYPE);
      msg.setSentDate(new Date());

      // create a Transport connection
      transport = session.getTransport(smtpSecure ? TRANSPORT_SMTPS : TRANSPORT_SMTP);

      // redefine the TransportListener interface.
      TransportListener transportListener = new TransportListener() {
        public void messageDelivered(TransportEvent e) {
        }

        public void messageNotDelivered(TransportEvent e) {
        }

        public void messagePartiallyDelivered(TransportEvent e) {
        }
      };

      transport.addTransportListener(transportListener);

      InternetAddress[] addresses = { new InternetAddress(access.getEmail()) };
      msg.setRecipients(Message.RecipientType.TO, addresses);
      // add Transport Listener to the transport connection.
      if (smtpAuthentication) {
        transport.connect(smtpHost, smtpPort, smtpUser, smtpPwd);
        msg.saveChanges();
      } else {
        transport.connect();
      }
      transport.sendMessage(msg, addresses);
    } catch (MessagingException e) {
      throw new SaasManagerException("SaasMailService.sendMail()",
        SilverpeasException.ERROR, "saasmanager.EX_SEND_MAIL", e);
    } finally {
      if (transport != null) {
        try {
          transport.close();
        } catch (Exception e) {
          SilverTrace.error("saasmanager", "SaasMailService.sendMail()", "saasmanager.EX_SEND_MAIL",
            "ClosingTransport", e);
        }
      }
    }
  }

  /**
   * @param mailType The type of the mail.
   * @param parameters The specific parameters linked to the mail's content.
   * @param lang The language.
   * @return The content of the mail depending on its type.
   */
  private String getMailContent(String mailType, HashMap<String, String> parameters, String lang) {
    String rootTemplatePath = managerSettings.getString("templatePath");
    String customerTemplatePath = managerSettings.getString("customersTemplatePath");
    Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rootTemplatePath);
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, customerTemplatePath);
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplate(
        templateConfiguration);
    for (String key : parameters.keySet()) {
      template.setAttribute(key, parameters.get(key));
    }
    return template.applyFileTemplate(mailType + '_' + lang);
  }

  /**
   * @param contextType The type of the URL context to add at the beginning of the link.
   * @param page The page to add to the link.
   * @param parameters The parameters required by the page.
   * @return A link to add to a mail's content.
   */
  private String getMailLink(int contextType, String page, String[][] parameters) {
    StringBuilder link = new StringBuilder().append(managerSettings.getString("silverpeasUrl", ""));
    switch (contextType) {
      case URL_CONTEXT_SILVERPEAS:
        link.append(URLManager.getApplicationURL());
        break;
      case URL_CONTEXT_MYSILVERPEAS:
        link.append(managerSettings.getString("mySilverpeasContext", ""));
        break;
    }
    link.append("/").append(page);
    for (int i = 0; i < parameters.length; i++) {
      link.append(i == 0 ? "?" : "&").append(parameters[i][0]).append("=").append(parameters[i][1]);
    }
    return link.toString();
  }

}
