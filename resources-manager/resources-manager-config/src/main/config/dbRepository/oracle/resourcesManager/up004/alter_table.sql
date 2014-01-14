ALTER TABLE sc_resources_managers DROP CONSTRAINT PK_Resources_Managers;

ALTER TABLE sc_resources_managers ADD managerId_temp NUMBER(19,0);
UPDATE sc_resources_managers SET managerId_temp = managerId;
ALTER TABLE sc_resources_managers DROP COLUMN managerId;
ALTER TABLE sc_resources_managers ADD managerId NUMBER(19,0);
UPDATE sc_resources_managers SET managerId = managerId_temp;
ALTER TABLE sc_resources_managers DROP COLUMN managerId_temp;
ALTER TABLE sc_resources_managers MODIFY managerId NOT NULL;

ALTER TABLE sc_resources_managers ADD resourceId_temp NUMBER(19,0);
UPDATE sc_resources_managers SET resourceId_temp = managerId;
ALTER TABLE sc_resources_managers DROP COLUMN resourceId;
ALTER TABLE sc_resources_managers ADD resourceId NUMBER(19,0);
UPDATE sc_resources_managers SET resourceId = resourceId_temp;
ALTER TABLE sc_resources_managers DROP COLUMN resourceId_temp;
ALTER TABLE sc_resources_managers MODIFY resourceId NOT NULL;

ALTER TABLE sc_resources_managers ADD CONSTRAINT PK_Resources_Managers PRIMARY KEY (
  resourceId, 
  managerId
);

ALTER TABLE sc_resources_reservedresource DROP CONSTRAINT PK_Resources_ReservedResource;

ALTER TABLE sc_resources_reservedresource ADD reservationId_temp NUMBER(19,0);
UPDATE sc_resources_reservedresource SET reservationId_temp = reservationId;
ALTER TABLE sc_resources_reservedresource DROP COLUMN reservationId;
ALTER TABLE sc_resources_reservedresource ADD reservationId NUMBER(19,0);
UPDATE sc_resources_reservedresource SET reservationId = reservationId_temp;
ALTER TABLE sc_resources_reservedresource DROP COLUMN reservationId_temp;
ALTER TABLE sc_resources_reservedresource MODIFY reservationId NOT NULL;

ALTER TABLE sc_resources_reservedresource ADD resourceId_temp NUMBER(19,0);
UPDATE sc_resources_reservedresource SET resourceId_temp = resourceId;
ALTER TABLE sc_resources_reservedresource DROP COLUMN resourceId;
ALTER TABLE sc_resources_reservedresource ADD resourceId NUMBER(19,0);
UPDATE sc_resources_reservedresource SET resourceId = resourceId_temp;
ALTER TABLE sc_resources_reservedresource DROP COLUMN resourceId_temp;
ALTER TABLE sc_resources_reservedresource MODIFY resourceId NOT NULL;

ALTER TABLE sc_resources_reservedresource ADD CONSTRAINT PK_Resources_ReservedResource PRIMARY KEY (
	reservationId,
	resourceId	
);


ALTER TABLE sc_resources_reservation DROP CONSTRAINT PK_Resources_Reservation;

ALTER TABLE sc_resources_reservation ADD userId_temp INTEGER;
UPDATE sc_resources_reservation SET userId_temp = userId;
ALTER TABLE sc_resources_reservation DROP COLUMN userId;
ALTER TABLE sc_resources_reservation ADD userId INTEGER;
UPDATE sc_resources_reservation SET userId = userId_temp;
ALTER TABLE sc_resources_reservation DROP COLUMN userId_temp;
ALTER TABLE sc_resources_reservation MODIFY userId NOT NULL;

ALTER TABLE sc_resources_reservation ADD id_temp NUMBER(19,0);
UPDATE sc_resources_reservation SET id_temp = id;
ALTER TABLE sc_resources_reservation DROP COLUMN id;
ALTER TABLE sc_resources_reservation ADD id NUMBER(19,0);
UPDATE sc_resources_reservation SET id = id_temp;
ALTER TABLE sc_resources_reservation DROP COLUMN id_temp;
ALTER TABLE sc_resources_reservation MODIFY id NOT NULL;

ALTER TABLE sc_resources_reservation ADD CONSTRAINT PK_Resources_Reservation PRIMARY KEY (id);


ALTER TABLE sc_resources_resource DROP CONSTRAINT PK_Resources_Resource;

ALTER TABLE sc_resources_resource ADD id_temp NUMBER(19,0);
UPDATE sc_resources_resource SET id_temp = id;
ALTER TABLE sc_resources_resource DROP COLUMN id;
ALTER TABLE sc_resources_resource ADD id NUMBER(19,0);
UPDATE sc_resources_resource SET id = id_temp;
ALTER TABLE sc_resources_resource DROP COLUMN id_temp;
ALTER TABLE sc_resources_resource MODIFY id NOT NULL;

ALTER TABLE sc_resources_resource ADD responsibleid_temp INTEGER;
UPDATE sc_resources_resource SET responsibleid_temp = responsibleid;
ALTER TABLE sc_resources_resource DROP COLUMN responsibleid;
ALTER TABLE sc_resources_resource ADD responsibleid INTEGER;
UPDATE sc_resources_resource SET responsibleid = responsibleid_temp;
ALTER TABLE sc_resources_resource DROP COLUMN responsibleid_temp;

ALTER TABLE sc_resources_resource ADD categoryid_temp NUMBER(19,0);
UPDATE sc_resources_resource SET categoryid_temp = categoryid;
ALTER TABLE sc_resources_resource DROP COLUMN categoryid;
ALTER TABLE sc_resources_resource ADD categoryid NUMBER(19,0);
UPDATE sc_resources_resource SET categoryid = categoryid_temp;
ALTER TABLE sc_resources_resource DROP COLUMN categoryid_temp;
ALTER TABLE sc_resources_resource MODIFY categoryid NUMBER(19,0) NOT NULL;

ALTER TABLE sc_resources_resource ADD CONSTRAINT PK_Resources_Resource PRIMARY KEY(id);


ALTER TABLE sc_resources_category DROP CONSTRAINT PK_Resources_Category;

ALTER TABLE sc_resources_category ADD id_temp NUMBER(19,0);
UPDATE sc_resources_category SET id_temp = id;
ALTER TABLE sc_resources_category DROP COLUMN id;
ALTER TABLE sc_resources_category ADD id NUMBER(19,0);
UPDATE sc_resources_category SET id = id_temp;
ALTER TABLE sc_resources_category DROP COLUMN id_temp;
ALTER TABLE sc_resources_category MODIFY id NUMBER(19,0) NOT NULL;

ALTER TABLE sc_resources_category ADD responsibleid_temp INTEGER;
UPDATE sc_resources_category SET responsibleid_temp = responsibleid;
ALTER TABLE sc_resources_category DROP COLUMN responsibleid;
ALTER TABLE sc_resources_category ADD responsibleid INTEGER;
UPDATE sc_resources_category SET responsibleid = responsibleid_temp;
ALTER TABLE sc_resources_category DROP COLUMN responsibleid_temp;

ALTER TABLE sc_resources_category ADD CONSTRAINT PK_Resources_Category PRIMARY KEY(id);


UPDATE uniqueid SET TABLENAME='sc_resources_reservation' WHERE TABLENAME='SC_Resources_Reservation';
UPDATE uniqueid SET TABLENAME='sc_resources_resource' WHERE TABLENAME='SC_Resources_Resource';
UPDATE uniqueid SET TABLENAME='sc_resources_category' WHERE TABLENAME='SC_Resources_Category';