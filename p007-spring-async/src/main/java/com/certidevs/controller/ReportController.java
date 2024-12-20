package com.certidevs.controller;

import com.certidevs.dto.ReportCreationRequest;
import com.certidevs.service.ReportService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/reports")
public class ReportController {


    private ReportService reportService;

    /**
     * Endpoint para generar el reporte consolidado.
     */
    @PostMapping("/consolidated")
    public ResponseEntity<String> createConsolidatedReport(@RequestBody ReportCreationRequest request) {
        reportService.generateConsolidatedReportForCompany(request); // Lanza proceso asíncrono
        return ResponseEntity.ok("Petición recibida, reporte en proceso. Se enviará a " + request.email());
    }

    /**
     * Endpoint para descargar el reporte generado.
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadReport(@RequestParam String file) {
        // Lógica para descargar el archivo
        // Por simplicidad, solo devolvemos un mensaje
        return ResponseEntity.ok("Descargando archivo: " + file);
    }
}