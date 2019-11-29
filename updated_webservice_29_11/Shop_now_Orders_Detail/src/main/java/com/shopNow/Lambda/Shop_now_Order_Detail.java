package com.shopNow.Lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Shop_now_Order_Detail implements RequestHandler<JSONObject, JSONObject> {

	@SuppressWarnings({ "unchecked", "unused" })
	public JSONObject handleRequest(JSONObject input, Context context) {

		LambdaLogger logger = context.getLogger();

		JSONObject jo_CartItem_Result_final = new JSONObject();
		JSONArray json_array_orderItem = new JSONArray();
		JSONArray json_array_orderShipingAddress = new JSONArray();
		JSONArray json_array_orderShipingAddress1 = new JSONArray();
		JSONArray json_array_orderBillingAddress = new JSONArray();
		JSONArray jo_promocode_array = new JSONArray();

		Object userid1 = input.get("userid");
		Object product_id1 = input.get("product_id").toString();
		Object order_id1 = input.get("order_id");
		String order_id = order_id1.toString();
		long userid;
		long product_id;

		// String order_status = input.get("order_status").toString();

		String Str_msg;
		int page_number = 1;
		String order1;
		int flagChange;
		float total = 0;
		String Order_Id = null;
		String delivery_status_code = null;
		String payment_mode = null;
		String payment_status = null;
		java.sql.Date expected_delivery_Date = null;
		String transaction_id;
		String billing_address = null;
		String shipping_address = null;
		java.sql.Date orderDate = null;
		String Order_Number = null, promocode_value = null;

		float grand_total = 0, sub_total = 0, tax = 0, shipping = 0;
		String promocode = null;

		int page_size = 10;
		float Product_total = 0;
		String Vendor_id = null;
		String order_status = null;
		Connection conn = null;

		// --------------------------------------------------------------------------------------------------

		if (input.get("page_number").toString() == "" || input.get("page_number").toString() == null) {
			page_number = 1;
		} else {
			String pagenumber1 = input.get("page_number").toString();
			page_number = Integer.parseInt(pagenumber1);
		}

		if (userid1 == null || userid1 == "") {

			Str_msg = "UserID cannot be null";
			jo_CartItem_Result_final.put("status", "0");
			jo_CartItem_Result_final.put("message", Str_msg);
			return jo_CartItem_Result_final;
		} else {
			userid = Long.parseLong(userid1.toString());
		}

		if (product_id1 == null || product_id1 == "") {

			product_id = 0;
		} else {
			product_id = Long.parseLong(product_id1.toString());

		}
		if (order_id1 == null || order_id1 == "") {

			order_id = null;

			Str_msg = "OrderID cannot be null";
			jo_CartItem_Result_final.put("status", "0");
			jo_CartItem_Result_final.put("message", Str_msg);
			return jo_CartItem_Result_final;

		} else {
			order_id = order_id1.toString();

		}

		try {
			String url = "";
			String username = "";
			String password = "";
			conn = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {

			logger.log("Caught exception: " + e.getMessage());
		}

		try {
			String sql = "SELECT id FROM wsimcpsn_shopnow.customers where id='" + userid + "'";
			Statement stmt = conn.createStatement();
			ResultSet srs_customer_id = stmt.executeQuery(sql);

			if (srs_customer_id.next() == false) {
				Str_msg = "user is not valid";
				jo_CartItem_Result_final.put("status", "0");
				jo_CartItem_Result_final.put("message", Str_msg);
				return jo_CartItem_Result_final;

			}

			String sql1 = "SELECT id FROM wsimcpsn_shopnow.order_details where order_id='" + order_id + "'";
			Statement stmt_order = conn.createStatement();
			ResultSet srs_order_id = stmt_order.executeQuery(sql1);

			if (srs_order_id.next() == false) {
				Str_msg = "Order not found";
				jo_CartItem_Result_final.put("status", "0");
				jo_CartItem_Result_final.put("message", Str_msg);
				return jo_CartItem_Result_final;

			}

			String sql11 = null;

			if ((product_id == 0) && (order_id != null || order_id != "")) {

				sql11 = "SELECT * FROM wsimcpsn_shopnow.order_details INNER JOIN products ON products.id=order_details.productId WHERE order_details.order_id ='"
						+ order_id + "' and order_details.user_id ='" + userid + "' limit " + (page_number - 1) * 10
						+ "," + page_size;

			} else if (product_id != 0 && userid != 0 && order_id == null) {

				sql11 = "SELECT * FROM wsimcpsn_shopnow.order_details  INNER JOIN products ON products.id=order_details.productId WHERE  order_details.user_id ='"
						+ userid + "' and order_details.productId='" + product_id + "'";

				Statement stmt1 = conn.createStatement();
				ResultSet str_order_product = stmt1.executeQuery(sql11);

				while (str_order_product.next()) {

					JSONObject jo_OrderItem1 = new JSONObject();

					// jo_OrderItem1.put("id", str_order_product.getInt("id"));
					jo_OrderItem1.put("product_id", str_order_product.getLong("productId"));

					jo_OrderItem1.put("product_name", str_order_product.getString("product_description"));
					jo_OrderItem1.put("vendor_id", str_order_product.getString("vendorId"));

					jo_OrderItem1.put("Image", str_order_product.getString("image"));
					float price = str_order_product.getFloat("price");
					jo_OrderItem1.put("price", price);
					jo_OrderItem1.put("quantity", str_order_product.getInt("quantity"));

					total = total + (price * str_order_product.getInt("quantity"));
					jo_OrderItem1.put("Product_total_price", price * str_order_product.getInt("quantity"));

					Order_Id = str_order_product.getString("order_id");
					Order_Number = str_order_product.getString("order_number");
					order_status = str_order_product.getString("order_status");
					payment_mode = str_order_product.getString("mode_of_payment");
					payment_status = str_order_product.getString("payment_status");

					transaction_id = str_order_product.getString("transaction_id");

					billing_address = str_order_product.getString("delivery_address");

					shipping_address = str_order_product.getString("delivery_address");

					grand_total = str_order_product.getFloat("grand_total");
					grand_total = str_order_product.getFloat("sub_total");
					grand_total = str_order_product.getFloat("tax");
					shipping = str_order_product.getFloat("shipping");
					promocode = str_order_product.getString("promocode");

					orderDate = str_order_product.getDate("order_Date_Time");
					expected_delivery_Date = str_order_product.getDate("expected_date_of_delivery");

					jo_OrderItem1.put("Order_Id", Order_Id);
					jo_OrderItem1.put("OrderNumber", Order_Number);
					jo_OrderItem1.put("Order_Date", orderDate);
					jo_OrderItem1.put("expected_delivery_Date", expected_delivery_Date);
					// jo_OrderItem1.put("Order_total_price", total);
					jo_OrderItem1.put("Order_status", order_status);
					jo_OrderItem1.put("payment_mode", payment_mode);
					jo_OrderItem1.put("payment_status", payment_status);
					jo_OrderItem1.put("transaction_id", "transaction_id");

					jo_CartItem_Result_final.put("Currency", "INR");
					json_array_orderItem.add(jo_OrderItem1);
					jo_CartItem_Result_final.put("Product_Detais", json_array_orderItem);

				}

			} else {

				sql11 = "SELECT * FROM wsimcpsn_shopnow.order_details  INNER JOIN products ON products.id=order_details.productId WHERE order_details.order_id ='"
						+ order_id + "' and order_details.user_id ='" + userid + "' and order_details.productId='"
						+ product_id + "'";

			}

			Statement stmt1 = conn.createStatement();
			ResultSet str_order_product = stmt1.executeQuery(sql11);

			while (str_order_product.next()) {

				JSONObject jo_OrderItem1 = new JSONObject();

				jo_OrderItem1.put("id", str_order_product.getLong("id"));
				jo_OrderItem1.put("product_id", str_order_product.getLong("productId"));

				jo_OrderItem1.put("product_name", str_order_product.getString("product_description"));
				jo_OrderItem1.put("vendor_id", str_order_product.getString("vendorId"));

				jo_OrderItem1.put("Image", str_order_product.getString("image"));
				float price = str_order_product.getFloat("price");
				jo_OrderItem1.put("price", price);
				jo_OrderItem1.put("quantity", str_order_product.getInt("quantity"));

				total = total + (price * str_order_product.getInt("quantity"));
				jo_OrderItem1.put("Product_total_price", price * str_order_product.getInt("quantity"));
				json_array_orderItem.add(jo_OrderItem1);

				Order_Id = str_order_product.getString("order_id");
				Order_Number = str_order_product.getString("order_number");
				delivery_status_code = str_order_product.getString("delivery_status_code");
				payment_mode = str_order_product.getString("mode_of_payment");
				payment_status = str_order_product.getString("payment_status");

				transaction_id = str_order_product.getString("transaction_id");

				billing_address = str_order_product.getString("delivery_address");
				shipping_address = str_order_product.getString("delivery_address");

				orderDate = str_order_product.getDate("order_Date_Time");
				expected_delivery_Date = str_order_product.getDate("expected_date_of_delivery");
				order_status = str_order_product.getString("order_status");

				grand_total = str_order_product.getFloat("grand_total");
				sub_total = str_order_product.getFloat("sub_total");
				tax = str_order_product.getFloat("tax");
				shipping = str_order_product.getFloat("shipping");
				promocode = str_order_product.getString("promocode");

			}

			json_array_orderShipingAddress.add(shipping_address);
			json_array_orderBillingAddress.add(billing_address);

			jo_CartItem_Result_final.put("shipping_address", json_array_orderShipingAddress);
			jo_CartItem_Result_final.put("billing address", json_array_orderBillingAddress);
			jo_CartItem_Result_final.put("Order_Id", Order_Id);
			jo_CartItem_Result_final.put("OrderNumber", Order_Number);
			jo_CartItem_Result_final.put("Order_Date", orderDate);
			jo_CartItem_Result_final.put("Order_Status", order_status);
			jo_CartItem_Result_final.put("expected_delivery_Date", expected_delivery_Date);
			jo_CartItem_Result_final.put("Order_total_price", total);
			jo_CartItem_Result_final.put("delivery_status_code", delivery_status_code);
			jo_CartItem_Result_final.put("payment_mode", payment_mode);
			jo_CartItem_Result_final.put("payment_status", payment_status);
			jo_CartItem_Result_final.put("transaction_id", "transaction_id");

			jo_CartItem_Result_final.put("Currency", "INR");
			jo_CartItem_Result_final.put("Tax", tax);
			jo_CartItem_Result_final.put("Shipping", shipping);
			jo_CartItem_Result_final.put("sub_total", sub_total);
			jo_CartItem_Result_final.put("grand_total", grand_total);
			jo_CartItem_Result_final.put("promocode", promocode);

			jo_CartItem_Result_final.put("Product_Detais", json_array_orderItem);

		} catch (Exception e) {

			logger.log("Caught exception: " + e.getMessage());
		}

		try {
			Statement stmt_promocode = conn.createStatement();
			String promo_array[] = promocode.split(",", -2);
			for (int i = 0; i <= promo_array.length; i++) {

				logger.log(promo_array[i]);
				String sql_promocode = "select * from promocodes where id='" + promo_array[i] + "'";

				ResultSet rs_promocode = stmt_promocode.executeQuery(sql_promocode);

				while (rs_promocode.next()) {
					JSONObject jo_promocode1 = new JSONObject();
					// String promocode=rs_promocode.getString("promo_value");
					// float prom=Float.parseFloat(promocode);
					// String type=rs_promocode.getString("promo_value_type");

					jo_promocode1.put("promocode", rs_promocode.getString("promocode"));
					jo_promocode1.put("description", rs_promocode.getString("description"));

					jo_promocode_array.add(jo_promocode1);
				}
			}

		} catch (Exception e) {

			logger.log("Caught exception: " + e.getMessage());
		}

		jo_CartItem_Result_final.put("promocode", jo_promocode_array);

		return jo_CartItem_Result_final;

	}
}
