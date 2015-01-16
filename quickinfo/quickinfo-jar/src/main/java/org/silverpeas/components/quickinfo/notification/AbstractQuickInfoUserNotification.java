package org.silverpeas.components.quickinfo.notification;

import org.silverpeas.components.quickinfo.QuickInfoComponentSettings;

import com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceProvider;
import com.stratelia.webactiv.beans.admin.UserDetail;

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
   * Gets the service instance of subscription management.
   * @return the subscriptions service instance.
   */
  protected SubscriptionService getSubscribeBm() {
    return SubscriptionServiceProvider.getSubscribeService();
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
  protected abstract UserDetail getSenderDetail();

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "quickinfo.news.notifNewsLinkLabel";
  }

}
