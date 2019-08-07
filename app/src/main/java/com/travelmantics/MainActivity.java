package com.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.Resource;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    EditText eTitle;
    EditText eDescription;
    EditText ePrice;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private ActivityList activityList;
    private static final int PICTURE_RESULTS = 42;
    String photoStringLink;
    ImageView imageView;
    TravelDeal deal = new TravelDeal();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);
      FirebaseUtil.openFbReference("traveldeals", activityList );
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        eTitle = findViewById(R.id.myTitle);
        eDescription = findViewById(R.id.txtDiscription);
        ePrice = findViewById(R.id.myPrice);
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if(deal == null){
            deal = new TravelDeal();
        }
        this.deal = deal;
        eTitle.setText(deal.getTitle());
        eDescription.setText(deal.getDescription());
        ePrice.setText(deal.getPrice());


        Button btnImage = findViewById(R.id.imageBtn);
        imageView = findViewById(R.id.myImage);
        showImage(deal.getImageUrl());
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULTS);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal Save", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return  true;

            case R.id.deleteDeal:
                deleteDeal();
                Toast.makeText(this, "Deal deleted", Toast.LENGTH_LONG).show();
                clean();
                backToList();
            case R.id.editDeal:
                editDeal();
                Toast.makeText(this, "Deal edited", Toast.LENGTH_LONG).show();
                clean();
                backToList();
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.save_menu).setVisible(true);
            menu.findItem(R.id.deleteDeal).setVisible(true);
            menu.findItem(R.id.editDeal).setVisible(true);
            enableEditText(true);
        } else {
            menu.findItem(R.id.insertDeal).setVisible(false);
            menu.findItem(R.id.deleteDeal).setVisible(false);
            menu.findItem(R.id.editDeal).setVisible(false);
            enableEditText(false);
        }

        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_RESULTS && resultCode == RESULT_OK){
            mStorageReference = FirebaseStorage.getInstance().getReference();

            final Uri imageUri = data.getData();
          final  StorageReference reference = mStorageReference.child(imageUri.getLastPathSegment());
            UploadTask uploadTask =  reference.putFile(imageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                       // System.out.println("Upload " + downloadUri);
                       // Toast.makeText(mActivity, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                        if (downloadUri != null) {

                             photoStringLink = downloadUri.toString(); //YOU WILL GET THE DOWNLOAD URL HERE !!!!
                            TravelDeal deal = new TravelDeal("","","", photoStringLink);
                            deal.setImageUrl(photoStringLink);
                            showImage(photoStringLink);
                            Log.d("Image Link", deal.getImageUrl());
                            System.out.println("Upload " + photoStringLink);

                        }

                    }
                }
            });
        }
    }

    private void saveDeal(){
        String title = eTitle.getText().toString();
        String price = ePrice.getText().toString();
        String description = eDescription.getText().toString();
        String id = "";

        if (deal.getId() == null){
            TravelDeal deal = new TravelDeal(title, price, description, photoStringLink);
            mDatabaseReference.push().setValue(deal);
        }

    }

    private void editDeal(){
        deal.setTitle(eTitle.getText().toString());
        deal.setDescription(eDescription.getText().toString());
        deal.setPrice(ePrice.getText().toString());
        if(deal.getId() != null){
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }

    }

    private void deleteDeal(){
        if (deal == null){
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_LONG).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
    }

    private void backToList(){
        Intent inten = new Intent(this , ActivityList.class);
        startActivity(inten);
    }


    private void clean(){
        ePrice.setText("");
        eDescription.setText("");
        eTitle.setText("");

        eTitle.requestFocus();
    }
    private void enableEditText(boolean isEnabled){
        eDescription.setEnabled(isEnabled);
        ePrice.setEnabled(isEnabled);
        eTitle.setEnabled(isEnabled);
    }

    private void showImage(String url){
        if (url != null && url.isEmpty() == false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this)
                    .load(url)
                    .resize(width, width*2/3 )
                    .centerCrop()
                    .into(imageView);
        }
    }
}
