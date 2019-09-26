package app.weasel.util;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Formatter;

import com.bumptech.glide.Glide;

import java.util.Date;
import java.util.Locale;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.databinding.BindingAdapter;
import app.weasel.R;
import app.weasel.WeaselApplication;
import app.weasel.model.DownloadItem;

public enum BindingUtils {
    ;

    /**
     * Helper method for binding album art  image to an {@link AppCompatImageView} for a
     * {@link app.weasel.model.LibraryItem} instance
     *
     * @param imageView .
     * @param img       .
     */
    @BindingAdapter({"albumArt"})
    public static void loadLibraryItemAlbumArt(AppCompatImageView imageView, byte[] img) {
        Glide.with(imageView.getContext())
            .load(img)
            .placeholder(R.drawable.ic_music_note)
            .into(imageView);
    }

    @BindingAdapter("duration")
    public static void setSongDuration(AppCompatTextView textView, long duration) {
        int minutes = (int) (duration / (float) (60000));
        int seconds = (int) ((duration % (float) (60000)) / 1000f);
        textView.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
    }

    @BindingAdapter("created")
    public static void setDateCreated(AppCompatTextView textView, Date date) {
        textView.setText(
            DateUtils.getRelativeDateTimeString(
                textView.getContext(),
                date.getTime(),
                DateUtils.SECOND_IN_MILLIS,
                0, 0
            )
        );
    }

    @BindingAdapter("fileSize")
    public static void formatFileSize(AppCompatTextView textView, long fileSize) {
        textView.setText(formatFileSize(fileSize));
    }

    /**
     * @param fileSize .
     * @return .
     * @see Formatter#formatFileSize(Context, long)
     */
    public static String formatFileSize(long fileSize) {
        return Formatter.formatFileSize(WeaselApplication.getInstance(), fileSize);
    }

    public static int itemProgress(DownloadItem item) {
        return (int) ((Math.max(0d, item.getDownloaded()) / Math.max(1d, item.getFileSize())) * 100d);
    }

    public static String itemProgressLabel(DownloadItem item) {
        return itemProgress(item) + "%";
    }
}
