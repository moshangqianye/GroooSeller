package com.wenym.groooseller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Order {

	String time;
	String phoneNumber;
	String address;
	List<String> orderFood;
	List<Integer> foodCount;
	String price;
	public String status;
	String method;
	String remark;
	String id;

	public Order(JSONObject json) {
		orderFood = new ArrayList<String>();
		foodCount = new ArrayList<Integer>();
		try {
			phoneNumber = json.getString("PhoneNumber");
			address = json.getString("buildingNum") + json.getString("roomNum")
					+ "寝室";
			price = json.getString("totalPrice");
			status = json.getString("status");
			time = json.getString("time");
			method = json.getString("method").equals("0") ? "上门" : "自取";
			remark = "备注：" + json.getString("remark");
			id = json.getString("id");
			if (json.has("list")) {
				String arrayString = json.getString("list");
				JSONArray array = new JSONArray(arrayString);
				for (int i = 0; i < array.length(); i++) {
					orderFood.add(array.getJSONObject(i).getString("name"));
					foodCount.add(Integer.parseInt(array.getJSONObject(i)
							.getString("count")));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public String toString() {
		return time + " " + address + " " + phoneNumber + " " + price + " "
				+ status;
	}

}
