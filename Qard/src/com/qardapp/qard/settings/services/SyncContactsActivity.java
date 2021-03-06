package com.qardapp.qard.settings.services;

import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import com.qardapp.qard.R;
import com.qardapp.qard.Services;
import com.qardapp.qard.database.FriendsDatabaseHelper;
import com.qardapp.qard.database.FriendsProvider;

public class SyncContactsActivity extends Activity {
	
	Activity a;
	
    public void updateDatabase(ContentResolver cr, String first, String last, String phone, String email)
    {
    	String lastname = "";
    	if (last == null) {
    	    lastname = "";
    	}
    	else {
    		lastname = last;
    	}
    	// Adding Contact
		ContentValues values = new ContentValues();
		values.put(FriendsDatabaseHelper.COLUMN_TITLE, "");
		values.put(FriendsDatabaseHelper.COLUMN_FIRST_NAME, first);
		values.put(FriendsDatabaseHelper.COLUMN_LAST_NAME, lastname);
		values.put(FriendsDatabaseHelper.COLUMN_PROFILE_PIC_LOC, "");
		values.put(FriendsDatabaseHelper.COLUMN_DATE_ADDED, new Date().getTime());
		
		// Delete existing entry and replace
		String fsprefix = FriendsDatabaseHelper.TABLE_FRIEND_SERVICES + ".";
		String fprefix = FriendsDatabaseHelper.TABLE_FRIENDS + ".";
		String where = fprefix+FriendsDatabaseHelper.COLUMN_FIRST_NAME+ "=? AND " + fprefix+FriendsDatabaseHelper.COLUMN_LAST_NAME + "=? AND " + fsprefix+FriendsDatabaseHelper.COLUMN_FS_SERVICE_ID + "=?";
		String[] args = new String[] { first, lastname, String.valueOf(Services.PHONE.id)};
		Cursor c = cr.query(Uri.withAppendedPath(FriendsProvider.CONTENT_URI, "/service_data"), new String[]{fsprefix+FriendsDatabaseHelper.COLUMN_FS_DATA}, where, args, null);
		
		if (c.moveToFirst())	
		{
			while (c.isAfterLast() == false)
			{
				String phoneMatch = c.getString(c.getColumnIndex(FriendsDatabaseHelper.COLUMN_FS_DATA));  
				Log.d("phone",phoneMatch);
				// Duplicate so don't add this
				if (phoneMatch.equals(phone))
				{
					return;
				} else {
					Log.d("norepeat",phone + " " + phoneMatch);
				}
				c.moveToNext();
			}

		}
		
		c.close();
		
		Uri uri = cr.insert(Uri.withAppendedPath(FriendsProvider.CONTENT_URI, "/0"), values);
		int user_id = Integer.parseInt(uri.getLastPathSegment());
		
		// Use ID to add services
		ContentValues pvalues = new ContentValues();
		ContentValues evalues = new ContentValues();
		pvalues.put(FriendsDatabaseHelper.COLUMN_FS_FRIEND_ID, user_id);
		pvalues.put(FriendsDatabaseHelper.COLUMN_FS_SERVICE_ID, Services.PHONE.id);
		pvalues.put(FriendsDatabaseHelper.COLUMN_FS_DATA, phone);
		cr.insert(Uri.withAppendedPath(FriendsProvider.CONTENT_URI, "/0/service/"+Services.PHONE.id), pvalues);
		
		if (email != null)
		{
			evalues.put(FriendsDatabaseHelper.COLUMN_FS_FRIEND_ID, user_id);
			evalues.put(FriendsDatabaseHelper.COLUMN_FS_SERVICE_ID, Services.EMAIL.id);
			evalues.put(FriendsDatabaseHelper.COLUMN_FS_DATA, email);		
			cr.insert(Uri.withAppendedPath(FriendsProvider.CONTENT_URI, "/0/service/"+Services.EMAIL.id), evalues);
		}
		
    }
	
	private class RetrieveContacts extends AsyncTask<Void, Void, Void> {
		
		ProgressDialog progDialog;
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDialog = new ProgressDialog(SyncContactsActivity.this);
            progDialog.setMessage("Syncing...");
            progDialog.setIndeterminate(true);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setCancelable(false);
            progDialog.show();
        }
		
