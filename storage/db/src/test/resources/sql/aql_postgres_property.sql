UPDATE unique_ids SET current_id = 10000 WHERE index_type = 'general';

INSERT INTO binaries VALUES
('dcab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', 3, 'bbbb5e734090fee6588f6c581567491790c67a9c7679457977ca72b872febbbb'),
('acab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', 78, 'acab5e734090fee6588f6c581567491790c67a9c7679457977ca72b872feacab'),
('bbbb88fc2a043c2479a6de676a2f8179e9eabbbb', '402a360ecad98a34b59863c1e65bcf71', 33, 'xxxx5e734090fee6588f6c581567491790c67a9c7679457977ca72b872fexxxx'),
('dddd88fc2a043c2479a6de676a2f8179e9eadddd', '502a360ecad98a34b59863c1e65bcf71', 333, 'yyyy5e734090fee6588f6c581567491790c67a9c7679457977ca72b872feyyyy'),
('dddd88fc2a043c2479a6de676a2f7179e9eaddac', '502a360ecad98a34b59863c1e6accf71', 500, 'tihi5e734090fee6588f6c581567491790c67a9c7679457977ca72b872fetihi'),
('dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', 666, 'zzzz5e734090fee6588f6c581567491790c67a9c7679457977ca72b872fezzzz');

INSERT INTO nodes VALUES
(1, 0, 'repo1', '.', '.', 0, 1340283204448, 'gandalf-1', 1340283204448, 'gandalf-1', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL,'repo-path-name-key-1'),
(2, 0, 'repo1', '.', 'ant', 1, 1340283204448, 'gandalf-1', 1340283205448,'gandalf-2', 1340283205448, 0, NULL, NULL, NULL, NULL, NULL,'repo-path-name-key-2'),
(3, 0, 'repo1', 'ant', 'ant', 2, 1340283204450, 'gandalf-1', 1340283204450,'gandalf-3', 1340283214450, 0, NULL, NULL, NULL, NULL, NULL,'repo-path-name-key-3'),
(4, 0, 'repo1', 'ant/ant', '1.5', 3, 1340283204448, 'gandalf-9614', 1340283204448,'gandalf-5612', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL,'repo-path-name-key-4'),
(5, 1, 'repo1', 'ant/ant/1.5', 'ant-1.5.jar', 4, 1340283204448, 'gandalf-2201', 1340283204448,'gandalf-3274', 1340283204448, 716139, 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', '902a360ecad98a34b59863c1e65bcf71', 'yyyy5e734090fee6588f6c581567491790c67a9c7679457977ca72b872feyyyy', 'repo-path-name-key-5'),
(6, 0, 'repo1', '.', 'ant-launcher', 1, 1340223204457, 'gandalf-2', 1340283204448,'gandalf-2', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL,'repo-path-name-key-6'),
(7, 0, 'repo1', 'ant-launcher', 'ant-launcher', 2, 1340223204457, 'gandalf-2', 1340283204448,'gandalf-2', 1340283204448, 0, NULL, NULL, NULL, NULL, NULL,'repo-path-name-key-7'),
(8, 1, 'repo1', 'ant/ant/1.6', 'ant-1.6.jar', 4, 1340283204448, 'gandalf-2202', 1340283204448,'gandalf-3276', 1340283204448, 716139, 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', '902a360ecad98a34b59863c1e65bcf71', 'acab5e734090fee6588f6c581567491790c67a9c7679457977ca72b872feacab', 'repo-path-name-key-8'),
(9, 1, 'repo1', 'ant/ant/1.7', 'ant-1.7.jar', 4, 1340283204448, 'gandalf-2203', 1340283204448,'gandalf-3277', 1340283204448, 716139, 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', '902a360ecad98a34b59863c1e65bcf71', 'zzzz5e734090fee6588f6c581567491790c67a9c7679457977ca72b872fezzzz', 'repo-path-name-key-9');

INSERT INTO node_props VALUES
(1, 2, 'build.name', 'ant'),
(2, 2, 'build.number', '67'),
(3, 1, 'build.name', 'ant'),
(4, 2, 'string', 'this is string'),
(5, 5, 'longvalue', 'qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s84t4n9ms1m8kmtguk1q6yupes5h7k6ij749qge7xxxllek8mlh4e2xrd08rkld6jzamcvy29teivchghbp1sdf10urotfokt7hj3zaku5ypmusvz2tbzq8wslwzgbvh89p96gt0fn2v8s1vh0aos5hp7uvzeyl9bwbg6vaq61fa63yn66methofmv4n7zyra0gv1b9u4o0866ch9w52gr'),
(6, 8, 'longvalue', 'qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s84t4n9ms1m8kmtguk1q6yupes5h7k6ij749qge7xxxllek8mlh4e2xrd08rkld6jzamcvy29teivchghbp1sdf10urotfokt7hj3zaku5ypmusvz2tbzq8wslwzgbvh89p96gt0fn2v8s1vh0aos5hp7uvzeyl9bwbg6vaq61fa63yn66methofmv4n7zyra0gv1b9u4o0866ch9w52gr1'),
(7, 9, 'longvalue', 'qvgxnu4zvlkrmrjw2q9cne4kbbfs2aaezn2nlvgf6s');