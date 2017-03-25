package photato.core.entities;

import com.google.gson.annotations.Expose;
import java.nio.file.Path;
import java.util.Objects;

public abstract class PhotatoItem {

    public final Path fsPath;
    
    @Expose
    public final String filename;

    public PhotatoItem(Path rootFolder, Path path) {
        this.fsPath = path;
        this.filename = path.getFileName().toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.fsPath);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhotatoItem other = (PhotatoItem) obj;
        if (!Objects.equals(this.fsPath, other.fsPath)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.fsPath.toString();
    }

}
