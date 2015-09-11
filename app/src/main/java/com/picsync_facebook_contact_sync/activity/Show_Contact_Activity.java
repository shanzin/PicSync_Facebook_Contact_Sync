package com.picsync_facebook_contact_sync.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.picsync_facebook_contact_sync.R;

import java.util.ArrayList;
import java.util.Iterator;


public class Show_Contact_Activity extends Activity {
    private ListView listView;
    private ArrayAdapter<String> listAdapter;
    private String[] contactsName;
    private String[] contactsID;
    private ArrayList<Contact_data_type> Contact_Data_Arraylist = new ArrayList<Contact_data_type>();

    /*class define*/
    public class Contact_data_type
    {                   // Don't use static here unless you want all of your Person
        // objects to have the same data
        String name;
        int id;

        public Contact_data_type(int id, String name)
        {
            this.id = id;
            this.name = name;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show__contact);

        show_contact_name_on_list();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show__contact, menu);
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

    private void getContactData() {
        int data_index = 0;
        //取得內容解析器
        ContentResolver contentResolver = this.getContentResolver();
        //設定你要從電話簿取出的欄位
        String[] projection = new String[]{ContactsContract.Data._ID, Contacts.People.NAME, Contacts.People.Phones.NUMBER};
        //取得所有聯絡人
        Cursor cursor = contentResolver.query(Contacts.People.CONTENT_URI, projection, null, null, Contacts.People.DEFAULT_SORT_ORDER);
        contactsName = new String[cursor.getCount()];
        contactsID = new String[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            if(cursor.getString(2) != null) {
                Contact_data_type data = new Contact_data_type(cursor.getInt(0), cursor.getString(1));
                Contact_Data_Arraylist.add(data);
                data_index++;
            }
        }
        cursor.close();
    }

    private void show_contact_name_on_list(){
        /*must get contact data first*/
        getContactData();
        Iterator<Contact_data_type> iterator = Contact_Data_Arraylist.iterator();
        listView = (ListView)findViewById(R.id.contact_name_listView);
        listAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1);

        while (iterator.hasNext()) {
            listAdapter.add(iterator.next().name);
        }

        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(Show_Contact_Activity.this, Show_Facebook_Match_User_Photo_Activity.class);

                Bundle bundle = new Bundle();
                bundle.putString("name", listView.getItemAtPosition(position).toString());
                bundle.putString("userID", contactsID[position]);

                intent.putExtras(bundle);

                //switch Activity
                startActivity(intent);
            }
        });
    }
}
