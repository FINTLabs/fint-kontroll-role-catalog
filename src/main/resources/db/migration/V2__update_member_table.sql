alter table members DROP user_id;
alter table members ADD organisation_unit_name VARCHAR(255);
alter table members ADD organisation_unit_id  VARCHAR(255);
alter table members ADD identity_provider_user_object_id uuid;