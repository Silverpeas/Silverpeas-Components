/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.model;

import com.silverpeas.notification.builder.UserNotificationBuider;
import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.components.suggestionbox.notification.SuggestionBoxSubscriptionUserNotification;
import org.silverpeas.components.suggestionbox.notification.SuggestionPendingValidationUserNotification;
import org.silverpeas.components.suggestionbox.notification.SuggestionValidationUserNotification;
import org.silverpeas.components.suggestionbox.repository.RepositoryBasedTest;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.contribution.model.ContributionValidation;
import org.silverpeas.wysiwyg.control.WysiwygController;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Unit test on the business operations of the SuggestionBox objects.
 * @author mmoquillon
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WysiwygController.class, UserNotificationHelper.class})
public class SuggestionBoxTest extends RepositoryBasedTest {

  private final static String SUGGESTION_BOX_INSTANCE_ID = "suggestionBox1";
  private final static String SUGGESTION_ID = "suggestion_1";
  private final static String SUGGESTION_ID_PENDING_VALIDATION = "suggestion_1_c";
  private final static int SUGGESTIONS_COUNT = 9;

  @Override
  public String getDataSetPath() {
    return "org/silverpeas/components/suggestionbox/suggestion-box-dataset.xml";
  }

  @Override
  public String getApplicationContextPath() {
    return "spring-suggestion-box.xml";
  }

  @Test
  public void addASuggestionIntoASuggestionBox() throws Exception {
    PowerMockito.mockStatic(WysiwygController.class);
    UserDetail author = aUser();
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion newSuggestion = new Suggestion("This is my suggestion");
    newSuggestion.setContent("This is the content of my suggestion");
    newSuggestion.setCreator(author);
    box.getSuggestions().add(newSuggestion);

    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    assertThat(table.getRowCount(), is(SUGGESTIONS_COUNT + 1));
    String actualTitle = (String) table.getValue(0, "title");
    assertThat(actualTitle, is(newSuggestion.getTitle()));

    PowerMockito.verifyStatic(times(1));
    WysiwygController.
        save(newSuggestion.getContent(), box.getComponentInstanceId(), newSuggestion.getId(),
            author.getId(), null, false);
  }

  @Test
  public void removeASuggestionFromASuggestionBox() throws Exception {
    PowerMockito.mockStatic(WysiwygController.class);
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion suggestion = box.getSuggestions().get(SUGGESTION_ID);
    box.getSuggestions().remove(suggestion);

    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    assertThat(table.getRowCount(), is(SUGGESTIONS_COUNT - 1));

    PowerMockito.verifyStatic(times(1));
    WysiwygController.deleteWysiwygAttachments(SUGGESTION_BOX_INSTANCE_ID, SUGGESTION_ID);
  }

