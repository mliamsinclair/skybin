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
    private String filepath;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "folderpath")
    private FolderInfo parent;

    private String parentpath;
    private String filename;
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
