/* The forums */
INSERT INTO sc_forums_forum (forumId, forumName, forumCreationDate, forumCreator, forumActive,
                             forumParent, instanceId, categoryId)
    VALUES (8, 'Utilisation de Silverpeas', '2004/03/26', '1', 1, 0, 'forums130', '2');

INSERT INTO sc_forums_forum (forumId, forumName, forumCreationDate, forumCreator, forumActive,
                             forumParent, instanceId, categoryId)
    VALUES (1, 'Forum racine', '2004/03/26', '1', 1, 0, 'forums122', NULL);

INSERT INTO sc_forums_forum (forumId, forumName, forumCreationDate, forumCreator, forumActive,
                             forumParent, instanceId, categoryId)
    VALUES (2, 'Forum Père', '2004/03/26', '1', 1, 0, 'forums122', NULL);

INSERT INTO sc_forums_forum (forumId, forumName, forumCreationDate, forumCreator, forumActive,
                             forumParent, instanceId, categoryId)
    VALUES (3, 'Forum Fils', '2004/03/26', '1', 1, 2, 'forums122', '2');

INSERT INTO sc_forums_forum (forumId, forumName, forumCreationDate, forumCloseDate, forumCreator,
                             forumActive, forumParent, forumLockLevel, instanceId, categoryId)
    VALUES (5, 'Forum Fermé', '2004/03/26', '2010/10/10', '10', 0, 0, 1, 'forums100', NULL);

INSERT INTO sc_forums_forum (forumId, forumName, forumCreationDate, forumCloseDate, forumCreator,
                             forumActive, forumParent, forumLockLevel, instanceId, categoryId)
    VALUES (7, 'Forum Modéré', '2004/03/26', '2010/10/10', '10', 0, 0, 2, 'forums100', NULL);

/* The messages in forums */
INSERT INTO sc_forums_message (messageId, messageTitle, messageAuthor, forumId, messageParentId,
                              messageDate, status)
    VALUES (10, 'Sujet Forum racine', '26', 1, 0, '2013-06-17 19:05:27', 'V');

INSERT INTO sc_forums_message (messageId, messageTitle, messageAuthor, forumId, messageParentId,
                               messageDate, status)
    VALUES (11, 'Re : Sujet Forum racine', '26', 1, 10, '2013-06-17 19:05:27', 'V');

INSERT INTO sc_forums_message (messageId, messageTitle, messageAuthor, forumId, messageParentId,
                               messageDate, status)
    VALUES (12, 'Re : Re : Sujet Forum racine', '26', 1, 11, '2013-06-17 19:05:27', 'V');

INSERT INTO sc_forums_message (messageId, messageTitle, messageAuthor, forumId, messageParentId,
                               messageDate, status)
    VALUES (20, 'Sujet Forum Père', '26', 2, 0, '2013-06-17 19:05:27', 'V');

INSERT INTO sc_forums_message (messageId, messageTitle, messageAuthor, forumId, messageParentId,
                               messageDate, status)
    VALUES (30, 'Sujet Forum Fils', '26', 3, 0, '2013-06-17 19:05:27', 'V');

/* The moderators */
INSERT INTO sc_forums_rights (userId, forumId) VALUES ('26', '2');
INSERT INTO sc_forums_rights (userId, forumId) VALUES ('26', '3');