package com.picsync_facebook_contact_sync.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.picsync_facebook_contact_sync.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Show_Facebook_Match_User_Photo_Activity extends Activity {
    private String Extra_Data_String = "facebook_return_data";

    Facebook_Utility Facebook_Utility = new Facebook_Utility();

    private int uid = 0;
    private String fb_object_string = null;
    private String return_string = null;

    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mReceiver;
    public static final String user_data_broadcast = "com.facebook_sync_broadcast_user_data";

    String find_user_name = null;
    long find_user_id = 0;
    private ImageView UserPhotoImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show__facebook__match__user__photo_);

        UserPhotoImage = (ImageView) findViewById(R.id.user_photo_imageView);
        //get intent bundle object
        Bundle bundle_object =this.getIntent().getExtras();
        find_user_name = bundle_object.getString("name");
        find_user_id = Integer.parseInt(bundle_object.getString("userID"));
        getFacebookReturnData();
//        showUserPicture("http://cdn.inside.com.tw/wp-content/uploads/2012/05/Chrome.jpg");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show__facebook__match__user__photo_, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getFacebookUserData() {
        uid = 0;

        if(fb_object_string != null) {
            while (fb_object_string != null) {
                return_string = Facebook_Utility.get_user_data_from_object_string(fb_object_string, uid, "name");
                if (return_string != null) {
                    if (find_user_name.equals(return_string)) {
                            /*get picture url*/
                        return_string = Facebook_Utility.get_user_data_from_object_string(fb_object_string, uid, "picture");
                        new ImageLoadTask(return_string, UserPhotoImage).execute();
                        fb_object_string = null;
                    }
                    Log.d("FB", return_string);
                    uid++;
                } else {
                    Log.d("FB", "Timer Stop");
                    fb_object_string = null;
                    break;
                }
            }
        }
    }

    private void getFacebookReturnData() {
        /*Broadcast register*/
        IntentFilter pic_url_filter = new IntentFilter();
        pic_url_filter.addAction(user_data_broadcast);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(user_data_broadcast)) {
                    if(intent.getStringExtra(Extra_Data_String) == null) {
                        Log.d("FB", "Get Friend Data Error");
                    }
                    else {
                        fb_object_string = intent.getStringExtra(Extra_Data_String);
                        getFacebookUserData();
                    }
                }
            }
        };
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mReceiver, pic_url_filter);

        Facebook_Utility.getFacebookTagFriendData();
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            boolean return_result;
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
            return_result = setContactPhoto(find_user_id, tools.bitmapToByteArray(result));
        }

    }

    public static Uri getPicture(Context context, String ID){
        ContentResolver cr = context.getContentResolver();
        Uri rawContactUri = null;
        Cursor rawContactCursor =  cr.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[] {ContactsContract.RawContacts._ID}, ContactsContract.RawContacts.CONTACT_ID + " = " + ID, null, null);
        if(!rawContactCursor.isAfterLast()) {
            rawContactCursor.moveToFirst();
            rawContactUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendPath(""+rawContactCursor.getLong(0)).build();
        }
        rawContactCursor.close();

        return rawContactUri;
    }

    public boolean setContactPhoto(long contactId, byte[] photo) {
        ContentResolver cr = this.getContentResolver();
        ContentValues values = new ContentValues();
        long photoId = -1;
        long rawContactId = -1;
        Cursor rawContactsCursor = cr.query(
                Contacts.People.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                String.format("%s=%d", ContactsContract.Data._ID, contactId),
                null,
                null
        );
        while (rawContactsCursor.moveToNext()) {
            rawContactId = rawContactsCursor.getLong(rawContactsCursor.getColumnIndex(ContactsContract.RawContacts._ID));
            String where = String.format(
                    "%s=%d AND %s=='%s'",
                    ContactsContract.Data.RAW_CONTACT_ID,
                    rawContactId,
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
            );
            Cursor dataCursor = cr.query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data._ID},
                    where,
                    null,
                    null
            );
            if (dataCursor.moveToFirst()) {
                photoId = dataCursor.getLong(dataCursor.getColumnIndex(ContactsContract.Data._ID));
                dataCursor.close();
                break;
            }
            dataCursor.close();
        }
        rawContactsCursor.close();

        if (rawContactId < 0) return false;

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, photo);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);

        if (photoId < 0) {
            return cr.insert(ContactsContract.Data.CONTENT_URI, values) != null;
        } else {
            return cr.update(ContactsContract.Data.CONTENT_URI, values, String.format("%s=%d", ContactsContract.Data._ID, photoId), null) == 1;
        }
    }
}
