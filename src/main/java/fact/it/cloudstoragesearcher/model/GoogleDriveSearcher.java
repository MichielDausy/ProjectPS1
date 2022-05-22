package fact.it.cloudstoragesearcher.model;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleDriveSearcher {

    private String fileID;
    private FileList result;
    private Drive service;
    private String mimeType;

    private static String getFilePath(Drive drive, File file) throws IOException {
        String folderPath = "";
        String fullFilePath = null;

        List<ParentReference> parentReferencesList = file.getParents();
        List<String> folderList = new ArrayList<String>();

        List<String> finalFolderList = getfoldersList(drive, parentReferencesList, folderList);
        Collections.reverse(finalFolderList);

        for (String folder : finalFolderList) {
            folderPath += "/" + folder;
        }

        fullFilePath = folderPath + "/" + file.getTitle();

        return fullFilePath;
    }

    private static List<String> getfoldersList(Drive drive, List<ParentReference> parentReferencesList, List<String> folderList) throws IOException {
        for (int i = 0; i < parentReferencesList.size(); i++) {
            String id = parentReferencesList.get(i).getId();

            File file = drive.files().get(id).execute();
            folderList.add(file.getTitle());

            if (!(file.getParents().isEmpty())) {
                List<ParentReference> parentReferenceslist2 = file.getParents();
                getfoldersList(drive, parentReferenceslist2, folderList);
            }
        }
        return folderList;
    }

    public GoogleDriveSearcher(Drive service) {
        this.result = null;
        this.service = service;
        this.fileID = null;
        this.mimeType = null;
    }

    public ArrayList<String> searchDriveFile(String fileName) throws IOException {

        ArrayList<String> bestanden = new ArrayList();

        String pageToken = null;
        do {
            result = service.files().list()
                    .setQ("title ='" + fileName + "'")
                    .setSpaces("drive")
                    //.setFields("nextPageToken, files(id, title, parents)")
                    .setPageToken(pageToken)
                    .execute();

            List<File> files = result.getItems();
            if (files.size() == 0) {
                System.out.printf("File: '%s', was not found.\n", fileName);
                System.out.println();

            } else {
                System.out.println("amount of files found: " + files.size() + "\n");
                for (File file : files) {
                    this.fileID = file.getId();
                    this.mimeType = file.getMimeType();
                    String filePath = null;
                    filePath = getFilePath(service, file);

                    System.out.println("name of file: " + file.getTitle());
                    System.out.println("path of file: " + filePath);
                    System.out.println();

                    //bestanden.add(file.getTitle());
                    bestanden.add(filePath);
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return bestanden;
    }
}
