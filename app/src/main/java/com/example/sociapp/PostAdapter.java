package com.example.sociapp;

import android.content.ContentValues;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.sip.SipSession;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.health.UidHealthStats;
import android.provider.MediaStore;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.snapshot.ChildKey;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Ref;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private FirebaseAuth mAuth;
    private DatabaseReference LikesRef, CommentsRef, ShareRef;

    DatabaseReference SCommentRef, SLikesRef, SshareRef;

    private boolean LikeChecker = false;
    private boolean ShareChecker = false;

    private static int TYPE_POST_PHOTO = 0;
    private static int TYPE_TEXT = 1;

    List<String> keyList;
    List<Posts> List;
    Context context;
    private String CurrentUserID, GlobalPostKey;
    private String statusUserID, tobevisitprofileId;


    public PostAdapter(List<Posts> list, List<String> keys, Context context) {
        List = list;
        keyList = keys;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {

        if (List.get(position).getType().equals("Photo"))
        {
            return TYPE_POST_PHOTO;
        }
        else
            {
                return TYPE_TEXT;
            }

        //return super.getItemViewType(position);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        mAuth = FirebaseAuth.getInstance();

        View view = null;

        if (viewType == TYPE_POST_PHOTO)
        {
try {
    view = LayoutInflater.from(context).inflate(R.layout.all_posts_layout, parent, false);
}catch (InflateException e){e.printStackTrace();}
          try {
              return new PostAdapter.viewHolder(view);
          }catch (NullPointerException e){e.printStackTrace();}

        return null;
        }
        else
        {
            view = LayoutInflater.from(context).inflate(R.layout.all_user_status_layout, parent, false);
            return new PostAdapter.sviewHolder(view);

        }
    }
 //MOST IMPORTANT:
//Sometime to get exact key you need to make final Viewholder and position,
// like I did for visiting profile from news feed. and deleting an item.

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {


        final String PostKey = keyList.get(position);
try {
    CurrentUserID = mAuth.getCurrentUser().getUid();
}
catch (NullPointerException e){e.printStackTrace();}

        if (getItemViewType(position) == TYPE_POST_PHOTO)
        {

//            AdapterAsyncTask myTask = new AdapterAsyncTask((viewHolder) holder, List.get(position), keyList.get(position),"photo");
//            myTask.execute(position);
            ((viewHolder)holder).photoPostMethod(List.get(holder.getAdapterPosition()),  keyList.get(position));
        } else {

//            AdapterAsyncTask myTask = new AdapterAsyncTask((sviewHolder) holder, List.get(position), keyList.get(position),"status");
//             myTask.execute(position);
             // new AdapterAsyncTask().execute(position);
              ((sviewHolder) holder).statusPostMethod(List.get(holder.getAdapterPosition()), keyList.get(position));

        }

        GlobalPostKey = PostKey;
        //The below code fetch the id of the user not post.
        // final String UserId = List.get(position).getUid();

    }


    @Override
    public int getItemCount() {
        return List.size();
    }



    public class viewHolder extends RecyclerView.ViewHolder {

        ImageView PostImage;
        CircleImageView PostProfileimage;
        TextView PostUserName;
        TextView PostDate;
        TextView PostTime;
        TextView PostDescription;
        ImageButton LikePostButton, CommentPostButton, SharePostButton;
        TextView DisplayNoOfLikes;
        TextView DisplayNoOfComments;
        TextView DisplayNoOfShare;

        int countLikes, countComments, countShares;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            PostImage = itemView.findViewById(R.id.post_image);
            PostProfileimage = itemView.findViewById(R.id.post_profile_image);
            PostUserName = itemView.findViewById(R.id.post_user_name);
            PostDate = itemView.findViewById(R.id.post_date);
            PostTime = itemView.findViewById(R.id.post_time);
            PostDescription = itemView.findViewById(R.id.post_description);
            LikePostButton = itemView.findViewById(R.id.like_button);
            CommentPostButton = itemView.findViewById(R.id.comment_button);
            SharePostButton = itemView.findViewById(R.id.share_button);
            DisplayNoOfLikes = itemView.findViewById(R.id.display_no_of_likes);
            DisplayNoOfComments = itemView.findViewById(R.id.display_no_of_comments);
            DisplayNoOfShare = itemView.findViewById(R.id.display_no_of_share);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
          /*  try {
                CommentsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(GlobalPostKey);

                mAuth = FirebaseAuth.getInstance();
                CurrentUserID = mAuth.getCurrentUser().getUid();

            } catch (NullPointerException e) {
                e.printStackTrace();
            }*/

            //this toast was to check that we can access the poskey here or not.
            // Toast.makeText(context, ""+GlobalPostKey, Toast.LENGTH_SHORT).show();

        }


        public void setLikesButtonStatus(final String PostKey) {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.child(PostKey).hasChild(CurrentUserID)) {
                            countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                            LikePostButton.setImageResource(R.drawable.like);
                            DisplayNoOfLikes.setText((Integer.toString(countLikes) + (" Likes")));
                        } else {
                            countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                            LikePostButton.setImageResource(R.drawable.dislike);
                            DisplayNoOfLikes.setText((Integer.toString(countLikes) + (" Likes")));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
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

        public void photoPostMethod(final Posts posts, final String myPostskey)
        {
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            CommentsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(myPostskey);
            ShareRef = FirebaseDatabase.getInstance().getReference().child("Shares");

            Picasso.get().load(posts.getPostimage()).placeholder(R.drawable.photo).into(PostImage);
            Picasso.get().load(posts.getProfileimage()).fit().into(PostProfileimage);
            PostUserName.setText(posts.getFullname());
            PostDate.setText(posts.getDate());
            PostTime.setText(posts.getTime());
            PostDescription.setText(posts.getDescription());

           tobevisitprofileId = posts.getUid();



            PostUserName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent visitprofileIntent = new Intent(context, ProfileActivity.class);
                    visitprofileIntent.putExtra("currentprofileid", tobevisitprofileId);
                    context.startActivity(visitprofileIntent);

                }
            });



            setLikesButtonStatus(myPostskey);
            setCommentButtonStatus();
            setShareButtonStatus(myPostskey);
