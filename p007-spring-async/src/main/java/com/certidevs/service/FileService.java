package com.certidevs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FileService {

    /**
     * Genera un reporte en un archivo de forma asíncrona.
     */
    public CompletableFuture<String> generateReport(String content) {
        return CompletableFuture.supplyAsync(() -> {
            String filePath = "report_" + System.currentTimeMillis() + ".txt";
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(content);
                // Simula tiempo de escritura
                TimeUnit.SECONDS.sleep(1);
                return filePath;
            } catch (IOException | InterruptedException e) {
                log.error("Error generando reporte: {}", e.getMessage());
                return null;
            }
        });
    }

}
