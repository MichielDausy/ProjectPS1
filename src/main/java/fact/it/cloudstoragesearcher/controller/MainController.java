/*Michiel Dausy
r0900099*/
package fact.it.cloudstoragesearcher.controller;


import com.box.sdk.BoxAPIConnection;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import fact.it.cloudstoragesearcher.model.BoxSearcher;
import fact.it.cloudstoragesearcher.model.DropboxSearcher;
import fact.it.cloudstoragesearcher.model.GoogleDriveSearcher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class MainController {

    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    //src/main/java/fact/it/supermarket/controller/MainController.java
    //src/main/resources/credentials.json
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    //remove dropbox access token
    private static final String DROPBOX_ACCESS_TOKEN = "dev token";

    //remove Box access token
    private static final String BOX_ACCESS_TOKEN = "dev token";

    public MainController() throws IOException, GeneralSecurityException {
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = MainController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        return credential;
    }

    final NetHttpTransport HTTP_TRANSPORT= GoogleNetHttpTransport.newTrustedTransport();
    Drive googleDrive = new Drive.Builder(HTTP_TRANSPORT,JSON_FACTORY,getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();

    // Dropbox:
    DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/CloudSearcher").build();
    DbxClientV2 dropboxStorage = new DbxClientV2(config, DROPBOX_ACCESS_TOKEN);

    //Box:
    BoxAPIConnection boxApi = new BoxAPIConnection(BOX_ACCESS_TOKEN);

    // Create Dropbox client
    DropboxSearcher dropbox = new DropboxSearcher(dropboxStorage);

    // Create Google Drive client
    GoogleDriveSearcher drive = new GoogleDriveSearcher(googleDrive);

    // Create Box client
    BoxSearcher box = new BoxSearcher(boxApi);

    @RequestMapping("search")
    public String search(HttpServletRequest request, Model model) throws IOException, DbxException {

        String fileName = request.getParameter("file");

        ArrayList<String> listDropboxFiles = dropbox.searchDropboxFile(fileName);
        ArrayList<String> listDriveFiles = drive.searchDriveFile(fileName);
        ArrayList<String> listBoxFiles = box.searchBoxFile(fileName);

        model.addAttribute("dropboxFiles", listDropboxFiles);
        model.addAttribute("driveFiles", listDriveFiles);
        model.addAttribute("boxFiles", listBoxFiles);

        model.addAttribute("errormessage", "No file name was given");
        //return "error";
        return "foundFiles";
    }

}
