CREATE TABLE IF NOT EXISTS `accounts` (
    `customer_id` int NOT NULL,
    `account_number` int AUTO_INCREMENT  PRIMARY KEY,
    `account_type` varchar(100) NOT NULL,
    `branch_address` varchar(200) NOT NULL,
    `created_at` date NOT NULL,
    `created_by` varchar(20) NOT NULL,
    `updated_at` date DEFAULT NULL,
    `updated_by` varchar(20) DEFAULT NULL
    );

-- Dummy data for accounts
INSERT INTO `accounts` (`customer_id`, `account_type`, `branch_address`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
  (1001, 'SAVINGS', '123 Main St, Springfield', DATE '2024-01-15', 'system', NULL, NULL);
INSERT INTO `accounts` (`customer_id`, `account_type`, `branch_address`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
  (1002, 'CHECKING', '45 Oak Ave, Metropolis', DATE '2024-02-10', 'system', DATE '2024-06-01', 'admin');
INSERT INTO `accounts` (`customer_id`, `account_type`, `branch_address`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
  (1003, 'BUSINESS', '78 Pine Rd, Gotham', DATE '2024-03-05', 'system', NULL, NULL);
INSERT INTO `accounts` (`customer_id`, `account_type`, `branch_address`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
  (1004, 'SAVINGS', '9 Elm St, Star City', DATE '2024-03-22', 'seed', DATE '2024-08-12', 'auditor');
INSERT INTO `accounts` (`customer_id`, `account_type`, `branch_address`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
  (1005, 'CHECKING', '250 Maple Blvd, Central City', DATE '2024-04-18', 'seed', NULL, NULL);
INSERT INTO `accounts` (`customer_id`, `account_type`, `branch_address`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
  (1006, 'SAVINGS', '16 Birch Ln, Smallville', DATE '2024-05-09', 'system', DATE '2024-10-01', 'ops');
INSERT INTO `accounts` (`customer_id`, `account_type`, `branch_address`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
  (1007, 'BUSINESS', '801 Cedar Cir, Keystone', DATE '2024-06-27', 'system', NULL, NULL);
INSERT INTO `accounts` (`customer_id`, `account_type`, `branch_address`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
  (1008, 'SAVINGS', '77 Willow Way, Coast City', DATE '2024-07-13', 'seed', NULL, NULL);
INSERT INTO `accounts` (`customer_id`, `account_type`, `branch_address`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
  (1009, 'CHECKING', '5 Aspen Dr, Blüdhaven', DATE '2024-08-02', 'seed', DATE '2025-01-10', 'admin');
INSERT INTO `accounts` (`customer_id`, `account_type`, `branch_address`, `created_at`, `created_by`, `updated_at`, `updated_by`) VALUES
  (1010, 'SAVINGS', '390 Poplar St, National City', DATE '2024-09-21', 'system', NULL, NULL);