package com.example.proyectointegradorfinalbootjob;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvInitials.setText(post.initials);
        holder.tvAuthor.setText(post.author);
        holder.tvCareer.setText(post.career + " · " + post.date);
        holder.tvCompanyBadge.setText(post.company);
        holder.tvTitle.setText(post.title);
        holder.tvContent.setText(post.content);
        holder.tvLikes.setText(post.likes + " Likes");
        holder.tvComments.setText(post.comments + " Comentarios");

        // Aquí puedes agregar lógica para cambiar los colores del badge dependiendo de la empresa
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvAuthor, tvCareer, tvCompanyBadge, tvTitle, tvContent, tvLikes, tvComments;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // Estos IDs deben coincidir con los de tu item_post.xml
            tvInitials = itemView.findViewById(R.id.tv_initials);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvCareer = itemView.findViewById(R.id.tv_career);
            tvCompanyBadge = itemView.findViewById(R.id.tv_company_badge);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvContent = itemView.findViewById(R.id.tv_post_content);
            tvLikes = itemView.findViewById(R.id.tv_likes);
            tvComments = itemView.findViewById(R.id.tv_comments);
        }
    }
}