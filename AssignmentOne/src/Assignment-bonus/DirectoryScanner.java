import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryScanner {
    public static List<File> getAllFiles(File directory) {
        List<File> fileList = new ArrayList<>();
        scanDirectory(directory, fileList);
        return fileList;
    }

    private static void scanDirectory(File directory, List<File> fileList) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, fileList);
                } else {
                    fileList.add(file);
                }
            }
        }
    }
}