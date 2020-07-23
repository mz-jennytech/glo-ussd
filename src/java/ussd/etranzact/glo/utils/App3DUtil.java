/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.utils;

import com.etz.ussd.dto.Bank; 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException; 
import ussd.etranzact.glo.dto.App3DResponse;

/**
 *
 * @author Damilola.Omowaye
 */
public class App3DUtil {

    private static final Logger L = Logger.getLogger(App3DUtil.class);

    public static App3DResponse getApp3Response(String accountNumber, String jsonString) {
        App3DResponse resp = null;
        StringBuilder sb = new StringBuilder("http://www.etranzact.net/3DApp/invoke.jsp?");//Live Url
        //StringBuilder sb = new StringBuilder("http://172.17.10.101/3DApp/invoke.jsp?");
        sb.append("clientid=FuelTopupUSSDKokoAPI&");
        sb.append("providerid=FuelTopupUSSDKokoAPI&");
        sb.append("xmlinfo=<Request>");
        sb.append("<clientID>FuelTopupUSSDKokoAPI</clientID>");
        sb.append("<providerID>FuelTopupUSSDKokoAPI</providerID>");
        sb.append("<providerName>FuelTopupUSSDKokoAPI</providerName>");
        sb.append("<accountNumber>").append(accountNumber).append("</accountNumber>");
        sb.append("<otherInfo>").append(jsonString).append("</otherInfo>");
        sb.append("</Request>");
        try {
            String sendGet = sendGet(sb.toString());
            resp = convert2Response(sendGet);
        } catch (Exception ex) {
            L.info(ex);
        }
        return resp;
    }

    public static String sendGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        System.out.println("Sending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        StringBuilder response;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        L.info("RESPONSE FROM SERVICE:: " + response.toString());
        return response.toString();
    }

    private static App3DResponse convert2Response(String responseXml) {
        App3DResponse resp = null;
        try {
            resp = new App3DResponse();
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(responseXml));
            Document doc = db.parse(is);

            NodeList responseCode = doc.getElementsByTagName("responseCode");
            Element rc = (Element) responseCode.item(0);
            String rcs = getCharacterDataFromElement(rc);
            resp.setResponseCode(rcs);
            NodeList otherInfo = doc.getElementsByTagName("otherInfo");
            Element oi = (Element) otherInfo.item(0);
            String ois = getCharacterDataFromElement(oi);
            resp.setOtherInfo(ois);
            NodeList accountNumber = doc.getElementsByTagName("accountNumber");
            Element an = (Element) accountNumber.item(0);
            String ans = getCharacterDataFromElement(an);
            resp.setAccountNumber(ans);
            NodeList providerName = doc.getElementsByTagName("providerName");
            Element pn = (Element) providerName.item(0);
            String pns = getCharacterDataFromElement(pn);
            resp.setProviderName(pns);
            NodeList providerID = doc.getElementsByTagName("providerID");
            Element pi = (Element) providerID.item(0);
            String pis = getCharacterDataFromElement(pi);
            resp.setProviderID(pis);
            NodeList clientID = doc.getElementsByTagName("clientID");
            Element ci = (Element) clientID.item(0);
            String cis = getCharacterDataFromElement(ci);
            resp.setClientID(cis);

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace();
        }
        return resp;
    }

    private static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    public static String getChosenOption(String[] queue, String option) {
        String selectedOption = "";
        for (String string : queue) {
            if (string.startsWith(option)) {
                selectedOption = string;
            }
        }
        return selectedOption;
    }

    public static String otherInfoConstructor(String chosenOption, String cardNumber) {
        String[] split = chosenOption.split(":");
        StringBuilder sb = new StringBuilder("3");
        sb.append("~");
        sb.append(split[1]);
        sb.append("~");
        sb.append(split[0]);
        sb.append("~");
        sb.append(cardNumber);

        return sb.toString();
    }

    public static String getOtherInfoString(App3DResponse cardNumber, App3DResponse hotlistOptions, String chosenOption) {
        String[] split = hotlistOptions.getOtherInfo().split("~");
        String chosenOption1 = getChosenOption(split, chosenOption);
        return otherInfoConstructor(chosenOption1, cardNumber.getOtherInfo());
    }
    
    
     public static String mask(int start, int end, String mask) {
      String initialPart = mask.substring(0, start - 1);
      String partToMask = mask.substring(start, end);
      String maskedString = createMaskString(partToMask.length());
      String lastPart = mask.substring(end, mask.length());
      return initialPart + maskedString + lastPart;
   }

   public static String createMaskString(int length) {
      String mask = "";
      for (int i = 0; i < length; i++) {
         mask += "*";

      }
      return mask;
   }

    


    public static String encode(String value) throws Exception {
       return new String(Base64.encodeBase64(value .getBytes()));
    }

    public static String decode(String value) throws Exception {
        return new String(Base64.decodeBase64(value.getBytes()), "UTF-8");
    }
    
    public static void main(String[] args) throws Exception {
      String test = "eyJzdGF0dXMiOjIwMCwicHJvY2Vzc2luZ19mZWUiOjQ1LCJtZW1iZXJfY2FycyI6W3siY2FyX2lkIjoiSjU3ODkiLCJjYXJfbmFtZSI6IkZvcmQgUmFuZ2VyIiwiY2FyX3BsYXRlX251bWJlciI6IktDMDAxIn0seyJjYXJfaWQiOiJBMTIzNDUiLCJjYXJfbmFtZSI6IkZvcmQgRnVzc2lvbiIsImNhcl9wbGF0ZV9udW1iZXIiOiJLQzAwMiJ9LHsiY2FyX2lkIjoiQzE1MjA5IiwiY2FyX25hbWUiOiJGb3JkIEZpc28iLCJjYXJfcGxhdGVfbnVtYmVyIjoiS0M0NTY3OCJ9LHsiY2FyX2lkIjoiQzY3MjQiLCJjYXJfbmFtZSI6IkZvcmQgRmlzbyIsImNhcl9wbGF0ZV9udW1iZXIiOiJLQzQ1Njc3In0seyJjYXJfaWQiOiJDMTUyMzgiLCJjYXJfbmFtZSI6IkZvcmQgVGFydXMiLCJjYXJfcGxhdGVfbnVtYmVyIjoiS0M0NTY3ODAifV19";

     // String res = App3DUtil.encode(test);
      //System.out.println( "Encode string =======> "  + res);

      //
      String res2 = App3DUtil.decode(test);
      System.out.println( "Decode string =======> "  + res2);
      /*
       * output
       *   try this howto base64 -> dHJ5IHRoaXMgaG93dG8=
       *   dHJ5IHRoaXMgaG93dG8= string --> try this howto
       */
      }

}
