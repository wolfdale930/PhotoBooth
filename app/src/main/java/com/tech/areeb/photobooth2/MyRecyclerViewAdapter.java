package com.tech.areeb.photobooth2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>{

    private Context context;
    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;
    private File file;

    private ArrayList<GalleryImages> galleryImages;

    MyRecyclerViewAdapter(Context context, ArrayList<GalleryImages> galleryImages){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.galleryImages = galleryImages;
        context.getCacheDir();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.grid_item_layout,parent,false);
        return new ViewHolder(view);

    }

    @Override
    public int getItemCount() {
        return galleryImages.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        file = new File(context.getCacheDir(), galleryImages.get(position).getImageFileName());
        if (file.exists()) {
            Picasso.with(context)
                    .load(file)
                    .into(holder.photo);
            holder.title.setText(galleryImages.get(position).getImageTitle());
            holder.descriptionBackground.setBackgroundColor(galleryImages.get(position).getImageRGB());
            holder.title.setTextColor(galleryImages.get(position).getImageTitleTextColor());
            holder.author.setTextColor(galleryImages.get(position).getImageBodyTextColor());
            holder.ratings.setRating(galleryImages.get(position).getImageRating());

        } else {
            Picasso.with(context)
                    .load(R.drawable.splash_logo)
                    .into(holder.photo);

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView photo;
        RelativeLayout descriptionBackground;
        TextView title,author;
        RatingBar ratings;


        ViewHolder(View itemView){
            super(itemView);
            photo = itemView.findViewById(R.id.photo);
            descriptionBackground = itemView.findViewById(R.id.photo_description_background);
            title = itemView.findViewById(R.id.photo_title);
            author = itemView.findViewById(R.id.photo_author);
            ratings = itemView.findViewById(R.id.ratings);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
