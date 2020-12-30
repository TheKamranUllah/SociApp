package com.example.sociapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.ProfilePostViewHolder>{

    List<Posts> ProfilePostsList;
    Context context;
    List<String> ProfileKeys;

    private FirebaseAuth mAuth;
    private DatabaseReference LikesRef,ShareRef,CommentsRef;
    private String CurrentUserID,GlobalPostKey;

    private boolean LikeChecker =false;
    private boolean ShareChecker = false;


    public ProfilePostAdapter(List<Posts> profilePostsList,List<String> Fkeys, Context context) {
        ProfilePostsList = profilePostsList;
        this.context = context;
        ProfileKeys = Fkeys;
    }

    @NonNull
    @Override
    public ProfilePostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_posts_layout, parent, false);
        return new ProfilePostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePostViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        final String PostKey = ProfileKeys.get(position);
        GlobalPostKey = PostKey;

        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        ShareRef = FirebaseDatabase.getInstance().getReference().child("Shares");
        CommentsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);


        Posts posts = ProfilePostsList.get(position);
        Picasso.get().load(posts.getPostimage()).placeholder(R.drawable.photo).into(holder.PostImage);
        Picasso.get().load(posts.getProfileimage()).fit().into(holder.PostProfileimage);
        holder.PostUserName.setText(posts.getFullname());
        holder.PostDate.setText(posts.getDate());
        holder.PostTime.setText(posts.getTime());
        holder.PostDescription.setText(posts.getDescription());

        holder.setLikesButtonStatus(PostKey);
        holder.setCommentButtonStatus();
        holder.setShareButtonStatus(PostKey);

        //Here I get description and image for share button,which I implement below of comment button.
        final String postDescription = posts.getDescription();
        final String postImage = posts.getPostimage();

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent clickpostintent = new Intent(context, ClickPostActivity.class);
                clickpostintent.putExtra("PostKey", PostKey);
                context.startActivity(clickpostintent);
                //String mykey = keyList.get(position);
                //  Toast.makeText(context, ""+mykey, Toast.LENGTH_LONG).show();

                return true;
            }
        });

        holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentsIntent = new Intent(context, CommentsActivity.class);
                commentsIntent.putExtra("PostKey", PostKey);
                context.startActivity(commentsIntent);
            }
        });

        holder.LikePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikeChecker = true;

                LikesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (LikeChecker == true)
                        {
                            if (dataSnapshot.child(PostKey).hasChild(CurrentUserID))
                            {
                                LikesRef.child(PostKey).child(CurrentUserID).removeValue();
                                LikeChecker = false;
                            }
                            else {
                                LikesRef.child(PostKey).child(CurrentUserID).setValue(true);
                                LikeChecker = false;
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        holder.SharePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ShareChecker = true;

//The below code was to check that, we are getting image link and image description or not here inside.
                // Toast.makeText(context, ""+postImage, Toast.LENGTH_SHORT).show();
                // Toast.makeText(context, ""+postDescription, Toast.LENGTH_SHORT).show();

                ShareRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (ShareChecker == true) {
                            if (dataSnapshot.child(PostKey).hasChild(CurrentUserID)) {
                                Toast.makeText(context, "You have shared once.", Toast.LENGTH_SHORT).show();
                                // ShareRef.child(PostKey).child(CurrentUserID).removeValue();
                                ShareChecker = false;
                            } else {
                                ShareRef.child(PostKey).child(CurrentUserID).setValue(true);
                                ShareChecker = false;
                            }
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                Uri imgUri = Uri.parse(postImage);
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, postDescription );
                whatsappIntent.putExtra(Intent.EXTRA_STREAM, imgUri);
                whatsappIntent.setType("image/jpeg");
                whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                try {
                    context.startActivity(whatsappIntent);
                } catch (android.content.ActivityNotFoundException ex) {

                    Toast.makeText(context, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public int getItemCount() {

        return ProfilePostsList.size();
    }

    public class ProfilePostViewHolder extends RecyclerView.ViewHolder
    {


        ImageView PostImage;
        CircleImageView PostProfileimage;
        TextView PostUserName;
        TextView PostDate;
        TextView PostTime;
        TextView PostDescription;

        ImageButton LikePostButton, CommentPostButton,SharePostButton;
        TextView DisplayNoOfLikes;
        TextView DisplayNoOfComments;
        TextView DisplayNoOfShare;

        int countComments, countShares;
        int countLikes;

        public ProfilePostViewHolder(@NonNull View itemView)
        {
            super(itemView);

            PostImage = itemView.findViewById(R.id.post_image);
            PostProfileimage = itemView.findViewById(R.id.post_profile_image);
            PostUserName = itemView.findViewById(R.id.post_user_name);
            PostDate = itemView.findViewById(R.id.post_date);
            PostTime = itemView.findViewById(R.id.post_time);
            PostDescription = itemView.findViewById(R.id.post_description);
            LikePostButton = itemView.findViewById(R.id.like_button);
            CommentPostButton = itemView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = itemView.findViewById(R.id.display_no_of_likes);
            SharePostButton = itemView.findViewById(R.id.share_button);
            DisplayNoOfLikes = itemView.findViewById(R.id.display_no_of_likes);
            DisplayNoOfComments = itemView.findViewById(R.id.display_no_of_comments);
            DisplayNoOfShare = itemView.findViewById(R.id.display_no_of_share);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
try {
    CommentsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(GlobalPostKey);
    CurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
}
  catch (NullPointerException e)
 {
    e.printStackTrace();
 }
        }

        public void setLikesButtonStatus(final String PostKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

    if (dataSnapshot.child(PostKey).hasChild(CurrentUserID))
    {
        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
        LikePostButton.setImageResource(R.drawable.like);
        DisplayNoOfLikes.setText((Integer.toString(countLikes) +(" Likes")));
    }
    else
    {
        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
        LikePostButton.setImageResource(R.drawable.dislike);
        DisplayNoOfLikes.setText((Integer.toString(countLikes) +(" Likes")));
    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setCommentButtonStatus() {

            CommentsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Comments")) {
                        countComments = (int) dataSnapshot.child("Comments").getChildrenCount();
                        DisplayNoOfComments.setText(Integer.toString(countComments) + " Comments");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setShareButtonStatus(final String PostKey) {

            ShareRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(PostKey).exists()) {
                        countShares = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        DisplayNoOfShare.setText(Integer.toString(countShares) + " Shares");
                    } else {

                        DisplayNoOfShare.setText("0 Shares");

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}
