/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Omowaye Damilola
 */
public class Dataplan {

   private int position;
   private String amount;
   private String size;
   private String validity;

   public Dataplan(int position, String amount, String size, String validity) {
      this.position = position;
      this.amount = amount;
      this.size = size;
      this.validity = validity;
   }

   public Dataplan() {
   }

   public Dataplan(int position) {
      this.position = position;
   }

   public String getAmount() {
      return amount;
   }

   public void setAmount(String amount) {
      this.amount = amount;
   }

   public String getSize() {
      return size;
   }

   public void setSize(String size) {
      this.size = size;
   }

   public String getValidity() {
      return validity;
   }

   public void setValidity(String validity) {
      this.validity = validity;
   }

   public int getPosition() {
      return position;
   }

   public void setPosition(int position) {
      this.position = position;
   }

   @Override
   public String toString() {
      return "Dataplan{" + "amount=" + amount + ", size=" + size + ", validity=" + validity + '}';
   }

   @Override
   public int hashCode() {
      int hash = 3;
      hash = 97 * hash + Objects.hashCode(this.position);
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
      final Dataplan other = (Dataplan) obj;
      if (!Objects.equals(this.position, other.position)) {
         return false;
      }
      return true;
   }

//   public static List<Dataplan> getDataPlans() {
//      List<Dataplan> dataplans = new ArrayList<>();
//      Dataplan dataplan;
//      dataplan = new Dataplan(1, "100", "10MB", "Daily");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(2, "500", "50MB", "7days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(3, "1000", "500MB", "30 days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(4, "2000", "2GB", "30 days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(5, "4000", "3GB", "30 days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(6, "6500", "5GB", "30 days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(7, "8000", "8GB", "30 days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(8, "10000", "10GB", "30 days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(9, "15000", "15GB", "30 days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(10, "18000", "20GB", "30 days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(11, "84992", "100GB", "30 days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(12, "27500", "30GB", "90 days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(13, "55000", "60GB", "180 days");
//      dataplans.add(dataplan);
//      dataplan = new Dataplan(14, "110000", "120GB", "365 days");
//      dataplans.add(dataplan);
//      return dataplans;
//   }

}
