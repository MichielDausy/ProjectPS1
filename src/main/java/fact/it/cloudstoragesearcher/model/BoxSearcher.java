package fact.it.cloudstoragesearcher.model;

import com.box.sdk.*;

import java.util.ArrayList;

public class BoxSearcher {

    private BoxAPIConnection api;

    public BoxSearcher(BoxAPIConnection api) {
        this.api = api;
    }

    public ArrayList<String> searchBoxFile(String name) {

        ArrayList<String> files = new ArrayList();

        long offsetValue = 0;
        long limitValue = 100;
        BoxSearch boxSearch = new BoxSearch(api);
        BoxSearchParameters searchParams = new BoxSearchParameters();
        searchParams.setQuery(name);
        searchParams.setType("file");
        PartialCollection<BoxItem.Info> searchResults = boxSearch.searchRange(offsetValue, limitValue, searchParams);
        if (searchResults.size() == 0) {
            System.out.printf("File: '%s', was not found.\n", name);
        } else {
            System.out.println("amount of files found: " + searchResults.size() + "\n");
            String path = "";
            for (BoxItem.Info file : searchResults) {
                System.out.println("name of the file: " + file.getName());
                for (BoxFolder.Info folder : file.getPathCollection()) {
                    path += "/" + folder.getName();
                }
                path += "/" + file.getName();
                //files.add(file.getName());
                files.add(path);
            }
            System.out.println("path of the file: " + path);
        }
        return files;
    }
}
