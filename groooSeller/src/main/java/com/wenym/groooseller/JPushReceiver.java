package com.wenym.groooseller;


import cn.grooo.seller.grooo_seller.R;
import cn.jpush.android.api.JPushInterface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class JPushReceiver extends BroadcastReceiver {

	public static MediaPlayer player;

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Log.d("JPushReceiver", "onReceive - " + intent.getAction());
		
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			String title = bundle
					.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			SharedPreferences mSharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			Toast.makeText(context, title, Toast.LENGTH_SHORT).show();
			editor.putString("userId", title);
			editor.commit();
		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
				.getAction())) {
			System.out.println("�յ����Զ�����Ϣ����Ϣ�����ǣ�"
					+ bundle.getString(JPushInterface.EXTRA_MESSAGE));
			if (bundle.getString(JPushInterface.EXTRA_MESSAGE).equals("play")) {
				if (null == player) {
					System.out.println("new");
					player = MediaPlayer.create(context, R.raw.beep);
					player.setLooping(true);
				}
				if (!player.isPlaying()) {
					System.out.println("play");
					player.start();
				}
			} else if (bundle.getString(JPushInterface.EXTRA_MESSAGE).equals(
					"pause")) {
				if (player.isPlaying()) {
					player.pause();
				}
			}
			// �Զ�����Ϣ����չʾ��֪ͨ������ȫҪ������д����ȥ����
		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
				.getAction())) {
			System.out.println("�յ���֪ͨ");
			// �����������Щͳ�ƣ�������Щ��������
		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
				.getAction())) {
			System.out.println("�û��������֪ͨ");
			// ����������Լ�д����ȥ�����û���������Ϊ
			Intent i = new Intent(context, MainActivity.class); // �Զ���򿪵Ľ���
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		} else {
			Log.d("JPushReceiver", "Unhandled intent - " + intent.getAction());
		}

	}

}
