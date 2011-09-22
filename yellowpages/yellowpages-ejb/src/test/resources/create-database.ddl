
-- UniqueId

CREATE TABLE UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

ALTER TABLE UniqueId ADD
CONSTRAINT PK_UniqueId PRIMARY KEY
(
    tableName
);

-- Contacts

CREATE TABLE SB_Contact_Contact
(
	contactId		int		NOT NULL ,
	contactFirstName	varchar (1000)	NULL ,
	contactLastName		varchar (1000)	NULL ,
	contactEmail		varchar (1000)	NULL ,
	contactPhone		varchar (20)	NULL ,
	contactFax		varchar (20)	NULL ,
	userId			varchar (100)	NULL ,
	contactCreationDate	varchar (10)	NOT NULL ,
	contactCreatorId	varchar (100)	NOT NULL ,
	instanceId		varchar (50)	NOT NULL
);

-- Company

CREATE TABLE SC_Contact_Company
(
	companyId			int			    NOT NULL,
	companyName			varchar (1000)	NULL,
	companyEmail		varchar (1000)	NULL,
	companyPhone		varchar (20)	NULL,
	companyFax			varchar (20)	NULL,
	companyCreationDate	varchar (10)	NOT NULL,
	companyCreatorId	varchar (100)	NOT NULL,
	instanceId			varchar (50)	NOT NULL
)
;

ALTER TABLE SC_Contact_Company
ADD CONSTRAINT PK_Contact_Company PRIMARY KEY
(
    companyId
)
;