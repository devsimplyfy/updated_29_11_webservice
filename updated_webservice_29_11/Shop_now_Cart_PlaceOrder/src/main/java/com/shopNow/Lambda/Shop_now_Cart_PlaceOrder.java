package com.shopNow.Lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Shop_now_Cart_PlaceOrder implements RequestHandler<JSONObject, JSONObject> {

	@SuppressWarnings({ "unchecked", "unused" })
	public JSONObject handleRequest(JSONObject input, Context context) {

		LambdaLogger logger = context.getLogger();

		JSONArray cart_Add_array = new JSONArray();
		JSONObject jsonObject_placeorder_result = new JSONObject();

		Object userid1 = input.get("userid");
		long userid = Long.parseLong(userid1.toString());
		float sub_total = 0;
		float total_promo_Discount =0;
		
		
		float tax = 0;
		float shipping = 0;
		float Grand_Total = 0;
		float final_sub_total = 0;
		String orderNumber = null;
		String promocode_id = null;
		Connection conn = null;

		int flag = 0;
		String transaction_id = input.get("transaction_id").toString();
		String paymentMode = input.get("paymentMode").toString();
		String payment_gateway = input.get("payment_gateway").toString();
		if (paymentMode.equalsIgnoreCase("COD")) {
			flag = 1;
			transaction_id = "COD";
			payment_gateway = "COD";

		}

		String Str_msg;

		if (userid1 == null || userid1 == "" || userid == 0) {

			Str_msg = "Order not placed successfully because userID is null";
			jsonObject_placeorder_result.put("status", "0");
			jsonObject_placeorder_result.put("message", Str_msg);
			return jsonObject_placeorder_result;

		}
		if (flag == 0) {

			if (transaction_id == null || transaction_id == "") {

				Str_msg = "Order not placed successfully because transaction_id is null";
				jsonObject_placeorder_result.put("status", "0");
				jsonObject_placeorder_result.put("message", Str_msg);
				return jsonObject_placeorder_result;

			}
			if (payment_gateway == null || payment_gateway == "") {

				Str_msg = "Order not placed successfully because payment_gateway is null";
				jsonObject_placeorder_result.put("status", "0");
				jsonObject_placeorder_result.put("message", Str_msg);
				return jsonObject_placeorder_result;

			}
		}

		// -----database connection-----------

		try {

			String url = "";
			String username = "";
			String password = "";
			conn = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			e.printStackTrace();
			logger.log("Caught exception: " + e.getMessage());

		}

		// Get time from DB server
		try {

			Statement stmt = conn.createStatement();
			ResultSet srs_product_id = stmt
					.executeQuery("SELECT * FROM wsimcpsn_shopnow.cart_items where UserId='" + userid + "'");

			if (srs_product_id.next() == false) {

				Str_msg = "Product is not present in cart";
				jsonObject_placeorder_result.put("status", "0");
				jsonObject_placeorder_result.put("message", Str_msg);
				return jsonObject_placeorder_result;
			}

			int quantity = 0;
			int productId;
			int i = 0;
			float sale_price;
			String attribute_value;
			String delivery_address;
			String orderId;
			String name;

			// ----------------Logic for Choose Payment Option----------------

			if (paymentMode.isEmpty()) {

				Str_msg = "Please Enter valid payment mode not empty! Thank You Try again";
				jsonObject_placeorder_result.put("status", "0");
				jsonObject_placeorder_result.put("message", Str_msg);

				return jsonObject_placeorder_result;

			}
			logger.log("\n in the if1  \n");

			/*
			 * if ((paymentMode.equalsIgnoreCase("COD") == false)||
			 * (paymentMode.equalsIgnoreCase("internet Banking") == false) ||
			 * (paymentMode.equalsIgnoreCase("Credit Card") == false) ||
			 * (paymentMode.equalsIgnoreCase("Debit Card") ==
			 * false)||(paymentMode.equalsIgnoreCase("EMI") == false)) {
			 * 
			 * 
			 * logger.log("\n in the if  \n"); Str_msg =
			 * "Please Enter valid payment mode! Thank You Try again";
			 * jsonObject_placeorder_result.put("status", "0");
			 * jsonObject_placeorder_result.put("message", Str_msg);
			 * 
			 * return jsonObject_placeorder_result;
			 * 
			 * }
			 */
			// --------------Logic For Create new Uniqe Order_id-----------------

			long current = System.currentTimeMillis();
			orderId = Long.toString(current) + "_" + Long.toString(userid);

			// -----------------------Logic for order Number---------

			int iRandom = new Random().nextInt(900000) + 100000;
			orderNumber = Long.toString(current) + "_" + Long.toString(iRandom);

			String sql = "SELECT products.*,table1.attribute_value,cart_items.UserId,cart_items.Quantity,SUM(cart_items.Quantity *products.sale_price) AS total ,(SELECT GROUP_CONCAT(firstName,\" \",lastName,\", phoneNumber- \",phoneNumber ,\" , Address\",address1,\" \",address2,\" \",address3,\" \",city,\" \",state) AS addresscustomer  FROM(SELECT * FROM  address WHERE customerId='"
					+ userid + "' LIMIT 1)AS table1 )AS delivery_address  FROM products LEFT JOIN \n"
					+ "(SELECT pa.product_id,GROUP_CONCAT(att_group_name,'\":\"',av.att_value) AS attribute_value FROM product_attributes pa INNER JOIN attributes_value av ON av.id=pa.att_group_val_id INNER JOIN attributes a ON a.id=pa.att_group_id GROUP BY pa.product_id) AS table1 ON products.id=table1.product_id \n"
					+ " LEFT JOIN cart_items ON cart_items.ProductId=products.id  WHERE  cart_items.UserId=" + userid
					+ " GROUP BY products.id";

			logger.log("\n sql =\n" + sql);

			Statement stmt1 = conn.createStatement();
			ResultSet srs_orderPlace = stmt1.executeQuery(sql);

			while (srs_orderPlace.next()) {

				productId = srs_orderPlace.getInt("id");
				quantity = srs_orderPlace.getInt("Quantity");
				name = srs_orderPlace.getString("name");
				sale_price = srs_orderPlace.getFloat("sale_price");
				attribute_value = srs_orderPlace.getString("attribute_value");

				if (attribute_value == null) {

					name = name + ",";

				} else {

					name = name + " ,  " + attribute_value;

				}

				delivery_address = srs_orderPlace.getString("delivery_address");
				String vendorId = srs_orderPlace.getString("vendor_id");
				String vendor_product_id = srs_orderPlace.getString("vendor_product_id");

				sub_total = sub_total + Float.parseFloat(srs_orderPlace.getString("total"));

				Date now = new Date();
				SimpleDateFormat simpleDateformat = new SimpleDateFormat("E");
				simpleDateformat = new SimpleDateFormat("EEEE"); // the day of the week spelled out completely
				String day = simpleDateformat.format(now);
				
				String day_delivery=simpleDateformat.format("2019-11-29");	
				logger.log("\n day name \n"+day_delivery);

				
				
				
				
				Statement stmt2 = conn.createStatement();

				if (quantity > 0) {

					if (day == "Sunday") {

						String sql_place_order = "INSERT INTO wsimcpsn_shopnow.order_details(order_id,user_id,product_description,productId,payment_status,delivery_status_code,delivery_address,vendorId,vendor_product_id,quantity,mode_of_payment,transaction_id,discounts,delivery_charges,price,expected_date_of_delivery,payment_gateway,order_number) VALUES\n"
								+ "('" + orderId + "'," + userid + ",'" + name + "'," + productId + ",'COD','200 ok','"
								+ delivery_address + "','" + vendorId + "','" + vendor_product_id + "'," + quantity
								+ ",'" + paymentMode + "','" + transaction_id + "',100,200," + sale_price
								+ ",DATE_ADD(Now(), INTERVAL 8 DAY),'" + payment_gateway + "','" + orderNumber + "')";

						// logger.log(sql_insert_address);
						stmt2.executeUpdate(sql_place_order);

						

					} else {

						String sql_place_order = "INSERT INTO wsimcpsn_shopnow.order_details(order_id,user_id,product_description,productId,payment_status,delivery_status_code,delivery_address,vendorId,vendor_product_id,quantity,mode_of_payment,transaction_id,discounts,delivery_charges,price,expected_date_of_delivery,payment_gateway,order_number) VALUES\n"
								+ "('" + orderId + "'," + userid + ",'" + name + "'," + productId + ",'COD','200 ok','"
								+ delivery_address + "','" + vendorId + "','" + vendor_product_id + "'," + quantity
								+ ",'" + paymentMode + "','" + transaction_id + "',100,200," + sale_price
								+ ",DATE_ADD(Now(), INTERVAL 7 DAY),'" + payment_gateway + "','" + orderNumber + "')";

						// logger.log(sql_insert_address);

						stmt2.executeUpdate(sql_place_order);

					}

				
					stmt2.close();

				}

			}
			final_sub_total = sub_total;
			logger.log("\n sub_total =\n" + sub_total);

		}

		catch (Exception e) {
			e.printStackTrace();
			logger.log("Caught exception: " + e.getMessage());

		}

		// -----------------------Logic for promocode--------------------------
		try {

			Statement stmt33 = conn.createStatement();
			Statement stmt44 = conn.createStatement();
			Statement stmt3 = conn.createStatement();
			Statement stmt4 = conn.createStatement();

			String sql_promocode;

			JSONObject jo_promocode = new JSONObject();
			JSONArray json_array_promocode = new JSONArray();

			sql_promocode = "SELECT promocodes.*,cart_promocode.* FROM cart_promocode INNER JOIN promocodes ON promocodes.id=cart_promocode.promocode_id WHERE user_id="
					+ userid;
			ResultSet rs_promocode = stmt33.executeQuery(sql_promocode);

			int i = 0;
			while (rs_promocode.next()) {
				i++;
				JSONObject jo_promocode1 = new JSONObject();
				String promocode = rs_promocode.getString("promo_value");
				float prom = Float.parseFloat(promocode);
				String type = rs_promocode.getString("promo_value_type");

				int temp = rs_promocode.getInt("id");
				
				String promocode_id1 = Integer.toString(temp);

				
				
				if (i == 1) {
					promocode_id = promocode_id1;

				} else {
					promocode_id = promocode_id.concat("," + promocode_id1);
				}
				float promovalue=0;
				if (type.equals("Variable")) {

					 promovalue = promovalue+ (sub_total * prom) / 100;
					
				
				} else {
					promovalue = promovalue+prom;

				}
				
				total_promo_Discount=total_promo_Discount+promovalue;		
				logger.log("\n total Discount \n"+Float.toString(promovalue));			
				json_array_promocode.add(jo_promocode1);

			}
			stmt33.close();
			float sub_total_final = final_sub_total;
			Grand_Total = (final_sub_total-total_promo_Discount) + tax + shipping;

			String sql = "update order_details set sub_total=" + sub_total_final + ", grand_total=" + Grand_Total
					+ ",tax=" + tax + ",shipping=" + shipping + ",promocode='" + promocode_id + "',discounts='"+total_promo_Discount+"'  where order_number='"
					+ orderNumber + "'";
			
			logger.log(sql);
			stmt44.executeUpdate(sql);
			stmt44.close();

			stmt3.executeUpdate("DELETE FROM wsimcpsn_shopnow.cart_items where UserId='" + userid + "'");
			stmt3.close();

			String sqldel = "DELETE FROM wsimcpsn_shopnow.cart_promocode where user_id='" + userid + "'";
			stmt4.executeUpdate(sqldel);
			stmt4.close();

		} catch (Exception e) {
			e.printStackTrace();
			logger.log("Caught exception: " + e.getMessage());
		}

		
		Str_msg = "Order Placed Successfully! Thank You !! ";
		jsonObject_placeorder_result.put("status", "1");
		jsonObject_placeorder_result.put("message", Str_msg);

		return jsonObject_placeorder_result;

	}

}
