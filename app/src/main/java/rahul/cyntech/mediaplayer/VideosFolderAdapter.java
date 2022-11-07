package rahul.cyntech.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VideosFolderAdapter extends RecyclerView.Adapter<VideosFolderAdapter.ViewHolder> {
    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> folderPath;

    public VideosFolderAdapter(ArrayList<MediaFiles> mediaFiles, ArrayList<String> folderPath, Context context) {
        this.mediaFiles = mediaFiles;
        this.folderPath = folderPath;
        this.context = context;
    }

    private Context context;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.folder_item,parent,false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //  /storage/media/videos
        int indexPath = folderPath.get(position).lastIndexOf("/");
        String nameOFFolder = folderPath.get(position).substring(indexPath+1);
        holder.folderName.setText(nameOFFolder);
        holder.folderPath.setText(folderPath.get(position));
        holder.numberOfFiles.setText(numberOfFolders(folderPath.get(position))+"Videos");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,VideosFileActivity.class);
                intent.putExtra("fname",nameOFFolder);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return folderPath.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView folderName,folderPath,numberOfFiles;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            folderName= itemView.findViewById(R.id.folderName);
            folderPath = itemView.findViewById(R.id.folderPath);
            numberOfFiles= itemView.findViewById(R.id.noOfFiles);
        }
    }
    int numberOfFolders(String folder_name){
        int files_no = 0;
        for (MediaFiles mediaFiles : mediaFiles){
            if (mediaFiles.getPath().substring(0,mediaFiles.getPath().lastIndexOf("/")).endsWith(folder_name)){
                files_no++;
            }
        }
        return files_no;
    }
}
