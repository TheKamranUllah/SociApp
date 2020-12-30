package com.example.sociapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    List<Comments> mList;
    List<String> commentsKeys;
    Context context;
    FirebaseAuth mAuth;
    DatabaseReference commentRef;
    String currentUserId;
    String CommentPostKey;


    public CommentsAdapter(String postKey, List<String>commentKeys, List<Comments> mList, Context context) {
        this.mList = mList;
        this.context = context;
       this.commentsKeys = commentKeys;
       this.CommentPostKey = postKey;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        mAuth = FirebaseAuth.getInstance();
        View view = LayoutInflater.from(context).inflate(R.layout.all_comments_layout, parent, false);
       return  new CommentsViewHolder(view);

            }

    @Override
    public void onBindViewHolder(@NonNull final CommentsViewHolder holder, final int position) {

        currentUserId = mAuth.getCurrentUser().getUid();
      final String TheCommentPostKey = CommentPostKey;

        final Comments comments = mList.get(position);
        final String currentComment = commentsKeys.get(position);
        final String userComment = comments.getComment();

        Picasso.get().load(comments.getProfileimage()).placeholder(R.drawable.profile).into(holder.commentProfilePic);
        holder.Username.setText("@" + comments.getUsername());
        holder.Date.setText("Date:" + comments.getDate());
        holder.Time.setText("Time:" + comments.getTime());
        holder.Comment.setText(comments.getComment());
        final String commentedUerId = comments.getUid();

        commentRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(TheCommentPostKey).child("Comments")
                .child(currentComment);

//        final View mview = LayoutInflater.from(context).inflate(R.layout.dialog_no_btn_layout, null);
//        final TextView message1 = mview.findViewById(R.id.dialog_option_one);
//        final TextView message2 = mview.findViewById(R.id.dialog_option_two);
//        final ImageView imageView = mview.findViewById(R.id.dialog_Image);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {


               if (currentUserId.equals(commentedUerId)) {
                   Intent CommentdeletionIntent = new Intent(context, CommentDeletionActivity.class);
                   CommentdeletionIntent.putExtra("PostKey", TheCommentPostKey);
                   CommentdeletionIntent.putExtra("CommentId", currentComment);
                   CommentdeletionIntent.putExtra("CommentUId", commentedUerId);
                   CommentdeletionIntent.putExtra("userComment", userComment);
                   context.startActivity(CommentdeletionIntent);
                   ((CommentsActivity) context).finish();
               }
               else
                   {
                       Toast.makeText(context, "Can't be modified cause, this is not your comment!", Toast.LENGTH_SHORT).show();
                   }

/*
                if (currentUserId.equals(commentedUerId)) {

                    imageView.setImageResource(R.drawable.process);
                    final AlertDialog.Builder mbuilder = new AlertDialog.Builder(context, R.style.mydialog);
                    mbuilder.setView(mview);

                    final Dialog dialog = mbuilder.create();

                    message1.setText("Comment Status");
                    message2.setText("Delete Comment");

                    message1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Toast.makeText(context, "Comment was added from your account!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            //The below code remove parentView the view we click once, if we don't add that the alert
                            //dialoge won't appear next time we click in chat activity and will throuw exception.
                            ((ViewGroup) mview.getParent()).removeView(mview);

                        }
                    });

                    message2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            commentRef.removeValue();

                            notifyItemRemoved(position);
                            // notifyItemRangeChanged(holder.getAdapterPosition(), mList.size());
                            dialog.dismiss();
                            notifyDataSetChanged();
                            Toast.makeText(context, "Comment Deleted!", Toast.LENGTH_SHORT).show();
                            ((ViewGroup) mview.getParent()).removeView(mview);
                            getItemId(position);
                        }
                    });

                    try {
                        dialog.show();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    Toast.makeText(context, "You can't delete someone else message", Toast.LENGTH_SHORT).show();
                }
*/

            }
        });

    }

    @Override
    public long getItemId(int position) {

        try {

            mList.remove(position);
            notifyItemRemoved(position);

        }catch (IllegalStateException e){e.printStackTrace();}

        return super.getItemId(position);
    }


    @Override
    public int getItemCount() {
        if(mList != null) {
            return mList.size();
        }
        Toast.makeText(context, "List is null", Toast.LENGTH_SHORT).show();
        return 0;
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder{


        CircleImageView commentProfilePic;
        TextView Comment,Date,Time,Username;

        public CommentsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            commentProfilePic = itemView.findViewById(R.id.comment_profile_pic);
            Comment = itemView.findViewById(R.id.comment_text);
            Date = itemView.findViewById(R.id.comment_date);
            Time = itemView.findViewById(R.id.comment_time);
            Username = itemView.findViewById(R.id.comment_username);
        }
    }
}
