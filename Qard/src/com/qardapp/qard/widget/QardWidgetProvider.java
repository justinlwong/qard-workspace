package com.qardapp.qard.widget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.qardapp.qard.MainActivity;
import com.qardapp.qard.R;
import com.qardapp.qard.comm.QardMessage;
import com.qardapp.qard.qrcode.QRCodeManager;

public class QardWidgetProvider extends AppWidgetProvider {
    
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	    //RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.id.qrcode_image);
	    updateSwitch(context, appWidgetManager, appWidgetIds[0]);

	}

	private void updateSwitch(Context context, AppWidgetManager appWidgetManager,
			int appWidgetIds){
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		
		//remoteViews.setImageViewResource(R.id.widget_qr, R.drawable.qrcode_image);
		//ImageView qrcode = (ImageView) findViewById(R.id.qrcode_image);
		//remoteViews.setImageViewResource(R.id.widget_qr, R.id.profile_qr_code);
		//ImageView imageView = (ImageView) findViewById(R.id.widget_qr);
		//QRCodeManager.genMyQRCode(context, imageView);
		//QardWidgetProvider myQardWidgetProvider = new QardWidgetProvider(this.parent);
		//View qr_code_image = parent.findViewById(R.id.widget_qr);
		//RemoteViews qr_code_image = new RemoteViews(context.getPackageName(),R.id.widget_qr);
		
		//remoteViews.setImageViewResource(R.id.widget_qr, getImageToSet());
		String msg = QardMessage.getMessage(context);
		remoteViews.setImageViewBitmap(R.id.widget_qr, QRCodeManager.genQRCodeBitmap(msg, 10));
		//change images
		//GAVIN
		//remoteViews.setOnClickPendingIntent(R.id.widget_button, buildButtonPendingIntent(context));
		pushWidgetUpdate(context, remoteViews);
		
		//Camera
	    Intent configIntent = new Intent(context, MainActivity.class);
	    configIntent.putExtra("widgetAction", "Scan");
	    PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 1);
	    //GAVIN
	    //remoteViews.setOnClickPendingIntent(R.id.widget_camera, configPendingIntent);
	    
	    configIntent.putExtra("widgetAction", "QR");
	    configPendingIntent = PendingIntent.getActivity(context, 1, configIntent, 1);
	    remoteViews.setOnClickPendingIntent(R.id.widget_qr, configPendingIntent);
	    appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}
	public static PendingIntent buildButtonPendingIntent(Context context) {
		Intent intent = new Intent();
	    intent.setAction("pl.looksok.intent.action.CHANGE_PICTURE");
	    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
		ComponentName myWidget = new ComponentName(context, QardWidgetProvider.class);
	    AppWidgetManager manager = AppWidgetManager.getInstance(context);
	    manager.updateAppWidget(myWidget, remoteViews);		
	}
}