package com.qardapp.qard.qrcode;

import java.util.Hashtable;

import net.sourceforge.zbar.Symbol;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class QRCodeManager {

	public static int SCANNER_REQUEST_CODE = 392;
	
	private static int QRCODE_COLOR_FOREGROUND = 0xFF000000; // Black
	private static int QRCODE_COLOR_BACKGROUND = 0x00FFFFFF; // Transparent
	
	public static void scanQRCode (Activity activity) {
		Intent intent = new Intent(activity, ZBarScannerActivity.class);
		intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
		activity.startActivityForResult(intent, SCANNER_REQUEST_CODE);
	}
	
	public static String checkScanActivityResult(int requestCode, int resultCode, Intent data)
	{    
		if (requestCode == SCANNER_REQUEST_CODE) {
		    if (resultCode == Activity.RESULT_OK) 
		    {
		        // Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
		        // Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
		        return (data.getStringExtra(ZBarConstants.SCAN_RESULT));
		        //Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE), Toast.LENGTH_SHORT).show();
		        // The value of type indicates one of the symbols listed in Advanced Options below.
		    } else if(resultCode == Activity.RESULT_CANCELED) {
		       // Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
		    	return null;
		    }
		}
		return null;
	}
	
	public static ImageView genQRCode (String text, ImageView imageView, int scale) {
		Hashtable<EncodeHintType, Object> hintMap = new Hashtable<EncodeHintType, Object>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
			BitMatrix bitMatrix = qrCodeWriter.encode(text,
			        BarcodeFormat.QR_CODE, imageView.getWidth(), imageView.getHeight(), hintMap);
			int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++)
            {
                int offset = y * width;
                for (int x = 0; x < width; x++)
                {
                     pixels[offset + x] = bitMatrix.get(x, y) ? QRCODE_COLOR_FOREGROUND
                     : QRCODE_COLOR_BACKGROUND;
                    //pixels[offset + x] = bitMatrix.get(x, y) ? colorBack : colorFront;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width*scale, height*scale, false);
			imageView.setImageBitmap(scaled);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return imageView;
        
	}
}
