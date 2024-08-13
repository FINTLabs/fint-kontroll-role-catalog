alter table roles add role_status varchar(255);
alter table roles add role_status_changed timestamp;
alter table role_memberships add membership_status varchar(255);
alter table role_memberships add membership_status_changed timestamp;