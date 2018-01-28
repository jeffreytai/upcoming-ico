package com.crypto.authentication;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarAuthentication {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarAuthentication.class);

    /**
     * Name of application
     */
    private static final String APPLICATION_NAME = "upcoming-ico";

    /**
     * Global instance of JSON factory
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required for this application
     */
    private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR);

    /**
     * Directory to store user credentials for application
     */
    private static final File DATA_STORE_DIR = new File(
            System.getProperty("user.dir"), ".credentials/upcoming-ico");

    /**
     * Global instance of the FileDataStoreFactory
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the HTTP transport
     */
    private static HttpTransport HTTP_TRANSPORT;


    /************************
     * Constants
     ************************/

    /**
     * Required access type for API
     */
    private static final String ACCESS_TYPE = "offline";

    /**
     * File name that contains OAuth 2.0 credentials
     */
    private static final String PROPERTIES_FILE = "client_secret.json";

    /**
     * File name that contains the service account credentials
     */
    private static final String SERVICE_ACCOUNT_FILE = "service-account.json";

    /**
     * Google sheets service
     */
    private static com.google.api.services.calendar.Calendar calendar;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public GoogleCalendarAuthentication() {}

    public static Calendar getCalendarService() throws IOException {
        /**
         * Use this if the Google account can be changed to a different domain
         */
//        InputStream stream = GoogleCalendarAuthentication.class.getClassLoader().getResourceAsStream(SERVICE_ACCOUNT_FILE);
//
//        GoogleCredential credential = GoogleCredential.fromStream(stream).createScoped(Collections.singleton(CalendarScopes.CALENDAR));
//        calendar = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
//                .setApplicationName(APPLICATION_NAME)
//                .build();


        Credential credential = authorize();
        calendar = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        logger.info("Connected to Google Sheets service through OAuth authentication");
        return calendar;
    }

    private static Credential authorize() throws IOException {
        // Load client secrets
        InputStream stream = GoogleCalendarAuthentication.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(stream));

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType(ACCESS_TYPE)
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        logger.info("Credentials saved to {}", DATA_STORE_DIR.getAbsolutePath());

        return credential;
    }
}
