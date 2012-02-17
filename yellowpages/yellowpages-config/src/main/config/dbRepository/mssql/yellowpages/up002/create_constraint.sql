ALTER TABLE SC_Contact_Company_GroupFather
ADD CONSTRAINT PK_SC_Contact_Company_GroupFather PRIMARY KEY
	(
		groupId, fatherId, instanceId
	)   
;

ALTER TABLE SC_Contact_Company
ADD CONSTRAINT PK_Contact_Company PRIMARY KEY 
	(
		companyId
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
    REFERENCES SB_Contact_Contact (contactId) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
;

ALTER TABLE SC_Contact_GenericContact
ADD CONSTRAINT FK_Contact_GenericContact_FKCompany FOREIGN KEY (companyId)
    REFERENCES SC_Contact_Company (companyId) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
;

ALTER TABLE SC_Contact_GenericContact_Rel
ADD CONSTRAINT PK_Contact_GenericContact_Rel PRIMARY KEY
	(
		relationId
	)   
;

ALTER TABLE SC_Contact_GenericContact_Rel
ADD CONSTRAINT FK_Contact_GenericContact_Rel_FKContact FOREIGN KEY (genericContactId)
    REFERENCES SC_Contact_GenericContact (genericContactId) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
;

ALTER TABLE SC_Contact_GenericContact_Rel
ADD	CONSTRAINT FK_Contact_GenericContact_Rel_FKCompany FOREIGN KEY (genericCompanyId)
    REFERENCES SC_Contact_GenericContact (genericContactId) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
;

ALTER TABLE SC_Contact_GenericContact_Topic
ADD CONSTRAINT PK_SC_Contact_GenericContact_Topic PRIMARY KEY
	(
		relationId
	)
;

ALTER TABLE SC_Contact_GenericContact_Topic
ADD	CONSTRAINT FK_Contact_GenericContact_Topic_FKGContact FOREIGN KEY (genericContactId)
    REFERENCES SC_Contact_GenericContact (genericContactId) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
;
