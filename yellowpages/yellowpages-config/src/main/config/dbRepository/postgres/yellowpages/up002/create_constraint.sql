ALTER TABLE SC_Contact_Company_Rel WITH NOCHECK ADD
	 CONSTRAINT PK_Contact_Company_Rel PRIMARY KEY CLUSTERED 
	(
		contactId,
		companyId
	)   
;

ALTER TABLE SC_Contact_Company_Rel WITH NOCHECK ADD
	CONSTRAINT FK_Contact_Company_Rel_FKContact FOREIGN KEY (contactId)
    REFERENCES SB_Contact_Contact (contactId) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
;

ALTER TABLE SC_Contact_Company_Rel WITH NOCHECK ADD
	CONSTRAINT FK_Contact_Company_Rel_FKCompany FOREIGN KEY (companyId)
    REFERENCES SC_Contact_Company (companyId) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
;

ALTER TABLE SC_Contact_Company WITH NOCHECK ADD
	 CONSTRAINT PK_Contact_Company PRIMARY KEY CLUSTERED 
	(
		companyId
	)   
;