package com.example.cashdocumentsservice.service;

import com.example.cashdocumentsservice.dto.DailySummaryReport;
import com.example.cashdocumentsservice.dto.FileDto;
import com.example.cashdocumentsservice.model.MyFile;
import com.example.cashdocumentsservice.repository.MyFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CashDocumentsClientService {
    // Define the download directory relative to project folder
    /**
     * Directory where downloaded files are persisted locally.
     * Relative to the project root directory.
     */
    private static final String DOWNLOAD_DIR = "downloads/";
    /**
     * DateTimeFormatter for generating timestamp-based filenames.
     * Format: yyyyMMdd_HHmmss (e.g., 20231201_143030)
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final WebClient webClient;
    private final String cashReportingServiceBaseUrl;
    private final String cashReportingServiceApiKey;
    private final MyFileRepository myFileRepository;
    private final RestTemplate restTemplate;

    public CashDocumentsClientService(MyFileRepository myFileRepository,
                                      RestTemplate restTemplate,
                                      WebClient.Builder webClientBuilder,
                                      @Value("${cashreportingservice.service.base-url}") String baseUrl,
                                      @Value("${cashreportingservice.service.api-key}") String apiKey) {
        this.myFileRepository = myFileRepository;
        this.restTemplate = restTemplate;
        this.cashReportingServiceBaseUrl = baseUrl;
        this.cashReportingServiceApiKey = apiKey;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("FIB-X-AUTH", apiKey)
                .build();
    }

    public Mono<ResponseEntity<DailySummaryReport>> getDailySummary() {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath("/api/v1/reports/daily-summary")
                .queryParamIfPresent("date", Optional.of(LocalDate.now()));

        return webClient.get()
                .uri(uriBuilder.build().toUriString())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException(
                                        "Error from CashReporting service: " + errorBody))))
                .bodyToMono(DailySummaryReport.class)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Creates a zip archive in memory from a list of XFile objects.
     * Uses ZipOutputStream with try-with-resources for automatic resource management.
     *
     * @param files list of XFile objects to include in the zip archive
     * @return byte array containing the zip file data
     * @throws IOException if zip creation fails
     */
    public byte[] createZipFile(List<MyFile> files) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (MyFile file : files) {
                ZipEntry entry = new ZipEntry(file.getFileName());
                zos.putNextEntry(entry);
                zos.write(file.getFile());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    /**
     * Persists a single file to the local project directory using efficient NIO operations.
     * Uses Files.write() for atomic file operations and optimal performance.
     *
     * <p>✅ Summary </p>
     * <h6>Yes, Files.write(Paths.get(...)) is definitely the way to go. It's: </h6>
     * <ul>
     *
     * <li> More elegant - Clean, readable one-liner </li>
     *
     * <li> More efficient - Better performance and resource management </li>
     *
     * <li> Safer - Built-in error handling and atomic operations </li>
     *
     * <li> Modern - Uses Java NIO API which is the current standard </li>
     * </ul>
     * <p>
     * The implementation I provided is both efficient and elegant while maintaining all the original functionality.
     *
     * @param fileGroup the file group name for filename generation
     * @param file      the XFile object containing data to persist
     * @return String representing the full path where file was saved
     * @throws IOException if file write operation fails
     * @see java.nio.file.Files#write(Path, byte[], OpenOption...)
     */
    public String saveSingleFileToDisk(String fileGroup, MyFile file) throws IOException {
        // Create downloads directory if it doesn't exist
        Path downloadPath = createDownloadDirectory(fileGroup);

        // Generate filename with timestamp to avoid overwrites
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String filename = String.format("%s_%s_%s",
                fileGroup, timestamp, file.getFileName());

        Path filePath = downloadPath.resolve(filename);

        // Elegant one-line file write - most efficient way
        Files.write(filePath, file.getFile());

        return filePath.toString();
    }

    /**
     * Persists a zip file to the local project directory.
     * Uses efficient NIO file operations with Files.write().
     *
     * @param fileGroup the file group name for filename generation
     * @param zipData   byte array containing zip file data
     * @return String representing the full path where zip file was saved
     * @throws IOException if file write operation fails
     */
    public String saveZipFileToDisk(String fileGroup, byte[] zipData) throws IOException {
        // Create downloads directory if it doesn't exist
        Path downloadPath = createDownloadDirectory(fileGroup);

        // Generate filename with timestamp
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String filename = String.format("%s_%s.zip", fileGroup, timestamp);

        Path filePath = downloadPath.resolve(filename);

        // Elegant one-line zip file write
        Files.write(filePath, zipData);

        return filePath.toString();
    }

    /**
     * Creates the download directory if it doesn't exist.
     * Uses Files.createDirectories() to create all intermediate directories.
     *
     * @throws IOException if directory creation fails
     * @see java.nio.file.Files#createDirectories(Path, FileAttribute[])
     */
    private Path createDownloadDirectory(String fileGroup) throws IOException {
        Path downloadPath = Paths.get(DOWNLOAD_DIR, fileGroup); // Path.of(DOWNLOAD_DIR, fileGroup) since Java 11+
        if (!Files.exists(downloadPath)) {
            Files.createDirectories(downloadPath);
        }
        return downloadPath;
    }

    public FileDto mapFileToDto(MyFile file) {

        FileDto dto = new FileDto();

        dto.setFileGroup(file.getFileGroup());
        dto.setFileName(file.getFileName());
        dto.setId(file.getId());

        return dto;
    }

    public MyFile findByFileGroupAndFileName(String fileGroup, String fileName) {
       return myFileRepository.findByFileGroupAndFileName(fileGroup, fileName);
    }

    public MyFile save(MyFile file) {
        return myFileRepository.save(file);
    }

    public List<MyFile> findByFileGroup(String fileGroup) {
        return myFileRepository.findByFileGroup(fileGroup);
    }

    public List<MyFile> findAll() {
        return myFileRepository.findAll();
    }

    private Mono<Void> saveDailySummaryToFileAndDB(ResponseEntity<DailySummaryReport> response) {
        return Mono.fromCallable(() -> {
            try {
                // Block and get the response (use with caution in reactive applications)
                //ResponseEntity<DailySummaryReport> response = dailySummaryMono.block(Duration.ofSeconds(30));

                if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    DailySummaryReport report = response.getBody();
                    String fileContent = formatDailySummary(report);

                    // Generate filename with timestamp
                    String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    String filename = "daily_summary_" + date + ".txt";

                    // Save to file
                    Path filePath = Paths.get("reports", filename);
                    Files.createDirectories(filePath.getParent()); // Create directory if it doesn't exist
                    Files.write(filePath, fileContent.getBytes(StandardCharsets.UTF_8));

                    MyFile existingFile = myFileRepository.findByFileGroupAndFileName("reports", filename);

                    if (existingFile != null) {
                        // Update existing file
                        existingFile.setFile(fileContent.getBytes());
                        myFileRepository.save(existingFile);
                    } else {
                        // Create new file entry
                        MyFile newFile = new MyFile("reports", filename, fileContent.getBytes());
                        myFileRepository.save(newFile);
                    }

                    System.out.println("Daily summary saved to: " + filePath.toAbsolutePath());
                } else {
                    System.out.println("No daily summary data received or error response");
                }
            } catch (Exception e) {
                System.err.println("Error saving daily summary to file: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then(); // Offload file I/O to bounded elastic scheduler
    }

    private String formatDailySummary(DailySummaryReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("DAILY SUMMARY REPORT\n");
        sb.append("====================\n");
        sb.append(String.format("Cashier: %s%n", report.getCashier() != null ? report.getCashier() : "N/A"));
        sb.append(String.format("Date: %s%n", report.getDate() != null ? report.getDate() : "N/A"));
        sb.append(String.format("Total Deposits: %s%n", report.getTotalDeposits()));
        sb.append(String.format("Total Withdrawals: %s%n", report.getTotalWithdrawals()));
        sb.append(String.format("End of Day Balance: %s%n", report.getEndOfDayBalance()));

        if (report.getCurrencyBreakdown() != null && !report.getCurrencyBreakdown().isEmpty()) {
            sb.append("\nCurrency Breakdown:\n");
            sb.append("------------------\n");
            report.getCurrencyBreakdown().forEach((currency, amount) ->
                    sb.append(String.format("%s: %s%n", currency, amount))
            );
        }

        sb.append("\nReport Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return sb.toString();
    }

    public Mono<Void> processAndSaveDailySummary() {
        return getDailySummary()
                .flatMap(response -> {
                    if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        return saveDailySummaryToFileAndDB(response);
                    } else {
                        System.out.println("No daily summary data received or error response");
                        return Mono.empty();
                    }
                })
                .doOnError(error -> System.err.println("Error fetching daily summary: " + error.getMessage()))
                .onErrorResume(e -> Mono.empty()); // Continue even if daily summary fails
    }
}
