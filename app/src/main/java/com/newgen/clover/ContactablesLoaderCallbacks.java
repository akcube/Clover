package com.newgen.clover;

import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Helper class to handle all the callbacks that occur when interacting with loaders.  Most of the
 * interesting code in this sample app will be in this file.
 */
public class ContactablesLoaderCallbacks extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    Context mContext;

    int counter = 0;

    String tvText = null;

    public static final String QUERY_KEY = "query";

    public static final String TAG = "CLoaderCallbacks";

    public ContactablesLoaderCallbacks(Context context) {
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderIndex, Bundle args) {
        // Where the Contactables table excels is matching text queries,
        // not just data dumps from Contacts db.  One search term is used to query
        // display name, email address and phone number.  In this case, the query was extracted
        // from an incoming intent in the handleIntent() method, via the
        // intent.getStringExtra() method.

        // BEGIN_INCLUDE(uri_with_query)
        String query = args.getString(QUERY_KEY);
        Uri uri = Uri.withAppendedPath(
                CommonDataKinds.Contactables.CONTENT_FILTER_URI, query);
        // END_INCLUDE(uri_with_query)


        // BEGIN_INCLUDE(cursor_loader)
        // Easy way to limit the query to contacts with phone numbers.
        String selection =
                CommonDataKinds.Contactables.HAS_PHONE_NUMBER + " = " + 1;

        // Sort results such that rows for the same contact stay together.
        String sortBy = CommonDataKinds.Contactables.LOOKUP_KEY;

        return new CursorLoader(
                mContext,  // Context
                uri,       // URI representing the table/resource to be queried
                null,      // projection - the list of columns to return.  Null means "all"
                selection, // selection - Which rows to return (condition rows must match)
                null,      // selection args - can be provided separately and subbed into selection.
                sortBy);   // string specifying sort order
        // END_INCLUDE(cursor_loader)
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {


        if (cursor.getCount() == 0) {
            return;
        }

        // Pulling the relevant value from the cursor requires knowing the column index to pull
        // it from.
        // BEGIN_INCLUDE(get_columns)
        int phoneColumnIndex = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER);
        int emailColumnIndex = cursor.getColumnIndex(CommonDataKinds.Email.ADDRESS);
        int nameColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.DISPLAY_NAME);
        int lookupColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.LOOKUP_KEY);
        int typeColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.MIMETYPE);
        // END_INCLUDE(get_columns)

        cursor.moveToFirst();
        // Lookup key is the easiest way to verify a row of data is for the same
        // contact as the previous row.
        String lookupKey = "";
        ArrayList<String> mylist = new ArrayList<String>();
        do {
            // BEGIN_INCLUDE(lookup_key)
            String currentLookupKey = cursor.getString(lookupColumnIndex);
            if (!lookupKey.equals(currentLookupKey)) {
                String displayName = cursor.getString(nameColumnIndex);
                lookupKey = currentLookupKey;
            }
            // END_INCLUDE(lookup_key)
            // BEGIN_INCLUDE(retrieve_data)
            // The data type can be determined using the mime type column.
            String mimeType = cursor.getString(typeColumnIndex);
            if (mimeType.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                mylist.add(counter, cursor.getString(phoneColumnIndex)); //this adds an element to the list.
                counter++;
                tvText = cursor.getString(phoneColumnIndex);
            } else if (mimeType.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
            }
            // END_INCLUDE(retrieve_data)

            // Look at DDMS to see all the columns returned by a query to Contactables.
            // Behold, the firehose!
            for(String column : cursor.getColumnNames()) {
                Log.d(TAG, column + column + ": " +
                        cursor.getString(cursor.getColumnIndex(column)) + "\n");
            }
        } while (cursor.moveToNext());
        outputHandler(mylist);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
    public void outputHandler(ArrayList<String> theArrayList)
    {
        if(tvText!=null)
        {
            String[]theArrayListC = theArrayList.toArray(new String[theArrayList.size()]);
            String lenght = String.valueOf(theArrayListC.length).toString();
            Log.e(TAG,lenght);
            if(theArrayListC.length==1)
            {
                String phoneNumberToCall = theArrayListC[0];
                Intent intentToCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:+"+phoneNumberToCall));
                mContext.startActivity(intentToCall);
            }
            else if(theArrayListC.length>=2)
            {
                Dialog dialog = new Dialog(mContext);
                dialog.setTitle("Multiple Contacts Found:");
                dialog.setContentView(R.layout.call_dialog_fragment);
                ListView mcfLV = (ListView)dialog.findViewById(R.id.callListView);
                ListAdapter mcfLA = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, theArrayListC);
                mcfLV.setAdapter(mcfLA);
                dialog.show();
                mcfLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String phoneNumberPicked = String.valueOf(parent.getItemAtPosition(position)).toString();
                        Intent intentToCallMC = new Intent(Intent.ACTION_CALL, Uri.parse("tel:+"+phoneNumberPicked));
                        mContext.startActivity(intentToCallMC);
                    }
                });
            }
        }
    }
}

