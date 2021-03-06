CREATE TABLE `clinical_consultation` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
  `encounter_id` varchar(255) DEFAULT NULL,
  `encounter_uuid` char(38) DEFAULT NULL,
  `encounter_type` int(11) DEFAULT NULL,
  `patient_id` varchar(255) DEFAULT NULL,
  `patient_uuid` char(38) DEFAULT NULL,
  `consultation_date` date DEFAULT NULL,
  `scheduled_date` date DEFAULT NULL,
  `observation_date` datetime DEFAULT NULL,
  `source_database` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `clinical_consultation_encounter_id` (`encounter_id`),
  KEY `clinical_consultation_encounter_uuid` (`encounter_uuid`),
  KEY `clinical_consultation_patient_id` (`patient_id`),
  KEY `clinical_consultation_patient_uuid` (`patient_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8