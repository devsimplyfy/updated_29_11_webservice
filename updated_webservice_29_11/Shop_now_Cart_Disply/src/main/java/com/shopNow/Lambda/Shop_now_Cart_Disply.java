package com.shopNow.Lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Shop_now_Cart_Disply implements RequestHandler<JSONObject, JSONObject> {

	@SuppressWarnings({ "unchecked", "unused" })
	public JSONObject handleRequest(JSONObject input, Context context) {

		LambdaLogger logger = context.getLogger();
		logger.log("Invoked JDBCSample.getCurrentTime");

		JSONArray cart_Add_array = new JSONArray();
		JSONObject jsonObject_cartDisplay_result = new JSONObject();

		Object userid1 = input.get("userid");
		Object device_id1 = input.get("device_id");
		Object search1 = null; 
		String device_id = device_id1.toString();
		String vendor_id = null;
		long userid;
		long product_id;
		


		String Str_msg = null;
		float Sub_total = 0,Tax=0,shipping_carge = 0;
		float total_promo_Discount=0,Grand_Total=0;
		String sql;
		JSONObject jo_cartInsert = new JSONObject();
		
		
		
		Statement stmt = null;
		Connection con = null;

		String url = "";
		String username = "";
		String password = "";
		JSONArray jsonArray_product = new JSONArray();
		JSONObject jsonObject_product_Result = new JSONObject();
		JSONObject jsonObject_image = new JSONObject();

		
		try {
			con = DriverManager.getConnection(url, username, password);
			stmt = con.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		

		JSONArray json_array_promocode = new JSONArray();
		
		if (userid1 == null || userid1 == "") {
			userid = 0;
		} else {
			userid = Long.parseLong(userid1.toString());
		}

		

		if ((device_id1 == null || device_id1 == "") && userid == 0) {

			Str_msg = "Please Enter either UserId or Device_id";
			jo_cartInsert.put("status", "0");
			jo_cartInsert.put("message", Str_msg);
			return jo_cartInsert;
		}

		// Get time from DB server
		try {
			
				
			
			
			if (userid != 0) {

				sql = "SELECT wsimcpsn_shopnow.products.*,table1.attribute_value,cart_items.UserId,cart_items.Quantity,(cart_items.Quantity * wsimcpsn_shopnow.products.sale_price) AS total,GROUP_CONCAT(product_offers.offer_name) as offer_name FROM wsimcpsn_shopnow.products LEFT JOIN \n"
						+ " (SELECT pa.original_product_id,GROUP_CONCAT(att_group_name,'\":\"',av.att_value) AS attribute_value FROM wsimcpsn_shopnow.product_attributes pa INNER JOIN attributes_value av ON av.id=pa.att_group_val_id INNER JOIN attributes a ON a.id=pa.att_group_id GROUP BY pa.original_product_id) AS table1 ON wsimcpsn_shopnow.products.id=table1.original_product_id \n"
						+ " LEFT JOIN wsimcpsn_shopnow.cart_items ON cart_items.ProductId=wsimcpsn_shopnow.products.id  LEFT JOIN wsimcpsn_shopnow.product_offers ON product_offers.product_id=wsimcpsn_shopnow.products.id WHERE cart_items.UserId='"
						+ userid + "' GROUP BY wsimcpsn_shopnow.products.id";

			} else {
				sql = "SELECT wsimcpsn_shopnow.products.*,table1.attribute_value,cart_items.UserId,cart_items.Quantity,(cart_items.Quantity *wsimcpsn_shopnow.products.sale_price) AS total,GROUP_CONCAT(product_offers.offer_name) as offer_name FROM wsimcpsn_shopnow.products LEFT JOIN \n"
						+ " (SELECT pa.original_product_id,GROUP_CONCAT(att_group_name,'\":\"',av.att_value) AS attribute_value FROM product_attributes pa INNER JOIN attributes_value av ON av.id=pa.att_group_val_id INNER JOIN attributes a ON a.id=pa.att_group_id GROUP BY pa.original_product_id) AS table1 ON wsimcpsn_shopnow.products.id=table1.original_product_id \n"
						+ " LEFT JOIN cart_items ON cart_items.ProductId=wsimcpsn_shopnow.products.id  LEFT JOIN product_offers ON product_offers.product_id=wsimcpsn_shopnow.products.id WHERE cart_items.device_id='"
						+ device_id
						+ "' and wsimcpsn_shopnow.cart_items.UserId='0' GROUP BY wsimcpsn_shopnow.products.id";

			}
 
           logger.log("\n SQL = \n "+sql);
			
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next() == false) {

				Str_msg = "cart_items are not present";
				jsonObject_cartDisplay_result.put("status", "0");
				jsonObject_cartDisplay_result.put("message", Str_msg);
				return jsonObject_cartDisplay_result;

			}

			JSONObject jo_CartItem_Result = new JSONObject();
			JSONArray json_array_CartItem = new JSONArray();

			

			do {

				JSONObject jo_cartItem = new JSONObject();
				JSONArray json_array_CartItem1 = new JSONArray();
				jo_cartItem.put("Product_id", rs.getString("id"));
				jo_cartItem.put("vendor_product_id", rs.getString("vendor_product_id"));
				jo_cartItem.put("quantity", rs.getString("Quantity"));
				jo_cartItem.put("sale_price", rs.getFloat("sale_price"));
				jo_cartItem.put("regular_price", rs.getFloat("regular_price"));
				jo_cartItem.put("name", rs.getString("name"));
				
             
				String offerArray[]= {};
				
				if (rs.getString("offer_name") == null) {

					jo_cartItem.put("offer", "");
				} else {

					offerArray=rs.getString("offer_name").split(",");
					jo_cartItem.put("offer", offerArray);
				}

				jo_cartItem.put("description", rs.getString("description"));
				jo_cartItem.put("Product_total", rs.getFloat("total"));
				
				jo_cartItem.put("image", rs.getString("image"));

				if (rs.getString("attribute_value") == null) {

				} else {
					json_array_CartItem1.add(rs.getString("attribute_value"));
					jo_cartItem.put("product_attribute", json_array_CartItem1);

				}
				
				jo_cartItem.put("product_attribute", json_array_CartItem1);
				Sub_total = Sub_total + rs.getFloat("total");
				json_array_CartItem.add(jo_cartItem);

			} while (rs.next());

			jo_CartItem_Result.put("Products", json_array_CartItem);
			jo_CartItem_Result.put("Sub_total", Sub_total);
     		jo_CartItem_Result.put("Shipping", "0");
			jo_CartItem_Result.put("tax", "0");
			jo_CartItem_Result.put("Currency", "INR");
			jsonObject_cartDisplay_result.put("Cart_Items", jo_CartItem_Result);

		} catch (Exception e) {
			e.printStackTrace();
			logger.log("Caught exception: " + e.getMessage());
		}
		
		
		try {
			
			
			String sql_promocode;	
			JSONObject jo_promocode = new JSONObject();			
			
			if(userid != 0) {
		    sql_promocode="SELECT promocodes.*,cart_promocode.* FROM cart_promocode INNER JOIN promocodes ON promocodes.id=cart_promocode.promocode_id WHERE user_id="+userid;	
			
			}
			else{
				
				sql_promocode="SELECT promocodes.*,cart_promocode.* FROM cart_promocode INNER JOIN promocodes ON promocodes.id=cart_promocode.promocode_id WHERE device_id='"+device_id+"'";
			}
			
			ResultSet rs_promocode = stmt.executeQuery(sql_promocode);
			
			while(rs_promocode.next()) {
				JSONObject jo_promocode1 = new JSONObject();
				 String promocode=rs_promocode.getString("promo_value");
				 float prom=Float.parseFloat(promocode);
				 String type=rs_promocode.getString("promo_value_type");
				 
				 jo_promocode1.put("promocode",rs_promocode.getString("promocode"));
				 jo_promocode1.put("description",rs_promocode.getString("description"));
				 		 
				 
				 float promovalue=0;
					if (type.equals("Variable")) {

						 promovalue = promovalue+ (Sub_total * prom) / 100;
						
					
					} else {
						promovalue = promovalue+prom;

					}
					
					total_promo_Discount=total_promo_Discount+promovalue;
				 
				
				  json_array_promocode.add(jo_promocode1);	
				
			} 
			
			Grand_Total = (Sub_total-total_promo_Discount) + Tax + shipping_carge;
			
			jsonObject_cartDisplay_result.put("Grand_Total", Grand_Total);
			//jsonObject_cartDisplay_result.put("Sub_total", Sub_total);
			jsonObject_cartDisplay_result.put("total_promo_Discount", total_promo_Discount);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.log("Caught exception: " + e.getMessage());
		}
		
		
		
		
		jsonObject_cartDisplay_result.put("promocode",json_array_promocode);
		return jsonObject_cartDisplay_result;

	}
}