//Here I get description and image for share button,which I implement below of comment button.
            final String postDescription = posts.getDescription();
            final String postImage = posts.getPostimage();


//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//
//
//                return true;
//            }
//        });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent clickpostintent = new Intent(context, ClickPostActivity.class);
                    clickpostintent.putExtra("PostKey", myPostskey);
                    context.startActivity(clickpostintent);
                    //String mykey = keyList.get(position);
                    //  Toast.makeText(context, ""+mykey, Toast.LENGTH_LONG).show();
                }
            });


            CommentPostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent commentsIntent = new Intent(context, CommentsActivity.class);
                    commentsIntent.putExtra("PostKey", myPostskey);
                    context.startActivity(commentsIntent);
                }
            });

            LikePostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LikeChecker = true;

                    LikesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (LikeChecker == true) {
                                if (dataSnapshot.child(myPostskey).hasChild(CurrentUserID)) {
                                    LikesRef.child(myPostskey).child(CurrentUserID).removeValue();
                                    LikeChecker = false;
                                } else {
                                    LikesRef.child(myPostskey).child(CurrentUserID).setValue(true);
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
           SharePostButton.setOnClickListener(new View.OnClickListener() {
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
                                if (dataSnapshot.child(myPostskey).hasChild(CurrentUserID)) {
                                    Toast.makeText(context, "You have shared once.", Toast.LENGTH_SHORT).show();
                                    // ShareRef.child(PostKey).child(CurrentUserID).removeValue();
                                    ShareChecker = false;
                                } else {
                                    ShareRef.child(myPostskey).child(CurrentUserID).setValue(true);
                                    ShareChecker = false;
                                }
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    share(PostImage, postDescription);
                }
            });
        }

        public Bitmap capture(View view) {
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            return bitmap;
        }

        private void share(ImageView view, String postDescription) {
            Context context = view.getContext();
            Bitmap bitmap = capture(view);
            try {
                File file = new File(context.getExternalCacheDir(), SimpleDateFormat.DEFAULT + ".png");
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/png");
                i.putExtra(Intent.EXTRA_TEXT, postDescription);
                Uri uri = Uri.fromFile(file);
                i.putExtra(Intent.EXTRA_STREAM, uri);
                context.startActivity(Intent.createChooser(i, "Share"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class sviewHolder extends RecyclerView.ViewHolder{

        private CircleImageView Profileimage;
        private TextView FullName;
        private TextView Date,Time;
        private TextView UserStatus;

        ImageButton LikeBtn,CommentBtn,ShareBtn;
        TextView   DisplayStatusLikes,DisplayStatusComments, DisplayStatusShare;

        int countLikes , countComments, countShares;

        public sviewHolder(@NonNull View itemView)
        {
            super(itemView);

            Profileimage = itemView.findViewById(R.id.status_profile_image);
            FullName = itemView.findViewById(R.id.status_user_name);
            Date = itemView.findViewById(R.id.status_date);
            Time = itemView.findViewById(R.id.status_time);
            UserStatus = itemView.findViewById(R.id.all_user_status);

            LikeBtn = itemView.findViewById(R.id.status_like_button);
            CommentBtn = itemView.findViewById(R.id.status_comment_button);
            ShareBtn = itemView.findViewById(R.id.status_share_button);

            DisplayStatusLikes = itemView.findViewById(R.id.status_display_no_of_likes);
            DisplayStatusComments = itemView.findViewById(R.id.status_display_no_of_comments);
            DisplayStatusShare = itemView.findViewById(R.id.status_display_no_of_share);


        }



        public void setStatusLikesButtonStatus(final String statusId)
        {
            SLikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.child(statusId).hasChild(CurrentUserID)) {
                            countLikes = (int) dataSnapshot.child(statusId).getChildrenCount();
                            LikeBtn.setImageResource(R.drawable.like);
                            DisplayStatusLikes.setText((Integer.toString(countLikes) + (" Likes")));
                        } else {
                            countLikes = (int) dataSnapshot.child(statusId).getChildrenCount();
                            LikeBtn.setImageResource(R.drawable.dislike);
                            DisplayStatusLikes.setText((Integer.toString(countLikes) + (" Likes")));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setCommentStatusButtonStatus() {

            SCommentRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Comments")) {
                        countComments = (int) dataSnapshot.child("Comments").getChildrenCount();
                        DisplayStatusComments.setText(Integer.toString(countComments) + " Comments");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setStatusShareButtonStatus(final String statusId) {

            SshareRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(statusId).exists()) {
                        countShares = (int) dataSnapshot.child(statusId).getChildrenCount();
                        DisplayStatusShare.setText(Integer.toString(countShares) + " Shares");
                    } else {

                        DisplayStatusShare.setText("0 Shares");

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }




        public void statusPostMethod(final Posts status, final String statusPostKey)
        {
          final String CurrentUserId = mAuth.getCurrentUser().getUid();

            SCommentRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(statusPostKey);
            SLikesRef = FirebaseDatabase.getInstance().getReference().child("StatLikes");
            SshareRef = FirebaseDatabase.getInstance().getReference().child("StatShares");


            Picasso.get().load(status.getProfileimage()).placeholder(R.drawable.profile).into(Profileimage);
            FullName.setText(status.getFullname());
            Date.setText(status.getDate());
            Time.setText(status.getTime());
            UserStatus.setText(status.getUserstatus());


            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                URL url = new URL(status.backgrounduri);
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                Drawable dr = new BitmapDrawable(image);
                UserStatus.setBackgroundDrawable(dr);
            } catch(IOException e) {
                System.out.println(e);
            }

            int status_color = (int) status.getTextcolor();
            UserStatus.setTextColor(status_color);
            int Text_Size = (int)  status.getTextsize()/ 3 ;
            UserStatus.setTextSize(Text_Size);

            setStatusLikesButtonStatus(statusPostKey);
            setCommentStatusButtonStatus();
            setStatusShareButtonStatus(statusPostKey);

            statusUserID = status.getUid();

            final String UserStatusforshare = status.getUserstatus();

            FullName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                                Intent visitprofileIntent = new Intent(context, ProfileActivity.class);
                                visitprofileIntent.putExtra("currentprofileid", statusUserID);
                                context.startActivity(visitprofileIntent);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent clickpostintent = new Intent(context, DeleteStatusActivity.class);
                    clickpostintent.putExtra("StatusKey", statusPostKey);
                    context.startActivity(clickpostintent);
                    //Toast.makeText(context, ""+statusPostKey, Toast.LENGTH_SHORT).show();

                }
            });


            CommentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent commentsIntent = new Intent(context, StatCommentActivity.class);
                    commentsIntent.putExtra("StatusKey", statusPostKey);
                    context.startActivity(commentsIntent);

                    // Toast.makeText(context, ""+StatusId, Toast.LENGTH_SHORT).show();
                }
            });


            LikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LikeChecker = true;

                    SLikesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (LikeChecker == true) {

                                if (dataSnapshot.child(statusPostKey).hasChild(CurrentUserId))
                                {
                                    SLikesRef.child(statusPostKey).child(CurrentUserId).removeValue();
                                    LikeChecker = false;
                                }
                                else {
                                    SLikesRef.child(statusPostKey).child(CurrentUserId).setValue(true);
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


             ShareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ShareChecker = true;

//The below code was to check that, we are getting image link and image description or not here inside.
                    // Toast.makeText(context, ""+postImage, Toast.LENGTH_SHORT).show();
                    // Toast.makeText(context, ""+postDescription, Toast.LENGTH_SHORT).show();

                    SshareRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (ShareChecker == true) {
                                if (dataSnapshot.child(statusPostKey).hasChild(CurrentUserId)) {
                                    Toast.makeText(context, "You have shared once.", Toast.LENGTH_SHORT).show();
                                    // ShareRef.child(PostKey).child(CurrentUserID).removeValue();
                                    ShareChecker = false;
                                } else {
                                    SshareRef.child(statusPostKey).child(CurrentUserId).setValue(true);
                                    ShareChecker = false;
                                }
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, UserStatusforshare);
                    whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                    try {
                        //   context.startActivity(whatsappIntent);
                        context.startActivity(Intent.createChooser(whatsappIntent, "Select an App to share With:"));
                    } catch (android.content.ActivityNotFoundException ex) {

                        Toast.makeText(context, "No supported app exists!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

   /* public  class AdapterAsyncTask extends AsyncTask<Integer, Void, Boolean>
    {
           sviewHolder holder;
           viewHolder pholder;
           Posts status;
           final String statusPostKey;
           String postType;
           List<String> postInfo;

           public AdapterAsyncTask(sviewHolder holder, Posts status, String statusPostKey, String postType)
           {
               this.holder = holder;
               this.status = status;
               this.statusPostKey = statusPostKey;
               this.postType = postType;
           }

          public AdapterAsyncTask(viewHolder pholder, Posts status, String statusPostKey, String postType)
          {
            this.pholder = pholder;
            this.status = status;
            this.statusPostKey = statusPostKey;
            this.postType = postType;
          }


        @Override
        protected void onPreExecute() {

          if (postType.equals("status"))
          {

              Picasso.get().load(status.getProfileimage()).placeholder(R.drawable.profile).into(holder.Profileimage);
              holder.FullName.setText(status.getFullname());
              holder.Date.setText(status.getDate());
              holder.Time.setText(status.getTime());
              holder.UserStatus.setText(status.getUserstatus());
              statusUserID = status.getUid();

              StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
              StrictMode.setThreadPolicy(policy);
              try {
                  URL url = new URL(status.backgrounduri);
                  Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                  Drawable dr = new BitmapDrawable(image);
                  holder.UserStatus.setBackgroundDrawable(dr);
              } catch (IOException e) {
                  System.out.println(e);
              }

              int status_color = (int) status.getTextcolor();
              holder.UserStatus.setTextColor(status_color);
              int Text_Size = (int) status.getTextsize() / 3;
              holder.UserStatus.setTextSize(Text_Size);

          }
          else
              {
//                  Picasso.get().load(status.getPostimage()).placeholder(R.drawable.photo).into(pholder.PostImage);
//                  Picasso.get().load(status.getProfileimage()).fit().into(pholder.PostProfileimage);
//                  pholder.PostUserName.setText(status.getFullname());
//                  pholder.PostDate.setText(status.getDate());
//                  pholder.PostTime.setText(status.getTime());
//                  pholder.PostDescription.setText(status.getDescription());
              }
               super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {

                  if (postType.equals("status"))
                  {
                      final String CurrentUserId = mAuth.getCurrentUser().getUid();

                      SCommentRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(statusPostKey);
                      SLikesRef = FirebaseDatabase.getInstance().getReference().child("StatLikes");
                      SshareRef = FirebaseDatabase.getInstance().getReference().child("StatShares");

//                 Picasso.get().load(status.getProfileimage()).placeholder(R.drawable.profile).into(holder.Profileimage);
//                   holder.FullName.setText(status.getFullname());
//                   holder.Date.setText(status.getDate());
//                   holder.Time.setText(status.getTime());
//                   holder.UserStatus.setText(status.getUserstatus());
//                   statusUserID = status.getUid();

//                   StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//                   StrictMode.setThreadPolicy(policy);
//                   try {
//                       URL url = new URL(status.backgrounduri);
//                       Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//                       Drawable dr = new BitmapDrawable(image);
//                       holder.UserStatus.setBackgroundDrawable(dr);
//                   } catch(IOException e) {
//                       System.out.println(e);
//                   }
//
//                   int status_color = (int) status.getTextcolor();
//                   holder.UserStatus.setTextColor(status_color);
//                   int Text_Size = (int)  status.getTextsize()/ 3 ;
//                   holder.UserStatus.setTextSize(Text_Size);

                      holder.setStatusLikesButtonStatus(statusPostKey);
                      holder.setCommentStatusButtonStatus();
                      holder.setStatusShareButtonStatus(statusPostKey);

                      tobevisitprofileId = status.getUid();

                      final String UserStatusforshare = status.getUserstatus();

                      holder.FullName.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v)
                          {
                              Intent visitprofileIntent = new Intent(context, ProfileActivity.class);
                              visitprofileIntent.putExtra("currentprofileid", tobevisitprofileId);
                              context.startActivity(visitprofileIntent);
                          }
                      });

                      holder.itemView.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {

                              Intent clickpostintent = new Intent(context, DeleteStatusActivity.class);
                              clickpostintent.putExtra("StatusKey", statusPostKey);
                              context.startActivity(clickpostintent);
                              //Toast.makeText(context, ""+statusPostKey, Toast.LENGTH_SHORT).show();

                          }
                      });


                      holder.CommentBtn.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                              Intent commentsIntent = new Intent(context, StatCommentActivity.class);
                              commentsIntent.putExtra("StatusKey", statusPostKey);
                              context.startActivity(commentsIntent);

                              // Toast.makeText(context, ""+StatusId, Toast.LENGTH_SHORT).show();
                          }
                      });


                      holder.LikeBtn.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                              LikeChecker = true;

                              SLikesRef.addValueEventListener(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                      if (LikeChecker == true) {

                                          if (dataSnapshot.child(statusPostKey).hasChild(CurrentUserId))
                                          {
                                              SLikesRef.child(statusPostKey).child(CurrentUserId).removeValue();
                                              LikeChecker = false;
                                          }
                                          else {
                                              SLikesRef.child(statusPostKey).child(CurrentUserId).setValue(true);
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


                      holder.ShareBtn.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {

                              ShareChecker = true;

//The below code was to check that, we are getting image link and image description or not here inside.
                              // Toast.makeText(context, ""+postImage, Toast.LENGTH_SHORT).show();
                              // Toast.makeText(context, ""+postDescription, Toast.LENGTH_SHORT).show();

                              SshareRef.addValueEventListener(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                      if (ShareChecker == true) {
                                          if (dataSnapshot.child(statusPostKey).hasChild(CurrentUserId)) {
                                              Toast.makeText(context, "You have shared once.", Toast.LENGTH_SHORT).show();
                                              // ShareRef.child(PostKey).child(CurrentUserID).removeValue();
                                              ShareChecker = false;
                                          } else {
                                              SshareRef.child(statusPostKey).child(CurrentUserId).setValue(true);
                                              ShareChecker = false;
                                          }
                                      }


                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError databaseError) {

                                  }
                              });



                              Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                              whatsappIntent.setType("text/plain");
                              whatsappIntent.putExtra(Intent.EXTRA_TEXT, UserStatusforshare);
                              whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                              whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                              try {
                                  //   context.startActivity(whatsappIntent);
                                  context.startActivity(Intent.createChooser(whatsappIntent, "Select an App to share With:"));
                              } catch (android.content.ActivityNotFoundException ex) {

                                  Toast.makeText(context, "No supported app exists!", Toast.LENGTH_SHORT).show();
                              }
                          }
                      });

                  }
                  else
                      {
                          LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
                          CommentsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(statusPostKey);
                          ShareRef = FirebaseDatabase.getInstance().getReference().child("Shares");

                          tobevisitprofileId = status.getUid();



                         pholder.PostUserName.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {

                                  Intent visitprofileIntent = new Intent(context, ProfileActivity.class);
                                  visitprofileIntent.putExtra("currentprofileid", tobevisitprofileId);
                                  context.startActivity(visitprofileIntent);

                              }
                          });


                          pholder.setLikesButtonStatus(statusPostKey);
                          pholder.setCommentButtonStatus();
                          pholder.setShareButtonStatus(statusPostKey);
//Here I get description and image for share button,which I implement below of comment button.
                          final String postDescription = status.getDescription();
                          final String postImage = status.getPostimage();


                          pholder.itemView.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {

                                  Intent clickpostintent = new Intent(context, ClickPostActivity.class);
                                  clickpostintent.putExtra("PostKey", statusPostKey);
                                  context.startActivity(clickpostintent);
                                  //String mykey = keyList.get(position);
                                  //  Toast.makeText(context, ""+mykey, Toast.LENGTH_LONG).show();
                              }
                          });

                          pholder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {
                                  Intent commentsIntent = new Intent(context, CommentsActivity.class);
                                  commentsIntent.putExtra("PostKey", statusPostKey);
                                  context.startActivity(commentsIntent);
                              }
                          });

                          pholder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {
                                  LikeChecker = true;

                                  LikesRef.addValueEventListener(new ValueEventListener() {
                                      @Override
                                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                          if (LikeChecker == true) {
                                              if (dataSnapshot.child(statusPostKey).hasChild(CurrentUserID)) {
                                                  LikesRef.child(statusPostKey).child(CurrentUserID).removeValue();
                                                  LikeChecker = false;
                                              } else {
                                                  LikesRef.child(statusPostKey).child(CurrentUserID).setValue(true);
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
                          pholder.SharePostButton.setOnClickListener(new View.OnClickListener() {
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
                                              if (dataSnapshot.child(statusPostKey).hasChild(CurrentUserID)) {
                                                  Toast.makeText(context, "You have shared once.", Toast.LENGTH_SHORT).show();
                                                  // ShareRef.child(PostKey).child(CurrentUserID).removeValue();
                                                  ShareChecker = false;
                                              } else {
                                                  ShareRef.child(statusPostKey).child(CurrentUserID).setValue(true);
                                                  ShareChecker = false;
                                              }
                                          }


                                      }

                                      @Override
                                      public void onCancelled(@NonNull DatabaseError databaseError) {

                                      }
                                  });


                                  Uri imgUri = Uri.parse(postImage);

                                  // String imgUri = posts.storageName;
                                  // Toast.makeText(context, ""+imgUri, Toast.LENGTH_SHORT).show();

                                  Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                                  whatsappIntent.setType("text/plain");
                                  whatsappIntent.putExtra(Intent.EXTRA_TEXT, postDescription);
                                  whatsappIntent.setType("image/jpeg");
                                  whatsappIntent.putExtra(Intent.EXTRA_STREAM, imgUri);
                                  whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                  whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                                  try {
                                      //context.startActivity(whatsappIntent);
                                      context.startActivity(Intent.createChooser(whatsappIntent, "Select an App to share With:"));

                                  } catch (android.content.ActivityNotFoundException ex) {

                                      Toast.makeText(context, "No supported app exists!", Toast.LENGTH_SHORT).show();
                                  }
                              }
                          });
                      }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {

               if (postType.equals("photo"))
               {
                   Picasso.get().load(status.getPostimage()).placeholder(R.drawable.photo).into(pholder.PostImage);
                  Picasso.get().load(status.getProfileimage()).fit().into(pholder.PostProfileimage);
                  pholder.PostUserName.setText(status.getFullname());
                  pholder.PostDate.setText(status.getDate());
                  pholder.PostTime.setText(status.getTime());
                  pholder.PostDescription.setText(status.getDescription());
               }


            super.onPostExecute(aBoolean);
        }
    }*/

}


