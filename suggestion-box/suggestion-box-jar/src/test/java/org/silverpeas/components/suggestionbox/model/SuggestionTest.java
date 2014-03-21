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

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.components.suggestionbox.repository.RepositoryBasedTest;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.wysiwyg.control.WysiwygController;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.silverpeas.components.suggestionbox.model.SuggestionCriteria.QUERY_ORDER_BY.*;
import static org.silverpeas.components.suggestionbox.model.SuggestionMatcher.PROPERTY.*;
import static org.silverpeas.contribution.ContributionStatus.*;


/**
 * Unit test on the business operations of the SuggestionBox objects.
 * @author mmoquillon
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(WysiwygController.class)
public class SuggestionTest extends RepositoryBasedTest {

  private final static String SUGGESTION_ID = "suggestion_1";
  private final static String SUGGESTION_BOX_INSTANCE_ID = "suggestionBox1";
  private final static String SECOND_SUGGESTION_BOX_INSTANCE_ID = "suggestionBox2";

  @Override
  public String getDataSetPath() {
    return "org/silverpeas/components/suggestionbox/suggestion-box-dataset.xml";
  }

  @Override
  public String getApplicationContextPath() {
    return "spring-suggestion-box.xml";
  }

  @Test
  public void updateAnExistingSuggestion() throws Exception {
    PowerMockito.mockStatic(WysiwygController.class);
    UserDetail author = aUser();
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion suggestion = box.getSuggestions().get(SUGGESTION_ID);
    assertThat(suggestion, not(is(Suggestion.NONE)));

    final String newTitle = "This is a new title";
    final String newContent = "This is a new content";
    suggestion.setTitle(newTitle);
    suggestion.setContent(newContent);
    suggestion.setLastUpdater(author);
    suggestion.save();

    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    assertThat(table.getRowCount(), is(6));
    String actualTitle = (String) table.getValue(0, "title");
    assertThat(actualTitle, is(newTitle));

    PowerMockito.verifyStatic(times(1));
    WysiwygController.
        save(newContent, box.getId(), suggestion.getId(), author.getId(), null, false);
  }

  @Test
  public void updateAnExistingSuggestionWithAnInvalidTitle() throws Exception {
    PowerMockito.mockStatic(WysiwygController.class);
    UserDetail author = aUser();
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion suggestion = box.getSuggestions().get(SUGGESTION_ID);
    assertThat(suggestion, not(is(Suggestion.NONE)));

    final String oldTitle = suggestion.getTitle();
    final String newContent = "This is a new content";
    suggestion.setTitle(null);
    suggestion.setContent(newContent);
    suggestion.setLastUpdater(author);
    try {
      suggestion.save();
      fail("An exception should be thrown");
    } catch (Exception ex) {
      IDataSet actualDataSet = getActualDataSet();
      ITable table = actualDataSet.getTable("sc_suggestion");
      assertThat(table.getRowCount(), is(6));
      String actualTitle = (String) table.getValue(0, "title");
      assertThat(actualTitle, is(oldTitle));

      PowerMockito.verifyStatic(times(0));
      WysiwygController.
          save(newContent, box.getId(), suggestion.getId(), author.getId(), null, false);
    }
  }

  @Test
  public void getAnUnExistingSuggestion() throws Exception {
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion suggestion = box.getSuggestions().get("toto");
    assertThat(suggestion, is(Suggestion.NONE));
  }

  @Test
  public void findBySuggestionCriteria() {

    // Only the mandatory filter of the suggestion box itself
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    List<Suggestion> suggestions =
        getPersistenceService().findByCriteria(SuggestionCriteria.from(box));
    assertThat(suggestions, hasSize(5));

    // Filtering on an existing creator
    UserDetail creator = UserDetail.getById("1");
    suggestions =
        getPersistenceService().findByCriteria(SuggestionCriteria.from(box).createdBy(creator));
    assertThat(suggestions, hasSize(4));

    // Filtering on an creator that doesn't exist
    creator = UserDetail.getById("999");
    suggestions =
        getPersistenceService().findByCriteria(SuggestionCriteria.from(box).createdBy(creator));
    assertThat(suggestions, hasSize(0));

    // Filtering on an existing creator and one contribution status
    creator = UserDetail.getById("1");
    suggestions = getPersistenceService()
        .findByCriteria(SuggestionCriteria.from(box).createdBy(creator).statusIsOneOf(DRAFT));
    assertThat(suggestions, hasSize(2));

    // Filtering on an existing creator and one other contribution status
    creator = UserDetail.getById("1");
    suggestions = getPersistenceService().findByCriteria(
        SuggestionCriteria.from(box).createdBy(creator).statusIsOneOf(PENDING_VALIDATION));
    assertThat(suggestions, hasSize(1));

    // Filtering on an existing creator and the two contribution status above used
    creator = UserDetail.getById("1");
    suggestions = getPersistenceService().findByCriteria(
        SuggestionCriteria.from(box).createdBy(creator).statusIsOneOf(PENDING_VALIDATION, DRAFT));
    assertThat(suggestions, hasSize(3));

    // Filtering on an existing creator and ordering by ascending last update date
    creator = UserDetail.getById("1");
    suggestions = getPersistenceService().findByCriteria(
        SuggestionCriteria.from(box).createdBy(creator).orderedBy(LAST_UPDATE_DATE_ASC));
    assertThat(suggestions, hasSize(4));
    assertThat(suggestions, contains(
        matches("suggestion_1_c", "suggestion 1 IDEM", PENDING_VALIDATION,
            timestamp("2014-03-13 17:34:00.0")),
        matches("suggestion_1_a", "suggestion 1 IDEM", REFUSED, timestamp("2014-03-15 17:34:00.0")),
        matches("suggestion_1", "suggestion 1", DRAFT, timestamp("2014-03-16 17:34:00.0")),
        matches("suggestion_1_b", "suggestion 1 / B", DRAFT, timestamp("2014-03-20 17:34:00.0"))
    ));

    // Filtering on an existing creator and ordering by descending last update date
    creator = UserDetail.getById("1");
    suggestions = getPersistenceService().findByCriteria(
        SuggestionCriteria.from(box).createdBy(creator).orderedBy(LAST_UPDATE_DATE_DESC));
    assertThat(suggestions, hasSize(4));
    assertThat(suggestions, contains(
        matches("suggestion_1_b", "suggestion 1 / B", DRAFT, timestamp("2014-03-20 17:34:00.0")),
        matches("suggestion_1", "suggestion 1", DRAFT, timestamp("2014-03-16 17:34:00.0")),
        matches("suggestion_1_a", "suggestion 1 IDEM", REFUSED, timestamp("2014-03-15 17:34:00.0")),
        matches("suggestion_1_c", "suggestion 1 IDEM", PENDING_VALIDATION,
            timestamp("2014-03-13 17:34:00.0"))
    ));

    // Filtering on an existing creator and ordering by descending title,
    // then by descending last update date
    creator = UserDetail.getById("1");
    suggestions = getPersistenceService().findByCriteria(
        SuggestionCriteria.from(box).createdBy(creator).orderedBy(TITLE_DESC, LAST_UPDATE_DATE_DESC)
    );
    assertThat(suggestions, hasSize(4));
    assertThat(suggestions, contains(
        matches("suggestion_1_a", "suggestion 1 IDEM", REFUSED, timestamp("2014-03-15 17:34:00.0")),
        matches("suggestion_1_c", "suggestion 1 IDEM", PENDING_VALIDATION,
            timestamp("2014-03-13 17:34:00.0")),
        matches("suggestion_1_b", "suggestion 1 / B", DRAFT, timestamp("2014-03-20 17:34:00.0")),
        matches("suggestion_1", "suggestion 1", DRAFT, timestamp("2014-03-16 17:34:00.0"))
    ));

    // Filtering on an existing creator and ordering by ascending title,
    // then by descending last update date
    creator = UserDetail.getById("1");
    suggestions = getPersistenceService().findByCriteria(
        SuggestionCriteria.from(box).createdBy(creator).orderedBy(TITLE_ASC, LAST_UPDATE_DATE_DESC)
    );
    assertThat(suggestions, hasSize(4));
    assertThat(suggestions,
        contains(matches("suggestion_1", "suggestion 1", DRAFT, timestamp("2014-03-16 17:34:00.0")),
            matches("suggestion_1_b", "suggestion 1 / B", DRAFT,
                timestamp("2014-03-20 17:34:00.0")),
            matches("suggestion_1_a", "suggestion 1 IDEM", REFUSED,
                timestamp("2014-03-15 17:34:00.0")),
            matches("suggestion_1_c", "suggestion 1 IDEM", PENDING_VALIDATION,
                timestamp("2014-03-13 17:34:00.0"))
        ));

    // Filtering on an existing creator and PENDING_VALIDATION or REFUSED status
    // and ordering by ascending title, then by ascending last update date
    creator = UserDetail.getById("1");
    suggestions = getPersistenceService().findByCriteria(
        SuggestionCriteria.from(box).createdBy(creator).statusIsOneOf(PENDING_VALIDATION, REFUSED)
            .orderedBy(TITLE_ASC, LAST_UPDATE_DATE_ASC)
    );
    assertThat(suggestions, hasSize(2));
    assertThat(suggestions, contains(
        matches("suggestion_1_c", "suggestion 1 IDEM", PENDING_VALIDATION,
            timestamp("2014-03-13 17:34:00.0")),
        matches("suggestion_1_a", "suggestion 1 IDEM", REFUSED, timestamp("2014-03-15 17:34:00.0"))
    ));
  }

  private Timestamp timestamp(String timestamp) {
    return java.sql.Timestamp.valueOf(timestamp);
  }

  private SuggestionMatcher matches(String id, String title, ContributionStatus status,
      Date lastUpdateDate) {
    return SuggestionMatcher.init().value(ID, id).value(TITLE, title).value(STATUS, status)
        .value(LAST_UPDATE_DATE, lastUpdateDate);
  }
}