  @Test
  public void publishASuggestionOfASuggestionBoxWithUserAccessRole() throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(0, "id"), is(SUGGESTION_ID));
    assertThat((String) table.getValue(0, "status"), is(ContributionStatus.DRAFT.name()));
    assertThat((String) table.getValue(0, "lastUpdatedBy"), is("1"));
    Date lastUpdateDate = (Date) table.getValue(0, "lastUpdateDate");
    assertThat(table.getValue(0, "validationDate"), is(nullValue()));
    assertThat(table.getValue(0, "validationComment"), is(nullValue()));
    assertThat(table.getValue(0, "validationBy"), is(nullValue()));

    PowerMockito.mockStatic(UserNotificationHelper.class);
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion suggestion = box.getSuggestions().get(SUGGESTION_ID);
    UserDetail updater = aUser();
    updater.setId("26");
    suggestion.setLastUpdater(updater);
    when(getOrganisationController()
        .getUserProfiles(suggestion.getLastUpdatedBy(), box.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.user.name()});
    box.getSuggestions().publish(suggestion);

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(0, "id"), is(SUGGESTION_ID));
    assertThat((String) table.getValue(0, "status"), is(ContributionStatus.DRAFT.name()));
    assertThat((String) table.getValue(0, "lastUpdatedBy"), is("1"));
    assertThat((Date) table.getValue(0, "lastUpdateDate"), is(lastUpdateDate));
    assertThat(table.getValue(0, "validationDate"), is(nullValue()));
    assertThat(table.getValue(0, "validationComment"), is(nullValue()));
    assertThat(table.getValue(0, "validationBy"), is(nullValue()));

    PowerMockito.verifyStatic(times(0));
    UserNotificationHelper.buildAndSend(any(UserNotificationBuider.class));
  }

  @Test
  public void publishASuggestionOfASuggestionBoxWithWriterAccessRole() throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(0, "id"), is(SUGGESTION_ID));
    assertThat((String) table.getValue(0, "status"), is(ContributionStatus.DRAFT.name()));
    assertThat((String) table.getValue(0, "lastUpdatedBy"), is("1"));
    Date lastUpdateDate = (Date) table.getValue(0, "lastUpdateDate");
    assertThat(table.getValue(0, "validationDate"), is(nullValue()));
    assertThat(table.getValue(0, "validationComment"), is(nullValue()));
    assertThat(table.getValue(0, "validationBy"), is(nullValue()));

    PowerMockito.mockStatic(UserNotificationHelper.class);
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion suggestion = box.getSuggestions().get(SUGGESTION_ID);
    UserDetail updater = aUser();
    updater.setId("26");
    suggestion.setLastUpdater(updater);
    when(getOrganisationController()
        .getUserProfiles(suggestion.getLastUpdatedBy(), box.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.writer.name()});
    box.getSuggestions().publish(suggestion);

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(0, "id"), is(SUGGESTION_ID));
    assertThat((String) table.getValue(0, "status"),
        is(ContributionStatus.PENDING_VALIDATION.name()));
    assertThat((String) table.getValue(0, "lastUpdatedBy"), is("26"));
    assertThat((Date) table.getValue(0, "lastUpdateDate"), greaterThan(lastUpdateDate));
    assertThat(table.getValue(0, "validationDate"), is(nullValue()));
    assertThat(table.getValue(0, "validationComment"), is(nullValue()));
    assertThat(table.getValue(0, "validationBy"), is(nullValue()));

    PowerMockito.verifyStatic(times(1));
    UserNotificationHelper.buildAndSend(any(SuggestionPendingValidationUserNotification.class));
  }

  @Test
  public void publishASuggestionOfASuggestionBoxWithPublisherAccessRole() throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(0, "id"), is(SUGGESTION_ID));
    assertThat((String) table.getValue(0, "status"), is(ContributionStatus.DRAFT.name()));
    assertThat((String) table.getValue(0, "lastUpdatedBy"), is("1"));
    Date lastUpdateDate = (Date) table.getValue(0, "lastUpdateDate");
    assertThat(table.getValue(0, "validationDate"), is(nullValue()));
    assertThat(table.getValue(0, "validationComment"), is(nullValue()));
    assertThat(table.getValue(0, "validationBy"), is(nullValue()));

    PowerMockito.mockStatic(UserNotificationHelper.class);
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion suggestion = box.getSuggestions().get(SUGGESTION_ID);
    UserDetail updater = aUser();
    updater.setId("26");
    suggestion.setLastUpdater(updater);
    when(getOrganisationController()
        .getUserProfiles(suggestion.getLastUpdatedBy(), box.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.publisher.name()});
    box.getSuggestions().publish(suggestion);

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(0, "id"), is(SUGGESTION_ID));
    assertThat((String) table.getValue(0, "status"), is(ContributionStatus.VALIDATED.name()));
    assertThat((String) table.getValue(0, "lastUpdatedBy"), is("26"));
    assertThat((Date) table.getValue(0, "lastUpdateDate"), greaterThan(lastUpdateDate));
    assertThat(DateUtil.resetHour((Date) table.getValue(0, "validationDate")),
        is(DateUtil.getDate()));
    assertThat(table.getValue(0, "validationComment"), is(nullValue()));
    assertThat((String) table.getValue(0, "validationBy"), is("26"));

    PowerMockito.verifyStatic(times(1));
    UserNotificationHelper.buildAndSend(any(SuggestionBoxSubscriptionUserNotification.class));
  }

  @Test
  public void validateAInDraftSuggestionOfASuggestionBoxWithWriterAccessRole() throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(0, "id"), is(SUGGESTION_ID));
    assertThat((String) table.getValue(0, "status"), is(ContributionStatus.DRAFT.name()));
    assertThat((String) table.getValue(0, "lastUpdatedBy"), is("1"));
    Date lastUpdateDate = (Date) table.getValue(0, "lastUpdateDate");
    assertThat(table.getValue(0, "validationDate"), is(nullValue()));
    assertThat(table.getValue(0, "validationComment"), is(nullValue()));
    assertThat(table.getValue(0, "validationBy"), is(nullValue()));

    PowerMockito.mockStatic(UserNotificationHelper.class);
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion suggestion = box.getSuggestions().get(SUGGESTION_ID);
    UserDetail updater = aUser();
    updater.setId("38");
    suggestion.setLastUpdater(updater);
    suggestion.setStatus(ContributionStatus.VALIDATED);
    when(getOrganisationController()
        .getUserProfiles(suggestion.getLastUpdatedBy(), box.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.writer.name()});
    box.getSuggestions().validate(suggestion, new ContributionValidation(updater, new Date()));

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(0, "id"), is(SUGGESTION_ID));
    assertThat((String) table.getValue(0, "status"), is(ContributionStatus.DRAFT.name()));
    assertThat((String) table.getValue(0, "lastUpdatedBy"), is("1"));
    assertThat((Date) table.getValue(0, "lastUpdateDate"), is(lastUpdateDate));
    assertThat(table.getValue(0, "validationDate"), is(nullValue()));
    assertThat(table.getValue(0, "validationComment"), is(nullValue()));
    assertThat(table.getValue(0, "validationBy"), is(nullValue()));

    PowerMockito.verifyStatic(times(0));
    UserNotificationHelper.buildAndSend(any(UserNotificationBuider.class));
  }

  @Test
  public void validateAInDraftSuggestionOfASuggestionBoxWithPublisherAccessRole() throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(0, "id"), is(SUGGESTION_ID));
    assertThat((String) table.getValue(0, "status"), is(ContributionStatus.DRAFT.name()));
    assertThat((String) table.getValue(0, "lastUpdatedBy"), is("1"));
    Date lastUpdateDate = (Date) table.getValue(0, "lastUpdateDate");
    assertThat(table.getValue(0, "validationDate"), is(nullValue()));
    assertThat(table.getValue(0, "validationComment"), is(nullValue()));
    assertThat(table.getValue(0, "validationBy"), is(nullValue()));

    PowerMockito.mockStatic(UserNotificationHelper.class);
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion suggestion = box.getSuggestions().get(SUGGESTION_ID);
    UserDetail updater = aUser();
    updater.setId("38");
    suggestion.setLastUpdater(updater);
    suggestion.setStatus(ContributionStatus.VALIDATED);
    when(getOrganisationController()
        .getUserProfiles(suggestion.getLastUpdatedBy(), box.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.publisher.name()});
    box.getSuggestions().validate(suggestion, new ContributionValidation(updater, new Date()));

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(0, "id"), is(SUGGESTION_ID));
    assertThat((String) table.getValue(0, "status"), is(ContributionStatus.DRAFT.name()));
    assertThat((String) table.getValue(0, "lastUpdatedBy"), is("1"));
    assertThat((Date) table.getValue(0, "lastUpdateDate"), is(lastUpdateDate));
    assertThat(table.getValue(0, "validationDate"), is(nullValue()));
    assertThat(table.getValue(0, "validationComment"), is(nullValue()));
    assertThat(table.getValue(0, "validationBy"), is(nullValue()));

    PowerMockito.verifyStatic(times(0));
    UserNotificationHelper.buildAndSend(any(UserNotificationBuider.class));
  }

  @Test
  public void validateAPendingValidationSuggestionOfASuggestionBoxWithPublisherAccessRole()
      throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    int index = getTableIndexForId(table, SUGGESTION_ID_PENDING_VALIDATION);
    assertThat((String) table.getValue(index, "id"), is(SUGGESTION_ID_PENDING_VALIDATION));
    assertThat((String) table.getValue(index, "status"),
        is(ContributionStatus.PENDING_VALIDATION.name()));
    assertThat((String) table.getValue(index, "lastUpdatedBy"), is("1"));
    Date lastUpdateDate = (Date) table.getValue(index, "lastUpdateDate");
    assertThat(table.getValue(index, "validationDate"), is(nullValue()));
    assertThat(table.getValue(index, "validationComment"), is(nullValue()));
    assertThat(table.getValue(index, "validationBy"), is(nullValue()));

    PowerMockito.mockStatic(UserNotificationHelper.class);
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion suggestion = box.getSuggestions().get(SUGGESTION_ID_PENDING_VALIDATION);
    UserDetail updater = aUser();
    updater.setId("38");
    suggestion.setLastUpdater(updater);
    suggestion.setStatus(ContributionStatus.VALIDATED);
    when(getOrganisationController()
        .getUserProfiles(suggestion.getLastUpdatedBy(), box.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.publisher.name()});
    Date acceptDate = DateUtil.getDate();
    box.getSuggestions().validate(suggestion, new ContributionValidation(updater, acceptDate));

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(index, "id"), is(SUGGESTION_ID_PENDING_VALIDATION));
    assertThat((String) table.getValue(index, "status"), is(ContributionStatus.VALIDATED.name()));
    assertThat((String) table.getValue(index, "lastUpdatedBy"), is("38"));
    assertThat((Date) table.getValue(index, "lastUpdateDate"), greaterThan(lastUpdateDate));
    assertThat(DateUtil.resetHour((Date) table.getValue(index, "validationDate")),
        is(acceptDate));
    assertThat(table.getValue(index, "validationComment"), is(nullValue()));
    assertThat((String) table.getValue(index, "validationBy"), is("38"));

    PowerMockito.verifyStatic(times(2));
    UserNotificationHelper.buildAndSend(any(SuggestionBoxSubscriptionUserNotification.class));
    UserNotificationHelper.buildAndSend(any(SuggestionValidationUserNotification.class));
  }

  @Test
  public void refuseAPendingValidationSuggestionOfASuggestionBoxWithPublisherAccessRole()
      throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    int index = getTableIndexForId(table, SUGGESTION_ID_PENDING_VALIDATION);
    assertThat((String) table.getValue(index, "id"), is(SUGGESTION_ID_PENDING_VALIDATION));
    assertThat((String) table.getValue(index, "title"), is("suggestion 1 IDEM"));
    assertThat((String) table.getValue(index, "status"),
        is(ContributionStatus.PENDING_VALIDATION.name()));
    assertThat((String) table.getValue(index, "lastUpdatedBy"), is("1"));
    Date lastUpdateDate = (Date) table.getValue(index, "lastUpdateDate");
    assertThat(table.getValue(index, "validationDate"), is(nullValue()));
    assertThat(table.getValue(index, "validationComment"), is(nullValue()));
    assertThat(table.getValue(index, "validationBy"), is(nullValue()));

    PowerMockito.mockStatic(UserNotificationHelper.class);
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion suggestion = box.getSuggestions().get(SUGGESTION_ID_PENDING_VALIDATION);
    UserDetail updater = aUser();
    updater.setId("38");
    suggestion.setLastUpdater(updater);
    suggestion.setStatus(ContributionStatus.REFUSED);
    suggestion.setTitle("the title");
    when(getOrganisationController()
        .getUserProfiles(suggestion.getLastUpdatedBy(), box.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.publisher.name()});
    Date refusalDate = DateUtil.getDate();
    box.getSuggestions().validate(suggestion, new ContributionValidation(updater, refusalDate,
        "Comment filled"));

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sc_suggestion");
    assertThat((String) table.getValue(index, "id"), is(SUGGESTION_ID_PENDING_VALIDATION));
    assertThat((String) table.getValue(index, "title"), is("suggestion 1 IDEM"));
    assertThat((String) table.getValue(index, "status"), is(ContributionStatus.REFUSED.name()));
    assertThat((String) table.getValue(index, "lastUpdatedBy"), is("38"));
    assertThat((Date) table.getValue(index, "lastUpdateDate"), greaterThan(lastUpdateDate));
    assertThat(DateUtil.resetHour((Date) table.getValue(index, "validationDate")),
        is(refusalDate));
    assertThat((String) table.getValue(index, "validationComment"), is("Comment filled"));
    assertThat((String) table.getValue(index, "validationBy"), is("38"));

    PowerMockito.verifyStatic(times(1));
    UserNotificationHelper.buildAndSend(any(SuggestionValidationUserNotification.class));
  }
}
