# start with value bigger than int (RTFACT-9568)
UPDATE unique_ids SET current_id = 7777777777 WHERE index_type = 'general';

INSERT INTO users VALUES
(1, 'oferc','masterpass',NULL,'e@mail.com',NULL,0,1,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(2, 'yossis', 'weakpass',NULL,NULL, NULL,1,1,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(3, 'shayy', 'weakpass',NULL,NULL, NULL,1,1,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(4, 'danf', 'weakpass',NULL,NULL, NULL,1,1,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
