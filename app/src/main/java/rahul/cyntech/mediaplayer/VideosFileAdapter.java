package rahul.cyntech.mediaplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class VideosFileAdapter  extends RecyclerView.Adapter<VideosFileAdapter.ViewHolder> {

    private ArrayList<MediaFiles> videosList;
    private Context context;

    ////// Bottom Sheet
    BottomSheetDialog bottomSheetDialog ;
    private int viewType;


    public VideosFileAdapter(ArrayList<MediaFiles> videosList, Context context, int viewType) {
        this.videosList = videosList;
        this.context = context;
        this.viewType = viewType;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        ///////  It's Use to Show Name Of Video to Video List Item Icon
        holder.videoName.setText(videosList.get(position).getDisplayName());
        ///////  It's Use to Show Name Of Video to Video List Item Icon


        /////  It's Use to Show Size Of Video(in MB or GB) to Video List Item Icon ///////
        String size = videosList.get(position).getSize();
        holder.videoSize.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(size)));
        /////  It's Use to Show Size Of Video(in MB or GB) to Video List Item Icon///////


        /////  It's Use to Show Duration Of Video to Video List Item Icon///////
        double milliseconds = Double.parseDouble(videosList.get(position).getDuration());
        holder.videoDuration.setText(timeConversion((long) milliseconds));
        /////  It's Use to Show Duration Of Video to Video List Item Icon///////

        /////  It's Use to Show Video Image to Video List Item Icon///////
        Glide.with(context).load(new File (videosList.get(position).getPath())).into(holder.thumbnail);
        ///// It's Use to Show Video Image to Video List Item Icon///////

        if (viewType == 0){

            //////// It's call When We clicked on Menu Icon
            holder.menu_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    bottomSheetDialog= new BottomSheetDialog(context,R.style.BottomSheetTheme);
                    View bsView = LayoutInflater.from(context).inflate(R.layout.video_bs_layout,v.findViewById(R.id.bottom_sheet));

                    bsView.findViewById(R.id.bs_play).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            holder.itemView.performClick();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    /////////Not Working
                    bsView.findViewById(R.id.bs_rename).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder alertDialog= new AlertDialog.Builder(context);
                            alertDialog.setTitle("Rename to");
                            EditText editText = new EditText(context);
                            String path = videosList.get(position).getPath();
                            final File file = new File(path);
                            String videoName = file.getName();
                            videoName = videoName.substring(0, videoName.lastIndexOf("."));
                            editText.setText(videoName);
                            alertDialog.setView(editText);
                            editText.requestFocus();


                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (TextUtils.isEmpty(editText.getText().toString())){
                                        Toast.makeText(context, "Can't Rename Empty file", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    String onlyPath = Objects.requireNonNull(file.getParentFile()).getAbsolutePath();
                                    String ext = file.getAbsolutePath();
                                    ext = ext.substring(ext.lastIndexOf("."));

                                    //////Media/Video/abc.mp4
                                    String newPath = onlyPath + "/" + editText.getText().toString() + ext;
                                    File newFile = new File(newPath);
                                    boolean rename = file.renameTo(newFile);

                                    if (rename){
                                        Toast.makeText(context,"Process Failed3",Toast.LENGTH_SHORT).show();
                                        ContentResolver resolver =context.getApplicationContext().getContentResolver();
                                        resolver.delete(MediaStore.Files.getContentUri("external"),
                                                MediaStore.MediaColumns.DATA+ "=?",new String[]{file.getAbsolutePath()});
                                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        intent.setData(Uri.fromFile(newFile));
                                        context.getApplicationContext().sendBroadcast(intent);
                                        notifyDataSetChanged();
                                        Toast.makeText(context,"Video Renamed",Toast.LENGTH_SHORT).show();
                                        SystemClock.sleep(200);
                                        ((Activity)context).recreate();
                                    }else{
                                        Toast.makeText(context,"Process Failed",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.create().show();
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bsView.findViewById(R.id.bs_share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse(videosList.get(position).getPath());
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("video/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            context.startActivity(Intent.createChooser(shareIntent,"share video via "));
                            bottomSheetDialog.dismiss();
                        }
                    });

                    ///////Not Working
                    bsView.findViewById(R.id.bs_delete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Delete");
                            alertDialog.setMessage("Do you want to delete this Video");
                            alertDialog.setPositiveButton("delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.parseLong(videosList.get(position).getId()));
                                    File file = new File(videosList.get(position).getPath());
                                    boolean delete = file.delete();
                                    if (delete){
                                        context.getContentResolver().delete(contentUri,null,null);
                                        videosList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position,videosList.size());
                                        Toast.makeText(context, "Video Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(context, "Can't Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.show();
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bsView.findViewById(R.id.bs_properties).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder  alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Properties");
                            String one = "File : "+videosList.get(position).getDisplayName();
                            String path = videosList.get(position).getPath();
                            int indexOfPath = path.lastIndexOf("/");
                            String two = "Path : "+path.substring(0,indexOfPath);
                            String three = "Size : "+android.text.format.Formatter.formatFileSize(context, Long.parseLong(videosList.get(position).getSize()));
                            String fourth = "Length : "+timeConversion( (long) milliseconds);
                            String nameWithFormate = videosList.get(position).getDisplayName();
                            int index = nameWithFormate.lastIndexOf(".");
                            String formate = nameWithFormate.substring(index+1);
                            String five = "formate : "+formate;
                            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                            mediaMetadataRetriever.setDataSource(videosList.get(position).getPath());
                            String height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                            String width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                            String six = "Resolution : "+width+"x"+height;


                            alertDialog.setMessage(one+"\n\n"+two+"\n\n"+three+"\n\n"+fourth+"\n\n"+five+"\n\n"+six);
                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.show();
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bottomSheetDialog.setContentView(bsView);
                    bottomSheetDialog.show();
                }
            });
            //////// It's call When We clicked on Menu Icon

        }else{
            holder.menu_more.setVisibility(View.GONE);
            holder.videoName.setTextColor(Color.WHITE);
            holder.videoSize.setTextColor(Color.WHITE);
        }


        //////// It's call When We clicked on Folder in MainActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( context,VideoPlayerActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("video_title",videosList.get(position).getDisplayName());

               //////////////////////// /// pass value for VideoPlayerActivity
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("videoArrayList",videosList);
                intent.putExtras(bundle);
                //////////////////////// /// pass value for VideoPlayerActivity

                context.startActivity(intent);
                if (viewType==1){
                    ((Activity) context).finish();
                }
            }
        });
        //////// It's call When We clicked on Folder in MainActivity
    }

    @Override
    public int getItemCount() {
        return videosList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail,menu_more;
        TextView videoName,videoSize,videoDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail= itemView.findViewById(R.id.thumbnail);
            menu_more= itemView.findViewById(R.id.video_menu_more);
            videoName= itemView.findViewById(R.id.video_name);
            videoSize= itemView.findViewById(R.id.video_size);
            videoDuration= itemView.findViewById(R.id.video_duration);

        }
    }

    public String timeConversion(long value){
        String videoTime ;
        int duration = (int) value;
        int hrs =(duration/3600000);
        int mns = (duration/60000)%60000;
        int scs = duration%60000/1000;
        if (hrs > 0) {
            videoTime = String.format("%02d:%02d:%02d",hrs,mns,scs);
        }else{
            videoTime = String.format("%02d:%02d",mns,scs);

        }
        return videoTime;

    }

    void updateVideoFiles(ArrayList<MediaFiles> files ){
        videosList = new ArrayList<>();
        videosList.addAll(files);
        notifyDataSetChanged();
    }
}
