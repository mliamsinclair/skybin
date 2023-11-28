package proj.skybin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(SharedFilesId.class)
@Entity
@Table(name = "shared")
public class SharedFiles {
    @Id
    private String path;
    @Id
    private String owner;
    @Id
    private String sharedUser;

    private String name;
}
