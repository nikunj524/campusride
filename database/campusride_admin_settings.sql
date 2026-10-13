-- CampusRide Admin Settings Table
-- This table stores application-wide settings that only admins can modify

USE campusride;

-- Drop table if exists
DROP TABLE IF EXISTS admin_settings;

-- Create admin_settings table
CREATE TABLE admin_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_name VARCHAR(100) DEFAULT 'CampusRide',
    support_email VARCHAR(100) DEFAULT 'support@campusride.com',
    support_phone VARCHAR(20) DEFAULT '+91 9999999999',
    require_driver_approval BOOLEAN DEFAULT TRUE,
    allow_registration BOOLEAN DEFAULT TRUE,
    max_rides_per_day INT DEFAULT 10,
    min_driver_rating DECIMAL(2,1) DEFAULT 4.0,
    max_passengers_per_ride INT DEFAULT 4,
    enable_email_notifications BOOLEAN DEFAULT TRUE,
    enable_sms_notifications BOOLEAN DEFAULT FALSE,
    auto_approve_drivers BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) DEFAULT 'system',
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default settings
INSERT INTO admin_settings (
    site_name,
    support_email,
    support_phone,
    require_driver_approval,
    allow_registration,
    max_rides_per_day,
    min_driver_rating,
    max_passengers_per_ride,
    enable_email_notifications,
    enable_sms_notifications,
    auto_approve_drivers,
    updated_by
) VALUES (
    'CampusRide',
    'support@campusride.com',
    '+91 9999999999',
    TRUE,
    TRUE,
    10,
    4.0,
    4,
    TRUE,
    FALSE,
    FALSE,
    'system'
);

-- Verify the data
SELECT * FROM admin_settings;

-- Show table structure
DESCRIBE admin_settings;
