/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.utils;

import com.etz.ussd.dto.Bank;  
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Oluremi Adekanmbi <oluremi.adekanmbi@etranzact.com>
 */
public class FullMenuUtil {

   private static final Logger L = Logger.getLogger(FullMenuUtil.class);

 
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

   

//   public static void main(String[] args) {
//      USSDSession session = new USSDSession("2348089139085", "232323232323232323");
//      JsonObject json = jsonConstructorAccSyncP("2348089139085", "3027767092", "1234");
//      System.out.println(json);
//      UtilMethods.run(json, session);
//   }
}
