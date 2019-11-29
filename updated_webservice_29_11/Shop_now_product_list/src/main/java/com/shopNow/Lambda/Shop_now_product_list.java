package com.shopNow.Lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Shop_now_product_list implements RequestHandler<JSONObject, JSONObject> {

	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {

		String url = "";
		String username = "";
		String password = "";
		Connection conn;

		Statement stmt = null;
		ResultSet resultSet;

		final int id;
		if (input.get("id") != null && input.get("id") != "") {
			id = Integer.parseInt(input.get("id").toString());
		} else {
			id = 0;
		}

		int page_number;
		if (input.get("page_number") != null && input.get("page_number") != "") {
			page_number = Integer.parseInt(input.get("page_number").toString());
		} else {

			page_number = 0;
		}

		int customerId = 0;
		if (input.get("customer_id") != null && input.get("customer_id") != "") {
			customerId = Integer.parseInt(input.get("customer_id").toString());
		} else {
			customerId = 0;

		}

		float min_price = 0;
		if (input.get("min_price") != null && input.get("min_price") != "") {
			min_price = Float.parseFloat(input.get("min_price").toString());
		} else {
			min_price = 0;
		}

		float max_price = 0;
		if (input.get("max_price") != null && input.get("max_price") != "") {
			max_price = Float.parseFloat(input.get("max_price").toString());
		} else {
			max_price = 0;
		}

		String search;
		if (input.get("search") != null && input.get("search") != "") {
			search = input.get("search").toString();
		} else {

			search = "";
		}
		String orderBy;

		if (input.get("order") != null) {

			orderBy = input.get("order").toString();

		} else {
			orderBy = null;
		}

		String sql4 = "", sql5 = "";
		JSONArray ja_product_list = new JSONArray();
		JSONObject jo_product_list_result = new JSONObject();

		// This Logic for Product Per Page
		int page_size = 50;
		if (page_number == 0) {
			page_number = 1;

		}
		int page_offset = (page_number - 1) * page_size;

		if (search == null) {
			search = "";
		}

		if (min_price == 0) {
			min_price = 0;
		}

		int flagChange = 0;
		String order = "ASC";

		if (orderBy == null) {
			order = "ASC";
			flagChange = 0;
		} else if (orderBy.equalsIgnoreCase("priceup")) {
			order = "ASC";
			flagChange = 1;
		} else if (orderBy.equalsIgnoreCase("pricedown")) {
			order = "DESC";
			flagChange = 1;
		} else {
			order = "ASC";
			flagChange = 0;

		}

		// =====================================================================================================================================

		if (flagChange == 0) {
			if (id == 0) {
				if (max_price == 0) {
					if (customerId == 0) {
						System.out.println("flagChange=0,id=0 max = 0 customer_id=0");

						sql4 = "SELECT table1.*,t1.c AS total,0 AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  NAME LIKE '%" + search
								+ "%' AND sale_price >" + min_price + " GROUP BY id " + order + " limit " + page_offset
								+ "," + page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";

						sql5 = "SELECT * FROM(\n"
								+ "SELECT t1.*,(CASE WHEN wish_list1.product_id IS NOT NULL THEN 1 ELSE 0 END)AS wishlist FROM(\n"
								+ "SELECT p1.*,p2.total FROM products AS p1  LEFT JOIN (SELECT category_id,COUNT(6) AS total FROM products GROUP BY category_id) AS p2 ON p1.category_id=p2.category_id WHERE p1.name LIKE '%"
								+ search + "%' AND p1.sale_price>" + min_price + ") AS t1\n"
								+ "LEFT JOIN (SELECT * FROM wish_list WHERE customer_id=113)AS wish_list1 ON t1.id=wish_list1.product_id)AS table2\n"
								+ "LEFT JOIN\n"
								+ "(SELECT product_id,GROUP_CONCAT(product_image.image) AS image1 FROM product_image GROUP BY product_id)AS table3 ON table2.id=table3.product_id  LIMIT 0,100";

						System.out.println(sql4);

					} else {
						System.out.println("flagChange=0,id=0 max = 0 customer_id=1");

						sql4 = "SELECT table1.*,t1.c AS total,(CASE WHEN wish_list1.product_id IS NOT NULL THEN 1 ELSE 0 END) AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  NAME LIKE '%" + search
								+ "%' AND sale_price >" + min_price + " GROUP BY id " + order + " limit " + page_offset
								+ "," + page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT product_id FROM wish_list WHERE customer_id=" + customerId
								+ ") AS wish_list1 ON wish_list1.product_id=table1.id\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";

						System.out.println(sql4);
					}
				} else {
					if (customerId == 0) {
						System.out.println("flagChange=0,id=0 max = 1 customer_id=0");

						sql4 = "SELECT table1.*,t1.c AS total,0 AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE NAME LIKE '%" + search
								+ "%' AND sale_price >" + min_price + " and sale_price<=" + max_price + " GROUP BY id "
								+ order + " limit " + page_offset + "," + page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";
						System.out.println(sql4);

					} else {
						System.out.println("flagChange=0,id=0 max = 1 customer_id=1");

						sql4 = "SELECT table1.*,t1.c AS total,(CASE WHEN wish_list1.product_id IS NOT NULL THEN 1 ELSE 0 END) AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  NAME LIKE '%" + search
								+ "%' AND sale_price >" + min_price + " and sale_price<=" + max_price + " GROUP BY id "
								+ order + " limit " + page_offset + "," + page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT product_id FROM wish_list WHERE customer_id=" + customerId
								+ ") AS wish_list1 ON wish_list1.product_id=table1.id\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";

						System.out.println(sql4);

					}

				}
			} else {

				if (max_price == 0) {
					if (customerId == 0) {
						System.out.println("flagChange=0,id=1 max = 0 customer_id=0");

						sql4 = "SELECT table1.*,t1.c AS total,0 AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  category_id=" + id
								+ " AND NAME LIKE '%" + search + "%' AND sale_price >" + min_price + " GROUP BY id "
								+ order + " limit " + page_offset + "," + page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";
						System.out.println(sql4);

					} else {
						System.out.println("flagChange=0,id=0 max = 0 customer_id=1");

						sql4 = "SELECT table1.*,t1.c AS total,(CASE WHEN wish_list1.product_id IS NOT NULL THEN 1 ELSE 0 END) AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  category_id=" + id
								+ " AND NAME LIKE '%" + search + "%' AND sale_price >" + min_price + " GROUP BY id "
								+ order + " limit " + page_offset + "," + page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT product_id FROM wish_list WHERE customer_id=" + customerId
								+ ") AS wish_list1 ON wish_list1.product_id=table1.id\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";

						System.out.println(sql4);
					}
				} else {
					if (customerId == 0) {
						System.out.println("flagChange=0,id=0 max = 1 customer_id=0");

						sql4 = "SELECT table1.*,t1.c AS total,0 AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  category_id=" + id
								+ " AND NAME LIKE '%" + search + "%' AND sale_price >" + min_price + " and sale_price<="
								+ max_price + " GROUP BY id " + order + " limit " + page_offset + "," + page_size
								+ ")AS table1\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";
						System.out.println(sql4);

					} else {
						System.out.println("flagChange=0,id=1 max = 1 customer_id=1");

						sql4 = "SELECT table1.*,t1.c AS total,(CASE WHEN wish_list1.product_id IS NOT NULL THEN 1 ELSE 0 END) AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  category_id=" + id
								+ " AND NAME LIKE '%" + search + "%' AND sale_price >" + min_price + " and sale_price<="
								+ max_price + " GROUP BY id " + order + " limit " + page_offset + "," + page_size
								+ ")AS table1\n" + "LEFT JOIN (SELECT product_id FROM wish_list WHERE customer_id="
								+ customerId + ") AS wish_list1 ON wish_list1.product_id=table1.id\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";

						System.out.println(sql4);

					}

				}

			}
		}

		else {

			if (id == 0) {
				if (max_price == 0) {
					if (customerId == 0) {
						System.out.println("flagChange=1,id=0 max = 0 customer_id=0");

						sql4 = "SELECT table1.*,t1.c AS total,0 AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  NAME LIKE '%" + search
								+ "%' AND sale_price >" + min_price + " GROUP BY id order by sale_price " + order
								+ " limit " + page_offset + "," + page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";

						sql5 = "SELECT * FROM(\n"
								+ "SELECT t1.*,(CASE WHEN wish_list1.product_id IS NOT NULL THEN 1 ELSE 0 END)AS wishlist FROM(\n"
								+ "SELECT p1.*,p2.total FROM products AS p1  LEFT JOIN (SELECT category_id,COUNT(6) AS total FROM products GROUP BY category_id) AS p2 ON p1.category_id=p2.category_id WHERE p1.name LIKE '%"
								+ search + "%' AND p1.sale_price>" + min_price + ") AS t1\n"
								+ "LEFT JOIN (SELECT * FROM wish_list WHERE customer_id=113)AS wish_list1 ON t1.id=wish_list1.product_id)AS table2\n"
								+ "LEFT JOIN\n"
								+ "(SELECT product_id,GROUP_CONCAT(product_image.image) AS image1 FROM product_image GROUP BY product_id)AS table3 ON table2.id=table3.product_id  LIMIT 0,100";

						System.out.println(sql4);

					} else {
						System.out.println("flagChange=1,id=0 max = 0 customer_id=1");

						sql4 = "SELECT table1.*,t1.c AS total,(CASE WHEN wish_list1.product_id IS NOT NULL THEN 1 ELSE 0 END) AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  NAME LIKE '%" + search
								+ "%' AND sale_price >" + min_price + " GROUP BY id order by sale_price " + order
								+ " limit " + page_offset + "," + page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT product_id FROM wish_list WHERE customer_id=" + customerId
								+ ") AS wish_list1 ON wish_list1.product_id=table1.id\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";

						System.out.println(sql4);
					}
				} else {
					if (customerId == 0) {
						System.out.println("flagChange=1,id=0 max = 1 customer_id=0");

						sql4 = "SELECT table1.*,t1.c AS total,0 AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE NAME LIKE '%" + search
								+ "%' AND sale_price >" + min_price + " and sale_price<=" + max_price
								+ " GROUP BY id order by sale_price " + order + " limit " + page_offset + ","
								+ page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";
						System.out.println(sql4);

					} else {
						System.out.println("flagChange=1,id=0 max = 1 customer_id=1");

						sql4 = "SELECT table1.*,t1.c AS total,(CASE WHEN wish_list1.product_id IS NOT NULL THEN 1 ELSE 0 END) AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  NAME LIKE '%" + search
								+ "%' AND sale_price >" + min_price + " and sale_price<=" + max_price
								+ " GROUP BY id order by sale_price " + order + " limit " + page_offset + ","
								+ page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT product_id FROM wish_list WHERE customer_id=" + customerId
								+ ") AS wish_list1 ON wish_list1.product_id=table1.id\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";

						System.out.println(sql4);

					}

				}
			} else {

				if (max_price == 0) {
					if (customerId == 0) {
						System.out.println("flagChange=1,id=1 max = 0 customer_id=0");

						sql4 = "SELECT table1.*,t1.c AS total,0 AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  category_id=" + id
								+ " AND NAME LIKE '%" + search + "%' AND sale_price >" + min_price
								+ " GROUP BY id order by sale_price " + order + " limit " + page_offset + ","
								+ page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";
						System.out.println(sql4);

					} else {
						System.out.println("flagChange=1,id=1 max = 0 customer_id=1");

						sql4 = "SELECT table1.*,t1.c AS total,(CASE WHEN wish_list1.product_id IS NOT NULL THEN 1 ELSE 0 END) AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  category_id=" + id
								+ " AND NAME LIKE '%" + search + "%' AND sale_price >" + min_price
								+ " GROUP BY id order by sale_price " + order + " limit " + page_offset + ","
								+ page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT product_id FROM wish_list WHERE customer_id=" + customerId
								+ ") AS wish_list1 ON wish_list1.product_id=table1.id\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";

						System.out.println(sql4);
					}
				} else {
					if (customerId == 0) {
						System.out.println("flagChange=1,id=1 max = 1 customer_id=0");

						sql4 = "SELECT table1.*,t1.c AS total,0 AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  category_id=" + id
								+ " AND NAME LIKE '%" + search + "%' AND sale_price >" + min_price
								+ "  and sale_price<=" + max_price + " GROUP BY id order by sale_price " + order
								+ " limit " + page_offset + "," + page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";
						System.out.println(sql4);

					} else {
						System.out.println("flagChange=1,id=1 max = 1 customer_id=1");

						sql4 = "SELECT table1.*,t1.c AS total,(CASE WHEN wish_list1.product_id IS NOT NULL THEN 1 ELSE 0 END) AS wishlist FROM\n"
								+ "(SELECT p1.*,GROUP_CONCAT(im.image) AS image1 FROM products AS p1\n"
								+ "LEFT JOIN product_image AS im ON p1.id=im.product_id WHERE  category_id=" + id
								+ " AND NAME LIKE '%" + search + "%' AND sale_price >" + min_price + " and sale_price<="
								+ max_price + " GROUP BY id order by sale_price " + order + " limit " + page_offset
								+ "," + page_size + ")AS table1\n"
								+ "LEFT JOIN (SELECT product_id FROM wish_list WHERE customer_id=" + customerId
								+ ") AS wish_list1 ON wish_list1.product_id=table1.id\n"
								+ "LEFT JOIN (SELECT category_id,COUNT(6) AS c FROM products GROUP BY category_id) AS t1 ON t1.category_id=table1.category_id";

						System.out.println(sql4);

					}

				}

			}

		}

		// ========================================================================================================================================

		try {
			conn = DriverManager.getConnection(url, username, password);
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql4);
			while (resultSet.next()) {
				JSONObject jo_product_list = new JSONObject();
				jo_product_list.put("id", resultSet.getString("id"));

				jo_product_list.put("wishlist", resultSet.getString("wishlist"));
				jo_product_list.put("name", resultSet.getString("name"));
				jo_product_list.put("description", resultSet.getString("description"));
				jo_product_list.put("regular_price", resultSet.getFloat("regular_price"));
				jo_product_list.put("sale_price", resultSet.getFloat("sale_price"));
				jo_product_list.put("stock", resultSet.getString("stock"));
				jo_product_list.put("image", resultSet.getString("image"));
				jo_product_list.put("Currency", "INR");
				jo_product_list.put("total", resultSet.getInt("total"));
				
				String image_url = resultSet.getString("image1");

				JSONObject joimage = new JSONObject();
				JSONArray jaimage = new JSONArray();

				String[] image_url1 = null;
				if (image_url == null) {

					joimage.put("image", "NA");
					jaimage.add(joimage);
				} else {
					image_url1 = image_url.split(",");

					for (int k = 0; k < image_url1.length; k++) {
						JSONObject joimage1 = new JSONObject();
						joimage1.put("image", image_url1[k]);
						jaimage.add(joimage1);
					}

				}
				jo_product_list.put("image_extra", jaimage);
				ja_product_list.add(jo_product_list);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		jo_product_list_result.put("products", ja_product_list);

		return jo_product_list_result;

	}
}
