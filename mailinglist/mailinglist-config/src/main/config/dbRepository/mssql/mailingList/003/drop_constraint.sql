alter table sc_mailinglist_message
        drop constraint mailinglist_message_mailid_key;
alter table sc_mailinglist_external_user
        drop constraint FK9290F7C94B1A1B47;
alter table sc_mailinglist_attachment
        drop constraint FKCE814959DB1C14EE;
alter table sc_mailinglist_internal_sub
        drop constraint fk_subscriber_mailinglist_id;


alter table sc_mailinglist_message
        drop constraint pk_mailinglist_message;
alter table sc_mailinglist_list
        drop constraint pk_mailinglist_list;
alter table sc_mailinglist_external_user
        drop constraint pk_mailinglist_external_user ;
alter table sc_mailinglist_attachment
        drop constraint pk_mailinglist_attachment;
alter table sc_mailinglist_internal_sub
        drop constraint pk_mailinglist_internal_sub;
