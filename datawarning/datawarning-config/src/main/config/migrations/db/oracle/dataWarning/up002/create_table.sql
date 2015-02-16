ALTER TABLE SC_DataWarning_Query
add persoUID varchar(50) NULL;
ALTER TABLE SC_DataWarning_Query
add persoColNB int DEFAULT (1) NOT NULL;
ALTER TABLE SC_DataWarning_Query
add persoValid int DEFAULT (0) NOT NULL;
