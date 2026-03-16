-- Final Optimized Database Schema for SmileAI

-- ==========================================================
-- MIGRATION SCRIPT (Run this if you have existing data)
-- ==========================================================
-- 1. Create the new dentist_profiles table
CREATE TABLE IF NOT EXISTS `dentist_profiles` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `dentist_id` VARCHAR(50) UNIQUE,
    `specialization` VARCHAR(100),
    `clinic_address` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `register`(`id`) ON DELETE CASCADE
);

-- 2. Move existing dentist data to the new table
INSERT INTO dentist_profiles (user_id, dentist_id, specialization, clinic_address)
SELECT id, dentist_id, specialization, clinic_address 
FROM register 
WHERE role = 'dentist' 
AND id NOT IN (SELECT user_id FROM dentist_profiles);
-- ==========================================================

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `reports`;
DROP TABLE IF EXISTS `case_files`;
DROP TABLE IF EXISTS `cases`;
DROP TABLE IF EXISTS `dentist_profiles`;
DROP TABLE IF EXISTS `register`;
DROP TABLE IF EXISTS `medications`;
DROP TABLE IF EXISTS `care_tips`;
DROP TABLE IF EXISTS `notifications`;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Register Table
CREATE TABLE `register` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(150) NOT NULL,
    `email` VARCHAR(150) UNIQUE NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(20) DEFAULT 'dentist',
    `patient_id` VARCHAR(50) UNIQUE, -- For patients
    `phone` VARCHAR(20),
    `plan_type` VARCHAR(50) DEFAULT 'Free',
    `profile_photo` VARCHAR(255),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Dentist Profiles Table
CREATE TABLE `dentist_profiles` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `dentist_id` VARCHAR(50) UNIQUE,
    `specialization` VARCHAR(100),
    `clinic_address` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `register`(`id`) ON DELETE CASCADE
);

-- 3. Cases Table
CREATE TABLE `cases` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `dentist_id` INT NOT NULL,
    `patient_id` VARCHAR(50), -- Links to clinical patient ID
    `patient_first_name` VARCHAR(100) NOT NULL,
    `patient_last_name` VARCHAR(100) NOT NULL,
    `patient_dob` VARCHAR(20),
    `patient_phone` VARCHAR(20),
    `patient_gender` VARCHAR(20),
    `medical_history` TEXT,
    `tooth_numbers` VARCHAR(100),
    `condition` VARCHAR(255),
    `intercanine_width` VARCHAR(50),
    `incisor_length` VARCHAR(50),
    `abutment_health` VARCHAR(100),
    `gingival_architecture` VARCHAR(100),
    `scan_id` VARCHAR(100),
    `restoration_type` VARCHAR(50),
    `material` VARCHAR(50),
    `shade` VARCHAR(10),
    `ai_deficiency` VARCHAR(100),
    `ai_report` TEXT,
    `ai_score` INT,
    `ai_grade` VARCHAR(2),
    `ai_recommendation` TEXT,
    `caries_status` VARCHAR(100),
    `hypodontia_status` VARCHAR(100),
    `discoloration_status` VARCHAR(100),
    `gum_inflammation_status` VARCHAR(100),
    `calculus_status` VARCHAR(100),
    `redness_analysis` VARCHAR(100),
    `face_photo_path` VARCHAR(255),
    `intra_photo_path` VARCHAR(255),
    `status` VARCHAR(20) DEFAULT 'Active',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`dentist_id`) REFERENCES `register`(`id`) ON DELETE CASCADE
);

-- 4. Case Files Table
CREATE TABLE `case_files` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `case_id` INT NOT NULL,
    `file_path` VARCHAR(255) NOT NULL,
    `file_type` VARCHAR(20) NOT NULL,
    `uploaded_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`case_id`) REFERENCES `cases`(`id`) ON DELETE CASCADE
);

-- 5. Reports Table
CREATE TABLE `reports` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `case_id` INT NOT NULL,
    `deficiency_addressed` TEXT,
    `ai_reasoning` TEXT,
    `final_recommendation` TEXT,
    `risk_analysis` TEXT,
    `aesthetic_prognosis` TEXT,
    `placement_strategy` TEXT,
    `hyperdontia_status` VARCHAR(50),
    `aesthetic_symmetry` VARCHAR(50),
    `golden_ratio` VARCHAR(50),
    `missing_teeth_status` VARCHAR(50),
    `medications` TEXT,
    `care_instructions` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`case_id`) REFERENCES `cases`(`id`) ON DELETE CASCADE
);
