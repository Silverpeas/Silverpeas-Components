ALTER TABLE sc_resources_managers DROP CONSTRAINT pk_resources_managers;
ALTER TABLE sc_resources_managers ALTER COLUMN managerId BIGINT NOT NULL;
ALTER TABLE sc_resources_managers ALTER COLUMN resourceId BIGINT NOT NULL;
ALTER TABLE sc_resources_managers WITH NOCHECK ADD CONSTRAINT PK_Resources_Managers PRIMARY KEY CLUSTERED (resourceId, managerId);

ALTER TABLE sc_resources_reservedresource DROP CONSTRAINT pk_resources_reservedresource;
ALTER TABLE sc_resources_reservedresource ALTER COLUMN reservationId BIGINT NOT NULL;
ALTER TABLE sc_resources_reservedresource ALTER COLUMN resourceId BIGINT NOT NULL;
ALTER TABLE sc_resources_reservedresource WITH NOCHECK ADD  CONSTRAINT PK_Resources_ReservedResource PRIMARY KEY CLUSTERED (reservationId, resourceId);

ALTER TABLE sc_resources_reservation DROP CONSTRAINT PK_Resources_Reservation;
ALTER TABLE sc_resources_reservation ALTER COLUMN userId INTEGER;
ALTER TABLE sc_resources_reservation ALTER COLUMN id BIGINT NOT NULL;
ALTER TABLE sc_resources_reservationWITH NOCHECK ADD CONSTRAINT PK_Resources_Reservation PRIMARY KEY CLUSTERED (id);

ALTER TABLE sc_resources_resource DROP CONSTRAINT PK_Resources_Resource;
ALTER TABLE sc_resources_resource ALTER COLUMN id BIGINT NOT NULL;
ALTER TABLE sc_resources_resource ALTER COLUMN responsibleid INTEGER;
ALTER TABLE sc_resources_resource ALTER COLUMN categoryid BIGINT;
ALTER TABLE sc_resources_resource WITH NOCHECK ADD  CONSTRAINT PK_Resources_Resource PRIMARY KEY CLUSTERED (id);

ALTER TABLE sc_resources_category DROP CONSTRAINT PK_Resources_Category;ALTER TABLE sc_resources_category ALTER COLUMN id BIGINT NOT NULL;
ALTER TABLE sc_resources_category ALTER COLUMN responsibleid INTEGER;
ALTER TABLE SC_Resources_Category WITH NOCHECK ADD CONSTRAINT PK_Resources_Category PRIMARY KEY CLUSTERED (id);

UPDATE uniqueid SET TABLENAME='sc_resources_reservation' WHERE TABLENAME='SC_Resources_Reservation';
UPDATE uniqueid SET TABLENAME='sc_resources_resource' WHERE TABLENAME='SC_Resources_Resource';
UPDATE uniqueid SET TABLENAME='sc_resources_category' WHERE TABLENAME='SC_Resources_Category';
