package Kysymyspankki;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import spark.ModelAndView;
import spark.Spark;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

public class Main {

    public static void main(String[] args) throws Exception {
        // Korjaus, jotta sovellus löytää oikean portin Herokussa
        if (System.getenv("PORT") != null) {
            Spark.port(Integer.valueOf(System.getenv("PORT")));
        }
        

        Spark.get("/", (req, res) -> {
            List<Kysymys> kysymykset = new ArrayList<>();
            
            // Avataan yhteys tietokantaan
            Connection conn = getConnection();
            
            // Tehdään kysely
            PreparedStatement stmt = conn.prepareStatement("SELECT id, kurssi, aihe, teksti FROM Kysymys");
            ResultSet tulos = stmt.executeQuery();
            
            // Käsittele kyselyn tulokset
            while (tulos.next()) {
                kysymykset.add(new Kysymys(tulos.getInt("id"), tulos.getString("kurssi"), tulos.getString("aihe"), tulos.getString("teksti")));
            }
            
            // sulje yhteys tietokantaan
            conn.close();
            HashMap map = new HashMap<>();
            map.put("lista", kysymykset);

            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        Spark.post("/create", (req, res) -> {
            // Avataan yhteys tietokantaan
            Connection conn = getConnection();
            // Tarkastetaan, että kaikki kentät on täytetty ja tehdään tietokantakysely
            if ((req.queryParams("kysymys").length() != 0) && (req.queryParams("aihe").length() != 0) && (req.queryParams("teksti").length() != 0)) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Kysymys (kurssi, aihe, teksti) VALUES (?, ?, ?)");
                stmt.setString(1, req.queryParams("kysymys"));
                stmt.setString(2, req.queryParams("aihe"));
                stmt.setString(3, req.queryParams("teksti"));
                stmt.executeUpdate();
            }
            // Suljetaan yhteys tietokantaan
            conn.close();

            res.redirect("/");
            return "";
        });
        
       Spark.post("/uusi/:id", (req, res) -> {
            // Avataan yhteys tietokantaan
            Connection conn = getConnection();
            
            // Tarkastetaan, että kaikki kentät on täytetty ja tehdään tietokantakysely
            if (req.queryParams("vastausteksti").length() != 0) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Vastaus (vastausteksti, oikein, id) VALUES (?, ?, ?)");
                stmt.setString(1, req.queryParams("vastausteksti"));                
                // Tarkastetaan onko kysymys oikein vai väärin
                if (req.queryParams("oikein") != null) {
                    stmt.setBoolean(2, true);
                } else {
                    stmt.setBoolean(2, false);
                }
                stmt.setInt(3, Integer.parseInt(req.params(":id")));
                stmt.executeUpdate();
            }
            
            //Haetaan id siirtymistä varten
            int osote = Integer.parseInt(req.params(":id"));
            
            // Suljetaan yhteys tietokantaan
            conn.close();

            res.redirect("/kysymykset/" + osote);
            return "";
        });
        
        Spark.get("/kysymykset/:id", (req, res) -> {
            List<Vastaus> vastaukset = new ArrayList<>();
            List<Kysymys> muisti = new ArrayList<>();
            
            // Avataan yhteys tietokantaan ja tehdään kyselyt
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT id, kurssi, aihe, teksti FROM Kysymys WHERE id = (?)");
            statement.setInt(1, Integer.parseInt(req.params(":id")));
            ResultSet tulokset = statement.executeQuery();
            PreparedStatement stmt = conn.prepareStatement("SELECT vastausid, vastausteksti, oikein FROM Vastaus WHERE id = (?)");
            stmt.setInt(1, Integer.parseInt(req.params(":id")));
            ResultSet vastaus = stmt.executeQuery();
            
            // Käydään läpi saadut arvot
            while (tulokset.next()) {
                muisti.add(new Kysymys(tulokset.getInt("id"), tulokset.getString("kurssi"), tulokset.getString("aihe"), tulokset.getString("teksti")));                            
            }
            while (vastaus.next()) {
                vastaukset.add(new Vastaus(vastaus.getInt("vastausid"), vastaus.getString("vastausteksti"), vastaus.getBoolean("oikein")));
            }
            
            // Suljetaan yhteys tietokantaan
            conn.close();
            
            // Lisätään tieto sivulle
            HashMap map = new HashMap<>();
            map.put("vastaus", muisti);
            map.put("vastaukset", vastaukset);
            
            return new ModelAndView(map, "kysymykset");
        }, new ThymeleafTemplateEngine());

        Spark.post("/poista/:vastausid", (req, res) -> {
            List<Integer> id = new ArrayList<>();
            
            // Haetaan id kun vastausid tiedetään
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT id FROM Vastaus WHERE vastausid = ?");
            statement.setInt(1, Integer.parseInt(req.params(":vastausid")));
            ResultSet id_raw = statement.executeQuery();
            
            // Postgresql vaatimus...
            while (id_raw.next()) {
                id.add(id_raw.getInt("id"));
            }
            
            // Poistetaan rivit tietokannasta
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM Vastaus WHERE vastausid = ?");
            stmt.setInt(1, Integer.parseInt(req.params(":vastausid")));
            stmt.executeUpdate();
            
            // Suljetaan yhteys tietokantaan
            conn.close();
            
            res.redirect("/kysymykset/" + id.get(0));
            return "";
        });
        
        Spark.post("/delete/:id", (req, res) -> {
            // Avataan yhteys tietokantaan
            Connection conn = getConnection();
            
            // Poistetaan kysymys tietokannasta id:n perusteella
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM Kysymys WHERE id = ?");
            stmt.setInt(1, Integer.parseInt(req.params(":id")));
            stmt.executeUpdate();
            
            // Suljetaan yhteys tietokantaan
            conn.close();
            
            res.redirect("/");
            return "";
        });
    }
    
    public static Connection getConnection() throws Exception {
        // Mahdollistaa käytön lokaalisti sqlitellä ja Herokussa Postgresql:llä
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
            if (dbUrl != null && dbUrl.length() > 0) {
                return DriverManager.getConnection(dbUrl);
            }
            return DriverManager.getConnection("jdbc:sqlite:kysymykset.db");
        }

}
