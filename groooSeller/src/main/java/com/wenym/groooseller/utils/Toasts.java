package com.wenym.groooseller.utils;

import com.wenym.groooseller.SellerApplication;

import android.widget.Toast;

public class Toasts {

	public static void show(String message) {
		Toast.makeText(SellerApplication.getSellerContext(), message,
				Toast.LENGTH_SHORT).show();
	}

}
