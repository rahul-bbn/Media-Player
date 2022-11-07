package rahul.cyntech.mediaplayer;

import static rahul.cyntech.mediaplayer.VideosFileActivity.MY_PREF;
import static rahul.cyntech.mediaplayer.VideosFileActivity.videosFileAdapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PlayListDialog extends BottomSheetDialogFragment {
    ArrayList<MediaFiles> arrayList = new ArrayList<>();
    VideosFileAdapter videosFolderAdapter;
    BottomSheetDialog bottomSheetDialog;
    RecyclerView recyclerView;
    TextView folder;

    public PlayListDialog(ArrayList<MediaFiles> arrayList, VideosFileAdapter videosFolderAdapter) {
        this.arrayList = arrayList;
        this.videosFolderAdapter = videosFolderAdapter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.playlist_bs_layout, null);
        bottomSheetDialog.setContentView(view);
        recyclerView = view.findViewById(R.id.playlist_rb);
        folder = view.findViewById(R.id.playlist_name);

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
        String folderName = sharedPreferences.getString("PlaylistFolderName", "abc");

        folder.setText(folderName);
        arrayList = fetchMedia(folderName);

        videosFileAdapter = new VideosFileAdapter(arrayList, getContext(),1);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(videosFileAdapter);
        videosFileAdapter.notifyDataSetChanged();

        return bottomSheetDialog;
    }

    private ArrayList<MediaFiles> fetchMedia(String folderName) {


        ArrayList<MediaFiles> videoFile = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;


        String selection = MediaStore.Video.Media.DATA + " like?";
//        Toast.makeText(this, selection, Toast.LENGTH_LONG).show();
        String[] selectionArg = new String[]{"%" + folderName + "%"};
        @SuppressLint("Recycle") Cursor cursor = getContext().getContentResolver().query(uri, null, selection, selectionArg, null);
        if (cursor != null && cursor.moveToNext()) {
            do {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                @SuppressLint("Range") String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                @SuppressLint("Range") String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                @SuppressLint("Range") String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));

                MediaFiles mediaFiles = new MediaFiles(id, title, displayName, size, duration, path, dateAdded);
                videoFile.add(mediaFiles);

            } while (cursor.moveToNext());
        }
        return videoFile;
    }
}