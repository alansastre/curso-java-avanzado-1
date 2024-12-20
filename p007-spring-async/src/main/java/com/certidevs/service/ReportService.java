package com.certidevs.service;

import com.certidevs.dto.Order;
import com.certidevs.dto.ReportCreationRequest;
import com.certidevs.dto.Transaction;
import com.certidevs.dto.UserConsolidated;
import com.certidevs.entity.Report;
import com.certidevs.entity.User;
import com.certidevs.repository.ReportRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class ReportService {

    private OrderService orderService;
    private UserService userService;
    private TransactionService transactionService;
    private FileService fileService;
    private EmailService emailService;
    private ReportRepository reportRepository;


    @Async
//    @Async("taskExecutor1")
//    @Async("taskExecutor2")
    public void generateConsolidatedReportForCompany(ReportCreationRequest request) {
        getUsers(request.companyId())
                .thenCompose(this::getConsolidatedDataForUsers)
                .thenCompose(this::generateReportContent)
                .thenCompose(this::saveReportToFile)
                .thenCompose(this::saveReportToDatabase)
                .thenCompose(this::sendReportByEmail)
                .exceptionally(ex -> {
                    log.error("Error en el proceso de generación del reporte: {}", ex.getMessage());
                    return null;
                });
    }

    private CompletableFuture<List<User>> getUsers(Long companyId) {
        return CompletableFuture.supplyAsync(() -> {
            List<User> users = userService.findAllByCompanyId(companyId);
            log.info("Obtenidos {} usuarios para la compañía ID {}", users.size(), companyId);
            return users;
        });
    }

    private CompletableFuture<List<UserConsolidated>> getConsolidatedDataForUsers(List<User> users) {
        List<CompletableFuture<UserConsolidated>> consolidatedFutures = users.stream()
                .map(user -> {
                    CompletableFuture<List<Order>> ordersFuture = orderService.findAllOrdersByUserId(user.getId());
                    CompletableFuture<List<Transaction>> transactionsFuture = transactionService.findAllTransactionsByUserId(user.getId());

                    return ordersFuture.thenCombine(transactionsFuture,
                            (orders, transactions) -> new UserConsolidated(user, orders, transactions));
                })
                .toList();

        CompletableFuture<Void> allDone = CompletableFuture.allOf(
                consolidatedFutures.toArray(new CompletableFuture[0])
        );

        return allDone.thenApply(v ->
                consolidatedFutures.stream()
                        .map(CompletableFuture::join) // Safe to join since all futures are done
                        .collect(Collectors.toList())
        );
    }


    private CompletableFuture<String> generateReportContent(List<UserConsolidated> userDataList) {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Reporte Consolidado para la Compañía\n\n");
            for (UserConsolidated data : userDataList) {
                sb.append("Usuario: ").append(data.user().getEmail()).append("\n");
                sb.append("Órdenes:\n");
                data.orders().forEach(o -> sb.append(o).append("\n"));
                sb.append("Transacciones:\n");
                data.transactions().forEach(t -> sb.append(t).append("\n"));
                sb.append("\n");
            }
            log.info("Contenido del reporte generado.");
            return sb.toString();
        });
    }


    private CompletableFuture<String> saveReportToFile(String reportContent) {
        return fileService.generateReport(reportContent)
                .thenApply(filePath -> {
                    if (filePath != null) {
                        log.info("Reporte guardado en archivo: {}", filePath);
                        return filePath;
                    } else {
                        log.error("Falló la generación del archivo de reporte.");
                        return null;
                    }
                });
    }


    private CompletableFuture<Report> saveReportToDatabase(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            Report report = Report.builder()
                    .filePath(filePath)
                    .createdAt(LocalDateTime.now())
                    .build();
            reportRepository.save(report);
            log.info("Reporte almacenado en base de datos con ID: {}", report.getId());
            return report;
        });
    }


    private CompletableFuture<Void> sendReportByEmail(Report report) {
        return CompletableFuture.runAsync(() -> {
            try {
                byte[] fileBytes = Files.readAllBytes(Paths.get(report.getFilePath()));
                emailService.sendEmailWithAttachment(
                        "admin@localhost.com", // Asumiendo que el correo se envía al nombre de la compañía
                        "Reporte Consolidado",
                        "Adjunto encontrarás el reporte solicitado.",
                        fileBytes,
                        "reporte.txt"
                ).join(); // Espera a que el correo se envíe antes de continuar
                log.info("Correo electrónico enviado con el reporte.");
            } catch (IOException e) {
                log.error("Error leyendo el archivo de reporte: {}", e.getMessage());
            }
        });
    }
}
