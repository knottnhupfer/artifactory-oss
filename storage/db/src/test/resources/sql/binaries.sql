INSERT INTO binaries VALUES
('b60d121b438a380c343d5ec3c2037564b82ffef3', '302a360ecad98a34b59863c1e65bcf71', 3, 'bbbb23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60bbbb'),
('f0d381ab0e057d4f835d639f6330a7c3e81eb6af', '902a360ecad98a34b59863c1e65bcf71', 2725, 'dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60dddd'),
('356a192b7913b04c54574d18c28d46e6395428ab', '502a360ecad98a34b59863c1e65bcf71', 1, 'dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60ddac'),
('74239116da1def240fe1d366eb535513efc1c40b', '402a360ecad98a34b59863c1e65bcf71', 33670080, 'yyyy23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy'),
('da39a3ee5e6b4b0d3255bfef95601890afd80709', '602a360ecad98a34b59863c1e65bcf71', 0, 'dcabf055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280');

INSERT INTO nodes VALUES
(1, 0, 'repo1', '.', '.', 0, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-1'),
(8, 0, 'repo1', '.', 'org', 1, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-2'),
(9, 0, 'repo1', 'org', 'yossis', 2, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-3'),
(10, 0, 'repo1', 'org/yossis', 'tools', 3, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-4'),
(11, 1, 'repo1', 'org/yossis/tools', 'test.bin', 4, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'b60d121b438a380c343d5ec3c2037564b82ffef3', 'acab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', '302a360ecad98a34b59863c1e65bcf71', 'dcabf055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280', 'repo-path-name-key-5'),
(12, 1, 'repo1', 'org/yossis/tools', 'file2.bin', 4, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'b60d121b438a380c343d5ec3c2037564b82ffef3', 'bcab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', '402a360ecad98a34b59863c1e65bcf71', 'dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60ddac', 'repo-path-name-key-6'),
(13, 1, 'repo1', 'org/yossis/tools', 'file3.bin', 4, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'f0d381ab0e057d4f835d639f6330a7c3e81eb6af', 'ccab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', '502a360ecad98a34b59863c1e65bcf71', 'yyyy23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy', 'repo-path-name-key-7'),
(14, 0, 'repo1', 'org/yossis', 'empty', 3, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-8');

