package com.silverpeas.blog.notification;

import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;

import java.util.Collection;

/**
 * @author Nicolas Eysseric
 */
public abstract class AbstractBlogUserNotification extends
    AbstractTemplateUserNotificationBuilder<PostDetail> {

  private final UserDetail userDetail;

  public AbstractBlogUserNotification(final PostDetail postDetail, final UserDetail userDetail) {
    super(postDetail);
    this.userDetail = userDetail;
  }

  @Override
  protected void performTemplateData(final String language, final PostDetail resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData()
        .addLanguage(language, getBundle(language).getString(getBundleSubjectKey(), getTitle()),
            "");
    template.setAttribute("blog", resource);
    template.setAttribute("blogName", resource.getPublication().getName(language));
    template.setAttribute("blogDate", DateUtil.getOutputDate(resource.getDateEvent(), language));
    final Category categorie = resource.getCategory();
    String categorieName = null;
    if (categorie != null) {
      categorieName = categorie.getName(language);
    }
    template.setAttribute("blogCategorie", categorieName);
    template.setAttribute("senderName", (userDetail != null ? userDetail.getDisplayedName() : ""));
  }

  @Override
  protected void performNotificationResource(final String language, final PostDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getPublication().getName(language));
  }

  @Override
  protected void perform(final PostDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getTemplatePath() {
    return "blog";
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    return userDetail.getId();
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "org.silverpeas.blog.multilang.blogBundle";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "blog.notifPostLinkLabel";
  }
}