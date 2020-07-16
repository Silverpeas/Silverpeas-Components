/*
 Modifying SC_FormsOnline_Forms:
 - date to timestamp
 - adding hierarchicalValidation
 - adding formInstExchangeReceiver
 - adding deleteAfterFormInstExchange
 */
ALTER TABLE sc_formsonline_forms
    MODIFY creationdate TIMESTAMP;

ALTER TABLE sc_formsonline_forms
    ADD hierarchicalValidation NUMBER(1, 0) DEFAULT 0 NOT NULL;

ALTER TABLE sc_formsonline_forms
    ADD formInstExchangeReceiver VARCHAR(200) DEFAULT NULL;

ALTER TABLE sc_formsonline_forms
    ADD deleteAfterFormInstExchange NUMBER(1, 0) DEFAULT 0 NOT NULL;

/*
 Creating new validation table and filling it with data from sc_formsonline_forminstances.
 */

CREATE TABLE SC_FormsOnline_FormInstVali
(
    id                INT                     NOT NULL,
    formInstId        INT                     NOT NULL,
    validationBy      VARCHAR(40)             NOT NULL,
    validationType    VARCHAR(20)             NOT NULL,
    status            VARCHAR(20)             NOT NULL,
    validationDate    TIMESTAMP               NOT NULL,
    validationComment VARCHAR(4000) DEFAULT NULL,
    follower          NUMBER(1, 0)  DEFAULT 0 NOT NULL,
    CONSTRAINT PK_SC_FormsOL_FormInstVali PRIMARY KEY (id),
    CONSTRAINT FK_SC_FormsOL_FormInstances_id FOREIGN KEY (formInstId) REFERENCES sc_formsonline_forminstances (id)
);

INSERT INTO SC_FormsOnline_FormInstVali (id, formInstId, validationBy, validationType, status,
                                         validationDate, validationComment)
SELECT row_number() over (ORDER BY f.id)   AS id,
       f.id                                AS formInstId,
       f.validatorid                       AS validationBy,
       'FINAL'                             AS validationType,
       CASE
           WHEN f.state = 3 THEN 'VALIDATED'
           WHEN f.state = 4 THEN 'REFUSED'
           WHEN f.state = 5 THEN 'UNKNOWN'
           ELSE 'ERROR'
           END                             AS status,
       CAST(f.validationdate AS TIMESTAMP) AS validationDate,
       f.comments                          AS validationComment
FROM sc_formsonline_forminstances f
WHERE f.validatorid IS NOT NULL;

/*
 Modifying sc_formsonline_forminstances:
 - date to timestamp
 - deleting useless columns
 */

ALTER TABLE sc_formsonline_forminstances
    MODIFY creationdate TIMESTAMP;

ALTER TABLE sc_formsonline_forminstances
    DROP COLUMN validatorid;

ALTER TABLE sc_formsonline_forminstances
    DROP COLUMN validationdate;

ALTER TABLE sc_formsonline_forminstances
    DROP COLUMN comments;
