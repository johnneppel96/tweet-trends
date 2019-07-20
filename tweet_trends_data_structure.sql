-- MySQL dump 10.13  Distrib 8.0.16, for Win64 (x86_64)
--
-- Host: localhost    Database: tweet_trends_data
-- ------------------------------------------------------
-- Server version	8.0.16

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `archived_location_data`
--

DROP TABLE IF EXISTS `archived_location_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `archived_location_data` (
  `ID` bigint(11) NOT NULL AUTO_INCREMENT,
  `LocationName` varchar(600) NOT NULL,
  `latitude` decimal(11,8) NOT NULL,
  `longitude` decimal(11,8) NOT NULL,
  PRIMARY KEY (`ID`,`LocationName`),
  UNIQUE KEY `locationName_UNIQUE` (`LocationName`),
  KEY `locationName_idx` (`LocationName`)
) ENGINE=InnoDB AUTO_INCREMENT=1598 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `associated_tags`
--

DROP TABLE IF EXISTS `associated_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `associated_tags` (
  `Index` bigint(20) NOT NULL AUTO_INCREMENT,
  `associated_tag_id` bigint(11) DEFAULT NULL,
  `TweetID` bigint(45) DEFAULT NULL,
  PRIMARY KEY (`Index`),
  UNIQUE KEY `Index_UNIQUE` (`Index`),
  KEY `fdasfjl_idx` (`associated_tag_id`),
  CONSTRAINT `assocID_FK` FOREIGN KEY (`associated_tag_id`) REFERENCES `associated_tags_description` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51258 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `associated_tags_description`
--

DROP TABLE IF EXISTS `associated_tags_description`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `associated_tags_description` (
  `ID` bigint(11) NOT NULL AUTO_INCREMENT,
  `tag_text` varchar(120) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `tag_text_UNIQUE` (`tag_text`)
) ENGINE=InnoDB AUTO_INCREMENT=45692 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tweet_data`
--

DROP TABLE IF EXISTS `tweet_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tweet_data` (
  `Index` bigint(45) NOT NULL AUTO_INCREMENT,
  `TweetID` bigint(45) NOT NULL,
  `TweetText` varchar(800) DEFAULT NULL,
  `query_tag_id` int(11) NOT NULL,
  `latitude` decimal(11,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `tweet_timestamp` datetime NOT NULL,
  `Hashtags` varchar(300) DEFAULT NULL,
  `InsertionTimestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`Index`,`TweetID`),
  KEY `idx_tweetdata_Tag_ID` (`query_tag_id`) /*!80000 INVISIBLE */,
  CONSTRAINT `TagIDFK` FOREIGN KEY (`query_tag_id`) REFERENCES `tweet_tags` (`query_tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=53163 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tweet_tags`
--

DROP TABLE IF EXISTS `tweet_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tweet_tags` (
  `Query_Tag_ID` int(11) NOT NULL AUTO_INCREMENT,
  `QueryTagText` varchar(150) NOT NULL,
  PRIMARY KEY (`Query_Tag_ID`,`QueryTagText`),
  UNIQUE KEY `QueryTagText_UNIQUE` (`QueryTagText`),
  UNIQUE KEY `Query_Tag_ID_UNIQUE` (`Query_Tag_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=50272 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-07-20 16:05:05
