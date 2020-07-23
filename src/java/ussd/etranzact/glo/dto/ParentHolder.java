/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.dto;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Oluremi Adekanmbi <oluremi.adekanmbi@etranzact.com>
 */
public class ParentHolder {

   @SerializedName("airtel-dataplans")
   private List<Dataplan> airteldataplans = new ArrayList<Dataplan>();
   @SerializedName("etisalat-dataplans")
   private List<Dataplan> etisalatdataplans = new ArrayList<Dataplan>();

   public List<Dataplan> getAirteldataplans() {
      return airteldataplans;
   }

   public void setAirteldataplans(List<Dataplan> airteldataplans) {
      this.airteldataplans = airteldataplans;
   }

   public List<Dataplan> getEtisalatdataplans() {
      return etisalatdataplans;
   }

   public void setEtisalatdataplans(List<Dataplan> etisalatdataplans) {
      this.etisalatdataplans = etisalatdataplans;
   }

}
