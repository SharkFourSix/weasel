package app.weasel.model;

import java.io.Serializable;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.PrimaryKey;

public class Item extends BaseObservable implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long songId;
    private Date created;

    public final long getSongId() {
        return songId;
    }

    public final void setSongId(long songId) {
        this.songId = songId;
    }

    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    @Bindable
    public final Date getCreated() {
        return created;
    }

    public final void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public final boolean equals(@Nullable Object obj) {
        return (obj instanceof Item) && ((Item) obj).id == this.id;
    }

    @NonNull
    @Override
    public final String toString() {
        return "[id=" + id + ", songId=" + songId + "]";
    }
}
