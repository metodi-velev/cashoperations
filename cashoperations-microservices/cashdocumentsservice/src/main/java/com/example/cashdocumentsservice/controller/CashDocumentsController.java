package com.example.cashdocumentsservice.controller;

import com.example.cashdocumentsservice.dto.FileDto;
import com.example.cashdocumentsservice.model.MyFile;
import com.example.cashdocumentsservice.service.CashDocumentsClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/documents")
public class CashDocumentsController {

    private CashDocumentsClientService cashDocumentsClientService;

    @Autowired
    public CashDocumentsController(CashDocumentsClientService cashDocumentsClientService) {
        this.cashDocumentsClientService = cashDocumentsClientService;
    }

    //define endpoints for download and upload

    /**
     * Handles multipart file upload requests to store files in the database.
     * Supports uploading multiple files to a specified file group with duplicate handling.
     *
     * <p>Validation and behavior:
     * <ul>
     *   <li>File size validation: Configured via Spring properties (default: 100KB max)</li>
     *   <li>Duplicate handling: Same fileName in same fileGroup replaces existing file</li>
     *   <li>Atomic operations: Each file is processed independently</li>
     * </ul>
     *
     * <p>Request parameters:
     * <ul>
     *   <li>fileGroup: String - The group identifier for organizing files</li>
     *   <li>files: MultipartFile[] - Array of files to upload</li>
     * </ul>
     *
     * <p>Error handling:
     * <ul>
     *   <li>Files exceeding size limit: Automatically rejected by Spring with 500 error</li>
     *   <li>IO exceptions during processing: Returns 500 Internal Server Error</li>
     *   <li>Partial failures: Some files may succeed while others fail</li>
     * </ul>
     *
     * @param fileGroup the name of the file group to organize uploaded files
     * @param files     array of multipart files to upload and store
     * @return ResponseEntity with status code indicating success or failure
     * @throws IOException if file processing fails (handled internally)
     * @HTTP 201 Created - Files successfully stored in database
     * @HTTP 500 Internal Server Error - File size exceeds limit or processing error occurs
     * @see org.springframework.web.multipart.MultipartFile
     * @see org.springframework.boot.autoconfigure.web.servlet.MultipartProperties
     */
    @PostMapping(value = "/uploader", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Void>> uploadFile(
            @RequestParam("fileGroup") String fileGroup,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "getAndSaveDailySummary", required = false) Optional<String> getAndSaveDailySummary) {

        Mono<Void> dailySummaryProcess = Mono.empty();

        if (getAndSaveDailySummary.isPresent() && getAndSaveDailySummary.get().equalsIgnoreCase("yes")) {
            dailySummaryProcess = cashDocumentsClientService.processAndSaveDailySummary();
        }
        return dailySummaryProcess
                .then(Mono.fromCallable(() -> {
                    // Your existing file upload logic here
                    for (MultipartFile file : files) {
                        // Check if file already exists for this fileGroup
                        MyFile existingFile = cashDocumentsClientService.findByFileGroupAndFileName(fileGroup, file.getOriginalFilename());

                        if (existingFile != null) {
                            // Update existing file
                            existingFile.setFile(file.getBytes());
                            cashDocumentsClientService.save(existingFile);
                        } else {
                            // Create new file entry
                            MyFile newFile = new MyFile(fileGroup, file.getOriginalFilename(), file.getBytes());
                            cashDocumentsClientService.save(newFile);
                        }
                    }
                    return ResponseEntity.status(HttpStatus.CREATED).<Void>build();
                }))
                .onErrorResume(e -> {
                    System.err.println("Error in upload process: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Handles file download requests for specified file groups.
     * Returns files as either single file download or zip archive for multiple files.
     * All downloaded files are automatically persisted to the local project directory.
     *
     * <p>Response behavior:
     * <ul>
     *   <li>Single file: Returns file directly with original filename</li>
     *   <li>Multiple files: Returns zip archive named {fileGroup}.zip</li>
     *   <li>No files: Returns 404 Not Found status</li>
     * </ul>
     *
     * <p>Local persistence:
     * <ul>
     *   <li>Files are saved to downloads/ directory with timestamps</li>
     *   <li>Filenames format: {fileGroup}_{timestamp}_{filename}</li>
     *   <li>Zip files format: {fileGroup}_{timestamp}.zip</li>
     * </ul>
     *
     * @param fileGroup the name of the file group to download
     * @return ResponseEntity containing file data or zip archive with appropriate headers
     * @throws IOException if file operations fail
     * @HTTP 200 Successful download (single file or zip)
     * @HTTP 404 No files found for the specified file group
     * @HTTP 500 Internal server error during file processing
     */
    @GetMapping("/downloader")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("fileGroup") String fileGroup) {
        List<MyFile> files = cashDocumentsClientService.findByFileGroup(fileGroup);

        if (files.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            if (files.size() == 1) {
                // Return single file
                MyFile file = files.get(0);

                // Save to project folder
                String filename = cashDocumentsClientService.saveSingleFileToDisk(fileGroup, file);
                System.out.println("File saved to: " + filename);

                // Return file in response
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", file.getFileName());
                headers.setContentLength(file.getFile().length);

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(file.getFile());
            } else {
                // Handle multiple files - create zip
                byte[] zipData = cashDocumentsClientService.createZipFile(files);

                // Save zip to project folder
                String zipFilename = cashDocumentsClientService.saveZipFileToDisk(fileGroup, zipData);
                System.out.println("Zip file saved to: " + zipFilename);

                // Return zip in response
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", fileGroup + ".zip");
                headers.setContentLength(zipData.length);

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(zipData);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/metadata")
    public ResponseEntity<List<FileDto>> getMetadata() {
        try {
            List<MyFile> files = cashDocumentsClientService.findAll();

            List<FileDto> results = new ArrayList<>();

            for (MyFile file : files) {
                results.add(cashDocumentsClientService.mapFileToDto(file));
            }

            if (files.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok()
                    .body(results);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        try {

            return ResponseEntity.ok()
                    .body("Hello - OK");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
