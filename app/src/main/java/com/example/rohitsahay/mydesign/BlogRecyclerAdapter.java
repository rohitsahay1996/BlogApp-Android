package com.example.rohitsahay.mydesign;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;
    private FirebaseFirestore firebaseFirestore;

    public BlogRecyclerAdapter(List<BlogPost> blog_list){
        this.blog_list = blog_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        String descData = blog_list.get(position).getDesc();
        String user_id = blog_list.get(position).getUser_id();
        String image_url = blog_list.get(position).getImage_url();
        long milliseconds = blog_list.get(position).getTimestamp().getTime();
        String dateString = android.text.format.DateFormat.format("dd/mm/yyyy",new Date(milliseconds)).toString();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
           if(task.isSuccessful()){

               String user_name = task.getResult().getString("name");
               String image = task.getResult().getString("image");

               holder.setUserData(user_name,image);

           }else{

               //Firebase Exceptions
           }
            }
        });
        holder.setDiscText(descData);
        holder.setblogImage(image_url);
        holder.setDate(dateString);



    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUserName;
        private CircleImageView blogUserImage;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }
        public void setDiscText(String descText){


            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
        }
        public void setblogImage(String downloadUri){

            blogImageView = mView.findViewById(R.id.blog_image);

            Glide.with(context).load(downloadUri).into(blogImageView);

        }
        public void setDate(String date){

        blogDate = mView.findViewById(R.id.blog_user_date);
        blogDate.setText(date);

        }

        public void setUserData(String Username,String Userimage){

        blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUserName = mView.findViewById(R.id.blog_user_name);
            blogUserName.setText(Username);
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.default_image);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(Userimage).into(blogUserImage);

        }
    }
}
