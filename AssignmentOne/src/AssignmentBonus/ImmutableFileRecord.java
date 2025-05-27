package AssignmentBonus;

import java.io.File;

public final class ImmutableFileRecord {
    private final File file;
    private final boolean isPDF;

    public ImmutableFileRecord(File file) {
        this.file = file;
        this.isPDF = file.getName().toLowerCase().endsWith(".pdf");
    }

    public File getFile() {
        return file;
    }

    public boolean isPDF() {
        return isPDF;
    }
}
