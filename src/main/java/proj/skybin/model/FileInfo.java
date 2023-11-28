package proj.skybin.model;

import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "files")
public class FileInfo {
    private String name;
    @Id
    @Column(name = "filepath")
    private String path;

    @ManyToOne
    @JoinColumn(name = "folderpath", referencedColumnName = "folderpath")
    @JsonBackReference
    private FolderInfo parent;

    private String type = "file";
    private String parentpath;
    private Boolean isDirectory = false;
    private String owner;
    private String directory;
    private ArrayList<String> sharedUsers;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "upload_date")
    private Date uploadDate;

    @PrePersist
    protected void onCreate() {
        this.uploadDate = new Date();
    }
}
