-- MySQL dump 10.13  Distrib 9.7.1, for macos26.4 (arm64)
--
-- Host: localhost    Database: campusride
-- ------------------------------------------------------
-- Server version	9.7.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ 'aab824aa-7bc8-11f1-9e39-cd92707e2645:1-258';

--
-- Table structure for table `admin_settings`
--

DROP TABLE IF EXISTS `admin_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `site_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'CampusRide',
  `support_email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'support@campusride.com',
  `support_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '+91 9999999999',
  `require_driver_approval` tinyint(1) DEFAULT '1',
  `allow_registration` tinyint(1) DEFAULT '1',
  `max_rides_per_day` int DEFAULT '10',
  `min_driver_rating` decimal(2,1) DEFAULT '4.0',
  `max_passengers_per_ride` int DEFAULT '4',
  `enable_email_notifications` tinyint(1) DEFAULT '1',
  `enable_sms_notifications` tinyint(1) DEFAULT '0',
  `auto_approve_drivers` tinyint(1) DEFAULT '0',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'system',
  PRIMARY KEY (`id`),
  KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_settings`
--

LOCK TABLES `admin_settings` WRITE;
/*!40000 ALTER TABLE `admin_settings` DISABLE KEYS */;
INSERT INTO `admin_settings` VALUES (1,'CampusRide','support@campusride.com','+91 9999999999',1,1,10,4.0,4,1,0,0,'2026-09-12 05:25:58','system');
/*!40000 ALTER TABLE `admin_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `passenger_id` bigint NOT NULL,
  `ride_id` bigint NOT NULL,
  `seats` int NOT NULL,
  `status` enum('CANCELLED','COMPLETED','CONFIRMED','PENDING') NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `driver_requests`
--

DROP TABLE IF EXISTS `driver_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `driver_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `requested_at` datetime(6) NOT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  `status` enum('APPROVED','PENDING','REJECTED','REVOKED') NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `driver_requests`
--

LOCK TABLES `driver_requests` WRITE;
/*!40000 ALTER TABLE `driver_requests` DISABLE KEYS */;
INSERT INTO `driver_requests` VALUES (1,'2026-09-01 14:04:24.965035','2026-09-01 18:24:42.338266',22,'APPROVED',15),(2,'2026-09-12 09:12:23.052414',NULL,NULL,'PENDING',24);
/*!40000 ALTER TABLE `driver_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rides`
--

DROP TABLE IF EXISTS `rides`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rides` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `available_seats` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `departure_time` datetime(6) NOT NULL,
  `destination` varchar(255) NOT NULL,
  `driver_id` bigint NOT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `passenger_id` bigint DEFAULT NULL,
  `price_per_seat` decimal(10,2) DEFAULT NULL,
  `source` varchar(255) NOT NULL,
  `status` enum('ACCEPTED','CANCELLED','COMPLETED','PENDING','STARTED') NOT NULL,
  `type` enum('OFFERING','REQUESTING') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rides`
--

LOCK TABLES `rides` WRITE;
/*!40000 ALTER TABLE `rides` DISABLE KEYS */;
INSERT INTO `rides` VALUES (1,3,'2026-08-28 08:46:20.428111','2026-09-02 11:38:13.000000','Gujarat University, Ahmedabad',14,'AC car, comfortable ride',17,50.00,'Satellite, Ahmedabad','ACCEPTED','OFFERING'),(2,2,'2026-08-28 08:46:55.454398','2026-09-02 11:38:13.000000','PDPU, Gandhinagar',14,NULL,18,80.00,'Maninagar, Ahmedabad','ACCEPTED','OFFERING'),(3,4,'2026-08-28 08:47:05.688342','2026-09-02 11:38:13.000000','IIM Ahmedabad',14,'Morning ride',NULL,30.00,'Vastrapur, Ahmedabad','PENDING','OFFERING'),(4,1,'2026-08-28 09:20:45.125082','2026-09-02 11:38:13.000000','dffdf',15,'dfdfbbddbbddggdddgdgddgdg',17,56.00,'gfhhgg','ACCEPTED','OFFERING');
/*!40000 ALTER TABLE `rides` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(120) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `role` enum('ADMIN','DRIVER','STUDENT') NOT NULL,
  `password_reset_otp_expires_at` datetime(6) DEFAULT NULL,
  `password_reset_otp_hash` varchar(100) DEFAULT NULL,
  `password_reset_verified_until` datetime(6) DEFAULT NULL,
  `driver_eligible` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (14,'2026-08-18 16:05:39.481859','venomhardik5@gmail.com','jay','k','$2a$10$PIS/s4zTw.fAcsbExV7Fl.fb8ui5RbCkzL0zl3sJ59BKO3uczsIa.','7777777777','DRIVER',NULL,NULL,NULL,NULL),(15,'2026-08-28 09:02:09.964789','h@gmail.com','m','h','$2a$10$W.tjy996646AVhdJSfqjounaW.BQQopOQ7.U9U7uEacIocpUO1NUi','7656789867','STUDENT',NULL,NULL,NULL,_binary ''),(16,'2026-08-28 09:03:09.993080','g@hmail.com','k','l','$2a$10$Ks6g/nJAhQE2lKysdMTb2.bdWolRFVdSd9sR5hM7E785FJnl4SY9m','5788556644','STUDENT',NULL,NULL,NULL,NULL),(17,'2026-09-01 11:31:15.138638','keshav@gmail.com','keshav','khatri','$2a$10$wQWxkHKD67NLwointSyiT.lKK6oUAkOPZ59ArdgUTJeDfmPM/3kQm','7765652476','DRIVER',NULL,NULL,NULL,NULL),(18,'2026-09-01 11:42:50.269493','mahesh@gmail.com','mahesh','makwana','$2a$10$hPEPtugqNlmtYeYEli3uH.9xjQTw.yDR0LOGhmeiS4Ud4rr5MGB3m','6474747474','DRIVER',NULL,NULL,NULL,NULL),(22,'2026-09-01 18:23:18.000000','admin@campusride.com','Admin','User','$2a$10$pD5VPXZ3CpCyWkdQiSx5Yu5g2A.gCFWotw3i13MDjO64ooUNn5FGK','9999999999','ADMIN',NULL,NULL,NULL,_binary ''),(23,'2026-09-01 18:23:31.323455','testadmin@test.com','Test','Admin','$2a$10$pD5VPXZ3CpCyWkdQiSx5Yu5g2A.gCFWotw3i13MDjO64ooUNn5FGK','1234567890','STUDENT',NULL,NULL,NULL,NULL),(24,'2026-09-12 09:12:04.758660','g@gmail.com','jayesh','vaja','$2a$10$uE704w2SNAdkpEX3RpZuDeoggxGVf4buG5t/hNPIoyIrNG/28EkW2','2626262626','STUDENT',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicles`
--

DROP TABLE IF EXISTS `vehicles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `ownership_type` enum('FAMILY','OTHER','OWN') NOT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL,
  `total_seats` int NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `vehicle_color` varchar(40) NOT NULL,
  `vehicle_model` varchar(80) NOT NULL,
  `vehicle_number` varchar(30) NOT NULL,
  `vehicle_type` enum('BIKE','CAR') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vehicles_user_id` (`user_id`),
  UNIQUE KEY `uk_vehicles_vehicle_number` (`vehicle_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicles`
--

LOCK TABLES `vehicles` WRITE;
/*!40000 ALTER TABLE `vehicles` DISABLE KEYS */;
/*!40000 ALTER TABLE `vehicles` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-12 10:56:32
