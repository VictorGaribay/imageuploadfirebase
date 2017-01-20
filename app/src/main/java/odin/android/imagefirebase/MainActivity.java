package odin.android.imagefirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private Button Selectimg;


    private StorageReference Storage;

    private static final int GALLERY_INTENT=2;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Storage= FirebaseStorage.getInstance().getReference();


        Selectimg =(Button) findViewById(R.id.selectimg);

        mProgressDialog =new ProgressDialog(this);








        Selectimg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Intent intent= new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

    }




    @Override
    protected void  onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GALLERY_INTENT && resultCode==RESULT_OK)
        {
            mProgressDialog.setMessage("Cargando....");
            mProgressDialog.show();


            Uri uri=data.getData();
            StorageReference filepath= Storage.child("Photos").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(MainActivity.this, "upload Done", Toast.LENGTH_LONG).show();
                    mProgressDialog.dismiss();
                }
            });
        }

        }
    }

