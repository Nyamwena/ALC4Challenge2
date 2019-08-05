package com.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    EditText eTitle;
    EditText eDescription;
    EditText ePrice;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private ActivityList activityList;
    private static final int PICTURE_RESULTS = 42;



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
        Button btnImage = findViewById(R.id.imageBtn);
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
                return  true;
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
            enableEditText(true);
        } else {
            menu.findItem(R.id.insertDeal).setVisible(false);
            menu.findItem(R.id.deleteDeal).setVisible(false);
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
                        System.out.println("Upload " + downloadUri);
                       // Toast.makeText(mActivity, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                        if (downloadUri != null) {

                            String photoStringLink = downloadUri.toString(); //YOU WILL GET THE DOWNLOAD URL HERE !!!!
                            TravelDeal deal = new TravelDeal("","","","");
                            deal.setImageUrl(photoStringLink);
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

        TravelDeal deal = new TravelDeal(title, price, description, "");
        mDatabaseReference.push().setValue(deal);
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
}
