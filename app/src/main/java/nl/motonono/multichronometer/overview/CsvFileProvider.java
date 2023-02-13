package nl.motonono.multichronometer.overview;

import androidx.core.content.FileProvider;

import nl.motonono.multichronometer.R;

public class CsvFileProvider extends FileProvider {
    public CsvFileProvider() {
        super(R.xml.file_paths);
    }
}
