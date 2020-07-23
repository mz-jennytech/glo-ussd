/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.dto;

/**
 *
 * @author Oluremi Adekanmbi <oluremi.adekanmbi@etranzact.com>
 */
public class App3D {

   private String clientId;
   private String providerId;

   public App3D(String clientId, String providerId) {
      this.clientId = clientId;
      this.providerId = providerId;
   }

   public String getClientId() {
      return clientId;
   }

   public void setClientId(String clientId) {
      this.clientId = clientId;
   }

   public String getProviderId() {
      return providerId;
   }

   public void setProviderId(String providerId) {
      this.providerId = providerId;
   }

}
