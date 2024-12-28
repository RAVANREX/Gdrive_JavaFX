/**
 *
 */
// module com.gdrive {
//     requires com.google.api.client;
//     requires com.google.api.services.drive;
//     requires com.google.http.client;
//     requires com.google.api.client.json.jackson2;
//     requires com.google.api.client.extensions.java6;
//     requires com.google.api.client.googleapis.javanet;
//     requires com.google.api.client.extensions.jetty.auth;
//     requires java.net.http;
//     requires java.logging;
//     requires javafx.controls;
//     requires javafx.fxml;
//     requires javafx.media;
//     requires com.fasterxml.jackson.databind;
//     requires com.fasterxml.jackson.annotation;
//     //opens com.gdrive to javafx.fxml;
//     opens com.gdrive to javafx.fxml, com.fasterxml.jackson.databind;
//     exports com.gdrive;
// }
module com.gdrive {
    
    // requires com.google.api.services.drive;
    // requires com.google.api.client.googleapis.auth.oauth2;
    // requires com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
    // requires com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
    // requires com.google.api.client.googleapis.javanet;
    // requires com.google.api.services.drive;
    // requires com.google.api.client.auth.oauth2;
    // requires com.google.api.client.googleapis.auth;
    //requires google.http.client;
    //requires google.api.services.drive;
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires google.api.client;
    requires com.google.api.client.json.jackson2;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    //requires java.net.http;
    //requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires google.api.services.drive.v3.rev197;
   // requires transitive javafx.graphics;
    requires jdk.httpserver;
    // 
    requires com.google.api.client.json.gson;
    //requires jfxrt;
    //requires rt;
    //requires jfxrt;


    opens com.gdrive to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.gdrive;
}