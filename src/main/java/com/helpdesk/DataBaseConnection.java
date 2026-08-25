package com.helpdesk;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;


@Slf4j
@Component
@RequiredArgsConstructor
public class DataBaseConnection {

    private final DataSource dataSource;

    @PostConstruct
    public void verificarConexion() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("Conexion a base de datos establecida correctamente");
            log.info("Motor: {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
            log.info("URL: {}", metaData.getURL());
            log.info("Usuario: {}", metaData.getUserName());
        } catch (Exception e) {
            log.error("No fue posible conectar a la base de datos: {}", e.getMessage());
        }
    }
}
