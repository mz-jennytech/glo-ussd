/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.dto;

import java.util.Objects;

/**
 *
 * @author Oluremi Adekanmbi <oluremi.adekanmbi@etranzact.com>
 */
public class Holder {

   private int position;
   private String accountNumber;
   private String bankCode;
   private String msisdn;

   public Holder(int position) {
      this.position = position;
   }

   public Holder(int position, String accountNumber, String bankCode, String msisdn) {
      this.position = position;
      this.accountNumber = accountNumber;
      this.bankCode = bankCode;
      this.msisdn = msisdn;
   }

   public int getPosition() {
      return position;
   }

   public void setPosition(int position) {
      this.position = position;
   }

   public String getAccountNumber() {
      return accountNumber;
   }

   public void setAccountNumber(String accountNumber) {
      this.accountNumber = accountNumber;
   }

   public String getBankCode() {
      return bankCode;
   }

   public void setBankCode(String bankCode) {
      this.bankCode = bankCode;
   }

   public String getMsisdn() {
      return msisdn;
   }

   public void setMsisdn(String msisdn) {
      this.msisdn = msisdn;
   }

   @Override
   public String toString() {
      return "Holder{" + "position=" + position + ", accountNumber=" + accountNumber + ", bankCode=" + bankCode + ", msisdn=" + msisdn + '}';
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 79 * hash + Objects.hashCode(this.position);
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final Holder other = (Holder) obj;
      if (!Objects.equals(this.position, other.position)) {
         return false;
      }
      return true;
   }

}
