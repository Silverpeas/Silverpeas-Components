ALTER TABLE sc_resources_managers MODIFY managerId NUMBER(19,0);
ALTER TABLE sc_resources_managers MODIFY resourceId NUMBER(19,0);

ALTER TABLE sc_resources_reservedresource MODIFY reservationId  NUMBER(19,0);
ALTER TABLE sc_resources_reservedresource MODIFY resourceId  NUMBER(19,0);

ALTER TABLE sc_resources_reservation MODIFY userId INTEGER;
ALTER TABLE sc_resources_reservation MODIFY id NUMBER(19,0);

ALTER TABLE sc_resources_resource MODIFY id NUMBER(19,0);
ALTER TABLE sc_resources_resource MODIFY responsibleid INTEGER;
ALTER TABLE sc_resources_resource MODIFY categoryid NUMBER(19,0);

ALTER TABLE sc_resources_category MODIFY id NUMBER(19,0);
ALTER TABLE sc_resources_category MODIFY responsibleid INTEGER;

UPDATE uniqueid SET TABLENAME='sc_resources_reservation' WHERE TABLENAME='SC_Resources_Reservation';
UPDATE uniqueid SET TABLENAME='sc_resources_resource' WHERE TABLENAME='SC_Resources_Resource';
UPDATE uniqueid SET TABLENAME='sc_resources_category' WHERE TABLENAME='SC_Resources_Category';