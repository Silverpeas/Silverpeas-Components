

-- ================= UniqueId =================

CREATE TABLE UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

ALTER TABLE UniqueId ADD
CONSTRAINT PK_UniqueId PRIMARY KEY
(
    tableName
);


-- ================= Contacts =================

CREATE TABLE SB_Contact_Contact
(
	contactId		int		NOT NULL ,
	contactFirstName	varchar (1000)	NULL ,
	contactLastName		varchar (1000)	NULL ,
	contactEmail		varchar (1000)	NULL ,
	contactPhone		varchar (20)	NULL ,
	contactFax		    varchar (20)	NULL ,
	userId			    varchar (100)	NULL ,
	contactCreationDate	varchar (10)	NOT NULL ,
	contactCreatorId	varchar (100)	NOT NULL ,
	instanceId		    varchar (50)	NOT NULL
);


-- ======================= Company =======================

CREATE TABLE SC_Contact_Company
(
	companyId			int			NOT NULL,
	companyName			varchar (1000)	NULL,
	companyEmail		varchar (1000)	NULL,
	companyPhone		varchar (20)	NULL,
	companyFax			varchar (20)	NULL,
	instanceId			varchar (50)	NOT NULL
)
;


ALTER TABLE SC_Contact_Company
ADD CONSTRAINT PK_Contact_Company PRIMARY KEY
(
    companyId
)
;


-- ===================== Generic contacts =========================

CREATE TABLE SC_Contact_GenericContact
(
	genericContactId	int		NOT NULL,
	contactType			int		NOT NULL,
	contactId			int		NULL,
	companyId			int	    NULL
)
;

ALTER TABLE SC_Contact_GenericContact
ADD CONSTRAINT PK_Contact_GenericContact PRIMARY KEY
	(
		genericContactId
	)
;

ALTER TABLE SC_Contact_GenericContact
ADD CONSTRAINT FK_Contact_GenericContact_FKContact FOREIGN KEY (contactId)
    REFERENCES SB_Contact_Contact (contactId)
    ON UPDATE NO ACTION ON DELETE NO ACTION
;

ALTER TABLE SC_Contact_GenericContact
ADD CONSTRAINT FK_Contact_GenericContact_FKCompany FOREIGN KEY (companyId)
    REFERENCES SC_Contact_Company (companyId)
    ON UPDATE NO ACTION ON DELETE NO ACTION
;



-- =============== Relations entre Contact et Company ===================

CREATE TABLE sc_contact_genericcontact_rel
(
	genericContactId	int		NOT NULL,
	genericCompanyId    int		NOT NULL,
	relationType	    int		NOT NULL,
	enabled 	    	int 	DEFAULT(1) NOT NULL
)
;

ALTER TABLE sc_contact_genericcontact_rel
ADD CONSTRAINT PK_Contact_GenericContact_Rel PRIMARY KEY
	(
		genericContactId,
		genericCompanyId
	)
;


ALTER TABLE sc_contact_genericcontact_rel
ADD CONSTRAINT FK_Contact_GenericContact_Rel_FKContact FOREIGN KEY (genericContactId)
    REFERENCES SC_Contact_GenericContact (genericContactId)
    ON UPDATE NO ACTION ON DELETE NO ACTION
;

ALTER TABLE sc_contact_genericcontact_rel
ADD	CONSTRAINT FK_Contact_GenericContact_Rel_FKCompany FOREIGN KEY (genericCompanyId)
    REFERENCES SC_Contact_GenericContact (genericContactId)
    ON UPDATE NO ACTION ON DELETE NO ACTION
;
