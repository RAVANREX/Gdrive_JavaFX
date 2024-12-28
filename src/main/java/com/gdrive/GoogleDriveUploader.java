package com.gdrive;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import javafx.scene.control.Alert;


public class GoogleDriveUploader{
    

    private static final String APPLICATION_NAME = "GoogleDriveUploader";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final java.util.List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json"; // Path to your credentials.json
    private static Drive service;

    private static Credential getCredentials() throws IOException, GeneralSecurityException {
        System.out.println("Getting credentials...");
        InputStream inputStream = GoogleDriveUploader.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (inputStream == null) {
            System.out.println("Resource file not found!");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(inputStream);
        String CLIENT_ID = jsonNode.get("installed").get("client_id").asText();
        String CLIENT_SECRET = jsonNode.get("installed").get("client_secret").asText();
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPES)
                .setAccessType("offline")
                .setDataStoreFactory(new com.google.api.client.util.store.FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .build();
        System.out.println("Credentials obtained.");
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    static {
        try {
            System.out.println("Initializing Google Drive service...");
            service = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    getCredentials())
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            System.out.println("Google Drive service initialized.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startMethod(String email,  boolean isDownloadMode,
                            double timeInterval, String downloadPath, List<String> selectedFiles) throws IOException {
        System.out.println("Starting method...");

        // Start the thread

        try {
            String folderId = null;
            folderId = getFolderIdByName(service,email);
           if (folderId == null){
               System.out.println("No folder found with emailId ");
               System.out.println("Creating new folder with emailId Started");
               folderId = createFolder( service, email, null);
           }





            if (folderId != null ){
            if (isDownloadMode) {
                System.out.println("Download mode enabled.");
                while (true) {
                for (String file : selectedFiles) {
                    int lastIndex = file.lastIndexOf("\\");
                    String fileName = file.substring(lastIndex + 1);
                    System.out.println("Downloading file: " + fileName);
                    downloadFileByName(service, fileName, folderId, downloadPath);
                }
                Thread.sleep( 60*60*(int) timeInterval * 1000); // 30 minutes in milliseconds
            }
            } else {
                System.out.println("Upload mode enabled.");
                while (true) {
                    for (String file : selectedFiles) {
                        int lastIndex = file.lastIndexOf("\\");
                        String fileName = file.substring(lastIndex + 1);

                        System.out.println("Updating file: " + fileName);
                        updateFile(service, fileName, folderId, file);
                        System.out.println("File updated on Google Drive.");

                    }

                    Thread.sleep(60*60* (int) timeInterval * 1000); // 30 minutes in milliseconds
                }
            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String uploadFile(Drive service,String folderId, String filePath) throws IOException {
        System.out.println("Uploading file: " + filePath);
        File fileMetadata = new File();
        fileMetadata.setName(Paths.get(filePath).getFileName().toString());
        fileMetadata.setParents(Collections.singletonList(folderId));
        java.io.File file = new java.io.File(filePath);
        Drive.Files.Create createRequest = service.files().create(
                fileMetadata,
                new com.google.api.client.http.FileContent(Files.probeContentType(file.toPath()), file));

        String fileId = createRequest.execute().getId();
        System.out.println("File uploaded with ID: " + fileId);
        return fileId;
    }

    private static void updateFile(Drive service, String fileName, String folderId, String filePath) throws IOException {
        System.out.println("Updating file: " + fileName);
        List<File> files = service.files().list()
                .setQ("name='" + fileName + "' and trashed=false and '" + folderId + "' in parents")
                .setFields("files(id, name)")
                .execute()
                .getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("File not found, uploading new file.");
            uploadFile(service,folderId, filePath);
            return;
        }
        String fileId = files.get(0).getId();
        java.io.File file = new java.io.File(filePath);
        Drive.Files.Update updateRequest = service.files().update(
                fileId,
                null,
                new com.google.api.client.http.FileContent(Files.probeContentType(file.toPath()), file));
        updateRequest.execute();
        System.out.println("File updated with ID: " + fileId);
    }

    private static void downloadFileByName(Drive service, String fileName, String folderId, String destinationPath) throws IOException {
        System.out.println("Downloading file: " + fileName);
        FileList result = service.files().list()
                .setQ("name='" + fileName + "' and trashed=false and '" + folderId + "' in parents")
                .setFields("files(id, name)")
                .execute();

        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("File not found: " + fileName);
            return;
        }

        File file = files.get(0);
        FileOutputStream outputStream = new FileOutputStream(destinationPath + "\\" + file.getName());
        service.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
        outputStream.close();
        System.out.println("File downloaded to: " + destinationPath);
    }
    private static String getFolderIdByName(Drive service, String folderName) throws IOException {
        FileList result = service.files().list()
                .setQ("name='" + folderName + "' and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setFields("files(id, name)")
                .execute();

        List<File> folders = result.getFiles();
        if (folders == null || folders.isEmpty()) {
            System.out.println("Folder not found: " + folderName);
            return null;
        }

        // Return the first matching folder's ID
        return folders.get(0).getId();
    }

    private static String createFolder(Drive service, String folderName, String parentFolderId) throws IOException {
        // Step 1: Check if the folder already exists
        FileList result = service.files().list()
                .setQ("name='" + folderName + "' and mimeType='application/vnd.google-apps.folder' and trashed=false" +
                        (parentFolderId != null ? " and '" + parentFolderId + "' in parents" : ""))
                .setFields("files(id, name)")
                .execute();

        List<File> folders = result.getFiles();
        if (folders != null && !folders.isEmpty()) {
            System.out.println("Folder already exists: " + folderName);
            return folders.get(0).getId(); // Return the existing folder's ID
        }

        // Step 2: Create a new folder
        File folderMetadata = new File();
        folderMetadata.setName(folderName);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");

        // If a parent folder is specified, set it
        if (parentFolderId != null) {
            folderMetadata.setParents(Collections.singletonList(parentFolderId));
        }

        File folder = service.files().create(folderMetadata)
                .setFields("id, name")
                .execute();

        System.out.println("Folder created: " + folder.getName() + " (ID: " + folder.getId() + ")");
        return folder.getId(); // Return the newly created folder's ID
    }

}
