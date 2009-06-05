alter table sc_mailinglist_internal_subscriber
        drop constraint fk_subscriber_mailinglist_id;

alter table sc_mailinglist_internal_subscriber
        drop constraint pk_mailinglist_internal_subscriber;