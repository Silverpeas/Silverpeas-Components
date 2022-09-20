INSERT INTO st_space (id, domainFatherId, name, lang, firstPageType, isInheritanceBlocked)
VALUES (1, NULL, 'Space 1', 'fr', 0, 0),
       (2, 1, 'Space 1-2', 'fr', 0, 0);

INSERT INTO st_componentinstance (id, spaceId, name, componentName, isPublic, isInheritanceBlocked)
VALUES (1, 1, 'Community 1', 'community', 1, 0),
       (2, 2, 'Community 2', 'community', 1, 0);

INSERT INTO st_accesslevel (id, name)
VALUES ('U', 'User'),
       ('A', 'Administrator'),
       ('G', 'Guest'),
       ('R', 'Removed'),
       ('K', 'KMManager'),
       ('D', 'DomainManager');

INSERT INTO st_user (id, domainId, specificId, lastName, firstName, login, accessLevel, state, stateSaveDate)
VALUES (1, 0, '1', 'Simpson', 'Lisa', 'lisa.simpson', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (2, 0, '2', 'Simpson', 'Bart', 'bart.simpson', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (3, 0, '3', 'Simpson', 'Marge', 'marge.simpson', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO sc_community(id, instanceId, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES ('45d42847-2009-4ca8-86bb-1918909dc094', 'community1', '2022-09-08T15:30:00Z', '1', '2022-09-08T15:30:00Z', '1', 0),
       ('a58d70b9-84c7-484a-9102-03eca8e54ea5', 'community1', '2022-09-08T15:30:00Z', '1', '2022-09-08T15:30:00Z', '1', 0);