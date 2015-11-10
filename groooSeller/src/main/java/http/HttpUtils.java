package http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.wenym.groooseller.MainActivity;
import com.wenym.groooseller.Menu;
import com.wenym.groooseller.MyClassItemFragment;
import com.wenym.groooseller.MyLoginActivity;
import com.wenym.groooseller.MyMenuFragment;
import com.wenym.groooseller.MyShopFragment;
import com.wenym.groooseller.Order;
import com.wenym.groooseller.SellerApplication;
import com.wenym.groooseller.utils.Toasts;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

public class HttpUtils {

	private static AsyncHttpClient httpClient;

	public static void init() {
		httpClient = new AsyncHttpClient();
		httpClient.setEnableRedirects(true);
	}
	
	public static AsyncHttpClient getHttpClient() {
		return httpClient;
	}

	public static int getVersionCode(Context context) {
		int versionCode = 0;
		try {
			// 获取软件版本号，
			versionCode = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	public static boolean isConnect(Context context) {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 获取网络连接管理的对象
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					// 判断当前网络是否已经连接
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Log.v("error", e.toString());
		}
		return false;
	}

	public static void checkUpdate(final Handler handler, final Context context) {
		httpClient.post(SellerApplication.update,
				new TextHttpResponseHandler() {

					@Override
					public void onSuccess(int arg0, Header[] arg1, String arg2) {
						if (getVersionCode(context) >= Integer.parseInt(arg2)) {
							handler.sendMessage(handler
									.obtainMessage(MyLoginActivity.NO_UPDATE));
						} else {
							handler.sendMessage(handler
									.obtainMessage(MyLoginActivity.HAS_UPDATE));
						}
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, String arg2,
							Throwable arg3) {
						Toasts.show("CheckUpdate " + arg0);
					}
				});
	}

	public static void onSubmit(final Handler handler, final Context context) {
		SellerApplication.orderOK = new ArrayList<Order>();
		SellerApplication.orderNotOK = new ArrayList<Order>();
		SellerApplication.orderToday = new ArrayList<Order>();
		final SharedPreferences mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		Thread trd = new Thread(new Runnable() {
			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.url);
				try {
					// parameters of HTTP request
					JSONObject param = new JSONObject();
					param.put("username", SellerApplication.username);
					param.put("password", SellerApplication.password);
					param.put("channelid",getVersionCode(context));
					param.put("userid",
							mSharedPreferences.getString("userId", JPushInterface.getRegistrationID(context)));
					// Make API call
					StringEntity se = new StringEntity(param.toString());
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("error".equals(sResponse)) {
							handler.sendMessage(handler
									.obtainMessage(MyLoginActivity.LOGIN_ERROR));
						} else {
							JSONObject result = new JSONObject(sResponse);
							if (result.has("logo")) {
								SellerApplication.logoUrl = result
										.getString("logo");
								SharedPreferences.Editor editor = mSharedPreferences
										.edit();
								editor.putString("logourl",
										SellerApplication.logoUrl);
								editor.commit();
							}
							if (result.has("shopname")) {
								SellerApplication.shopName = result
										.getString("shopname");
							}
							if (result.has("status")) {
								SellerApplication.status = result
										.getString("status");
							}
							if (result.has("sid")) {
								SellerApplication.shopId = result
										.getString("sid");
							}
							if (result.has("order")) {
								JSONArray jsonArray = result
										.getJSONArray("order");
								for (int i = 0; i < jsonArray.length(); i++) {
									Order order = new Order(jsonArray
											.getJSONObject(i));
									SellerApplication.orderToday.add(order);
									if ("0".equals(order.status)
											|| "2".equals(order.status)) {
										SellerApplication.orderNotOK.add(order);
									} else {
										SellerApplication.orderOK.add(order);
									}
								}
							}
							handler.sendMessage(handler
									.obtainMessage(MyLoginActivity.LOGIN_SUCCESS));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		trd.start();
	}

	public static void autoLogin(final Handler handler, final Context context) {
		SellerApplication.orderOK = new ArrayList<Order>();
		SellerApplication.orderNotOK = new ArrayList<Order>();
		SellerApplication.orderToday = new ArrayList<Order>();
		StringEntity se = null;
		// parameters of HTTP request
		JSONObject param = new JSONObject();
		try {
			param.put("username",
					PreferenceManager.getDefaultSharedPreferences(context)
							.getString("username", SellerApplication.username));
			param.put("password",
					PreferenceManager.getDefaultSharedPreferences(context)
							.getString("password", SellerApplication.password));
			param.put("channelid",getVersionCode(context));
			param.put("userid",
					PreferenceManager.getDefaultSharedPreferences(context)
							.getString("userId", JPushInterface.getRegistrationID(context)));
			// Make API call
			se = new StringEntity(param.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpClient.post(context, SellerApplication.url, se,
				"application/x-www-form-urlencoded",
				new TextHttpResponseHandler() {

					@Override
					public void onSuccess(int arg0, Header[] arg1,
							String sResponse) {
						try {
							if ("error".equals(sResponse)) {
								handler.sendMessage(handler
										.obtainMessage(MyLoginActivity.LOGIN_ERROR));
							} else {
								JSONObject result = new JSONObject(sResponse);

								if (result.has("logo")) {
									SellerApplication.logoUrl = result
											.getString("logo");
								}
								if (result.has("shopname")) {
									SellerApplication.shopName = result
											.getString("shopname");
								}
								if (result.has("status")) {
									SellerApplication.status = result
											.getString("status");
								}
								if (result.has("sid")) {
									SellerApplication.shopId = result
											.getString("sid");
								}
								if (result.has("order")) {
									JSONArray jsonArray = result
											.getJSONArray("order");
									for (int i = 0; i < jsonArray.length(); i++) {
										Order order = new Order(jsonArray
												.getJSONObject(i));
										SellerApplication.orderToday.add(order);
										if ("0".equals(order.status)
												|| "2".equals(order.status)) {
											SellerApplication.orderNotOK
													.add(order);
										} else {
											SellerApplication.orderOK
													.add(order);
										}
									}
								}
								handler.sendMessage(handler
										.obtainMessage(MyLoginActivity.AUTOLOGIN_SUCCESS));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

					@Override
					public void onFailure(int arg0, Header[] arg1, String arg2,
							Throwable arg3) {
						Toasts.show("AutoLogin " + arg0);
						handler.sendMessage(handler
								.obtainMessage(MyLoginActivity.LOGIN_ERROR));
					}
				});

	}

	public static boolean reloadOrder(final Handler handler,
			final Context context) {
		SellerApplication.orderOK = new ArrayList<Order>();
		SellerApplication.orderNotOK = new ArrayList<Order>();
		SellerApplication.orderToday = new ArrayList<Order>();
		new Handler().postDelayed(new Runnable() {
			public void run() {
				handler.sendMessage(handler
						.obtainMessage(MainActivity.RELOAD_FAILED));
			}
		}, 20000);
		Thread trd = new Thread(new Runnable() {

			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.url);
				try {
					// parameters of HTTP request
					JSONObject param = new JSONObject();
					param.put(
							"username",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("username",
									SellerApplication.username));
					param.put(
							"password",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("password",
									SellerApplication.password));
					param.put(
							"channelid",getVersionCode(context));
					param.put(
							"userid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					// Make API call
					StringEntity se = new StringEntity(param.toString());
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						JSONObject result = new JSONObject(sResponse);
						if (result.has("logo")) {
							SellerApplication.logoUrl = result
									.getString("logo");
						}
						if (result.has("shopname")) {
							SellerApplication.shopName = result
									.getString("shopname");
						}
						if (result.has("status")) {
							SellerApplication.status = result
									.getString("status");
						}
						if (result.has("sid")) {
							SellerApplication.shopId = result.getString("sid");
						}
						if (result.has("order")) {
							JSONArray jsonArray = result.getJSONArray("order");
							for (int i = 0; i < jsonArray.length(); i++) {
								Order order = new Order(jsonArray
										.getJSONObject(i));
								SellerApplication.orderToday.add(order);
								if ("0".equals(order.status)
										|| "2".equals(order.status)) {
									SellerApplication.orderNotOK.add(order);
								} else {
									SellerApplication.orderOK.add(order);
								}
							}
						}
						handler.sendMessage(handler
								.obtainMessage(MainActivity.RELOAD_SUCCESS));
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		trd.start();
		return true;
	}

	public static boolean getMenu(final Handler handler, final Context context) {
		SellerApplication.menu = new ArrayList<Menu>();
		new Handler().postDelayed(new Runnable() {
			public void run() {
				handler.sendMessage(handler
						.obtainMessage(MyMenuFragment.MENU_FAILED));
			}
		}, 20000);
		Thread trd = new Thread(new Runnable() {

			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/info/getmenu/");
				try {
					// parameters of HTTP request
					JSONObject param = new JSONObject();
					param.put(
							"channelid",getVersionCode(context));
					param.put(
							"userid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					// Make API call
					StringEntity se = new StringEntity(param.toString());
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						JSONArray result = new JSONArray(sResponse);
						for (int i = 0; i < result.length(); i++) {
							JSONObject jsonObject = result.getJSONObject(i);
							Menu menu = new Menu(jsonObject);
							SellerApplication.menu.add(menu);
						}
						handler.sendMessage(handler
								.obtainMessage(MyMenuFragment.MENU_SUCCESS));
					} else {
						handler.sendMessage(handler
								.obtainMessage(MyMenuFragment.MENU_FAILED));
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});
		trd.start();
		return true;
	}

	// public static boolean pushMenu(final Handler handler, final Context
	// context) {
	// new Handler().postDelayed(new Runnable() {
	// public void run() {
	// handler.sendMessage(handler
	// .obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
	// }
	// }, 20000);
	// Thread trd = new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// String sResponse = null;
	// // 要传递的参数.
	// HttpPost httppost = new HttpPost(SellerApplication.urlNormal
	// + SellerApplication.shopId + "/info/pushmenu/");
	// try {
	// // parameters of HTTP request
	// JSONObject param = new JSONObject();
	// param.put(
	// "channelid",
	// PreferenceManager.getDefaultSharedPreferences(
	// context).getString("channelId",
	// "233"));
	// param.put(
	// "userid",
	// PreferenceManager.getDefaultSharedPreferences(
	// context).getString("userId",
	// null));
	// JSONArray jsonArray = new JSONArray();
	// for (int i = 0; i < SellerApplication.menu.size(); i++) {
	// JSONObject object = new JSONObject();
	// Menu menu = SellerApplication.menu.get(i);
	// object.put("foodclass", menu.className);
	// JSONArray jsonArray2 = new JSONArray();
	// for (int j = 0; j < menu.food.size(); j++) {
	// JSONObject object2 = new JSONObject();
	// object2.put("name", menu.food.get(j));
	// object2.put("price", menu.price.get(j));
	// object2.put("ontop", menu.isOnTop.get(j));
	// jsonArray2.put(object2);
	// }
	// object.put("foodlist", jsonArray2);
	// jsonArray.put(object);
	// }
	// param.put("menu", jsonArray);
	// // Make API call
	// StringEntity se = new StringEntity(param.toString(),
	// HTTP.UTF_8);
	// se.setContentType("application/x-www-form-urlencoded");
	// httppost.setEntity(se);
	// HttpResponse response = new DefaultHttpClient()
	// .execute(httppost);
	// HttpEntity responseEntity = response.getEntity();
	// if (responseEntity != null) {
	// sResponse = EntityUtils.toString(responseEntity);
	// Log.v("json_result", sResponse);
	// if ("1".equals(sResponse)) {
	// handler.sendMessage(handler
	// .obtainMessage(MyMenuFragment.PUSHMENU_SUCCESS));
	// } else if ("error".equals(sResponse)) {
	// handler.sendMessage(handler
	// .obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
	// }
	// }
	// } catch (ClientProtocolException e) {
	// Log.v("ClientProtocolException", e.getMessage());
	// } catch (IOException e) {
	// Log.v("IOException", e.getMessage());
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// });
	// trd.start();
	// return true;
	// }

	public static boolean addFood(final Handler handler, final Context context,
			final JSONObject jsonObject) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				handler.sendMessage(handler
						.obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
			}
		}, 20000);
		Thread trd = new Thread(new Runnable() {

			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/menu/food/add/");
				try {
					// parameters of HTTP request
					jsonObject.put(
							"channelid",getVersionCode(context));
					jsonObject.put(
							"userid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					StringEntity se = new StringEntity(jsonObject.toString(),
							HTTP.UTF_8);
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("error".equals(sResponse)
								|| sResponse.contains("Error")) {
							handler.sendMessage(handler
									.obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
						} else {
							handler.sendMessage(handler
									.obtainMessage(
											MyClassItemFragment.ADD_SUCCESS,
											jsonObject));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});
		trd.start();
		return true;
	}

	public static boolean whatFood(final Handler handler,
			final Context context, final JSONObject jsonObject) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				handler.sendMessage(handler
						.obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
			}
		}, 20000);
		Thread trd = new Thread(new Runnable() {

			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/menu/food/mod/");
				try {
					// parameters of HTTP request
					jsonObject.put(
							"channelid",getVersionCode(context));
					jsonObject.put(
							"userid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					StringEntity se = new StringEntity(jsonObject.toString(),
							HTTP.UTF_8);
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("error".equals(sResponse)
								|| sResponse.contains("Error")) {
							handler.sendMessage(handler
									.obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
						} else {
							handler.sendMessage(handler
									.obtainMessage(
											MyClassItemFragment.MOD_SUCCESS,
											jsonObject));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});
		trd.start();
		return true;
	}

	public static boolean delFood(final Handler handler, final Context context,
			final JSONObject jsonObject) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				handler.sendMessage(handler
						.obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
			}
		}, 20000);
		Thread trd = new Thread(new Runnable() {

			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/menu/food/del/");
				try {
					// parameters of HTTP request
					jsonObject.put(
							"channelid",getVersionCode(context));
					jsonObject.put(
							"userid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					StringEntity se = new StringEntity(jsonObject.toString(),
							HTTP.UTF_8);
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("error".equals(sResponse)
								|| sResponse.contains("Error")) {
							handler.sendMessage(handler
									.obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
						} else {
							handler.sendMessage(handler
									.obtainMessage(
											MyClassItemFragment.DEL_SUCCESS,
											jsonObject));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});
		trd.start();
		return true;
	}

	public static boolean delClass(final Handler handler,
			final Context context, final String name) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				handler.sendMessage(handler
						.obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
			}
		}, 20000);
		Thread trd = new Thread(new Runnable() {

			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/menu/class/del/");
				try {
					// parameters of HTTP request
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("name", name);
					jsonObject.put(
							"channelid",getVersionCode(context));
					jsonObject.put(
							"userid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					StringEntity se = new StringEntity(jsonObject.toString(),
							HTTP.UTF_8);
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("error".equals(sResponse)
								|| sResponse.contains("Error")) {
							handler.sendMessage(handler
									.obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
						} else {
							handler.sendMessage(handler.obtainMessage(
									MyClassItemFragment.DEL_SUCCESS, name));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});
		trd.start();
		return true;
	}

	public static boolean renClass(final Handler handler,
			final Context context, final String oldname, final String newname) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				handler.sendMessage(handler
						.obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
			}
		}, 20000);
		Thread trd = new Thread(new Runnable() {

			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/menu/class/ren/");
				try {
					// parameters of HTTP request
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("origin", oldname);
					jsonObject.put("current", newname);
					jsonObject.put(
							"channelid",getVersionCode(context));
					jsonObject.put(
							"userid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					StringEntity se = new StringEntity(jsonObject.toString(),
							HTTP.UTF_8);
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("error".equals(sResponse)
								|| sResponse.contains("Error")) {
							handler.sendMessage(handler
									.obtainMessage(MyMenuFragment.PUSHMENU_FAILED));
						} else {
							handler.sendMessage(handler.obtainMessage(
									MyClassItemFragment.MOD_SUCCESS, oldname
											+ "@" + newname));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});
		trd.start();
		return true;
	}

	public static boolean postOK(final Handler handler, final String id,
			final int position) {
		Thread trd = new Thread(new Runnable() {
			@Override
			public void run() {
				String sResponse = null;

				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/order/" + id
						+ "/finish/");
				try {
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("1".equals(sResponse)) {
							handler.sendMessage(handler.obtainMessage(
									MainActivity.FINISH_SUCCESS,
									SellerApplication.orderNotOK.get(position)));
						} else {
							handler.sendMessage(handler
									.obtainMessage(MainActivity.FINISH_FAILED));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (IndexOutOfBoundsException e) {
					handler.sendMessage(handler
							.obtainMessage(MainActivity.FINISH_FAILED));
				}
			}
		});
		trd.start();
		return true;
	}

	public static boolean postNotOK(final Handler handler, final String id,
			final int position) {
		Thread trd = new Thread(new Runnable() {
			@Override
			public void run() {
				String sResponse = null;

				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/order/" + id
						+ "/finish/");
				try {
					// parameters of HTTP request
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("1".equals(sResponse)) {
							handler.sendMessage(handler.obtainMessage(
									MainActivity.UNFINISH_SUCCESS,
									SellerApplication.orderOK.get(position)));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (IndexOutOfBoundsException e) {
					handler.sendMessage(handler
							.obtainMessage(MainActivity.UNFINISH_FAILED));
				}
			}
		});
		trd.start();
		return true;
	}

	public static boolean postBack(final Handler handler, final String id,
			final int position, final Context context) {
		Thread trd = new Thread(new Runnable() {
			@Override
			public void run() {
				String sResponse = null;

				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/order/" + id
						+ "/cancel/");
				try {
					// parameters of HTTP request
					httppost.addHeader(
							"uesrid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("1".equals(sResponse)) {
							handler.sendMessage(handler.obtainMessage(
									MainActivity.BACK_SUCCESS,
									SellerApplication.orderNotOK.get(position)));
						} else {
							handler.sendMessage(handler
									.obtainMessage(MainActivity.BACK_FAILED));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (IndexOutOfBoundsException e) {
					handler.sendMessage(handler
							.obtainMessage(MainActivity.BACK_FAILED));
				}
			}
		});
		trd.start();
		return true;
	}

	public static boolean startSell(final Context context, final Handler handler) {
		Thread trd = new Thread(new Runnable() {
			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/info/start/");
				try {
					// parameters of HTTP request
					JSONObject param = new JSONObject();
					param.put(
							"userid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					// Make API call
					StringEntity se = new StringEntity(param.toString());
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("1".equals(sResponse)) {
							SellerApplication.status = "true";
							handler.sendMessage(handler
									.obtainMessage(MainActivity.START_SUCCESS));
						} else if ("0".equals(sResponse)) {
							handler.sendMessage(handler
									.obtainMessage(MainActivity.START_FAILED));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		trd.start();
		return true;
	}

	public static boolean endSell(final Context context, final Handler handler) {
		Thread trd = new Thread(new Runnable() {
			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/info/end/");
				try {
					// parameters of HTTP request
					JSONObject param = new JSONObject();
					param.put(
							"userid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					// Make API call
					StringEntity se = new StringEntity(param.toString());
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("1".equals(sResponse)) {
							SellerApplication.status = "false";
							handler.sendMessage(handler
									.obtainMessage(MainActivity.END_SUCCESS));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		trd.start();
		return true;
	}

	public static boolean setAnnouncement(final Context context,
			final Handler handler, final String string) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				handler.sendMessage(handler
						.obtainMessage(MyShopFragment.SETANNOUNCEMENT_FAILED));
			}
		}, 20000);
		Thread trd = new Thread(new Runnable() {
			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/info/setAnnouncement/");
				try {
					StringEntity se = new StringEntity(string, HTTP.UTF_8);
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					httppost.addHeader(
							"uesrid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					// Make API call
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("1".equals(sResponse)) {
							handler.sendMessage(handler
									.obtainMessage(MyShopFragment.SETANNOUNCEMENT_SUCCESS));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				}
			}
		});
		trd.start();
		return true;
	}

	public static boolean getAnnouncement(final Context context,
			final Handler handler) {
		Thread trd = new Thread(new Runnable() {
			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/info/getAnnouncement/");
				try {
					// parameters of HTTP request
					httppost.addHeader(
							"uesrid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					// Make API call
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						SellerApplication.gonggao = sResponse;
						handler.sendMessage(handler
								.obtainMessage(MyShopFragment.GET_SUCCESS));
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				}
			}
		});
		trd.start();
		return true;
	}

	public static boolean setDescription(final Context context,
			final Handler handler, final String string) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				handler.sendMessage(handler
						.obtainMessage(MyShopFragment.SETDESCRIPTION_FAILED));
			}
		}, 20000);
		Thread trd = new Thread(new Runnable() {
			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/info/setDescription/");
				try {
					StringEntity se = new StringEntity(string, HTTP.UTF_8);
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					httppost.addHeader(
							"uesrid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					// Make API call
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("1".equals(sResponse)) {
							handler.sendMessage(handler
									.obtainMessage(MyShopFragment.SETDESCRIPTION_SUCCESS));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				}
			}
		});
		trd.start();
		return true;
	}

	public static boolean getDescription(final Context context,
			final Handler handler) {
		Thread trd = new Thread(new Runnable() {
			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/info/getDescription/");
				try {
					// parameters of HTTP request
					httppost.addHeader(
							"uesrid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					// Make API call
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						SellerApplication.jieshao = sResponse;
						handler.sendMessage(handler
								.obtainMessage(MyShopFragment.GET_SUCCESS));
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				}
			}
		});
		trd.start();
		return true;
	}

	public static boolean changePassword(final Handler handler,
			final Context context, final String currPassword,
			final String newPassword) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				handler.sendMessage(handler
						.obtainMessage(MainActivity.CHANGE_FAILED));
			}
		}, 20000);
		Thread trd = new Thread(new Runnable() {

			@Override
			public void run() {
				String sResponse = null;
				// 要传递的参数.
				HttpPost httppost = new HttpPost(SellerApplication.urlNormal
						+ SellerApplication.shopId + "/info/changePassword/");
				try {
					// parameters of HTTP request
					JSONObject param = new JSONObject();
					param.put(
							"channelid",getVersionCode(context));
					param.put(
							"userid",
							PreferenceManager.getDefaultSharedPreferences(
									context).getString("userId", JPushInterface.getRegistrationID(context)));
					param.put("oldpassword", currPassword);
					param.put("newpassword", newPassword);
					// Make API call
					StringEntity se = new StringEntity(param.toString(),
							HTTP.UTF_8);
					se.setContentType("application/x-www-form-urlencoded");
					httppost.setEntity(se);
					HttpResponse response = new DefaultHttpClient()
							.execute(httppost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						sResponse = EntityUtils.toString(responseEntity);
						Log.v("json_result", sResponse);
						if ("1".equals(sResponse)) {
							handler.sendMessage(handler
									.obtainMessage(MainActivity.CHANGE_SUCCESS));
						} else if ("error".equals(sResponse)) {
							handler.sendMessage(handler
									.obtainMessage(MainActivity.CHANGE_FAILED));
						}
					}
				} catch (ClientProtocolException e) {
					Log.v("ClientProtocolException", e.getMessage());
				} catch (IOException e) {
					Log.v("IOException", e.getMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});
		trd.start();
		return true;
	}
}
