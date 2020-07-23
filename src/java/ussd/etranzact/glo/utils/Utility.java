package ussd.etranzact.glo.utils;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;
import ussd.etranzact.glo.dto.VasGateResponse;

public class Utility {

    public static String vasgatePublicKey = ""; 
    public static final String provider = "GLO";
    private static final Map<Integer, String> autoSwitchCodes = new HashMap();

    // public static String PUBLIC_KEY_FILE = "C:\\wildfly-8.1.0.Final\\bin\\ussd2_public.key";//Prod Env
    //TEST PARAMETERS
      /*
    public static String PUBLIC_KEY_FILE = "C:\\DEVELOPMENT\\IDE\\wildfly-10.1.0.Final\\bin\\vas-smsinterface.key";//myLocal
    //public static String PUBLIC_KEY_FILE = "E:\\wildfly-8.1.0.Final\\bin\\vas-smsinterface.key";// 172.17.10.20    
    public static String vasgateURL = "http://172.17.10.16:1505/receiver/action/vasgate";//Test Env;
    public static String encryptionKey = "SMSINTERFACE";//Test
   */
    //LIVE PARAMETERS
    public static String PUBLIC_KEY_FILE = Utils.getConfigDetails("PUBLIC_KEY_FILE");
    public static String vasgateURL = Utils.getConfigDetails("VASGATE_URL");
    public static String encryptionKey = Utils.getConfigDetails("ENCRYPTION_KEY");

    static {
        loadRSAKey();

        autoSwitchCodes.put(Integer.valueOf(-1), "Request Timeout");
        autoSwitchCodes.put(Integer.valueOf(0), "Transaction Successful");
        autoSwitchCodes.put(Integer.valueOf(1), "Destination Card Not Found");
        autoSwitchCodes.put(Integer.valueOf(2), "Card Number Not Found");
        autoSwitchCodes.put(Integer.valueOf(3), "Invalid Card PIN");
        autoSwitchCodes.put(Integer.valueOf(4), "Card Expiration Incorrect");
        autoSwitchCodes.put(Integer.valueOf(5), "Insufficient balance");
        autoSwitchCodes.put(Integer.valueOf(6), "Spending Limit Exceeded");
        autoSwitchCodes.put(Integer.valueOf(7), "Internal System Error Occurred, please contact the service provider");

        autoSwitchCodes.put(Integer.valueOf(8), "Financial Institution cannot authorize transaction, Please try later");
        autoSwitchCodes.put(Integer.valueOf(9), "PIN tries Exceeded");
        autoSwitchCodes.put(Integer.valueOf(10), "Card has been locked");
        autoSwitchCodes.put(Integer.valueOf(11), "Invalid Terminal Id");
        autoSwitchCodes.put(Integer.valueOf(12), "Payment Timeout");
        autoSwitchCodes.put(Integer.valueOf(13), "Destination card has been locked");
        autoSwitchCodes.put(Integer.valueOf(14), "Card has expired");
        autoSwitchCodes.put(Integer.valueOf(15), "PIN change required");

        autoSwitchCodes.put(Integer.valueOf(16), "Invalid Amount");
        autoSwitchCodes.put(Integer.valueOf(17), "Card has been disabled");
        autoSwitchCodes.put(Integer.valueOf(18), "Unable to credit this account immediately, credit will be done later");
        autoSwitchCodes.put(Integer.valueOf(19), "Transaction not permitted on terminal");
        autoSwitchCodes.put(Integer.valueOf(20), "Exceeds withdrawal frequency");
        autoSwitchCodes.put(Integer.valueOf(21), "Destination Card has expired");
        autoSwitchCodes.put(Integer.valueOf(22), "Destination Card Disabled");
        autoSwitchCodes.put(Integer.valueOf(23), "Source Card Disabled");
        autoSwitchCodes.put(Integer.valueOf(24), "Invalid Bank Account");
        autoSwitchCodes.put(Integer.valueOf(25), "Insufficient Balance");
        autoSwitchCodes.put(Integer.valueOf(27), "This Bank is currently not available to process your transaction");
        autoSwitchCodes.put(Integer.valueOf(28), "Insufficient TSS account balance");
        autoSwitchCodes.put(Integer.valueOf(29), "Unable to obtain Transaction Status");
        autoSwitchCodes.put(Integer.valueOf(30), "Bank Account Restricted");
        autoSwitchCodes.put(Integer.valueOf(31), "Successfully received your request. Please DO NOT RETRY beneficiary will be credited shortly");
        autoSwitchCodes.put(Integer.valueOf(32), "Successfully received your request. Please DO NOT RETRY beneficiary will be credited shortly");

    }

    public static void main(String[] args) {
        System.out.println("Result ::::::::::: " + getAutoSwitchMessage(-1));
    }

    public static String getAutoSwitchMessage(int code) {
        return (String) autoSwitchCodes.get(Integer.valueOf(code));
    }

