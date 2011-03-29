
alter table sc_delegatednews_new 
        add constraint pk_delegatednews_new
        primary key (pubId);

alter table sc_delegatednews_new
        add constraint fk_delegatednews_new_pubid
        foreign key (pubId)
        references SB_Publication_Publi (pubId);
