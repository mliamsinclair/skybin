package proj.skybin.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "files")
public class FileInfo {
    @Id
    @Column(name = "filepath")
    private String path;

    @ManyToOne
    @JoinColumn(name = "folderpath", referencedColumnName = "folderpath")
    @JsonBackReference
    private FolderInfo parent;

    private String type = "file";
    private String parentpath;
    private String name;
    private Boolean isDirectory;
    private String owner;
    private String directory;
    private String[] sharedUsers;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "upload_date")
    private Date uploadDate;

    @PrePersist
    protected void onCreate() {
        this.uploadDate = new Date();
    }
}
