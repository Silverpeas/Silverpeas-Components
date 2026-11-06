-- the FAQ questionReply2: one question with only a private reply, another one with a public reply
INSERT INTO SC_QuestionReply_Question (id, title, content, creatorId, creationDate, status,
  publicReplyNumber, privateReplyNumber, replyNumber, instanceId, categoryId)
VALUES (1, 'Can HR see my sick-leave diagnosis?', 'Asking privately', '1', '2026/01/05', 2, 0, 1, 1,
  'questionReply2', NULL);
INSERT INTO SC_QuestionReply_Question (id, title, content, creatorId, creationDate, status,
  publicReplyNumber, privateReplyNumber, replyNumber, instanceId, categoryId)
VALUES (2, 'How many days off?', 'Days off', '1', '2026/01/06', 2, 1, 0, 1, 'questionReply2',
  NULL);
-- the FAQ questionReply5: a new question, a waiting one with a private reply and a closed one
-- with a public and a private replies
INSERT INTO SC_QuestionReply_Question (id, title, content, creatorId, creationDate, status,
  publicReplyNumber, privateReplyNumber, replyNumber, instanceId, categoryId)
VALUES (3, 'A new question', 'New', '2', '2026/01/10', 0, 0, 0, 0, 'questionReply5', NULL);
INSERT INTO SC_QuestionReply_Question (id, title, content, creatorId, creationDate, status,
  publicReplyNumber, privateReplyNumber, replyNumber, instanceId, categoryId)
VALUES (4, 'A waiting question', 'Waiting', '2', '2026/02/10', 1, 0, 1, 1, 'questionReply5',
  NULL);
INSERT INTO SC_QuestionReply_Question (id, title, content, creatorId, creationDate, status,
  publicReplyNumber, privateReplyNumber, replyNumber, instanceId, categoryId)
VALUES (5, 'A closed question', 'Closed', '3', '2026/03/10', 2, 1, 1, 2, 'questionReply5', '12');

INSERT INTO SC_QuestionReply_Reply (id, questionId, title, content, creatorId, creationDate,
  publicReply, privateReply)
VALUES (10, 1, 'Private reply to 1', '', '4', '2026/01/07', 0, 1);
INSERT INTO SC_QuestionReply_Reply (id, questionId, title, content, creatorId, creationDate,
  publicReply, privateReply)
VALUES (11, 2, 'Public reply to 2', '', '4', '2026/01/07', 1, 0);
INSERT INTO SC_QuestionReply_Reply (id, questionId, title, content, creatorId, creationDate,
  publicReply, privateReply)
VALUES (12, 4, 'Private reply to 4', '', '4', '2026/02/11', 0, 1);
INSERT INTO SC_QuestionReply_Reply (id, questionId, title, content, creatorId, creationDate,
  publicReply, privateReply)
VALUES (13, 5, 'Public reply to 5', '', '4', '2026/03/11', 1, 0);
INSERT INTO SC_QuestionReply_Reply (id, questionId, title, content, creatorId, creationDate,
  publicReply, privateReply)
VALUES (14, 5, 'Private reply to 5', '', '4', '2026/03/12', 0, 1);

INSERT INTO SC_QuestionReply_Recipient (id, questionId, userId) VALUES (1, 3, '4');
INSERT INTO SC_QuestionReply_Recipient (id, questionId, userId) VALUES (2, 4, '4');
INSERT INTO SC_QuestionReply_Recipient (id, questionId, userId) VALUES (3, 5, '4');
INSERT INTO SC_QuestionReply_Recipient (id, questionId, userId) VALUES (4, 1, '4');
INSERT INTO SC_QuestionReply_Recipient (id, questionId, userId) VALUES (5, 4, '5');

INSERT INTO UniqueId (maxId, tableName) VALUES (5, 'sc_questionreply_question');
INSERT INTO UniqueId (maxId, tableName) VALUES (14, 'sc_questionreply_reply');
INSERT INTO UniqueId (maxId, tableName) VALUES (5, 'sc_questionreply_recipient');
