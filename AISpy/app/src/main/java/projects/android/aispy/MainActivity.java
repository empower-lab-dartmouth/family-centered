/*
This project was extracted from the Android App in the https://github.com/StanfordHCI/family-centered repo. I extracted only the code relevant to image processing for
AI Spy (and perhaps Explore Mode). The intention is to have a bare bones project space where we can focus on the more technical challenge of developing an excellent
back-end for AI Spy image processing which we will then build the rest of AI Spy around.

One strange bug exists in both this project and the family-centered Android App project where occasionally the project won't compile due to a build error. All you need to do is click
Build -> Clean Project and then try running again and it will compile.
 */

package projects.android.aispy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static android.provider.MediaStore.EXTRA_OUTPUT;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.Images.Media.getBitmap;

//import android.support.v4.content.FileProvider;

/**
 * MainActivity is the initial Activity. In this Activity, a user has 2 options:
 * 1. take a photo
 * 2. select a photo from gallery
 * Upon successfully completing one of the above, the next Activity is called.
 */
public class MainActivity extends AppCompatActivity {

    String currentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_GALLERY_SELECT = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void gallerySelect(View view){ dispatchGallerySelect(); }

    private void dispatchGallerySelect() {
        Intent gallerySelect = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
        startActivityForResult(gallerySelect , REQUEST_GALLERY_SELECT);
    }

    public void takePicture(View view) {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "projects.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        Context thisContent = this.getApplicationContext();
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //The image is already stored in currentPhotoPath so just start the next activity
            startDisplayImageActivity();
        }
        else if (requestCode == REQUEST_GALLERY_SELECT && resultCode == RESULT_OK){
            try {
                //First, store the image in currentPhotoPath
                Bitmap bitmap = getBitmap(this.getContentResolver(), data.getData());

                String newFilePath = createNewImageFile();
                try {
                    File file = new File(newFilePath);
                    FileOutputStream fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                    fOut.flush();
                    fOut.close();
                    currentPhotoPath = newFilePath;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            //Then start the next activity
            startDisplayImageActivity();
        }
    }

    private void startDisplayImageActivity(){
        Intent intent = new Intent(this, ProcessingImageActivity.class);
        intent.putExtra("image_path", currentPhotoPath);
        startActivity(intent);
    }

    //Creates a String file name based on the current time when the method is called
    private String createNewImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String newImagePath = image.getAbsolutePath();

        return newImagePath;
    }

}