/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etz.ussd.glo.executor;

import java.util.Date;
import org.apache.log4j.Logger;
import ussd.etranzact.glo.model.UssdRequest;

/**
 *
 * @author Oluremi Adekanmbi <oluremi.adekanmbi@etranzact.com>
 */
public class TestTask implements Runnable {

   private static final Logger L = Logger.getLogger(ConcurrentTask1.class);

   @Override
   public void run() {
      for (int i = 0; i < 5000; i++) {
         try {
            System.out.println(i);
            L.info("" + i);
            UssdRequest ur = new UssdRequest();
            ur.setAppid("Test");
            ur.setCreated(new Date());
            ur.setMessage("My message");
            ur.setMobileNo("08098753155");
            ur.setProvider("My Provider");
            //ur.setUniqueTransId("UniqueTransID");
            //USSDServiceImpl.getInstance().createSubsciberRequest(ur);
         } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            L.info(e.getLocalizedMessage());
         }
      }
   }

}
