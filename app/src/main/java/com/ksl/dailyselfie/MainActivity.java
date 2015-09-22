package com.ksl.dailyselfie;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    final String TAG = "DailySelfie";

    //name for saving selfies to shared preferences
    private static final String SELFIES_PREF_NAME = "com.ksl.dailyselfie.selfies";

    //image capture request code
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    //notifications interval
    private final long ALARM_INTERVAL = 30 * 1000;  //30 seconds in milliseconds

    //list and associated adapter
    SelfieListViewAdapter mAdapter;
    ListView mListView;

    //latest image file
    File mCurrentImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get list view
        mListView = (ListView)findViewById(android.R.id.list);

        //set listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //get the Selfie first
                Selfie selfie = (Selfie)mAdapter.getItem(i);

                //show the image
                showFullScreenImage(selfie);
            }
        });

        //create a view adapter
        mAdapter = new SelfieListViewAdapter(getApplicationContext());

        //load selfies from shared preferences
        SharedPreferences settings = getSharedPreferences(SELFIES_PREF_NAME, 0);
        String json = settings.getString("selfies", "");
        Gson gson = new Gson();
        Type type = new TypeToken<List<Selfie>>(){}.getType();
        List<Selfie> selfies = gson.fromJson(json, type);
        mAdapter.setAllItems(selfies);

        //set adapter for list view
        mListView.setAdapter(mAdapter);

        //trigger alarms for status bar notifications
        startNotifications();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //save selfies to shared preferences
        SharedPreferences settings = getSharedPreferences(SELFIES_PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mAdapter.getAllItems());
        editor.putString("selfies", json);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_deleteAll) {
            deleteAll();
        } else if (id == R.id.action_camera) {
            activateCamera();
        }

        return super.onOptionsItemSelected(item);
    }

    private void startNotifications() {
        //start repeating alarm for status bar notification
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(this, SelfieAlarmReceiver.class), 0);
        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + ALARM_INTERVAL,
                ALARM_INTERVAL,
                pi);
    }

    private void showFullScreenImage(Selfie selfie) {
        //start an intent that shows the full size photo of a given Selfie
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(selfie.getImageFile()), "image/jpg");
        startActivity(intent);
    }

    private void deleteAll() {

        //check if there are any selfies to delete first
        if (mAdapter.getCount() == 0) {
            Toast.makeText(this, "No selfies to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        //show dialog for user to confirm whether to remove all selfies
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.dialog_deleteAll_title)
            .setMessage(R.string.dialog_deleteAll_text)
            .setPositiveButton(R.string.dialog_deleteAll_yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //remove photos on disk
                    for (Selfie selfie: mAdapter.getAllItems()) {
                        selfie.getThumbnailFile().delete();
                        selfie.getImageFile().delete();
                    }

                    //clear the items in the model
                    mAdapter.clear();

                }

            })
            .setNegativeButton(R.string.dialog_deleteAll_no, null)
            .show();

    }

    private void activateCamera() {

        //create image capture intent first
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            //create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                //error occurred while creating the File
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            //create thumbnail on disk (otherwise dynamically creating thumbnails everytime is too slow)
            File thumbnailFile = createThumbnailFile(mCurrentImageFile);

            //create Selfie instance
            Selfie selfie = new Selfie(thumbnailFile, mCurrentImageFile);

            //add to the adapter for list view display
            mAdapter.add(selfie);
        }

    }

    private File createImageFile() throws IOException {

        //create unique image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp;

        //get storage dir
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);  //this does not work for KitKat and above because non-system apps are not allowed to write to public folders
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);  //private app folder
        storageDir.mkdirs();

        //create the temp file
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentImageFile = imageFile;

        return imageFile;
    }

    private File createThumbnailFile(File imageFile) {

        //set dimensions of thumbnail
        int targetW = 100;
        int targetH = 100;

        //get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.toString(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        //determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        //decode the image file into a thumbnail Bitmap
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.toString(), bmOptions);

        //save bitmap to disk
        String dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        File outputFile = new File(dir, "t_" + imageFile.getName());
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputFile.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outputFile;
    }

}
