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
public class VasGateResponse {

   private String responseCode;
   private String fault;
   private String error;
   private String reference;
   private String clientRef;

   public VasGateResponse(String responseCode) {
      this.responseCode = responseCode;
   }

   public VasGateResponse(String responseCode, String fault, String error) {
      this.responseCode = responseCode;
      this.fault = fault;
      this.error = error;
   }

   public String getReference() {
      return reference;
   }

   public void setReference(String reference) {
      this.reference = reference;
   }

   public String getClientRef() {
      return clientRef;
   }

   public void setClientRef(String clientRef) {
      this.clientRef = clientRef;
   }

   public String getResponseCode() {
      return responseCode;
   }

   public void setResponseCode(String responseCode) {
      this.responseCode = responseCode;
   }

   public String getFault() {
      return fault;
   }

   public void setFault(String fault) {
      this.fault = fault;
   }

   public String getError() {
      return error;
   }

   public void setError(String error) {
      this.error = error;
   }

}
