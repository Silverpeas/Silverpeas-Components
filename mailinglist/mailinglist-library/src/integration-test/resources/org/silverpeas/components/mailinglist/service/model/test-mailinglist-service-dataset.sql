INSERT INTO st_domain (id, name, description, propFilename, className, authenticationServer, silverpeasServerURL)
VALUES (0, 'Silverpeas', 'Domaine local', 'org.silverpeas.domains.domainSP',
        'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP', 'http://localhost:8000');

INSERT INTO st_domain (id, name, description, propFilename, className, authenticationServer, silverpeasServerURL)
VALUES (-1, 'internal', 'internal', '-', '-', '-', '');

INSERT INTO st_user (id, domainId, specificId, lastName, email, login, accessLevel, state, stateSaveDate)
VALUES (0, 0, '0', 'Administrateur', 'admin@silverpeas.com', 'SilverAdmin', 'A', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstname, lastName, email, login, accessLevel, state, stateSaveDate)
VALUES (200, 0, '200', 'bart', 'simpson', 'bart.simpson@silverpeas.com', 'bsimpson', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstname, lastName, email, login, accessLevel, state, stateSaveDate)
VALUES (201, 0, '201', 'lisa', 'simpson', 'lisa.simpson@silverpeas.com', 'lsimpson', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstname, lastName, email, login, accessLevel, state, stateSaveDate)
VALUES (202, 0, '202', 'homer', 'simpson', 'homer.simpson@silverpeas.com', 'hsimpson', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstname, lastName, email, login, accessLevel, state, stateSaveDate)
VALUES (203, 0, '203', 'marge', 'simpson', 'marge.simpson@silverpeas.com', 'msimpson', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstname, lastName, email, login, accessLevel, state, stateSaveDate)
VALUES (204, 0, '204', 'maggie', 'simpson', 'maggie.simpson@silverpeas.com', 'magsimpson', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstname, lastName, email, login, accessLevel, state, stateSaveDate)
VALUES (205, 0, '205', 'krusty', 'theklown', '"krusty.theklown@silverpeas.com', 'ktheklown', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO domainsp_user (id, lastName, login, password, passwordvalid)
VALUES (1, 'Administrateur', 'SilverAdmin', 'SPJmYbTN83aEs', 'Y');

INSERT INTO st_space (id, domainFatherId, name, lang, firstPageType, firstpageextraparam, ordernum, isInheritanceBlocked)
VALUES (3, NULL, 'MGI Coutier', 'fr', 1, 'indicateurs16', 2, 0);

INSERT INTO st_spaceuserrole (id, spaceid, name, rolename, isinherited) VALUES (2, 3, '', 'admin', 0);
INSERT INTO st_spaceuserrole (id, spaceid, name, rolename, isinherited) VALUES (3, 3, 'Manager d''espace', 'Manager', 0);
INSERT INTO st_spaceuserrole (id, spaceid, name, rolename, isinherited) VALUES (4, 3, '', 'reader', 0);

INSERT INTO st_componentinstance (id, spaceid, name, componentname, lang, description, ordernum)
VALUES (100, 3, 'Liste de diffusion de test', 'mailingList', 'fr', 'Gestion d''une liste de diffusion', 5);

INSERT INTO st_instance_data (id, componentid, name, label, value)
VALUES (400, 100, 'notify', 'Liste de distribution / d''archivage', 'true');

INSERT INTO st_instance_data (id, componentid, name, label, value)
VALUES (403, 100, 'moderated', 'Liste de diffusion modérée', 'true');

INSERT INTO st_instance_data (id, componentid, name, label, value)
VALUES (404, 100, 'open', 'Liste de diffusion ouverte', 'no');

INSERT INTO st_instance_data (id, componentid, name, label, value)
VALUES (405, 100, 'subscribedAddress', 'Adresse', 'thesimpsons@silverpeas.com');

INSERT INTO st_instance_data (id, componentid, name, label, value)
VALUES (406, 100, 'rss', 'Flux RSS', 'yes');

INSERT INTO st_userrole (id, instanceid, rolename, name) VALUES (300, 100, 'admin', 'Gestionnaire');
INSERT INTO st_userrole (id, instanceid, rolename, name) VALUES (301, 100, 'moderator', 'Modérateur');
INSERT INTO st_userrole (id, instanceid, rolename, name) VALUES (302, 100, 'reader', 'Abonnés');

/* bart admin */
INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES (300, 200);
/* homer & marge moderator */
INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES (301, 202);
INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES (301, 203);
/* lisa and maggie subscribers */
INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES (302, 201);
INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES (302, 204);
