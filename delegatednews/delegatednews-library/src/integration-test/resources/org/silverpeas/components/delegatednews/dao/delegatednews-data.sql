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

INSERT INTO sc_delegatednews_news (pubid, instanceid, status, contributorid, validatorid, validationdate, begindate, enddate, newsorder)
VALUES ('1', 'kmelia1', 'ToValidate', '1', null, null, '2012-09-17 00:00:00', null, '2'),
       ('2', 'kmelia1', 'Valid', '1', '1', '2012-10-17 00:00:00', '2012-09-17 00:00:00', null, '1'),
       ('3', 'kmelia1', 'Valid', '1', '1', '2012-09-27 00:00:00', '2012-09-17 00:00:00', null, '0');
