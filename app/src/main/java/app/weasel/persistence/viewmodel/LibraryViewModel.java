package app.weasel.persistence.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import app.weasel.model.LibraryItem;
import app.weasel.persistence.database.ApplicationDatabase;
import app.weasel.persistence.repository.LibraryRepository;
import app.weasel.persistence.repository.Repositories;

public final class LibraryViewModel extends BaseViewModel<LibraryItem> {
    public LibraryViewModel(@NonNull Application application) {
        super(
            Repositories.of(
                LibraryRepository.class,
                ApplicationDatabase.getInstance(application)
            ),
            application
        );
    }
}
