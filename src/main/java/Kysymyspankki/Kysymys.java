/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Kysymyspankki;
import java.util.ArrayList;
import java.util.List;



/**
 *
 * @author Ville
 */
public class Kysymys {
    private String id;
    private String kurssi;
    private String aihe;


    private String teksti;
    private List<Vastaus> vastaukset = new ArrayList<>();

    public Kysymys(String id, String kurssi, String aihe, String teksti) {
        this.id = id;
        this.kurssi = kurssi;
        this.aihe = aihe;
        this.teksti = teksti;
    }

    public void setVastaukset(String vastausteksti, boolean oikein) {
        vastaukset.add(new Vastaus(vastausteksti, oikein));
    }
    
    
    public String getId() {
        return id;
    }
    
    public String getKurssi() {
        return kurssi;
    }

    public String getAihe() {
        return aihe;
    }

    public String getTeksti() {
        return teksti;
    }
    
    
    
}
