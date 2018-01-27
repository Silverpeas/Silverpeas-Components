/*
 * Copyright (C) 2000 - 2018 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.forums;

import org.silverpeas.components.forums.service.ForumService;
import org.silverpeas.components.forums.model.Forum;
import org.silverpeas.components.forums.model.ForumPK;
import org.silverpeas.components.forums.model.Message;
import org.silverpeas.components.forums.model.Moderator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.components.forums.test.WarBuilder4Forums;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.WAPrimaryKey;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration test on the implementation of the ComponentInstanceDeletion interface.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ForumsInstancePreDestructionIT {

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/components/forums/create-database.sql";
  private static final String DATASET_SCRIPT =
      "/org/silverpeas/components/forums/forums-dataset.sql";

  private static final String COMPONENT_INSTANCE_ID = "forums122";

  @Inject
  private ForumService forumService;

  @Inject
  private ForumsInstancePreDestruction destruction;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Forums.onWarForTestClass(ForumsInstancePreDestructionIT.class)
        .build();
  }

  @Before
  public void beforeAnyTests() {
    assertThat(forumService, notNullValue());
    assertThat(destruction, notNullValue());
  }

  @Test
  public void deleteAllToDosForAnExistingComponentInstance() {
    Collection<Forum> forums = forumService.getForums(new ForumPK(COMPONENT_INSTANCE_ID, null));
    List<Message> messages = forums.stream()
        .flatMap(forum -> forumService.getMessages(forum.getPk()).stream())
        .collect(Collectors.toList());
    List<Moderator> moderators = forums.stream()
        .flatMap(forum -> forumService.getModerators(forum.getId()).stream())
        .collect(Collectors.toList());
    assertThat(forums.isEmpty(), is(false));
    assertThat(messages.isEmpty(), is(false));
    assertThat(moderators.isEmpty(), is(false));

    destruction.preDestroy(COMPONENT_INSTANCE_ID);

    forums = forumService.getForums(new ForumPK(null, COMPONENT_INSTANCE_ID));
    messages = forums.stream()
        .flatMap(forum -> forumService.getMessages(forum.getPk()).stream())
        .collect(Collectors.toList());
    moderators = forums.stream()
        .flatMap(forum -> forumService.getModerators(forum.getId()).stream())
        .collect(Collectors.toList());
    assertThat(forums.isEmpty(), is(true));
    assertThat(messages.isEmpty(), is(true));
    assertThat(moderators.isEmpty(), is(true));
  }

  @Test
  public void deleteAllToDosForANonExistingComponentInstance() {
    Collection<Forum> forums = forumService.getForums(new ForumPK("toto123", null));
    assertThat(forums.isEmpty(), is(true));

    destruction.preDestroy(COMPONENT_INSTANCE_ID);

    forums = forumService.getForums(new ForumPK(null, "toto123"));
    assertThat(forums.isEmpty(), is(true));
  }

  @Singleton
  @Alternative
  @Priority(APPLICATION + 10)
  public static class AttachmentServiceStub implements AttachmentService {

    @Override
    public void deleteAllAttachments(final String componentInstanceId) throws AttachmentException {

    }

    @Override
    public void getBinaryContent(final File file, final SimpleDocumentPK pk, final String lang) {

    }

    @Override
    public void getBinaryContent(final OutputStream output, final SimpleDocumentPK pk,
        final String lang) {

    }

    @Override
    public void getBinaryContent(final OutputStream output, final SimpleDocumentPK pk,
        final String lang, final long contentOffset, final long contentLength) {

    }

    @Override
    public void addXmlForm(final SimpleDocumentPK pk, final String language,
        final String xmlFormName) {

    }

    @Override
    public SimpleDocumentPK cloneDocument(final SimpleDocument original,
        final String foreignCloneId) {
      return null;
    }

    @Override
    public Map<String, String> mergeDocuments(final ForeignPK originalForeignKey,
        final ForeignPK cloneForeignKey, final DocumentType type) {
      return null;
    }

    @Override
    public SimpleDocumentPK copyDocument(final SimpleDocument original, final ForeignPK targetPk) {
      return null;
    }

    @Override
    public List<SimpleDocumentPK> copyAllDocuments(final WAPrimaryKey resourceSourcePk,
        final WAPrimaryKey targetDestinationPk) {
      return null;
    }

    @Override
    public SimpleDocumentPK moveDocument(final SimpleDocument document,
        final ForeignPK destination) {
      return null;
    }

    @Override
    public List<SimpleDocumentPK> moveAllDocuments(final WAPrimaryKey resourceSourcePk,
        final WAPrimaryKey targetDestinationPk) {
      return null;
    }

    @Override
    public SimpleDocument createAttachment(final SimpleDocument document, final InputStream content)
        throws AttachmentException {
      return null;
    }

    @Override
    public SimpleDocument createAttachment(final SimpleDocument document, final InputStream content,
        final boolean indexIt) {
      return null;
    }

    @Override
    public SimpleDocument createAttachment(final SimpleDocument document, final InputStream content,
        final boolean indexIt, final boolean invokeCallback) {
      return null;
    }

    @Override
    public SimpleDocument createAttachment(final SimpleDocument document, final File content)
        throws AttachmentException {
      return null;
    }

    @Override
    public SimpleDocument createAttachment(final SimpleDocument document, final File content,
        final boolean indexIt) {
      return null;
    }

    @Override
    public SimpleDocument createAttachment(final SimpleDocument document, final File content,
        final boolean indexIt, final boolean invokeCallback) {
      return null;
    }

    @Override
    public void createIndex(final SimpleDocument document) {

    }

    @Override
    public void deleteIndex(final SimpleDocument document) {

    }

    @Override
    public void createIndex(final SimpleDocument document, final Date startOfVisibilityPeriod,
        final Date endOfVisibilityPeriod) {

    }

    @Override
    public void deleteAllAttachments(final String resourceId, final String componentInstanceId) {

    }

    @Override
    public void deleteAttachment(final SimpleDocument document) {

    }

    @Override
    public void deleteAttachment(final SimpleDocument document, final boolean invokeCallback) {

    }

    @Override
    public void removeContent(final SimpleDocument document, final String lang,
        final boolean invokeCallback) {

    }

    @Override
    public void reorderAttachments(final List<SimpleDocumentPK> pks) throws AttachmentException {

    }

    @Override
    public void reorderDocuments(final List<SimpleDocument> documents) throws AttachmentException {

    }

    @Override
    public SimpleDocument searchDocumentById(final SimpleDocumentPK primaryKey, final String lang) {
      return null;
    }

    @Override
    public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKey(
        final WAPrimaryKey foreignKey, final String lang) {
      return null;
    }

    @Override
    public SimpleDocumentList<SimpleDocument> listAllDocumentsByForeignKey(
        final WAPrimaryKey foreignKey, final String lang) {
      return null;
    }

    @Override
    public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKeyAndType(
        final WAPrimaryKey foreignKey, final DocumentType type, final String lang) {
      return new SimpleDocumentList<>();
    }

    @Override
    public void unindexAttachmentsOfExternalObject(final WAPrimaryKey foreignKey) {

    }

    @Override
    public void updateAttachment(final SimpleDocument document, final boolean indexIt,
        final boolean invokeCallback) {

    }

    @Override
    public void updateAttachment(final SimpleDocument document, final File content,
        final boolean indexIt, final boolean invokeCallback) {

    }

    @Override
    public void updateAttachment(final SimpleDocument document, final InputStream content,
        final boolean indexIt, final boolean invokeCallback) {

    }

    @Override
    public List<SimpleDocument> listDocumentsRequiringWarning(final Date alertDate,
        final String language) {
      return null;
    }

    @Override
    public List<SimpleDocument> listExpiringDocuments(final Date alertDate, final String language) {
      return null;
    }

    @Override
    public List<SimpleDocument> listDocumentsToUnlock(final Date expiryDate,
        final String language) {
      return null;
    }

    @Override
    public boolean lock(final String attachmentId, final String userId, final String language) {
      return false;
    }

    @Override
    public boolean unlock(final UnlockContext context) {
      return false;
    }

    @Override
    public SimpleDocumentPK changeVersionState(final SimpleDocumentPK pk, final String comment) {
      return null;
    }

    @Override
    public SimpleDocument findExistingDocument(final SimpleDocumentPK pk, final String fileName,
        final ForeignPK foreign, final String lang) {
      return null;
    }

    @Override
    public List<SimpleDocument> listDocumentsLockedByUser(final String usedId,
        final String language) {
      return null;
    }

    @Override
    public void updateIndexEntryWithDocuments(final FullIndexEntry indexEntry) {

    }

    @Override
    public void indexAllDocuments(final WAPrimaryKey fk, final Date startOfVisibilityPeriod,
        final Date endOfVisibilityPeriod) {

    }

    @Override
    public void switchComponentBehaviour(final String componentId, final boolean toVersionning) {

    }

    @Override
    public void switchAllowingDownloadForReaders(final SimpleDocumentPK pk,
        final boolean allowing) {

    }
  }
}
