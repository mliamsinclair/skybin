package proj.skybin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectoryWrapper {
    private String path;
    private String name;
    private String owner;
    private String directory;
    private Boolean isDirectory;
    private Date uploadDate;
    private ArrayList<DirectoryWrapper> contents;
}
