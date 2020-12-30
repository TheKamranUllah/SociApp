package com.example.sociapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private List<FindFriends> AllUserSearchResult;

    private ImageButton SearchButton;
    private EditText SearchInputText;
    private RecyclerView SearchResultList;

    List<String> FKeys;

    private DatabaseReference AllUserDatabaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mtoolbar = (Toolbar) findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Seach Friends");

        AllUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");


        SearchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton = (ImageButton) findViewById(R.id.search_friend_button);
        SearchInputText = (EditText) findViewById(R.id.search_box_input);

        AllUserSearchResult = new ArrayList<>();

        SearchResultList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        SearchResultList.setLayoutManager(linearLayoutManager);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String SearchBoxInput = SearchInputText.getText().toString();
                SearchPeopleAndFriends(SearchBoxInput);
            }
        });
    }

    private void SearchPeopleAndFriends(String searchBoxInput)
    {
        Toast.makeText(this, "Searching", Toast.LENGTH_SHORT).show();

        Query  searchFriendQuery = AllUserDatabaseRef.orderByChild("Full_Name")
                .startAt(searchBoxInput).endAt(searchBoxInput +"\uf8ff");

        FKeys = new ArrayList<>();

        searchFriendQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                AllUserSearchResult.clear();

                if (dataSnapshot.exists())
                {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                        FKeys.add(dataSnapshot1.getKey());

                        FindFriends findFriends = dataSnapshot1.getValue(FindFriends.class);
                        AllUserSearchResult.add(findFriends);

                    }

                    FindFriendsAdapter findFriendsAdapter = new FindFriendsAdapter(AllUserSearchResult,FKeys,FindFriendsActivity.this);
                    SearchResultList.setAdapter(findFriendsAdapter);
                    findFriendsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
