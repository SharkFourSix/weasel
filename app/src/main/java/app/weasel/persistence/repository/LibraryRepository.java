package app.weasel.persistence.repository;

import app.weasel.model.LibraryItem;
import app.weasel.persistence.database.ApplicationDatabase;

public final class LibraryRepository extends Repository<LibraryItem> {

    public LibraryRepository(ApplicationDatabase database) {
        super(database.libraryDao());
    }
}
