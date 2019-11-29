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

public class Shop_now_Order_Display implements RequestHandler<JSONObject, JSONObject> {

	@SuppressWarnings({ "unchecked", "unused" })
	public JSONObject handleRequest(JSONObject input, Context context) {

		LambdaLogger logger = context.getLogger();

		JSONObject jo_CartItem_Result_final = new JSONObject();
		JSONArray json_array_orderItem = new JSONArray();
		JSONArray jo_promocode_array = new JSONArray();

		Object userid1 = input.get("userid");
		long userid;
		String orderBy = input.get("orderBy").toString();
		String search = input.get("search").toString();
		String order_status = input.get("order_status").toString();
		String vendor_id1 = input.get("vendor_id").toString();
		String promocode_value = null;

		String vendor_id = null;
		float price = 0;

		logger.log(vendor_id1);

		String Str_msg;
		int page_number = 1;
		String order1;
		int flagChange;
		Connection conn = null;
		// --------------------------------------------------------------------------------------------------

		if (input.get("page_number").toString() == "" || input.get("page_number").toString() == null) {
			page_number = 1;
		} else {
			String pagenumber1 = input.get("pageNumber").toString();
			page_number = Integer.parseInt(pagenumber1);
		}

		int page_size = 10;
		float Product_total = 0;

		if (userid1 == null || userid1 == "") {

			Str_msg = "UserID cannot be null";
			jo_CartItem_Result_final.put("status", "0");
			jo_CartItem_Result_final.put("message", Str_msg);
			return jo_CartItem_Result_final;
		} else {
			userid = Long.parseLong(userid1.toString());
		}

		if (order_status == null || order_status == "") {

			order_status = "Order placed";
		}

		if (orderBy == null) {
			order1 = "DESC";
			flagChange = 0;
		} else if (orderBy.equalsIgnoreCase("ASC")) {
			order1 = "ASC";
			flagChange = 1;
		} else if (orderBy.equalsIgnoreCase("DESC")) {
			order1 = "DESC";
			flagChange = 1;
		} else {
			order1 = "DESC";
			flagChange = 0;

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

			String sql2 = "select DISTINCT (order_id),delivery_status_code,vendorId,mode_of_payment,transaction_id,delivery_address,order_Date_Time,expected_date_of_delivery,payment_status,order_number from wsimcpsn_shopnow.order_details where user_id ='"
					+ userid + "' and order_status='" + order_status + "'";

			String sql1;

			if (vendor_id1 == null || vendor_id1 == "") {
				sql1 = "select DISTINCT (order_id),product_description,delivery_status_code,vendorId,mode_of_payment,transaction_id,delivery_address,order_Date_Time,expected_date_of_delivery,payment_status,order_status,order_number,grand_total,sub_total,tax,shipping,promocode from wsimcpsn_shopnow.order_details where user_id ='"
						+ userid + "' AND product_description LIKE '%" + search + "%' order by order_Date_Time "
						+ order1 + " limit " + (page_number - 1) * 10 + "," + page_size;

				logger.log("\n we are in if:\n" + sql1);

			} else {

				sql1 = "select DISTINCT (order_id),product_description,delivery_status_code,vendorId,mode_of_payment,transaction_id,delivery_address,order_Date_Time,expected_date_of_delivery,payment_status,order_status,order_number,grand_total,sub_total,tax,shipping,promocode from wsimcpsn_shopnow.order_details where user_id ='"
						+ userid + "' AND product_description LIKE '%" + search
						+ "%' and wsimcpsn_shopnow.order_details.vendorId='" + vendor_id1
						+ "' order by order_Date_Time " + order1 + " limit " + (page_number - 1) * 10 + "," + page_size;

				logger.log("\n we are in else\n" + sql1);
			}

			Statement stmt1 = conn.createStatement();
			ResultSet str_order_ids = stmt1.executeQuery(sql1);

			while (str_order_ids.next()) {
				JSONArray json_array_orderItem1 = new JSONArray();
				JSONObject jo_CartItem_Result = new JSONObject();
				String sql3;

				if (vendor_id1 == null || vendor_id1 == "") {

					sql3 = "SELECT * FROM wsimcpsn_shopnow.order_details  INNER JOIN wsimcpsn_shopnow.products ON wsimcpsn_shopnow.products.id=wsimcpsn_shopnow.order_details.productId WHERE wsimcpsn_shopnow.order_details.order_id ='"
							+ str_order_ids.getString("order_id") + "'";
					// and wsimcpsn_shopnow.order_details.order_status='"+ order_status + "'";

					logger.log("\n we are in if2:\n" + sql1);
				} else {

					sql3 = "SELECT * FROM wsimcpsn_shopnow.order_details INNER JOIN wsimcpsn_shopnow.products ON wsimcpsn_shopnow.products.id=wsimcpsn_shopnow.order_details.productId WHERE wsimcpsn_shopnow.order_details.order_id ='"
							+ str_order_ids.getString("order_id") + "' and wsimcpsn_shopnow.order_details.vendorId='"
							+ vendor_id1 + "'";
					// + " and wsimcpsn_shopnow.order_details.order_status='"+ order_status + "'";

					logger.log("\n we are in else2\n" + sql1);

				}

				Statement stmt3 = conn.createStatement();
				ResultSet str_order_product = stmt3.executeQuery(sql3);

				float total = 0;
				while (str_order_product.next()) {

					JSONObject jo_OrderItem1 = new JSONObject();
					jo_OrderItem1.put("product_id", str_order_product.getLong("productId"));
					jo_OrderItem1.put("Image", str_order_product.getString("image"));
					jo_OrderItem1.put("product_name", str_order_product.getString("product_description"));
					// jo_OrderItem1.put("Vendor Id",str_order_product.getString("vendorId"));

					vendor_id = str_order_product.getString("vendorId");

					price = str_order_product.getFloat("price");
					Product_total = Product_total + (price * str_order_product.getInt("quantity"));
					jo_OrderItem1.put("Product_total_price", price * str_order_product.getInt("quantity"));
					json_array_orderItem1.add(jo_OrderItem1);

				}

				jo_CartItem_Result.put("Order_product", json_array_orderItem1);
				// jo_CartItem_Result.put("venodr_id",vendor_id);
				// jo_CartItem_Result.put("pricprice",price);

				jo_CartItem_Result.put("Order_Id", str_order_ids.getString("order_id"));
				jo_CartItem_Result.put("OrderNumber", str_order_ids.getString("order_number"));
				jo_CartItem_Result.put("Order_total_price", Product_total);
				promocode_value = str_order_ids.getString("promocode");

				logger.log("\n promocode_value\n " + promocode_value);

				jo_CartItem_Result.put("payment_mode", str_order_ids.getString("mode_of_payment"));

				if (str_order_ids.getString("mode_of_payment") != "COD") {
					jo_CartItem_Result.put("transaction_id", str_order_ids.getString("transaction_id"));
				}

				jo_CartItem_Result.put("Order Date", str_order_ids.getDate("order_Date_Time"));
				jo_CartItem_Result.put("Order_Status", str_order_ids.getString("order_status"));
				jo_CartItem_Result.put("expected_delivery_Date", str_order_ids.getDate("expected_date_of_delivery"));
				jo_CartItem_Result.put("transaction_id", "COD");
				jo_CartItem_Result.put("Currency", "INR");

				jo_CartItem_Result.put("Currency", "INR");

				jo_CartItem_Result.put("grand_total", str_order_ids.getFloat("grand_total"));
				jo_CartItem_Result.put("sub_total", str_order_ids.getFloat("sub_total"));
				jo_CartItem_Result.put("tax", str_order_ids.getFloat("tax"));
				jo_CartItem_Result.put("shipping", str_order_ids.getFloat("shipping"));
				jo_CartItem_Result.put("promocode", str_order_ids.getString("promocode"));

				json_array_orderItem.add(jo_CartItem_Result);

			}

		} catch (Exception e) {

			logger.log("Caught exception: " + e.getMessage());
		}

		
		jo_CartItem_Result_final.put("orders", json_array_orderItem);

		return jo_CartItem_Result_final;

	}
}
