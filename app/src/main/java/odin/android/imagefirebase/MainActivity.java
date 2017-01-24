package odin.android.imagefirebase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{



    private static final int PICK_IMAGE_REQUEST=200;

    private static final int CAMERA_REQUEST_CODE = 300;
    private final int PHOTO_CODE = 0;

    private Button Selectimg;
    private ImageView imageView;
    private Button Enviar;
    private Button Camera;

    private StorageReference Storage;

    private Uri filepath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Storage= FirebaseStorage.getInstance().getReference();


        Selectimg =(Button) findViewById(R.id.selectimg);
        Enviar = (Button) findViewById(R.id.send);
        imageView = (ImageView) findViewById(R.id.img);
        Camera= (Button) findViewById(R.id.camera);


        Selectimg.setOnClickListener(this);
        Enviar.setOnClickListener(this);
        Camera.setOnClickListener(this);

    }

    @Override
    protected void  onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filepath = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {


            filepath = data.getData();
           try {
               Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
               imageView.setImageBitmap(bitmap);

           } catch (IOException e) {
               e.printStackTrace();
           }
        }



    }

    private void uploadFile()
    {

        if (filepath !=null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Cargando...");
            progressDialog.show();
            StorageReference riversRef = Storage.child("images/profile.jpg");

            riversRef.putFile(filepath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Archivo cargado", Toast.LENGTH_LONG).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage(((int) progress)+"% Cargando...");
                }
            });
        }
    }

    private void  showFileImage()
    {
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "seleccionar imagen"), PICK_IMAGE_REQUEST);
    }
    private void openCamera() {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PHOTO_CODE);


        }

    private void cameraFileImage()
    {

        final Intent intent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(intent, CAMERA_REQUEST_CODE);

    }




    @Override
    public void onClick(View view) {
        if(view== Selectimg)
        {
            showFileImage();
        }
        else if (view== Camera)
        {
            cameraFileImage();
        }

        else if (view==Enviar)
        {
                uploadFile();
        }

    }
}

