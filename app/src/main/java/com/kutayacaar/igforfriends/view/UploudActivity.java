package com.kutayacaar.igforfriends.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kutayacaar.igforfriends.databinding.ActivityUploudBinding;

import java.util.HashMap;
import java.util.UUID;

public class UploudActivity extends AppCompatActivity {
    Uri imageData;
ActivityResultLauncher<Intent> activityResultLauncher;
ActivityResultLauncher<String> permissionLauncher;
private FirebaseAuth firebaseAuth;
private ActivityUploudBinding binding;
private StorageReference storageReference;
private FirebaseFirestore firebaseFirestore;
private FirebaseStorage firebaseStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityUploudBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
registerLauncher();
firebaseFirestore = FirebaseFirestore.getInstance();
firebaseAuth = FirebaseAuth.getInstance();
firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }
    public void uploudButtonClicked(View view){
if (imageData != null)  {
    //universal unique  id
    UUID uuid = UUID.randomUUID();
    final String imageName = "images/"+ uuid+ ".jpg";
    storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            StorageReference newReference = firebaseStorage.getReference(imageName);
            newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String downloadUrl = uri.toString();
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    String comment = binding.commentText.getText().toString();

                       String userEmail = firebaseUser.getEmail();

                    HashMap<String , Object> postData = new HashMap<>();
                    postData.put("userEmail",userEmail);
                    postData.put("downloadUrl",downloadUrl);
                    postData.put("comment",comment);
                    postData.put("date", FieldValue.serverTimestamp());
                    firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
Intent intent = new Intent(UploudActivity.this,FeedActivity.class);
intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
Toast.makeText(UploudActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

                }
            });
//Download url
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(UploudActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }
    });

}


    }public void selectImage(View view){
if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
        PackageManager.PERMISSION_GRANTED){
    if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE )){
        Snackbar.make(view,"Galeriye gitmek için izin gerekli",Snackbar.LENGTH_INDEFINITE).setAction("İzin ver", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);


            }
        }).show();
    }else{
permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

    }

}else{
    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    activityResultLauncher.launch(intentToGallery);
}
    }
    private void registerLauncher(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
if (result.getResultCode()== Activity.RESULT_OK){
    Intent intentFromResult = result.getData();
    if(intentFromResult != null){
        imageData=intentFromResult.getData();
        binding.imageView.setImageURI(imageData);
    }
}
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
           if (result){
               Intent intentToGallery=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
               activityResultLauncher.launch(intentToGallery);

           }else{
               Toast.makeText(UploudActivity.this, "İzin lazım", Toast.LENGTH_SHORT).show();
           }
            }
        });
    }
}