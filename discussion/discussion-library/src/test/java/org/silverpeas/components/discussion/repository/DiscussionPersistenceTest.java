/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.discussion.repository;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.discussion.model.*;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** Executes the shipped migration and actual repository JPQL against H2/Hibernate. */
@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionPersistenceTest {
  private static final String URL = "jdbc:h2:mem:discussion;DB_CLOSE_DELAY=-1";
  @TestManagedMock
  private EntityManagerProvider provider;
  @TestedBean
  private ConversationRepository conversations;
  @TestedBean
  private DiscussionMessageRepository messages;
  @TestedBean
  private DiscussionReadStateRepository reads;
  @TestedBean
  private DiscussionLikeRepository likes;
  @TestedBean
  private DiscussionSeenRepository seen;
  private SessionFactory factory;
  private Session session;

  @BeforeEach
  void database() throws Exception {
    try (var connection = DriverManager.getConnection(URL, "sa", "");
         var statement = connection.createStatement()) {
      statement.execute("DROP ALL OBJECTS");
      // SQLScript in silversetup splits on every semicolon, including those in comments.
      for (String sql : installerStatements("h2")) {
        statement.addBatch(sql);
      }
      statement.executeBatch();
    }
    factory = new Configuration().addAnnotatedClass(Conversation.class)
        .addAnnotatedClass(DiscussionSeen.class).addAnnotatedClass(DiscussionLike.class).addAnnotatedClass(DiscussionMessage.class).addAnnotatedClass(DiscussionReadState.class)
        .setProperty("hibernate.connection.url", URL)
        .setProperty("hibernate.connection.username", "sa")
        .setProperty("hibernate.connection.password", "")
        .setProperty("hibernate.hbm2ddl.auto", "validate").buildSessionFactory();
    session = factory.openSession();
    when(provider.getEntityManager()).thenReturn(session);
    session.beginTransaction();
    session.createNativeMutationQuery("""
        insert into sc_discussion_conversation
        (id, instanceId, scopeKey, lastSequence, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
        values ('c1', 'discussion1', 'main', 4, CURRENT_TIMESTAMP, 'user', CURRENT_TIMESTAMP, 'user', 0),
               ('c2', 'discussion2', 'main', 0, CURRENT_TIMESTAMP, 'user', CURRENT_TIMESTAMP, 'user', 0)
        """).executeUpdate();
    insertMessage("m1", "c1", 1, "other", false);
    insertMessage("m2", "c1", 2, "user", false);
    insertMessage("m3", "c1", 3, "other", true);
    insertMessage("m4", "c1", 4, "other", false);
    insertMessage("m5", "c2", 1, "other", false);
    session.createNativeMutationQuery("""
        insert into sc_discussion_read
        (id, conversationId, userId, lastReadSequence, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
        values ('r1', 'c1', 'user', 1, CURRENT_TIMESTAMP, 'user', CURRENT_TIMESTAMP, 'user', 0)
        """).executeUpdate();
  }

  @AfterEach
  void closeDatabase() {
    if (session != null) {
      session.getTransaction().rollback();
      session.close();
    }
    if (factory != null) {
      factory.close();
    }
  }

  @Test
  void developmentPinUpgradePreservesExistingMessagesAndDefaultsToUnpinned() throws Exception {
    session.createNativeMutationQuery("alter table sc_discussion_message drop column pinned").executeUpdate();
    try (var input = getClass().getResourceAsStream("/migrations/db/h2/discussion/001/add_pins.sql")) {
      assertNotNull(input);
      String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
      for (String statement : script.split(";")) {
        if (!statement.isBlank()) {
          session.createNativeMutationQuery(statement).executeUpdate();
        }
      }
    }
    session.clear();
    assertTrue(messages.pinned("c1").isEmpty());
    assertEquals(4, messages.history("c1", Long.MAX_VALUE, 10).size());
  }

  @Test
  void pinnedMessagesIncludeOldHistoryButExcludeDeletedAndOtherConversations() {
    session.createNativeMutationQuery("update sc_discussion_message set pinned=true where id in ('m1','m3','m4','m5')").executeUpdate();
    assertEquals(List.of("m4", "m1"), messages.pinned("c1").stream().map(DiscussionMessage::getId).toList());
    session.createNativeMutationQuery("update sc_discussion_message set pinned=false where id='m1'").executeUpdate();
    session.clear();
    assertEquals(List.of("m4"), messages.pinned("c1").stream().map(DiscussionMessage::getId).toList());
  }

  @Test
  void publicationReferencesIncludeOlderMessagesButExcludeDeletedAndOtherInstances() {
    session.createNativeMutationQuery("update sc_discussion_message set body='/silverpeas/Publication/12' where id in ('m1','m3','m5')").executeUpdate();
    try (var bodies = messages.publicationLinkBodies("c1")) {
      assertEquals(List.of("/silverpeas/Publication/12"), bodies.toList());
    }
    session.createNativeMutationQuery("update sc_discussion_message set body='Citation removed' where id='m1'").executeUpdate();
    try (var bodies = messages.publicationLinkBodies("c1")) {
      assertTrue(bodies.toList().isEmpty());
    }
  }

  @Test
  void aVisibleRecentMessageDoesNotAcknowledgeOlderUnseenMessages() {
    assertEquals(2, messages.unread("c1", "user", 0));
    session.createNativeMutationQuery("""
        insert into sc_discussion_seen
        (id, conversationId, messageId, userId, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
        values ('s1', 'c1', 'm4', 'user', CURRENT_TIMESTAMP, 'user', CURRENT_TIMESTAMP, 'user', 0)
        """).executeUpdate();
    assertEquals(1, messages.unread("c1", "user", 0));
    assertEquals(0, messages.unread("c1", "user", 1));
    assertEquals(1, messages.unread("c2", "user", 0));
    assertEquals(List.of("m4"), seen.seen("c1", "user", List.of("m1", "m4")));
    assertTrue(seen.seen("c1", "other", List.of("m4")).isEmpty());
    assertTrue(seen.seen("c2", "user", List.of("m4")).isEmpty());
    assertTrue(seen.seen("c1", "user", List.of()).isEmpty());
    seen.deleteConversation("c1");
    assertEquals(2, messages.unread("c1", "user", 0));
  }

  @Test
  void liveQueriesIncludeDeletedRowsAndNeverLeakAnotherConversation() {
    assertEquals(List.of("m2", "m3", "m4"), messages.newer("c1", 1).stream()
        .map(DiscussionMessage::getId).toList());
    assertEquals(List.of("m3"), messages.selected("c1", List.of("m3", "m5")).stream()
        .map(DiscussionMessage::getId).toList());
    assertTrue(messages.selected("c1", List.of()).isEmpty());
    java.util.stream.IntStream.rangeClosed(5, 110)
        .forEach(sequence -> insertMessage("new" + sequence, "c1", sequence, "other", false));
    var batch = messages.newer("c1", 4);
    assertEquals(100, batch.size());
    assertEquals(5, batch.getFirst().getSequenceNumber());
    assertEquals(104, batch.getLast().getSequenceNumber());
  }

  @Test
  void historyIsBoundedAndIsolatedAndSurvivesAppends() {
    assertEquals("c1", conversations.main("discussion1", true).getId());
    assertEquals(List.of(4L, 3L), messages.history("c1", Long.MAX_VALUE, 2).stream()
        .map(DiscussionMessage::getSequenceNumber).toList());
    insertMessage("m6", "c1", 5, "other", false);
    assertEquals(List.of(2L, 1L), messages.history("c1", 3, 2).stream()
        .map(DiscussionMessage::getSequenceNumber).toList());
    assertTrue(messages.history("c1", 1, 2).isEmpty());
  }

  @Test
  void authorsAreDistinctAndIncludeOlderHistoryButExcludeDeletedMessagesAndOtherInstances() {
    insertMessage("deleted-only", "c1", 6, "deleted-author", true);
    insertMessage("other-instance", "c2", 2, "outside-author", false);
    assertEquals(List.of("other", "user"), messages.authorIds("c1"));
  }

  @Test
  void unreadExcludesOwnAndDeletedMessagesAndOtherConversations() {
    assertEquals(2, messages.unread("c1", "user", 0));
    assertEquals(1, messages.unread("c1", "user", 1));
    assertEquals(0, messages.unread("c1", "user", 4));
    assertEquals(1, reads.find("c1", "user").orElseThrow().getLastReadSequence());
    assertTrue(reads.find("c2", "user").isEmpty());
  }

  @Test
  void instanceCleanupDeletesMessagesAndReadPositionsTogether() {
    messages.deleteConversation("c1");
    reads.deleteConversation("c1");
    assertTrue(messages.history("c1", 10, 30).isEmpty());
    assertTrue(reads.find("c1", "user").isEmpty());
    assertEquals(1, messages.history("c2", 10, 30).size());
  }

  @Test
  void allVendorScriptsAreCompatibleWithTheInstallerStatementSplitter() throws Exception {
    for (String database : List.of("h2", "postgresql", "mssql", "oracle")) {
      List<String> statements = installerStatements(database);
      assertEquals(6, statements.size(), database);
      for (String statement : statements) {
        String executable = statement.lines()
            .filter(line -> !line.stripLeading().startsWith("--"))
            .collect(Collectors.joining("\n")).strip();
        assertTrue(executable.startsWith("CREATE TABLE ") || executable.startsWith("CREATE INDEX "),
            database + ": " + executable);
      }
    }
  }

  @Test
  void likesAreUniqueAndCountsAreScopedAndCanBeCleanedUp() {
    session.createNativeMutationQuery("""
        insert into sc_discussion_like
        (id, conversationId, messageId, userId, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
        values ('like1', 'c1', 'm1', 'user', CURRENT_TIMESTAMP, 'user', CURRENT_TIMESTAMP, 'user', 0),
               ('like2', 'c1', 'm1', 'other', CURRENT_TIMESTAMP, 'other', CURRENT_TIMESTAMP, 'other', 0),
               ('like3', 'c2', 'm5', 'user', CURRENT_TIMESTAMP, 'user', CURRENT_TIMESTAMP, 'user', 0)
        """).executeUpdate();
    var states = likes.states("c1", List.of("m1", "m5"), "user");
    assertEquals(2, states.get("m1").count());
    assertTrue(states.get("m1").liked());
    assertFalse(states.containsKey("m5"));
    assertTrue(likes.find("m1", "user").isPresent());
    likes.deleteMessage("m1");
    assertTrue(likes.states("c1", List.of("m1"), "user").isEmpty());
    assertFalse(likes.states("c2", List.of("m5"), "user").isEmpty());
    likes.deleteConversation("c2");
    assertTrue(likes.find("m5", "user").isEmpty());
  }

  @Test
  void theDatabaseRejectsDuplicateLikesForTheSameMessageAndUser() {
    String insert = """
        insert into sc_discussion_like
        (id, conversationId, messageId, userId, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
        values (:id, 'c1', 'm1', 'user', CURRENT_TIMESTAMP, 'user', CURRENT_TIMESTAMP, 'user', 0)
        """;
    session.createNativeMutationQuery(insert).setParameter("id", "unique-like").executeUpdate();
    assertThrows(jakarta.persistence.PersistenceException.class,
        () -> session.createNativeMutationQuery(insert).setParameter("id", "duplicate-like").executeUpdate());
  }

  @Test
  void developmentUpdateAddsLikesToTheOriginal001SchemaWithoutLosingMessages() throws Exception {
    try (var connection = DriverManager.getConnection("jdbc:h2:mem:developmentUpdate", "sa", "");
         var statement = connection.createStatement()) {
      // Rebuild the original 001 from its first four statements, then apply only the additive script.
      for (String sql : installerStatements("h2").subList(0, 4)) {
        statement.execute(sql);
      }
      statement.execute("""
          insert into sc_discussion_conversation
          (id, instanceId, scopeKey, lastSequence, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
          values ('existing', 'discussion99', 'main', 0, CURRENT_TIMESTAMP, 'user', CURRENT_TIMESTAMP, 'user', 0)
          """);
      statement.execute("""
          insert into sc_discussion_message
          (id, conversationId, sequenceNumber, body, deleted, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
          values ('kept', 'existing', 1, 'Preserved message', false, CURRENT_TIMESTAMP, 'user', CURRENT_TIMESTAMP, 'user', 0)
          """);
      try (var input = getClass().getResourceAsStream("/migrations/db/h2/discussion/001/add_likes.sql")) {
        assertNotNull(input);
        for (String sql : new String(input.readAllBytes(), StandardCharsets.UTF_8).split(";")) {
          if (!sql.isBlank()) { statement.execute(sql); }
        }
      }
      try (var rows = statement.executeQuery("select count(*) from sc_discussion_conversation")) {
        assertTrue(rows.next());
        assertEquals(1, rows.getInt(1));
      }
      try (var rows = statement.executeQuery("select body from sc_discussion_message where id = 'kept'")) {
        assertTrue(rows.next());
        assertEquals("Preserved message", rows.getString(1));
      }
      try (var rows = statement.executeQuery("select count(*) from sc_discussion_like")) {
        assertTrue(rows.next());
        assertEquals(0, rows.getInt(1));
      }
    }
  }

  private List<String> installerStatements(String database) throws Exception {
    String resource = "/migrations/db/" + database + "/discussion/001/create_table.sql";
    try (var input = getClass().getResourceAsStream(resource)) {
      assertNotNull(input, resource);
      String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
      return Arrays.stream(script.split(";")).map(String::strip)
          .filter(statement -> !statement.isEmpty()).toList();
    }
  }

  private void insertMessage(String id, String conversation, long sequence, String author, boolean deleted) {
    session.createNativeMutationQuery("""
        insert into sc_discussion_message
        (id, conversationId, sequenceNumber, body, deleted, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
        values (:id, :conversation, :sequence, :body, :deleted, CURRENT_TIMESTAMP, :author,
                CURRENT_TIMESTAMP, :author, 0)
        """).setParameter("id", id).setParameter("conversation", conversation)
        .setParameter("sequence", sequence).setParameter("body", deleted ? " " : "message")
        .setParameter("deleted", deleted).setParameter("author", author).executeUpdate();
  }
}
