ALTER TABLE SC_Contact_GroupFather
DROP CONSTRAINT PK_SC_Contact_GroupFather
;

ALTER TABLE SC_Contact_Company_GroupFather
DROP CONSTRAINT PK_SC_Contact_Company_GroupFather
;

ALTER TABLE SC_Contact_Company_Rel
DROP CONSTRAINT PK_Contact_Company_Rel
;

ALTER TABLE SC_Contact_Company_Rel 
DROP CONSTRAINT FK_Contact_Company_Rel_FKContact
;

ALTER TABLE SC_Contact_Company_Rel
DROP CONSTRAINT FK_Contact_Company_Rel_FKCompany
;

ALTER TABLE SC_Contact_Company
DROP CONSTRAINT PK_Contact_Company
;

ALTER TABLE SC_Contact_GenericContact
DROP CONSTRAINT PK_Contact_GenericContact
;

ALTER TABLE SC_Contact_GenericContact
DROP CONSTRAINT FK_Contact_GenericContact_FKContact
;

ALTER TABLE SC_Contact_GenericContact
DROP CONSTRAINT FK_Contact_GenericContact_FKCompany
;