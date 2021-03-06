package com.qardapp.qard.profile;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.qardapp.qard.BaseFragment;
import com.qardapp.qard.QRCodeDisplayActivity;
import com.qardapp.qard.R;
import com.qardapp.qard.Services;
import com.qardapp.qard.comm.QardMessage;
import com.qardapp.qard.database.FriendsDatabaseHelper;
import com.qardapp.qard.database.FriendsProvider;
import com.qardapp.qard.friends.profile.FriendsProfileAdapter;
import com.qardapp.qard.qrcode.QRCodeManager;
import com.qardapp.qard.util.ImageUtil;

public class ProfileFragment extends BaseFragment{

	private String lastUserId = "";
	private FriendsProfileAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.profile_layout,
				container, false);
		
		Button scan = (Button) rootView.findViewById(R.id.profile_scan);
		scan.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				QRCodeManager.scanQRCode(ProfileFragment.this.getActivity());
			}
		});
		
		
		Button send = (Button) rootView.findViewById(R.id.profile_send);
		send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), QRCodeDisplayActivity.class);

				intent.putExtra("msg", QardMessage.getMessage(getActivity()));
				startActivity(intent);
			}
		});
		
		GridView gridView = (GridView) rootView.findViewById(R.id.profile_gridview);
		adapter = new FriendsProfileAdapter(
				this.getActivity(),
				null, this);
		gridView.setAdapter(adapter);
		
		return rootView;
	}

	@Override
	public void updateViews() {
		ContentResolver res = getActivity().getContentResolver();
		Cursor cursor = res.query(FriendsProvider.MY_URI, null, null, null, null);
		
		cursor.moveToFirst();
		ImageView profilePic = (ImageView) getView().findViewById(R.id.profile_image);
		profilePic.setImageBitmap(ImageUtil.getProfilePic(getActivity(), 0));
		
		TextView nameView = (TextView) getView().findViewById(R.id.profile_name);
		String first_name = cursor.getString(cursor.getColumnIndex(FriendsDatabaseHelper.COLUMN_FIRST_NAME));
		String last_name = cursor.getString(cursor.getColumnIndex(FriendsDatabaseHelper.COLUMN_LAST_NAME));
		nameView.setText(first_name + " " + last_name);
		String number = null;
		do {
			int val = cursor.getInt(cursor.getColumnIndex(FriendsDatabaseHelper.COLUMN_SERVICE_ID));
			if (val == Services.PHONE.id) {
				number = cursor.getString(cursor.getColumnIndex(FriendsDatabaseHelper.COLUMN_FS_DATA));
				if (number != null) {
					TextView phoneView = (TextView) getView().findViewById(R.id.profile_phone);
					phoneView.setText(PhoneNumberUtils.formatNumber(number));
				}
			}
		} while (cursor.moveToNext()) ;
		//cursor.close();
		if (number == null)
			number = "";
		adapter.changeCursor(cursor);
		
		/*
		SharedPreferences pref = getActivity().getSharedPreferences(getString(R.string.app_package_name), Context.MODE_PRIVATE);
		String user_id = pref.getString("user_id", "noid");
		
		
		// Don't regenerate everytime
		//if (!(lastUserId.equals(user_id))) {
			ImageView image = (ImageView) getView().findViewById(R.id.profile_qr_code);
			String msg = QardMessage.encodeMessage(user_id, first_name, last_name, number);
			QRCodeManager.genQRCode (msg, image); 
		//	lastUserId = user_id;
		//}*/
	}

}
