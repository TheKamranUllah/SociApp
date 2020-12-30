package com.example.sociapp;

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

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.FindFriendsviewHolder> {

    List<String> keyList;

    List<FindFriends> List;
    Context context;

    public FindFriendsAdapter(java.util.List<FindFriends> list,List<String> keys, Context context) {
        List = list;
        keyList = keys;
        this.context = context;
    }

    @NonNull
    @Override
    public FindFriendsviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.all_user_display_layout, parent, false);
        return new FindFriendsviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendsviewHolder holder, int position) {

        FindFriends findFriends = List.get(position);
        final String visit_user_id = keyList.get(position);

        Picasso.get().load(findFriends.getProfileimage()).placeholder(R.drawable.profile).into(holder.AllUserProfilePic);
        holder.AllUserFullName.setText(findFriends.getFull_Name());
        holder.Status.setText(findFriends.getStatus());
      //  List.remove(position);
       // Toast.makeText(context, ""+visit_user_id, Toast.LENGTH_SHORT).show();

      holder.itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Intent visitUserIntent = new Intent(context, PersonProfileActivity.class);
              visitUserIntent.putExtra("visit_user_id", visit_user_id);
              context.startActivity(visitUserIntent);
          }
      });
    }

    @Override
    public int getItemCount() {
        if(List != null) {
            return List.size();
        }
        Toast.makeText(context, "List is null", Toast.LENGTH_SHORT).show();
        return 0;

    }

    public class FindFriendsviewHolder extends RecyclerView.ViewHolder {


        CircleImageView AllUserProfilePic;
        TextView AllUserFullName;
        TextView Status;

        public FindFriendsviewHolder(@NonNull View itemView) {
            super(itemView);

            AllUserProfilePic = itemView.findViewById(R.id.all_user_post_image);
            AllUserFullName = itemView.findViewById(R.id.all_user_profile_full_name);
            Status = itemView.findViewById(R.id.all_user_status);


        }
    }
}
