CREATE DATABASE IF NOT EXISTS maru_egg_load_test
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

GRANT ALL PRIVILEGES ON maru_egg_load_test.* TO 'loadtest'@'%';
FLUSH PRIVILEGES;
