-- SPDX-FileCopyrightText: Contributors to the GXF project
--
-- SPDX-License-Identifier: Apache-2.0
-- No production data was set before this change so we can drop all existing data
delete from pre_shared_key;

alter table pre_shared_key
    add column secret varchar(255) not null;
