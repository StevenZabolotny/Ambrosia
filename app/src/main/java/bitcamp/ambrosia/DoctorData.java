import java.io.*;
import java.net.*;
import java.net.URL;
import org.json.*;


public class DoctorData {

   public static String[][] getData(String type, String lat, String lon) {
	   try
	   {
		String urlToRead = "https://api.betterdoctor.com/2016-03-01/doctors?location=" + lat + "%2C" + lon + "%2C100&user_location=" + lat + "%2C" + lon + "&skip=0&limit=3&user_key=027c0eae17c1551502696f2dfc9ae2eb";
		  
		  URL link = new URL(urlToRead);
		  URLConnection conn = link.openConnection();
		  conn.setRequestProperty("Accept-Charset", "UTF-8");
		  InputStream response = conn.getInputStream();
			String responseBody = convertStreamToString(response);
		  String[][] results = new String[3][3];
		 JSONObject data = new JSONObject(responseBody);
		  JSONArray practices = data.getJSONArray("data");
		  for(int i = 0; i < 3; i++)
		  {
			  JSONObject addressData = practices.getJSONObject(i).getJSONArray("practices").getJSONObject(0).getJSONObject("visit_address");
			  String address = addressData.getString("street") + "\n" + addressData.getString("city") + ", " + addressData.getString("state") + " " + addressData.getString("zip");
			  JSONObject nameData = practices.getJSONObject(i).getJSONObject("profile");
			  String name = nameData.getString("first_name") + " " + nameData.getString("last_name") + " " + nameData.getString("title");
			  JSONArray phoneData = practices.getJSONObject(i).getJSONArray("practices").getJSONObject(0).getJSONArray("phones");
			  String phone = phoneData.getJSONObject(0).getString("number");
			  results[i][0] = name;
			  results[i][1] = address;
			  results[i][2] = phone;
		  }
		  
		  return results;
	   }
	   catch(MalformedURLException e)
	   {
		   return null;
	   }
	   catch(IOException e)
	   {
		   return null;
	   }
	   catch(JSONException e) {
		   return null;
	   }
   }
   
   static String convertStreamToString(java.io.InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
}
}