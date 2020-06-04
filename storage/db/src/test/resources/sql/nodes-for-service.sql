UPDATE unique_ids SET current_id = 10000 WHERE index_type = 'general';

INSERT INTO binaries VALUES
('dcab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', 3, 'bbbb23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60bbbb'),
('ecab88fc2a043c2479a6de676a2f8179e9ea2167', '002a360ecad98a34b59863c1e65bcf71', 42, 'dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60dddd'),
('dddd88fc2a043c2479a6de676a2f7179e9eaddac', '502a360ecad98a34b59863c1e6accf71', 89, 'yyyy23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy'),
('cccc88fc2a043c2479a6de676a2f8179e9eacccc', '777a360ecad98a34b59863c1e6accf71', 789, 'dcabf055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280');

INSERT INTO nodes VALUES
(1, 0, 'repo1', '.', '.', 0, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-1'),
(2, 0, 'repo1', '.', 'ant', 1, 1340283204448, 'yossis-1', 1340283205448,'yossis-2', 1340283205448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-2'),
(3, 0, 'repo1', 'ant', 'ant', 2, 1340283204450, 'yossis-1', 1340283204450,'yossis-3', 1340283214450, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-3'),
(4, 0, 'repo1', 'ant/ant', '1.5', 3, 1340283204448, 'yossis-9614', 1340283204448,'yossis-5612', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-4'),
(5, 1, 'repo1', 'ant/ant/1.5', 'ant-1.5.jar', 4, 1340283204448, 'yossis-2201', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', '902a360ecad98a34b59863c1e65bcf71', 'bbbb23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60bbbb', 'repo-path-name-key-5'),
(6, 0, 'repo1', '.', 'org', 1, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-6'),
(7, 0, 'repo1', 'org', 'yossis', 2, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-7'),
(500, 0, 'repo2', '.', '.', 0, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-8'),
(501, 0, 'repo2', '.', 'org', 1, 1340283204448, 'yossis-1', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-9'),
(502, 0, 'repo2', 'org', 'jfrog', 2, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-10'),
(503, 0, 'repo2', 'org/jfrog', 'test', 3, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL,'repo-path-name-key-11'),
(504, 1, 'repo2', 'org/jfrog/test', 'test.jar', 4, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 716139, 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', '902a360ecad98a34b59863c1e65bcf71', 'bbbb23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60bbbb', 'repo-path-name-key-12'),
(505, 1, 'repo2', 'org/jfrog/test', 'test2.pom', 4, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 321, 'ecab88fc2a043c2479a6de676a2f8179e9ea2167', 'ecab88fc2a043c2479a6de676a2f8179e9ea2167', '002a360ecad98a34b59863c1e65bcf71', '002a360ecad98a34b59863c1e65bcf71', 'dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60dddd', 'repo-path-name-key-13'),
(600, 1, 'repo-copy', 'org/shayy/trustme', 'trustme.jar', 4, 1340283204447, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'dddd88fc2a043c2479a6de676a2f7179e9eaddac', 'NO_ORIG', '502a360ecad98a34b59863c1e6accf71', 'NO_ORIG', 'yyyy23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy', 'repo-path-name-key-14'),
(601, 1, 'repo-copy', 'org/shayy/badsha1', 'badsha1.jar', 4, 1340283204447, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'dddd88fc2a043c2479a6de676a2f7179e9eaddac', 'dddd88fc2a043c2479a6de676a2f7179e9eadd34', '502a360ecad98a34b59863c1e65bcf32', '502a360ecad98a34b59863c1e65bcf32', 'c90a5b57531c28529736a9451ea5b331a9a8a42afb709ff2a1170bf3a4af4526', 'repo-path-name-key-15'),
(602, 1, 'repo-copy', 'org/shayy/badmd5', 'badmd5.jar', 4, 1340283204447, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'dddd88fc2a043c2479a6de676a2f7179e9eaddac', 'NO_ORIG', '502a360ecad98a34b59863c1e6accf71', '502a360ecad98a34b59863c1e65bcf32', 'c90a5b57531c28529736a9451ea5b331a9a8a42afb709ff2a1170bf3a4af4526', 'repo-path-name-key-16'),
(603, 1, 'repo-copy', 'org/danf/badsha2', 'badsha2.jar', 4, 1340283204447, 'yossis-1', 1340283204448, 'gidis-1', 1340283204448, 43434, 'dddd88fc2a043c2479a6de676a2f7179e9eaddac', 'NO_ORIG', '502a360ecad98a34b59863c1e65bcf32', '502a360ecad98a34b59863c1e65bcf32', NULL, 'repo-path-name-key-17');

INSERT INTO node_props VALUES
(100, 5, 'build.name', 'ant'),
(101, 5, 'build.number', '67'),
(102, 7, 'yossis', 'value1'),
(103, 7, 'yossis', 'value2');

INSERT INTO node_meta_infos VALUES
(5, 1340286203444, 'yossis'),
(7, 1340286203121, 'yoyo');

INSERT INTO watches VALUES
(1, 4, 'scott', 1340286203555),
(2, 4, 'amy', 1340286203666),
(3, 5, 'scott', 1340286203555),
(4, 7, 'yossis', 1340286203432),
(5, 7, 'ariel', 1340286203433),
(6, 502, 'yossis', 1340286203433),
(7, 503, 'yossis', 1340286203433),
(8, 504, 'yossis', 1340286203433),
(9, 4, 'yoyo', 1340286203932),
(10, 4, 'yoyo', 1340282203433),
(11, 4, 'yoyo', 1340285203433);

INSERT INTO stats VALUES
(5, 2, 1340283207850, 'ariels');

INSERT INTO stats_remote VALUES
(5, 'myhost.com', 3, 1340283207853, 'michaelp', 'no-path');

INSERT INTO indexed_archives VALUES
('dcab88fc2a043c2479a6de676a2f8179e9ea2167', 6001),
('cccc88fc2a043c2479a6de676a2f8179e9eacccc', 6002);

INSERT INTO archive_paths VALUES
(8001, 'META-INF'),
(8002, 'org/apache/tools/ant/filters'),
(8003, 'org/apache/tools/mail'),
(8004, '.'),
(8005, 'another');

INSERT INTO archive_names VALUES
(9001, 'LICENSE.txt'),
(9002, 'MANIFEST.MF'),
(9003, 'BaseFilterReader.class'),
(9004, 'BaseParamFilterReader.class'),
(9005, 'MailMessage.class'),
(9006, 'Test'),
(9007, 'test.me');

INSERT INTO indexed_archives_entries VALUES
(6001, 8001, 9001),
(6001, 8001, 9002),
(6001, 8002, 9003),
(6002, 8001, 9002),
(6002, 8004, 9006),
(6002, 8005, 9007);

INSERT INTO tasks VALUES
('INDEX', 'repo1:ant/ant/1.5/ant-1.5.jar', 0),
('INDEX', 'reponone:ant/ant/1.5/ant-1.5.jar', 0),
('XRAY_EVENT', 'xray-event-2', 1340285701421),
('XRAY_EVENT', 'xray-event-1', 1340285601421),
('DEB', 'deb-local::trusty/main/i386::ADD::dddd88fc2a043c2479a6de676a2f7179e9eaddac', 1340285601421),
('DEB', 'deb-local::trusty/main/i386::ADD::aaaa88fc2a043c2479a6de676a2f7179e9dddd', 1340285601422),
('DEB', 'deb-local::trusty/main/i386::ADD::aaaa88fc2a043c2479a6de676a2f7179e9dddd', 1340285601427),
('DEB', 'deb-local::trusty/main/i386::DELETE::dddd88fc2a043c2479a6de676a2f7179e9eaddac', 1340285601426),
('DEB', 'deb-local::precise/main/i386::ADD::aaaa88fc2a043c2479a6de676a2f7179e9dddd', 1340285601423),
('DEB', 'deb-local::precise/main/i386::DELETE::aaaa88fc2a043c2479a6de676a2f7179e9dddd', 1340285601425),
('DEB', 'deb-other::trusty/main/i386::ADD::aaaa88fc2a043c2479a6de676a2f7179e9dddd', 1340285601424);

INSERT INTO jobs VALUES
(1,'REPLICATION', 'RUNNING', 1418380780000, NULL, 'my-json-blob'),
(2,'REPLICATION', 'FINISHED', 1418380600000, 1418380720000, 'my-json-blob'),
(3,'REPLICATION', 'STOPPED', 1418380610000, 1418380780000, NULL),
(4,'OTHER', 'RUNNING', 1418380780000, 1418381140000, 'my-json-blob'),
(5,'REPLICATION', 'FINISHED', 1355308500000, 1355308560000, 'old'),
(6,'REPLICATION', 'FINISHED', 1355308620000, 1355308680000, 'new'),
(7,'TYPE_TO_DELETE', 'FINISHED', 8355308660000, 8355308680000, 'deleteMe'),
(8,'TYPE_TO_DELETE', 'FINISHED', 8355308669000, 8355308680000, 'deleteMe2'),
(9,'TYPE_TO_DELETE', 'FINISHED', 8355308660000, 8355308680000, 'deleteME3');

INSERT INTO node_events VALUES
(1, 1515653241184, 1, 'repo1/'),
(2, 1515653241185, 1, 'repo1/ant/'),
(3, 1515653241185, 1, 'repo1/ant/ant/'),
(4, 1515653241185, 1, 'repo1/ant/ant/'),
(6, 1515653241186, 1, 'repo1/ant/ant/1.5/ant-1.5.jar'),
(5, 1515653241186, 1, 'repo1/ant/ant/1.5/'),
(7, 1515653241196, 1, 'repo1/ant/ant/1.5/ant-1.5.pom'),
(8, 1515653242123, 2, 'repo1/ant/'),
(9, 1515653242223, 2, 'repo1/ant/ant/1.5/ant-1.5.jar'),
(10, 1515653241289, 3, 'repo1/ant/ant/1.5/ant-1.5.pom'),
(11, 1515653241293, 4, 'repo1/ant/ant/1.5/ant-1.5.jar'),

(20, 1515653250000, 1, 'repo2/'),
(21, 1515653250001, 1, 'repo2/org/'),
(23, 1515653250002, 1, 'repo2/org/yossis/'),
(24, 1515653250003, 1, 'repo2/org/yossis/tools/'),
(25, 1515653250004, 1, 'repo2/org/yossis/tools/test.bin'),
(26, 1515653250004, 4, 'repo2/org/yossis/tools/test.bin'),

(30, 1515653250000, 3, 'repo-no-create/');
