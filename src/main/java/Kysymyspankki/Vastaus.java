/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Kysymyspankki;

public class Vastaus {
    private int vastausid;
    private String vastausteksti;
    private boolean oikein;

    public Vastaus(int vastaus, String vastusteksti, boolean oikein) {
        this.vastausteksti = vastusteksti;
        this.oikein = oikein;
        this.vastausid = vastaus;
    }

    public int getVastausid() {
        return vastausid;
    }

    public String getVastausteksti() {
        return vastausteksti;
    }

    public boolean isOikein() {
        return oikein;
    }  
}
