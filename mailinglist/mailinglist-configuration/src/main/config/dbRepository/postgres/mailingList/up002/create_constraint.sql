alter table sc_mailinglist_internal_sub
        add constraint pk_mailinglist_internal_sub
        primary key (id);

alter table sc_mailinglist_internal_sub
        add constraint fk_subscriber_mailinglist_id
         foreign key (mailinglistid)
        references sc_mailinglist_list (id);