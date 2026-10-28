-- Ejecutar con el cliente mysql sobre la BD de la aplicación.
-- Son datos FICTICIOS para probar clustering. No representan hogares reales.
DELIMITER $$
DROP PROCEDURE IF EXISTS cargar_censos_masivos$$
CREATE PROCEDURE cargar_censos_masivos(IN cantidad INT, IN centro_lat DOUBLE, IN centro_lon DOUBLE)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE lote VARCHAR(36);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    IF cantidad IS NULL OR cantidad < 1 OR cantidad > 100000
       OR centro_lat IS NULL OR centro_lat NOT BETWEEN -89.9 AND 89.9
       OR centro_lon IS NULL OR centro_lon NOT BETWEEN -179.9 AND 179.9 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cantidad: 1..100000; centro debe permitir un radio de 0.1 grados';
    END IF;
    SET lote = UUID();
    START TRANSACTION;
    WHILE i < cantidad DO
        INSERT INTO censuses(name, last_name, total_members, television_count, latitude, longitude, origin, censist_id)
        VALUES (CONCAT('Prueba ', i + 1), CONCAT('Lote ', lote),
                1 + FLOOR(RAND() * 8), FLOOR(RAND() * 5),
                centro_lat + (RAND() - 0.5) * 0.2, centro_lon + (RAND() - 0.5) * 0.2, 'MASIVO', NULL);
        SET i = i + 1;
    END WHILE;
    COMMIT;
    SELECT lote AS lote_generado, cantidad AS censos_insertados;
END$$
DELIMITER ;

-- Ejemplo, ejecutar explícitamente cuando se quieran insertar datos:
-- CALL cargar_censos_masivos(10000, 14.6349, -90.5069);
