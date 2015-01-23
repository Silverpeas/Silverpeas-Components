package org.silverpeas.components.quickinfo.notification;

import com.silverpeas.usernotification.builder.AbstractTemplateUserNotificationBuilder;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.components.quickinfo.QuickInfoComponentSettings;

public abstract class AbstractQuickInfoUserNotification<T> extends
    AbstractTemplateUserNotificationBuilder<T> {

  public AbstractQuickInfoUserNotification(T resource) {
    super(resource);
  }
  
  public AbstractQuickInfoUserNotification(final T resource, final String title,
      final String fileName) {
    super(resource, title, fileName);
  }

  @Override
  protected String getMultilangPropertyFile() {
    return QuickInfoComponentSettings.MESSAGES_PATH;
  }

  @Override
  protected String getTemplatePath() {
    return QuickInfoComponentSettings.COMPONENT_NAME;
  }

  /**
   * Gets the name of the sender.
   * @return
   */
  protected String getSenderName() {
    UserDetail sender = getSenderDetail();
    if (sender != null) {
      return sender.getDisplayedName();
    }
    return getSender();
  }

  /**
   * Gets the {@link UserDetail} instance of the sender.
   * @return
   */
  protected final UserDetail getSenderDetail() {
    return UserDetail.getById(getSender());
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "quickinfo.news.notifNewsLinkLabel";
  }

}
