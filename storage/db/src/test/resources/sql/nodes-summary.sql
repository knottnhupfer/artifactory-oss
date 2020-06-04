UPDATE unique_ids SET current_id = 10000 WHERE index_type = 'general';

INSERT INTO binaries VALUES
('666b88fc2a043c2479a6de676a2f8179e9ea2777', '902a360ecad98a34b59863c1e65bcf71', 3, 'bbbb23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60bbbb'),
('acab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', 78, 'dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60dddd'),
('bbbb88fc2a043c2479a6de676a2f8179e9eabbbb', '402a360ecad98a34b59863c1e65bcf71', 33, 'yyyy23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy'),
('dddd88fc2a043c2479a6de676a2f8179e9eadddd', '502a360ecad98a34b59863c1e65bcf71', 333, 'dcabf055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280'),
('dddd88fc2a043c2479a6de676a2f7179e9eaddac', '502a360ecad98a34b59863c1e6accf71', 500, 'zzzz23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60zzzz'),
('dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', 666, 'acab23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy');

INSERT INTO nodes VALUES
(1, 0, 'repo1', '.', '.', 0, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-1'),
(2, 0, 'repo1', '.', 'ant', 1, 1340283204448, 'yossis', 1340283205448,'yossis', 1340283205448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-2'),
(3, 0, 'repo1', 'ant', 'ant', 2, 1340283204450, 'yossis', 1340283204450,'yossis-3', 1340283214450, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-3'),
(4, 0, 'repo1', 'ant/ant', '1.5', 3, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-4'),
(5, 1, 'repo1', 'ant/ant/1.5', 'ant-1.5.jar', 4, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 4, '666b88fc2a043c2479a6de676a2f8179e9ea2777', '666b88fc2a043c2479a6de676a2f8179e9ea2777', '902a360ecad98a34b59863c1e65bcf71', '902a360ecad98a34b59863c1e65bcf71', 'bbbb23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60bbbb', 'repo-path-name-key-5'),
(6, 0, 'repo1', '.', 'org', 1, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-6'),
(7, 0, 'repo1', 'org', 'yossis', 2, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-7'),
(8, 0, 'repo1', 'org/yossis', 'tools', 3, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-8'),
(9, 1, 'repo1', 'org/yossis/tools', 'test.bin', 4, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 1, 'acab88fc2a043c2479a6de676a2f8179e9ea2167', 'acab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', '302a360ecad98a34b59863c1e65bcf71', 'dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60dddd', 'repo-path-name-key-9'),
(10, 1, 'repo1', 'org/yossis/tools', 'file2.bin', 4, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 2, 'bbbb88fc2a043c2479a6de676a2f8179e9eabbbb', 'bcab88fc2a043c2479a6de676a2f8179e9ea2167', '402a360ecad98a34b59863c1e65bcf71', '402a360ecad98a34b59863c1e65bcf71', 'yyyy23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy', 'repo-path-name-key-10'),
(11, 1, 'repo1', 'org/yossis/tools', 'file3.bin', 4, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 3, 'dddd88fc2a043c2479a6de676a2f8179e9eadddd', 'ccab88fc2a043c2479a6de676a2f8179e9ea2167', '502a360ecad98a34b59863c1e65bcf71', '502a360ecad98a34b59863c1e65bcf71', 'dcabf055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280', 'repo-path-name-key-11'),
(12, 0, 'repo2', '.', '.', 0, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-12'),
(13, 0, 'repo2', '.', 'a', 1, 1340283204448, 'yossis', 1340283205448,'yossis', 1340283205448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-13'),
(14, 0, 'repo2', 'a', 'b', 2, 1340283204450, 'yossis', 1340283204450,'yossis-3', 1340283214450, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-14'),
(15, 1, 'repo2', 'a', 'ant-1.5.jar', 4, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 10, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71', 'acab23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy','repo-path-name-key-15'),
(16, 1, 'repo2', 'a/b', 'ant-1.5.jar', 4, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 20, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71', 'acab23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy','repo-path-name-key-16'),
(17, 0, 'repo2', '.', 'aa', 1, 1340283204448, 'yossis', 1340283205448,'yossis', 1340283205448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-17'),
(18, 0, 'repo2', 'aa', 'b', 2, 1340283204450, 'yossis', 1340283204450,'yossis', 1340283214450, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-18'),
(19, 1, 'repo2', 'aa', 'ant-1.5.jar', 4, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 30, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71', 'acab23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy', 'repo-path-name-key-19'),
(20, 1, 'repo2', 'aa/b', 'ant-1.5.jar', 4, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 40, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71', 'acab23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy', 'repo-path-name-key-20'),
(21, 0, 'repo3', '.', '.', 0, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL, 'repo-path-name-key-21');