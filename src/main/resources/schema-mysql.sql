CREATE TABLE IF NOT EXISTS `customer` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `firstName` varchar(255) default NULL,
  `lastName` VARCHAR (255) DEFAULT NULL,
  `birthDate` VARCHAR (255)
) AUTO_INCREMENT=1;

CREATE TABLE IF NOT EXISTS `new_customer` (
  `id` mediumint(8) unsigned NOT NULL auto_increment,
  `firstName` varchar(255) default NULL,
  `lastName` varchar(255) default NULL,
  `birthdate` varchar(255),
  PRIMARY KEY (`id`)
) AUTO_INCREMENT=1;
