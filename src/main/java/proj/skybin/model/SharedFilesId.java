package proj.skybin.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SharedFilesId implements Serializable {
    private String path;
    private String owner;
    private String sharedUser;

    // equals() and hashCode() methods

}