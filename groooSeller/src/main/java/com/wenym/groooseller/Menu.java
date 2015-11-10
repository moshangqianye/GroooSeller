package com.wenym.groooseller;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Menu {

	public String className;

	public List<String> food;
	public List<String> price;
	public List<String> packageprice;
	public List<Boolean> isOnTop;
	public List<Integer> foodid;

	public boolean isTop;

	public Menu(JSONObject json) {
		super();
		food = new ArrayList<String>();
		price = new ArrayList<String>();
		isOnTop = new ArrayList<Boolean>();
		foodid = new ArrayList<Integer>();
		packageprice = new ArrayList<String>();
		try {
			className = json.getString("foodclass");
			JSONArray jsonArray = json.getJSONArray("foodlist");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				food.add(jsonObject.getString("name"));
				price.add(jsonObject.getString("price"));
				isOnTop.add(jsonObject.getBoolean("ontop"));
				foodid.add(jsonObject.getInt("id"));
				isTop = jsonObject.getBoolean("ontop");
				packageprice.add(jsonObject.getString("packageprice"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Menu(String className, boolean b) {
		super();
		this.className = className;
		food = new ArrayList<String>();
		price = new ArrayList<String>();
		isOnTop = new ArrayList<Boolean>();
		foodid = new ArrayList<Integer>();
		packageprice = new ArrayList<String>();
		isTop = b;
	}

}
