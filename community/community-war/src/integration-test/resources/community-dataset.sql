INSERT INTO uniqueid (maxid, tablename)
VALUES ('5', 'st_space'),
       ('4', 'st_componentinstance'),
       ('6', 'st_user'),
       ('13', 'st_spaceuserrole');

INSERT INTO st_space (id, domainFatherId, name, lang, firstPageType, isInheritanceBlocked)
VALUES (1, NULL, 'Space 1', 'fr', 0, 0),
       (2, 1, 'Space 1-2', 'fr', 0, 0),
       (3, NULL, 'Space 2', 'fr', 0, 0),
       (4, NULL, 'Space 3', 'fr', 0, 0);

INSERT INTO st_componentinstance (id, spaceId, name, componentName, isPublic, isInheritanceBlocked)
VALUES (1, 1, 'Community 1', 'community', 1, 0),
       (2, 2, 'Community 2', 'community', 1, 0),
       (3, 3, 'community 3', 'community', 1, 0);

INSERT INTO st_user (id, domainId, specificId, lastName, firstName, login, accessLevel, state, stateSaveDate)
VALUES (1, 0, '1', 'Simpson', 'Lisa', 'lisa.simpson', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (2, 0, '2', 'Simpson', 'Bart', 'bart.simpson', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (3, 0, '3', 'Simpson', 'Marge', 'marge.simpson', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (4, 0, '4', 'Simpson', 'Homer', 'homer.simpson', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (5, 0, '5', 'Foo', 'John', 'jfoo', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_spaceuserrole(id, spaceid, name, rolename, description, isinherited)
VALUES (1, 1, 'Manager d''espace', 'Manager', '', 0),
       (2, 1, '', 'admin', '', 0),
       (3, 1, '', 'publisher', '', 0),
       (4, 1, '', 'writer', '', 0),
       (5, 1, '', 'reader', '', 0),
       (6, 2, '', 'admin', '', 1),
       (7, 2, '', 'publisher', '', 1),
       (8, 2, '', 'writer', '', 1),
       (9, 2, '', 'reader', '', 1),
       (10, 2, '', 'publisher', '', 0),
       (11, 4, '', 'admin', '', 0),
       (12, 4, '', 'publisher', '', 0);

INSERT INTO st_spaceuserrole_user_rel(spaceuserroleid, userid)
VALUES (1, 0), (1, 1), (2, 1), (3, 2), (5, 3),
       (6, 1),(7, 2),(9,3),(10, 1), (10, 2),
       (11, 2), (12, 3);

INSERT INTO sc_community(id, instanceId, spaceId, homePage, homePageType, charterURL)
VALUES ('45d42847-2009-4ca8-86bb-1918909dc094', 'community1', 'WA1', 'kmelia42', 1, 'https://www.silverpeas.org'),
       ('a58d70b9-84c7-484a-9102-03eca8e54ea5', 'community2', 'WA2', NULL, NULL, NULL),
       ('4aaf4587-fa14-4acf-ac04-e63fb57b87ec', 'community3', 'WA3', NULL, NULL, NULL);

INSERT INTO sc_community_membership
(id, community, userId, status, createDate, createdBy, joiningDate, lastUpdateDate, lastUpdatedBy, version)
VALUES
    ('fdf8ec9c-650f-43aa-905e-d5289648a008', '45d42847-2009-4ca8-86bb-1918909dc094', '1', 'COMMITTED', '2022-01-01 00:00:00.000', '0', '2022-01-01 00:00:00.000', '2022-01-01 00:00:00.000', '0', 1),
    ('6d571b08-7baf-47d9-b74c-dfbbe3cee6ad', '45d42847-2009-4ca8-86bb-1918909dc094', '2', 'COMMITTED', '2022-01-01 00:00:00.000', '0', '2022-01-02 00:00:00.000', '2022-01-02 00:00:00.000', '0', 1),
    ('3b374e3d-2fff-4d1a-9ece-d4abf82bd2f4', '45d42847-2009-4ca8-86bb-1918909dc094', '3', 'COMMITTED', '2022-01-01 00:00:00.000', '0', '2022-01-03 00:00:00.000', '2022-01-03 00:00:00.000', '0', 1),
    ('1555d0f1-d9ad-4d55-91e4-1f140c8ff914', '45d42847-2009-4ca8-86bb-1918909dc094', '4', 'REFUSED', '2021-12-23 00:00:00.000', '0', NULL, '2021-12-24 00:00:00.000', '0', 1),
    ('da60d652-bfe2-486d-ab9c-a8171b5cd7b9', '45d42847-2009-4ca8-86bb-1918909dc094', '5', 'PENDING', '2022-01-01 09:00:00.000', '0', NULL, '2022-01-09 00:00:00.000', '0', 0),
    ('c44064ef-1118-4bfb-8494-de6df5e7cbdb', '45d42847-2009-4ca8-86bb-1918909dc094', '4', 'REMOVED', '2022-01-01 00:00:00.000', '0', '2022-01-01 10:00:00.000', '2022-04-05 00:00:00.000', '0', 1),
    ('5d64ded7-89d2-4ffa-87cb-03146e94588d', 'a58d70b9-84c7-484a-9102-03eca8e54ea5', '1', 'REMOVED', '2022-01-01 00:00:00.000', '0', '2022-01-01 00:00:00.000', '2022-04-01 00:00:00.000', '0', 1),
    ('5f78752a-ed3c-4c88-bfe9-c647cd9c98e6', 'a58d70b9-84c7-484a-9102-03eca8e54ea5', '3', 'COMMITTED', '2022-01-01 00:00:00.000', '0', '2022-01-02 00:00:00.000', '2022-01-02 00:00:00.000', '0', 0),
    ('67571a42-443c-4100-b0b0-9a9638204646', 'a58d70b9-84c7-484a-9102-03eca8e54ea5', '4', 'PENDING', '2022-01-03 00:00:00.000', '0', NULL, '2022-01-03 00:00:00.000', '0', 0);
