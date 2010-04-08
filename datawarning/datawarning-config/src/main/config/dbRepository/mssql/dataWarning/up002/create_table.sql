ALTER TABLE SC_DataWarning_Query
add persoUID varchar(50) NULL;
ALTER TABLE SC_DataWarning_Query
add persoColNB int NOT NULL DEFAULT (1);
ALTER TABLE SC_DataWarning_Query
add persoValid int NOT NULL DEFAULT (0);
