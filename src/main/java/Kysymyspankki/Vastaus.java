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
