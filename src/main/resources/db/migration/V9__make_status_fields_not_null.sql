update roles
set role_status = 'UNKNOWN'
where role_status is null;

update role_memberships
set membership_status = 'UNKNOWN'
where membership_status is null;

alter table roles alter column role_status set not null;
alter table role_memberships alter column membership_status set not null;
