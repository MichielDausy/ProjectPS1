package fact.it.cloudstoragesearcher.model;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.SearchMatchV2;
import com.dropbox.core.v2.files.SearchV2Result;

import java.util.ArrayList;
import java.util.List;

public class DropboxSearcher {

    private DbxClientV2 client;

    public DropboxSearcher(DbxClientV2 client) {
        this.client = client;
    }

    public ArrayList<String> searchDropboxFile(String name) throws DbxException {

        ArrayList<String> files = new ArrayList();

        SearchV2Result result = client.files().searchV2(name);
        List<SearchMatchV2> searchMatches = result.getMatches();

        if (searchMatches.size() == 0){
            System.out.printf("File: '%s', was not found.\n", name);
        } else {
            System.out.println("amount of files found: " + searchMatches.size() + "\n");

            for (SearchMatchV2 search : searchMatches) {
                if (search.getMetadata().isMetadata()) {
                    System.out.println("name of file: " + search.getMetadata().getMetadataValue().getName());
                    System.out.println("path of file: " + search.getMetadata().getMetadataValue().getPathDisplay());
                    System.out.println();
                    //files.add(search.getMetadata().getMetadataValue().getName());
                    files.add(search.getMetadata().getMetadataValue().getPathDisplay());
                }
            }
        }
        return files;
    }
}
