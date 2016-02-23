/* The categories */
INSERT INTO sc_resources_category (id, instanceId, name, creationDate, updateDate, bookable, form,
                                   responsibleId, createrId, updaterId, description)
VALUES
  (1, 'resourcesManager42', 'Salles', '1315232752398', '1315232752398', 1, 'model1.xml', 5, '5',
      '5', 'Salles de réunion');

INSERT INTO sc_resources_category (id, instanceId, name, creationDate, updateDate, bookable, form,
                                   responsibleId, createrId, updaterId, description)
VALUES
  (2, 'resourcesManager42', 'Voitures', '1315232752398', '1315232752398', 1, NULL, 6, '6',
      '6', 'Véhicules utilitaires');

/* The available resources */
INSERT INTO sc_resources_resource (id, instanceid, categoryid, name, creationdate, updatedate,
                                   bookable, createrid, updaterid, description)
VALUES
  (1, 'resourcesManager42', 1, 'Salle Chartreuse', '1315232852398', '1315232852398', 1, '5', '5',
   'Salle de réunion jusqu''à 4 personnes');

INSERT INTO sc_resources_resource (id, instanceid, categoryid, name, creationdate, updatedate,
                                   bookable, createrid, updaterid, description)
VALUES
  (2, 'resourcesManager42', 1, 'Salle Belledonne', '1315232852398', '1315232852398', 1, '5', '5',
   'Salle de réunion jusqu''à 12 personnes');

INSERT INTO sc_resources_resource (id, instanceid, categoryid, name, creationdate, updatedate,
                                   bookable, createrid, updaterid, description)
VALUES
  (3, 'resourcesManager42', 2, 'Twingo verte - 156 VV 38', '1315232852398', '1315232852398', 1, '5',
   '5', 'Twingo verte 4 places 5 portes');

INSERT INTO sc_resources_resource (id, instanceid, categoryid, name, creationdate, updatedate,
                                   bookable, createrid, updaterid, description)
VALUES
  (4, 'resourcesManager42', 2, 'Twingo verte - 156 VV 73', '1315232852398', '1315232852398', 0, '5',
   '5', 'Twingo bleue 4 places 5 portes');

INSERT INTO sc_resources_resource (id, instanceid, categoryid, name, creationdate, updatedate,
                                   bookable, createrid, updaterid, description)
VALUES
  (5, 'resourcesManager42', 1, 'Salle Vercors', '1315232852398', '1315232852398', 1, '5', '5',
   'Salle de réunion jusqu''à 9 personnes');

/* the resources' managers */
INSERT INTO sc_resources_managers (resourceid, managerid) VALUES (1, 0);
INSERT INTO sc_resources_managers (resourceid, managerid) VALUES (1, 1);
INSERT INTO sc_resources_managers (resourceid, managerid) VALUES (1, 2);
INSERT INTO sc_resources_managers (resourceid, managerid) VALUES (2, 0);
INSERT INTO sc_resources_managers (resourceid, managerid) VALUES (3, 0);
INSERT INTO sc_resources_managers (resourceid, managerid) VALUES (3, 9);
INSERT INTO sc_resources_managers (resourceid, managerid) VALUES (5, 3);

/* The reservations */
INSERT INTO sc_resources_reservation (id, instanceId, evenement, userId, creationDate, updateDate, beginDate, endDate, reason, place, status)
VALUES (3, 'resourcesManager42', 'Test de la Toussaint', 2, '1319811924467', '1319811924467',
           '1320134400000', '1320163200000', 'To test', 'at work', 'test');

INSERT INTO sc_resources_reservation (id, instanceId, evenement, userId, creationDate, updateDate, beginDate, endDate, reason, place, status)
VALUES (4, 'resourcesManager42', 'Test réservation 20/12/2011', 9, '1320225012008', '1320225012008',
           '1324368000000', '1324375200000', 'To test a reservzation', 'at work', 'A');

INSERT INTO sc_resources_reservation (id, instanceId, evenement, userId, creationDate, updateDate, beginDate, endDate, reason, place, status)
VALUES (5, 'resourcesManager42', 'Test réservation validée 20/12/2011', 2, '1319811924467', '1319811924467',
           '1324368000000', '1324375200000', 'To test a reservzation validated', 'at work', 'V');

INSERT INTO sc_resources_reservation (id, instanceId, evenement, userId, creationDate, updateDate, beginDate, endDate, reason, place, status)
VALUES (6, 'resourcesManager42', 'Test réservation refusée 20/12/2011', 2, '1319811924467', '1319811924467',
           '1324375200000', '1324382400000', 'To test a reservzation refused', 'at work', 'R');


/* The resources reserved in the reservations */
INSERT INTO sc_resources_reservedresource (reservationid, resourceid, status) VALUES (3, 1, 'V');
INSERT INTO sc_resources_reservedresource (reservationid, resourceid, status) VALUES (3, 2, 'R');
INSERT INTO sc_resources_reservedresource (reservationid, resourceid, status) VALUES (3, 3, 'test');
INSERT INTO sc_resources_reservedresource (reservationid, resourceid, status) VALUES (4, 3, 'A');
INSERT INTO sc_resources_reservedresource (reservationid, resourceid, status) VALUES (5, 1, 'V');
INSERT INTO sc_resources_reservedresource (reservationid, resourceid, status) VALUES (6, 2, 'R');

/* The next id for resources */
INSERT INTO uniqueid (maxId, tableName) VALUES (20, 'SC_Resources_Resource');
