-- MyBank Pro SQL schema
CREATE DATABASE IF NOT EXISTS mybank_system;
USE mybank_system;

CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(100),
  email VARCHAR(100),
  phone VARCHAR(20),
  address VARCHAR(255),
  photo VARCHAR(255),
  role ENUM('user','admin') DEFAULT 'user',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
  account_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  account_number VARCHAR(32) UNIQUE NOT NULL,
  account_type VARCHAR(20),
  balance DECIMAL(15,2) DEFAULT 0.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions (
  transaction_id INT AUTO_INCREMENT PRIMARY KEY,
  account_id INT NOT NULL,
  type ENUM('deposit','withdraw','transfer') NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  note VARCHAR(255),
  date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  target_account VARCHAR(32),
  FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
  notification_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  message VARCHAR(255),
  date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- create default admin (password 'admin' hashed client-side on registration)
INSERT IGNORE INTO users (username, password, full_name, email, role)
VALUES ('admin','admin','Admin User','admin@local','admin');