    public static VasGateResponse postToVasgate(String msisdn, String postRequest) {

        VasGateResponse result = new VasGateResponse("500", "", "");

        String postResponse;
        System.out.println("VASGATE POST REQUEST FOR : [" + msisdn + "] == " + postRequest);
        HttpURLConnection conn = null;
        try {
            URL url = new URL(vasgateURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/plain");
            conn.setRequestProperty("CLIENT_APP", encryptionKey);//For Prod
            //conn.setRequestProperty("CLIENT_APP", "SMSINTERFACE");//For Test
            conn.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(doRSA(postRequest));
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();
            System.out.println("[" + msisdn + "] Response Code : " + responseCode);

            String inputLine;
            StringBuilder response = new StringBuilder();

            InputStream is;
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }

            switch (responseCode) {
                case 200:
                    BufferedReader in = new BufferedReader(new InputStreamReader(is));
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    postResponse = response.toString();
                    System.out.println("[" + msisdn + "] VASGATE POST RESPONSE : " + postResponse);
                    Gson gson = new Gson();
                    Map resultMap = gson.fromJson(postResponse, HashMap.class);
                    result = new VasGateResponse("200",
                            (String) resultMap.get("fault"),
                            (String) resultMap.get("error"));
                    break;
                case 503:
                    System.out.println("[" + msisdn + "] 503 Timeout Error from VASGATE");
                    result = new VasGateResponse("503");
                    break;
                default:
                    System.out.println("[" + msisdn + "] HTTP Connection Failed to : " + url);
                    result = new VasGateResponse(responseCode + "");
                    break;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return result;
    }

    public static String verifyGLODataPlan(String alias, String type, String client, String uniqueId) {
        String json = "";
        String ret = "";
        try {
            json = "{"
                    + "	\"reference\": \"" + uniqueId + "\","
                    + "	\"alias\": \"" + alias + "\","
                    + "	\"action\": \"query\","
                    + "	\"type\": \"" + type + "\","
                    + "	\"account\": \"\","
                    + "	\"client\": \"" + client + "\""
                    + "}";
            System.out.println("json: " + json);
            ret = postToServer(vasgateURL, json);
            //ret = sendRequest(doRSA(json));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return ret;
    }

    public static String postToServer(String urls, String json) {
        String postResponse = "";
        String successful = "06";
        HttpURLConnection conn = null;
        try {

            URL url = new URL(urls);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            conn.setRequestProperty("CLIENT_APP", encryptionKey);//For Test
            //conn.setRequestProperty("CLIENT_APP", "USSD2");//For Prod
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            //System.out.println("Sending 'POST' request to URL : " + url);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            String rsaa = doRSA(json);
            wr.writeBytes(rsaa);
            wr.flush();
            wr.close();
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            String inputLine;
            StringBuffer response = new StringBuffer();

            InputStream is;
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
            } else {
                /* error from server */
                is = conn.getErrorStream();
            }
            if (responseCode == 200) {
                //System.out.println("[" + terminalID + "] HTTP Connection Successful to : " + url);
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                is.close();
                //System.out.println("RESPONSE : " + response.toString());
                postResponse = response.toString();
                System.out.println("VTU POST RESPONSE : " + postResponse);
                if (postResponse.indexOf("\"error\":\"0\"") > -1 || postResponse.indexOf("\"error\":\"00\"") > -1) {
                    successful = "0";
                } else if (postResponse.indexOf("\"error\":\"56\"") > -1) {
                    successful = "56";
                }
            } else if (responseCode == 503) {
                successful = "0";
            } else {
                System.out.println(" HTTP Connection Failed to : " + url + postResponse);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        //maybe we need to add finally to close connection
        return postResponse;
    }

    public static String doVasGate(String url, String json) {
        String ret = "";
        try {
            System.out.println("json: " + json);
            ret = postToServer(url, json);
            //ret = sendRequest(doRSA(json));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return ret;
    }

    public static void loadRSAKey() {

        try {
            //      Encrypt the string using the public key
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
            Utility.vasgatePublicKey = (String) inputStream.readUTF();
            inputStream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static String doRSA(String data) {
        String encData = "";
        try {

            String publicKey = vasgatePublicKey;

            Cipher cipher = Cipher.getInstance("RSA");//MD5withRSA
            //Convert PublicKeyString to Byte Stream
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] sigBytes2 = decoder.decodeBuffer(publicKey);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(sigBytes2);
            KeyFactory keyFact = KeyFactory.getInstance("RSA");
            PublicKey pubKey2 = keyFact.generatePublic(x509KeySpec);

            cipher.init(Cipher.ENCRYPT_MODE, pubKey2);
            byte[] cipherText = cipher.doFinal(data.getBytes());
            encData = new BASE64Encoder().encode(cipherText);

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return encData;
    }

    public static String genKEY(int len) {
        String uCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String intChar = "0123456789";
        Random r = new Random();
        String pass = "";

        while (pass.length() != len) {
            int rPick = r.nextInt(4);
            if (rPick == 1) {
                int spot = r.nextInt(25);
                pass += uCase.charAt(spot);
            } else if (rPick == 3) {
                int spot = r.nextInt(9);
                pass += intChar.charAt(spot);
            }
        }

        return "02" + pass;
    }

    public static String genKEYID(int len) {
        String uCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String intChar = "0123456789";
        Random r = new Random();
        String pass = "";

        while (pass.length() != len) {
            int rPick = r.nextInt(4);
            if (rPick == 1) {
                int spot = r.nextInt(25);
                pass += uCase.charAt(spot);
            } else if (rPick == 3) {
                int spot = r.nextInt(9);
                pass += intChar.charAt(spot);
            }
        }

        return pass;
    }

}
