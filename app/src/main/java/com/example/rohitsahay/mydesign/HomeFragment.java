package com.example.rohitsahay.mydesign;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private RecyclerView blost_list_view;
    private List<BlogPost> blog_list ;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        blog_list = new ArrayList<>();
        blost_list_view = view.findViewById(R.id.blog_list_view);
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        firebaseAuth = FirebaseAuth.getInstance();
        blost_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blost_list_view.setAdapter(blogRecyclerAdapter);
        blost_list_view.setHasFixedSize(true);

        if(firebaseAuth.getCurrentUser() != null){

        firebaseFirestore = FirebaseFirestore.getInstance();

        blost_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom  = !recyclerView.canScrollVertically(1);

                if(reachedBottom){

                    String desc = lastVisible.getString("desc");
                    Toast.makeText(container.getContext(), "Reached: "+desc , Toast.LENGTH_SHORT).show();
                    LoadMorePost();
                }
            }
        });



            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);


            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if(isFirstPageFirstLoad) {
                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
            }
                for(DocumentChange doc:documentSnapshots.getDocumentChanges()){

                    if(doc.getType()== DocumentChange.Type.ADDED){

                        BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                        if(isFirstPageFirstLoad) {
                            blog_list.add(blogPost);
                        }else{
                            blog_list.add(0, blogPost);
                        }
                        blogRecyclerAdapter.notifyDataSetChanged();


                    }
                }
                isFirstPageFirstLoad = false;
            }
        });

       }
        return view;
    }

    public void LoadMorePost(){
        Query nextQuery = firebaseFirestore.collection("Posts").
                orderBy("timestamp", Query.Direction.DESCENDING).
         startAfter(lastVisible).
        limit(3);


        nextQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()) {
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                            blog_list.add(blogPost);
                            blogRecyclerAdapter.notifyDataSetChanged();


                        }
                    }
                }
            }
        });

    }

}
