CREATE TABLE IF NOT EXISTS `customer` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `firstName` varchar(255) default NULL,
  `lastName` VARCHAR (255) DEFAULT NULL,
  `birthDate` VARCHAR (255)
) AUTO_INCREMENT=1;