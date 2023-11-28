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
    private ArrayList<String> sharedUsers;

    public DirectoryWrapper(FolderInfo folderInfo) {
        this.path = folderInfo.getPath();
        this.name = folderInfo.getName();
        this.owner = folderInfo.getOwner();
        this.directory = folderInfo.getDirectory();
        this.isDirectory = folderInfo.getIsDirectory();
        this.uploadDate = folderInfo.getUploadDate();
        this.contents = new ArrayList<>();
        this.sharedUsers = new ArrayList<>();
        if (folderInfo.getSubfolders() != null) {
            for (FolderInfo subfolder : folderInfo.getSubfolders()) {
                this.contents.add(new DirectoryWrapper(subfolder));
            }
        }
        if (folderInfo.getFiles() != null) {
            for (FileInfo file : folderInfo.getFiles()) {
                this.contents.add(new DirectoryWrapper(file));
            }
        }
    }

    public DirectoryWrapper(FileInfo fileInfo) {
        this.path = fileInfo.getPath();
        this.name = fileInfo.getName();
        this.owner = fileInfo.getOwner();
        this.directory = fileInfo.getDirectory();
        this.isDirectory = fileInfo.getIsDirectory();
        this.uploadDate = fileInfo.getUploadDate();
        this.contents = null;
        this.sharedUsers = new ArrayList<>();
        if (fileInfo.getSharedUsers() != null) {
            for (String user : fileInfo.getSharedUsers()) {
                this.sharedUsers.add(user);
            }
        }
    }
}
