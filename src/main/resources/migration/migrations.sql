-- For changing the column name in USERS and retaining the data
ALTER TABLE USERS
ADD COLUMN inappropriate_warning_count INTEGER;

UPDATE USERS
SET inappropriate_warning_count = inappropriate_tag_count;

ALTER TABLE USERS
DROP COLUMN inappropriate_tag_count;