/* Access level */
INSERT INTO st_accesslevel (id, name) VALUES ('U', 'User');
INSERT INTO st_accesslevel (id, name) VALUES ('A', 'Administrator');
INSERT INTO st_accesslevel (id, name) VALUES ('G', 'Guest');
INSERT INTO st_accesslevel (id, name) VALUES ('R', 'Removed');
INSERT INTO st_accesslevel (id, name) VALUES ('K', 'KMManager');
INSERT INTO st_accesslevel (id, name) VALUES ('D', 'DomainManager');

INSERT INTO st_user (id, domainid, specificid, firstname, lastname, email, login, loginmail, accesslevel, loginquestion, loginanswer, state, stateSaveDate)
VALUES (1, 0, '1', 'Bart', 'Simpson', 'ehu@silverpeas.com', 'user_a', '', 'U', '', '', 'VALID',
        '2012-01-01 00:00:00.0');

INSERT INTO st_componentinstance (id, spaceId, name, componentName, createdBy, createTime, lang)
VALUES (1, 1, 'Our Documents', 'kmelia', 0, '1464811876258', 'en');

INSERT INTO st_componentinstance (id, spaceId, name, componentName, createdBy, createTime, lang)
  VALUES (2, 1, 'Delegated News', 'delegatednews', 0, '1464811876258', 'en');

INSERT INTO sc_delegatednews_news (pubId, instanceId, status, contributorId, beginDate, newsOrder)
  VALUES (1, 'kmelia1', 'ToValidate', '1', '2012-09-17 00:00:00.000', 2);

INSERT INTO sc_delegatednews_news (pubId, instanceId, status, contributorId, beginDate, newsOrder)
  VALUES (2, 'kmelia1', 'Valid', '1', '2012-09-17 00:00:00.000', 1);

INSERT INTO sc_delegatednews_news (pubId, instanceId, status, contributorId, beginDate, newsOrder)
  VALUES (3, 'kmelia1', 'Valid', '1', '2012-09-17 00:00:00.000', 0);