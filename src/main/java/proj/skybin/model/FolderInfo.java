package proj.skybin.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "folders")
public class FolderInfo {
    private String name;
    @Id
    @Column(name = "folderpath")
    private String path;
    private String owner;
    private String directory;
    private String type = "folder";
    private Boolean isDirectory = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "upload_date")
    private Date uploadDate;

    @ManyToOne
    @JoinColumn(name = "parentpath")
    @JsonBackReference
    private FolderInfo parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<FolderInfo> subfolders = new ArrayList<>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<FileInfo> files = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.uploadDate = new Date();
    }
}
