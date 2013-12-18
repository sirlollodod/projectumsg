package com.lollotek.umessage.adapters;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lollotek.umessage.R;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class ContactAdapter extends SimpleCursorAdapter {

	private static final String TAG = ContactAdapter.class.getName();
	
	private Cursor cursor;
	private Context context;

	private File mainFolder;
	private String contactProfileImagesFolder;

	public ContactAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to, int flags) {
		super(context, layout, c, from, to, flags);

		this.context = context;
		this.cursor = c;
		this.mainFolder = Utility.getMainFolder(context);
		this.contactProfileImagesFolder = Settings.CONTACT_PROFILE_IMAGES_FOLDER;

	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View newRow;

		newRow = inflater.inflate(R.layout.usercontact, parent, false);
		
		TextView name, prefix, num;
		ImageView icon;
		
		cursor.moveToPosition(position);
		
		name = (TextView) newRow.findViewById(R.id.textView1);
		prefix = (TextView) newRow.findViewById(R.id.textView2);
		num = (TextView) newRow.findViewById(R.id.textView3);
		
		name.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_NAME)));
		prefix.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PREFIX)));
		num.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_NUM)));
		
		String iconSrc = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_IMGSRC));
		
		if(!iconSrc.equals("0")){
			icon = (ImageView) newRow.findViewById(R.id.imageView1);
			File iconFile  = new File(mainFolder.toString() + contactProfileImagesFolder + iconSrc);
			icon.setImageURI(Uri.fromFile(iconFile));
		}
		
		return newRow;
		
	}

}
