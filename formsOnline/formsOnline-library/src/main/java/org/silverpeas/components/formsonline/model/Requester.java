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
        return new Requester(UserFull.getById(userId));
    }

    public static List<Requester> getByIds(Set<String> userIds) {
        return UserFull.getByIds(userIds)
                .stream()
                .map(Requester::new)
                .collect(Collectors.toList());
    }

    public String getRequestValidator() {

        String bossId = user.getValue(BOSS_FIELDNAME);
        if (bossId != null && !bossId.isEmpty()) {
            UserFull boss = UserFull.getById(bossId);
            String delegateApprover = boss.getAllDefinedValues(boss.getUserPreferences().getLanguage()).get(DELEGATE_APPROVER_FIELDNAME);
            if (delegateApprover != null && !delegateApprover.isEmpty()) {
                if (!boss.getValue(DELEGATE_APPROVER_FIELDNAME).isEmpty()) {
                    // field 'delegateApprover' is in user domain properties
                    return boss.getValue(DELEGATE_APPROVER_FIELDNAME);
                } else {
                    // field 'delegateApprover' is in user form
                    return getDelegateApprover(boss);
                }
            } else {
                return bossId;
            }
        }

        return EMPTY;
    }
    private String getDelegateApprover(final UserFull u) {
        PagesContext pageContext = new PagesContext();
        pageContext.setDomainId(u.getDomainId());
        pageContext.setObjectId(u.getId());
        pageContext.setLanguage(u.getUserPreferences().getLanguage());

        PublicationTemplate template = PublicationTemplateManager.getInstance().getDirectoryTemplate(pageContext);
        if (template != null) {
            try {
                DataRecord data = template.getRecordSet().getRecord(u.getId());
                if (data != null) {
                    Field f = data.getField(DELEGATE_APPROVER_FIELDNAME);
                    if (f != null) {
                        UserDetail delegateApprover = (UserDetail) f.getObjectValue();
                        return delegateApprover.getId();
                    }
                }
            } catch (Exception e) {
                SilverLogger.getLogger(this).error(e);
            }
        }
        return EMPTY;
    }
}
