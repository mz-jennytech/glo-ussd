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
public class App3DResponse {

   private String responseCode;
   private String accountNumber;
   private String providerName;
   private String clientID;
   private String providerID;
   private String otherInfo;

   public String getResponseCode() {
      return responseCode;
   }

   public void setResponseCode(String responseCode) {
      this.responseCode = responseCode;
   }

   public String getAccountNumber() {
      return accountNumber;
   }

   public void setAccountNumber(String accountNumber) {
      this.accountNumber = accountNumber;
   }

   public String getProviderName() {
      return providerName;
   }

   public void setProviderName(String providerName) {
      this.providerName = providerName;
   }

   public String getClientID() {
      return clientID;
   }

   public void setClientID(String clientID) {
      this.clientID = clientID;
   }

   public String getProviderID() {
      return providerID;
   }

   public void setProviderID(String providerID) {
      this.providerID = providerID;
   }

   public String getOtherInfo() {
      return otherInfo;
   }

   public void setOtherInfo(String otherInfo) {
      this.otherInfo = otherInfo;
   }

   @Override
   public String toString() {
      return "App3DResponse [responseCode = "
              + responseCode + ", accountNumber = "
              + accountNumber + ", providerName = "
              + providerName + ", clientID = "
              + clientID + ", providerID = "
              + providerID + ", otherInfo = "
              + otherInfo + "]";
   }
}
