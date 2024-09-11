alter table role_memberships add is_active boolean;

ALTER TABLE role_memberships ALTER COLUMN is_active SET DEFAULT true;
