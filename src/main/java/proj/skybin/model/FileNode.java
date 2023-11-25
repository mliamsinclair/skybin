package proj.skybin.model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is for helping index a user's directory recursivly
 */
public class FileNode {
        public final String name;
        public final String path;
        private List<FileNode> contents = new ArrayList<>();

        private static String getRelativePath(File baseDir, File file) {
            Path basePath = baseDir.toPath();
            Path filePath = file.toPath();
    
            // Use the relativize method to get the relative path
            Path relativePath = basePath.relativize(filePath);
    
            // Convert the relative path to a string
            return relativePath.toString();
        }

        /**
         * @param name name of the directory
         * @param contents List of items in the directory
         */
        public FileNode(File root) {
            this.name = root.getName();
            this.path = getRelativePath(Paths.get(System.getProperty("user.dir"), "filedir").toFile(), root);

            if (root.isDirectory()){
                File[] files = root.listFiles();
                if (files != null)
                    for (File file : files){
                        this.contents.add(new FileNode(file));
                    }
            }
        }

        /**
         * returns the name of the FileNode
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         * returns the list of inner files of the folder
         * @return list of FileNodes
         */
        public List<FileNode> getContents() {
            return contents;
        }

        /**
         * adds a list of FileNodes
         * @param contents list of filenodes
         * @return list of filenodes
         */
        public List<FileNode> addContents(List<FileNode> contents) {
            this.contents.addAll(contents);
            return this.contents;
        }

        /**
         * 
         * @param contents FileNode to add
         * @return list of filenodes
         */
        public List<FileNode> addContents(FileNode contents) {
            this.contents.add(contents);
            return this.contents;
        }
    }
