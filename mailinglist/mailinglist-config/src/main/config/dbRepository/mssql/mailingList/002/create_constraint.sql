
alter table sc_mailinglist_message
        add constraint pk_mailinglist_message
        primary key (id);
alter table sc_mailinglist_list
        add constraint pk_mailinglist_list
        primary key (id);
alter table sc_mailinglist_external_user
        add constraint pk_mailinglist_external_user
        primary key (id);
alter table sc_mailinglist_attachment
        add constraint pk_mailinglist_attachment
        primary key (id);
alter table sc_mailinglist_internal_subscriber
        add constraint pk_mailinglist_internal_subscriber
        primary key (id);



alter table sc_mailinglist_external_user
        add constraint FK9290F7C94B1A1B47
        foreign key (listId)
        references sc_mailinglist_list (id);
alter table sc_mailinglist_attachment
        add constraint FKCE814959DB1C14EE
        foreign key (messageId)
        references sc_mailinglist_message (id);
alter table sc_mailinglist_internal_subscriber
        add constraint fk_subscriber_mailinglist_id
         foreign key (mailinglistid)
        references sc_mailinglist_list (id);
alter table sc_mailinglist_message
        add constraint mailinglist_message_mailid_key
        unique (mailId, componentId);