package com.shopNow.Lambda;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Shop_now_product_detail implements RequestHandler<JSONObject, JSONObject> {

	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject j, Context context) {

		Object id;
		LambdaLogger logger = context.getLogger();


		if (j.get("id") != null && j.get("id") != "") {
			id = j.get("id");
		} else {
			id = 0;
		}
		
		logger.log("\n Invoked products Start " +id );
		ResultSet resultSet = null, resultSet1,resultSet_vendor,resultSet_offer, resultSet2 = null;
		String similar_products_id = null;
		String[] similar_product_array = {};
		String[] recommended_product_id_array = {};
		String vendor_id = null;
		
		
		Statement stmt = null;
		Connection con = null;
		String url = "";
		String username = "";
		String password = "";
		JSONArray jsonArray_product = new JSONArray();
		JSONObject jsonObject_product_Result = new JSONObject();
		JSONObject jsonObject_image = new JSONObject();
		
		JSONArray vendor_id_array = new JSONArray();
		List<Integer> ints = new ArrayList<Integer>();
		try {
			con = DriverManager.getConnection(url, username, password);
			stmt = con.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {

			String SQL_Product_detail="SELECT p.id,p.name,p.description,p.regular_price,p.sale_price,p.stock,categories.name AS category,p.image,p.product_url,p.similar_product_id,p.recommended_product_id,p.vendor_id,\r\n" + 
					"product_attributes.original_product_id,product_attributes.product_id FROM products AS p INNER JOIN  categories ON p.category_id=categories.id left JOIN product_attributes ON p.id=product_attributes.product_id\r\n" + 
					" WHERE p.id='"+id+"' and product_attributes.original_product_id='"+id+"' LIMIT 1";
			
			String SQL_Product_detail1="SELECT p.id,p.name,p.description,p.regular_price,p.sale_price,p.stock,categories.name AS category,p.image,p.product_url,p.similar_product_id,p.recommended_product_id,p.vendor_id\r\n" + 
					" FROM products AS p INNER JOIN categories ON p.category_id=categories.id where p.id='"+id+"'"; 
			
			
			
			logger.log(SQL_Product_detail1);
			resultSet = stmt.executeQuery(SQL_Product_detail1);
			
			
			
			logger.log("");
			while (resultSet.next()) {
				JSONObject jsonObject_product_detail = new JSONObject();

				jsonObject_product_detail.put("id", resultSet.getString("id"));
				jsonObject_product_detail.put("name", resultSet.getString("name"));
				jsonObject_product_detail.put("description", resultSet.getString("description"));
				jsonObject_product_detail.put("regular_price", resultSet.getFloat("regular_price"));
				jsonObject_product_detail.put("sale_price", resultSet.getFloat("sale_price")+" Rs");
				
				jsonObject_product_detail.put("stock", resultSet.getString("stock"));
				jsonObject_product_detail.put("product_url", resultSet.getString("product_url"));
				//jsonObject_product_detail.put("original_id", resultSet.getString("original_product_id"));
				jsonObject_image.put("Image", resultSet.getString("image"));
				
				
				JSONArray jsonArray_image = new JSONArray();

				jsonArray_image.add(jsonObject_image);
				jsonObject_product_detail.put("Image", jsonArray_image);
				jsonObject_product_detail.put("category", resultSet.getString("category"));
				jsonArray_product.add(jsonObject_product_detail);
				
				similar_products_id = resultSet.getString("similar_product_id");	
				if (similar_products_id != null) {
					similar_product_array = similar_products_id.split(",");
				}
				String recommended_product_id = resultSet.getString("recommended_product_id");				
				if (recommended_product_id != null) {
					recommended_product_id_array = recommended_product_id.split(",");
				}

			//-----------------------vendor-----	
				
				vendor_id = resultSet.getString("vendor_id");
				
				
				ints.add(Integer.parseInt(vendor_id.toString()));
				
				logger.log("\n Invoked products vendor" +vendor_id );
				
			}
			jsonObject_product_Result.put("product_detail", jsonArray_product);
			jsonObject_product_Result.put("Similar_Products", similar_product_array);
			jsonObject_product_Result.put("Recommended_Products", recommended_product_id_array);	
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		
		
		     logger.log("\n Invoked products offer");  
		     
		     try {
		    	   String SQl_offer="SELECT product_offers.* FROM products  left JOIN product_offers ON products.id=product_offers.product_id WHERE products.id="+id;
		    			   logger.log("\n Invoked products offer_SQL \n" +SQl_offer); 
		    			resultSet_offer=stmt.executeQuery(SQl_offer);
				
		  			
		  			JSONArray offer_array = new JSONArray();
					
		  			JSONObject jsonObject_offer = new JSONObject();
		  			JSONObject jsonObject_offer1 = new JSONObject();
		  			int i=1;
		  			while(resultSet_offer.next()) {			
						
						offer_array.add(resultSet_offer.getString("offer_name"));
		  			}
					
					jsonObject_product_Result.put("offers", offer_array);
					resultSet_offer.close();
		 	 
		    
		     }
		     catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	

		logger.log("\n Invoked products attribute");

		String sql_product_att1 = "SELECT pa.product_id,GROUP_CONCAT(att_group_name,'\":\"',av.att_value) AS attribute_value,pa.price_change AS price,pa.product_url FROM product_attributes pa  \n"
				+ "INNER JOIN\n"
				+ "attributes_value av ON av.id=pa.att_group_val_id INNER JOIN attributes a ON a.id=pa.att_group_id  where pa.product_id="
				+ id;
		
		
		
		
		String sql_product_att="SELECT t3.*,products.* FROM products INNER JOIN(SELECT t2.original_product_id,GROUP_CONCAT(attribute_value ORDER BY original_product_id) AS attribute_value FROM\r\n" + 
				"(SELECT attributes_value.id,GROUP_CONCAT(att_group_name,'\":\"',attributes_value.att_value) AS attribute_value FROM attributes_value  INNER JOIN  attributes ON attributes_value.att_group_id= attributes.id GROUP BY attributes_value.id)AS t1\r\n" + 
				"INNER JOIN (SELECT original_product_id,att_group_val_id FROM product_attributes WHERE product_id="+id+") AS t2 ON t1.id=t2.att_group_val_id GROUP BY original_product_id)AS t3 ON t3.original_product_id=products.id";
		
		
		try {

			resultSet1 = stmt.executeQuery(sql_product_att);
			JSONArray attribute_array = new JSONArray();

			
			while (resultSet1.next()) {
				JSONObject jsonObject_attribute = new JSONObject();
				String attribute_value = resultSet1.getString("attribute_value");
				String[] attribute_value1 = {};
				if (attribute_value != null) {
					attribute_value1 = attribute_value.split(",");
				}
				jsonObject_attribute.put("attribute_value1",attribute_value1);
				jsonObject_attribute.put("product_url",resultSet1.getString("product_url"));
				jsonObject_attribute.put("original_id",resultSet1.getString("original_product_id"));
				
				String vedondr_id=resultSet1.getString("vendor_id");
			    int a = Integer.parseInt(vedondr_id.toString());
				
				if(!ints.contains(a))
				{
				
					ints.add(a);
				}
				
				
			
				
				
				jsonObject_attribute.put("vendor_id",vedondr_id);
				
				jsonObject_attribute.put("sale_price",resultSet1.getString("sale_price"));
				jsonObject_attribute.put("regular_price",resultSet1.getString("regular_price"));
				//jsonObject_attribute.put("vendor_id",resultSet1.getString("vendor_id"));
				
				attribute_array.add(jsonObject_attribute);
				
				
				
			}
			jsonObject_product_Result.put("attribute", attribute_array);
			resultSet1.close();

			
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		logger.log("\n Invoked products vendor");	
	     try {
		
	    	 
	    	 for(int i=0;i<ints.size();i++) {
	    		 resultSet_vendor=stmt.executeQuery("select * from admin where id ="+ints.get(i));
		 
	     while(resultSet_vendor.next()) {
			JSONObject jsonObject_vendor = new JSONObject();	
			jsonObject_vendor.put("vendor_id",resultSet_vendor.getString("id"));
			jsonObject_vendor.put("vendor_name",resultSet_vendor.getString("name"));
			jsonObject_vendor.put("image","");
			//resultSet_vendor.getString("image")
			//jsonObject_vendor.put("contact_detail",resultSet_vendor.getString("emailid"));
			jsonObject_vendor.put("is_external",resultSet_vendor.getString("is_external"));
			vendor_id_array.add(jsonObject_vendor);	
			
		}
	    	 
		
	    	 
		 resultSet_vendor.close();
		 
	    	 }
	    	 jsonObject_product_Result.put("vendors", vendor_id_array);
	     } catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	
		
		
		
		

		logger.log("\n Invoked products_option fields");

		String sql_product_option = "SELECT table1.product_id,GROUP_CONCAT(table1.option_name,'\":\"',product_option_value.value) AS product_option FROM(\n"
				+ "SELECT product_options.id,product_options.product_id,product_option_group.option_name FROM product_options INNER JOIN  product_option_group ON product_options.id=product_option_group.id WHERE product_options.product_id=16) AS table1\n"
				+ "INNER JOIN\n" + "product_option_value ON table1.id=product_option_value.id WHERE table1.product_id="
				+ id;

		try {

			resultSet2 = stmt.executeQuery(sql_product_option);
			while (resultSet2.next()) {
				String product_option = resultSet2.getString("product_option");
				String[] product_option_array = {};
				if (product_option != null) {
					product_option_array = product_option.split(",");
				}

				jsonObject_product_Result.put("option", product_option_array);
			}
			resultSet2.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		jsonObject_product_Result.put("Currency", "INR");
		return jsonObject_product_Result;
	}
}