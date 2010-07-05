ALTER TABLE SC_FormsOnline_Forms ADD 
	 CONSTRAINT PK_SC_FormsOnline_Forms PRIMARY KEY
	(
		id
	)
;

ALTER TABLE SC_FormsOnline_FormInstances ADD 
	 CONSTRAINT PK_SC_FormsOL_FormInstances PRIMARY KEY
	(
		id
	)
;

ALTER TABLE SC_FormsOnline_FormInstances ADD CONSTRAINT FK_FormInstance FOREIGN KEY (formId) REFERENCES SC_FormsOnline_Forms(id);
ALTER TABLE SC_FormsOnline_UserRights ADD CONSTRAINT FK_UserRights FOREIGN KEY (formId) REFERENCES SC_FormsOnline_Forms(id);
ALTER TABLE SC_FormsOnline_GroupRights ADD CONSTRAINT FK_GroupRights FOREIGN KEY (formId) REFERENCES SC_FormsOnline_Forms(id);
