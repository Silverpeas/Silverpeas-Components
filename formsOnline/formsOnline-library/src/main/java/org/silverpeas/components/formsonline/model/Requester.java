package org.silverpeas.components.formsonline.model;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.silverpeas.kernel.util.StringUtil.EMPTY;

public class Requester {

    public static final String BOSS_FIELDNAME = "boss";
    public static final String DELEGATE_APPROVER_FIELDNAME = "delegateApprover";

    private final UserFull user;

    private Requester(UserFull user) {
        this.user = user;
    }

    public String getId() {
        return user.getId();
    }

    public UserFull getUser() {
        return user;
    }

    public static Requester getById(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        final UserFull user = UserFull.getById(userId);
        return user != null ? new Requester(user) : null;
    }

    public static List<Requester> getByIds(Set<String> userIds) {
        return UserFull.getByIds(userIds)
                .stream()
                .map(Requester::new)
                .collect(Collectors.toList());
    }

    public String getRequestValidator() {
        if (user == null) {
            return EMPTY;
        }

        final String bossId = user.getValue(BOSS_FIELDNAME);
        if (bossId == null || bossId.isBlank()) {
            return EMPTY;
        }

        final UserFull boss = UserFull.getById(bossId);
        if (boss == null) {
            SilverLogger.getLogger(this).warn(
                "Hierarchical validator {0} not found for requester {1}", bossId, user.getId());
            return EMPTY;
        }

        final String domainDelegate = boss.getValue(DELEGATE_APPROVER_FIELDNAME);
        final String delegateId;
        if (domainDelegate != null && !domainDelegate.isBlank()) {
            delegateId = domainDelegate;
        } else {
            final String language = boss.getUserPreferences() != null ?
                boss.getUserPreferences().getLanguage() : null;
            final java.util.Map<String, String> values = boss.getAllDefinedValues(language);
            final String configuredDelegate = values != null ?
                values.get(DELEGATE_APPROVER_FIELDNAME) : null;
            delegateId = configuredDelegate != null && !configuredDelegate.isBlank() ?
                getDelegateApprover(boss) : EMPTY;
        }

        if (delegateId == null || delegateId.isBlank()) {
            return boss.getId();
        }
        final UserFull delegate = UserFull.getById(delegateId);
        if (delegate == null) {
            SilverLogger.getLogger(this).warn(
                "Delegate approver {0} not found for hierarchical validator {1}",
                delegateId, boss.getId());
            return boss.getId();
        }
        return delegate.getId();
    }

    private String getDelegateApprover(final UserFull boss) {
        final PagesContext pageContext = new PagesContext();
        pageContext.setDomainId(boss.getDomainId());
        pageContext.setObjectId(boss.getId());
        if (boss.getUserPreferences() != null) {
            pageContext.setLanguage(boss.getUserPreferences().getLanguage());
        }

        try {
            final PublicationTemplate template =
                PublicationTemplateManager.getInstance().getDirectoryTemplate(pageContext);
            if (template == null || template.getRecordSet() == null) {
                return EMPTY;
            }
            final DataRecord data = template.getRecordSet().getRecord(boss.getId());
            if (data == null) {
                return EMPTY;
            }
            final Field field = data.getField(DELEGATE_APPROVER_FIELDNAME);
            if (field == null || field.isNull()) {
                return EMPTY;
            }
            final Object value = field.getObjectValue();
            if (value == null) {
                return EMPTY;
            }
            if (!(value instanceof UserDetail)) {
                SilverLogger.getLogger(this).warn(
                    "Invalid delegate approver field type for user {0}: {1}",
                    boss.getId(), value.getClass().getName());
                return EMPTY;
            }
            return ((UserDetail) value).getId();
        } catch (Exception e) {
            SilverLogger.getLogger(this).error(e);
            return EMPTY;
        }
    }
}
