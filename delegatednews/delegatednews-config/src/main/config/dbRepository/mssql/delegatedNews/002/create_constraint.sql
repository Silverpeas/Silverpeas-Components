
alter table sc_delegatednews_new with nocheck 
        add constraint pk_delegatednews_new
        primary key clustered (pubId);

alter table sc_delegatednews_new
        add constraint fk_delegatednews_new_pubid
        foreign key (pubId)
        references SB_Publication_Publi (pubId);
