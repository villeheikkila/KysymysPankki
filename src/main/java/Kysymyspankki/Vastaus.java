/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Kysymyspankki;

/**
 *
 * @author Ville
 */
public class Vastaus {
    private String vastusteksti;
    private boolean oikein;

    public Vastaus(String vastusteksti, boolean oikein) {
        this.vastusteksti = vastusteksti;
        this.oikein = oikein;
    }

    public String getVastusteksti() {
        return vastusteksti;
    }

    public boolean isOikein() {
        return oikein;
    }
    
    
    
}
