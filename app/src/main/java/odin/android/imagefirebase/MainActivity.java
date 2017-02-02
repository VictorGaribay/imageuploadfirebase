package odin.android.imagefirebase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    int clicks = 0;
    private static final int PICK_IMAGE_REQUEST=200;
    private static final int CAMERA_REQUEST_CODE = 300;

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

        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
           if(filepath != null){
               Uri selectedImage = filepath;
               getContentResolver().notifyChange(selectedImage, null);
               Bitmap reducedSizeBitmap = getBitmap(filepath.getPath());
               if(reducedSizeBitmap != null){
                   imageView.setImageBitmap(reducedSizeBitmap);
                   Button uploadImageButton = (Button) findViewById(R.id.send);
                   uploadImageButton.setVisibility(View.VISIBLE);
               }else{
                   Toast.makeText(this,"Error mientras se captura imagen",Toast.LENGTH_LONG).show();
               }
           }
       }



    }

    private Bitmap getBitmap(String path) {

        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b = null;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                Log.d("", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                        (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            Log.d("", "bitmap size - width: " + b.getWidth() + ", height: " +
                    b.getHeight());
            return b;
        } catch (IOException e) {
            Log.e("", e.getMessage(), e);
            return null;
        }
    }

    private void uploadFile()
    {
        if (filepath !=null ) {
            clicks++;

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Cargando...");
            progressDialog.show();
            StorageReference riversRef = Storage.child("images/foto N:"+clicks+"");

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


    private void cameraFileImage()
    {


        Intent chooserIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(Environment.getExternalStorageDirectory(), "POST_IMAGE.jpg");
        chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        filepath = Uri.fromFile(f);
        startActivityForResult(chooserIntent, CAMERA_REQUEST_CODE);

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

