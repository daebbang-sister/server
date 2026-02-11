CREATE TABLE IF NOT EXISTS users (
    id bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    provider varchar(20) NOT NULL,
    status tinyint(1) NOT NULL,
    login_id varchar(16) NOT NULL,
    login_pwd varchar(255) NULL,
    name varchar(50) NOT NULL,
    phone_number varchar(2) NOT NULL,
    created_at datetime NOT NULL,
    last_login_at datetime NOT NULL
)