package com.wooliesx;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/HttpExample 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     * 
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    CloseableHttpResponse resp;
    JSONArray sortedJsonArray;
    @FunctionName("sort")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws UnsupportedOperationException, IOException {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("sortOption");
        System.out.println(query);
        HttpGet get;
        String content = null;
       
         {
            String products = "http://dev-wooliesx-recruitment.azurewebsites.net/api/resource/products?token=57612656-ad80-40e4-b4b6-597c72369ca7";
            String shopperHistory = "http://dev-wooliesx-recruitment.azurewebsites.net/api/resource/shopperHistory?token=57612656-ad80-40e4-b4b6-597c72369ca7";
            CloseableHttpClient newClient = HttpClientBuilder.create().build();
            if (query.equals("Recommended")){
                get = new HttpGet(shopperHistory);
            }else {
                get = new HttpGet(products);
                
            }
            get.setHeader("content-type", "application/json");
            try {
                
                 resp = newClient.execute(get);
                 HttpEntity entity = resp.getEntity();
                
          if (entity != null) {
            content= (EntityUtils.toString(entity));
            System.out.println(content);

            JSONArray jsonArray = new JSONArray(content);
             sortedJsonArray = new JSONArray();
            List<JSONObject> jsonValues = new ArrayList<JSONObject>();
            for(int i = 0; i < jsonArray.length(); i++) {
               jsonValues.add(jsonArray.getJSONObject(i));
            }
            
            Collections.sort(jsonValues, new Comparator<JSONObject>() {
               private static final String KEY_NAME = "name";
               @Override
               public int compare(JSONObject a, JSONObject b) {
                  String str1 = new String();
                  String str2 = new String();
                  try {
                     str1 = (String)a.get(KEY_NAME);
                     str2 = (String)b.get(KEY_NAME);
                  } catch(JSONException e) {
                     e.printStackTrace();
                  }
                  if (query.equals("Descending")) {
                     
                     return -str1.compareTo(str2);
                  } else {
                     
                     return str1.compareTo(str2);
                  }
               }
            });
            for(int i = 0; i < jsonArray.length(); i++) {
               sortedJsonArray.put(jsonValues.get(i));
            }
           
            content = sortedJsonArray.toString();
          }
               
            }catch (IOException io) {
                io.printStackTrace();

            }           
        }
            return request.createResponseBuilder(HttpStatus.OK)
            .header("content-type", "application/json")
            .body(content).build(); 
        
    }
}