/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 *
 * @author Damilola.Omowaye
 */
public class Utils {

    private static Properties prop;
    private static InputStream input = null;
    private static final String filename = "gloConfig.properties";

    static {
        try {
            prop = new Properties();
            input = Utils.class.getClassLoader().getResourceAsStream(filename);
            //load a properties file from class path, inside static method
            prop.load(input);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static double getConfigDetail(String key) {
        String value = null;
        double dValue = 0;
        if (input == null) {
            System.out.println("Sorry, unable to find " + filename);
            return 0;
        }
        value = prop.getProperty(key);
        if (input != null) {
            try {
                if (!value.isEmpty()) {
                    dValue = Double.parseDouble(value);
                }
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dValue;
    }

    public static String getConfigDetails(String key) {
        String value = null;
        if (input == null) {
            System.out.println("Sorry, unable to find " + filename);
            return null;
        }
        value = prop.getProperty(key);
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public static Map optOutOfService(String phoneNumber, String appID, boolean service) {
        System.out.println("Entering ");
        Map limitMap = new HashMap();
        int responseCode = 0;
        String message = null;

        StringBuilder sbUrl = new StringBuilder();
        String baseServiceUrl = getConfigDetails("OPT_OUT_SERVICE_URL");
        String basePlatformUrl = getConfigDetails("OPT_OUT_PLATFORM_URL");
        if (!baseServiceUrl.endsWith("/")) {
            baseServiceUrl = baseServiceUrl + "/";
        }
        if (!basePlatformUrl.endsWith("/")) {
            basePlatformUrl = basePlatformUrl + "/";
        }
        if (service) {
            sbUrl.append(baseServiceUrl).append(phoneNumber).append("/").append(appID);
        } else {
            sbUrl.append(basePlatformUrl).append(phoneNumber);
        }
        try {
            JsonObject jObject = getHttpClient(sbUrl.toString());
            System.out.println("jObject :::::::::::::::: " + jObject.toString());
            responseCode = jObject.get("responseCode").getAsInt();
            String mMessage = jObject.get("responseMessage").getAsString();
            JsonParser parser = new JsonParser();
            JsonObject rJson = parser.parse(mMessage).getAsJsonObject();
            message = rJson.get("message").getAsString();

            limitMap.put("mainResponse", jObject);
            limitMap.put("responseCode", responseCode);
            limitMap.put("message", message);

        } catch (Exception v) {
            v.printStackTrace();
        }
        return limitMap;
    }

    public static Map getLimitValue(String mobile, String ussdCode, double amount, String appID) {
        Map limitMap = new HashMap();
        int responseCode = 0;
        String errorMessageSingle = null, errorMessageCum = null;
        boolean useToken = false, allowSingle = false, allowTotal = false;
        double singleValue = 0.0, totalValue = 0.0;

        JsonObject jj = new JsonObject();
        jj.addProperty("mobile", mobile);
        jj.addProperty("appId", appID);
        jj.addProperty("ussdCode", ussdCode);
        jj.addProperty("amount", amount);

        try {
            JsonObject jObject = postHttpClient(getConfigDetails("LIMIT_URL"), jj.toString());
//            String jjk = "{\"responseCode\":200,\"responseMessage\":\"{\\\"useToken\\\":false,\\\"dailyLimit\\\":{\\\"single\\\":{\\\"allow\\\":true,\\\"value\\\":20000},\\\"cumulative\\\":{\\\"allow\\\":true,\\\"value\\\":100000}},\\\"frequency\\\":{\\\"weeklySum\\\":0,\\\"monthlySum\\\":0,\\\"dailySum\\\":0}}\"}";
//            JsonObject jObject = new JsonParser().parse(jjk).getAsJsonObject();
                       
            System.out.println("getAppLimit jObject :::::::::::::::: " + jObject);
            responseCode = jObject.get("responseCode").getAsInt();
            String mMessage = jObject.get("responseMessage").getAsString();
            JsonParser parser = new JsonParser();
            JsonObject rJson = parser.parse(mMessage).getAsJsonObject();

            JsonObject dailyLimit = rJson.get("dailyLimit").getAsJsonObject();

            useToken = rJson.get("useToken").getAsBoolean();

            JsonObject jSingle = dailyLimit.getAsJsonObject("single");
            JsonObject mCumulative = dailyLimit.getAsJsonObject("cumulative");
            allowSingle = jSingle.get("allow").getAsBoolean();
            singleValue = jSingle.get("value").getAsDouble();

            try {
                errorMessageSingle = jSingle.get("errorMessage").getAsString();
            } catch (Exception x) {
            }
            allowTotal = mCumulative.get("allow").getAsBoolean();
            totalValue = mCumulative.get("value").getAsDouble();

            try {
                errorMessageCum = mCumulative.get("errorMessage").getAsString();
            } catch (Exception x) {
            }

            limitMap.put("mainResponse", jObject);
            limitMap.put("responseCode", responseCode);
            limitMap.put("useToken", useToken);
            limitMap.put("allowSingle", allowSingle);
            limitMap.put("singleValue", singleValue);
            limitMap.put("errorMessageSingle", errorMessageSingle);
            limitMap.put("allowTotal", allowTotal);
            limitMap.put("totalValue", totalValue);
            limitMap.put("errorMessageCum", errorMessageCum);

            Iterator iter = limitMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry mEntry = (Map.Entry) iter.next();
                System.out.println(mEntry.getKey() + " : " + mEntry.getValue());
            }

        } catch (Exception x) {
            x.printStackTrace();
        }
        return limitMap;
    }

    // HTTP/HTTPS POST request
    public static JsonObject postHttpClient(String url, String params) {

        String response = null;
        JsonObject resJson = new JsonObject();
        HttpClient httpclient = new HttpClient();

        PostMethod httppost = new PostMethod(url);
        httppost.setRequestHeader("Content-Type", "application/json");
        try {
            RequestEntity entity = new StringRequestEntity(params);

            httppost.setRequestEntity(entity);
            int statusCode = httpclient.executeMethod(httppost);

            response = httppost.getResponseBodyAsString();
            //System.out.println("body:" + response);
            //resJson = new JsonParser().parse(response).getAsJsonObject();
            // System.out.println("resp >>>> " + resJson.toString());
            resJson.addProperty("responseCode", statusCode);
            resJson.addProperty("responseMessage", response);

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            httppost.releaseConnection();
        }

        return resJson;
    }

    // HTTP/HTTPS GET request
    public static JsonObject getHttpClient(String url) {
        String response = null;
        //JsonObject resJson = new JsonObject();
        JsonObject nRJson = new JsonObject();

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            // Read the response body.
            response = method.getResponseBodyAsString();

            //resJson = new JsonParser().parse(response).getAsJsonObject();
            nRJson.addProperty("responseCode", statusCode);
            nRJson.addProperty("responseMessage", response);

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            method.releaseConnection();
        }

        return nRJson;
    }

    public static String getAPP_ID(String baseCode) {
        String[] msgLength = baseCode.split("\\*");
        String appId = msgLength[2].replace("#", "").trim();
        //System.out.println(" appId :::::::::::::::: " + appId);
        String aCode[] = getConfigDetails("APP_ID").split("~");
        //System.out.println(" aCode :::::::::::::::: " + Arrays.toString(aCode));
        String appCode = "";

        for (String aCode1 : aCode) {

            if (baseCode.startsWith("*805") && aCode1.equalsIgnoreCase("125")) {
                appCode = aCode1.trim();
            } else {
                 if (baseCode.startsWith("*777") && aCode1.equalsIgnoreCase("777")) {
                     appCode = aCode1.trim();
                 }else{
                if (appId.equalsIgnoreCase("805") && aCode1.equalsIgnoreCase("125")) {
                    appCode = aCode1.trim();
                } else if (appId.equalsIgnoreCase(aCode1)) {
                    appCode = appId;
                }
                //System.out.println(" aCode1 :::::::::::::::: " + aCode1);
            }
            }
        }
        return appCode;
    }

    public static void main(String[] args) throws Exception {
        // String nation = "germany";
        // String[] tm = getTeams(nation);
        String mm = "*389*805*1*500*08035335353*805#";

        //"063-0053072008-fd5f56b40a79a385708428e7b32ab996a681080a166a2206e750eb4819186145";
        // System.out.println(">>>>>>  == " + Arrays.toString(mm.split("\\*")));
        System.out.println(" Details :::::::::::::::: " + getAPP_ID(mm));

    }

}