	     protected Void doInBackground(Void... v) {
	         ContentResolver cr = getContentResolver();
	         Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, new String [] {ContactsContract.Contacts._ID,ContactsContract.Contacts.HAS_PHONE_NUMBER},
	                 null, null, null);
	         
	         if (cur.getCount() > 0) {
	             while (cur.moveToNext()) {
	            	 
	                 String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
	                 Log.d("here",id);
                     String hasPhone = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                     if (hasPhone.equalsIgnoreCase("1")) 
                     {
                    	 
                    	  String cNumber = null, cEmail = null;
                    	  String first = null;
                    	  String last = null;
                    	  
                    	  String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = " + id; 
                    	  String[] whereNameParams = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
                    	 
                          Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER}, 
                                 ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                          Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,new String[] {ContactsContract.CommonDataKinds.Email.DATA}, 
                                  ContactsContract.CommonDataKinds.Email.CONTACT_ID +" = "+ id,null, null);
                          Cursor nameCur = cr.query(ContactsContract.Data.CONTENT_URI, new String[] {ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME}, whereName, whereNameParams, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
                          //Cursor wCur = cr.query(ContactsContract.Data.CONTENT_URI, new String[] {ContactsContract.Data.DATA1, ContactsContract.Data.MIMETYPE},ContactsContract.Contacts.Data._ID + " = " + id, null, null);
                          
                          if (phones.moveToFirst())
                          {
                              cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));                       	  
                          }

                          if (emails.moveToFirst()) {
                              cEmail = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));                        	  
                          }
                          
                          while (nameCur.moveToNext()) { 
                        	    first = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                        	    last = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                          }
                          
//                          while (wCur.moveToNext()) { 
//                      	    String wid = wCur.getString(wCur.getColumnIndex(ContactsContract.Data.DATA1));
//                      	    String mtype = wCur.getString(wCur.getColumnIndex(ContactsContract.Data.MIMETYPE));
//                      	    if (wid != null)
//                      	    {
//                      	    	Log.d("wid",wid + " " + mtype );
//                      	    }
//                        }
                                                  
                          
                          if (cEmail != null)
                          {
    		                  Log.d("here",first + " " + cNumber + " " + cEmail);                      	  
                          }
                          
                          // format phone number before adding
                          
                          // strip spaces
                          cNumber = cNumber.replaceAll("[^\\d.]","");
                          //cNumber = cNumber.replace("(","").replace(")","").replace("+","").replace("-", "").replace(" ", "");
                          // leave country code in ... but on server we have to check for both possibility of no country code or country code
                          //cNumber = cNumber.startsWith("1") ? cNumber.substring(1) : cNumber;
                          
                          
                          // Update Database
                          updateDatabase(cr, first, last, cNumber, cEmail);
                          
                          phones.close();
                          emails.close();
                          nameCur.close();

		             }
	             }
	             Cursor wCur = cr.query(ContactsContract.Data.CONTENT_URI, new String [] {ContactsContract.Data._ID, ContactsContract.Data.DATA1, ContactsContract.Data.MIMETYPE}, null, null, null);
                 
                 while (wCur.moveToNext()) { 
             	    String wid = wCur.getString(wCur.getColumnIndex(ContactsContract.Data.DATA1));
             	    String mtype = wCur.getString(wCur.getColumnIndex(ContactsContract.Data.MIMETYPE));
             	    if (wid != null)
             	    {
             	    	Log.d("wid",wid + " " + mtype );
             	    }
                 }
             
	         }
	         
	         cur.close();

			return null;

	     }
	     
		@Override  
		protected void onPostExecute(Void v) { 
			a.finish();
		}

/*	     protected void onProgressUpdate(Integer... progress) {
	         setProgressPercent(progress[0]);
	     }

	     protected void onPostExecute(Long result) {
	         showDialog("Downloaded " + result + " bytes");
	     }*/
	 }
	
	public static int PICK_CONTACT = 1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth_layout);
        
        a = this;
        
        new RetrieveContacts().execute();  
        //new SendInviteActivity();
        SendInviteActivity.sendInvite(this);

        //finish();
       
    }

}
