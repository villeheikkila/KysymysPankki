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
            // käsittele kyselyn tulokset
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
            // Tehdään kysely
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Kysymys (kurssi, aihe, teksti) VALUES (?, ?, ?)");
            stmt.setString(1, req.queryParams("kysymys"));
            stmt.setString(2, req.queryParams("aihe"));
            stmt.setString(3, req.queryParams("teksti"));
            stmt.executeUpdate();
            // Suljetaan yhteys tietokantaan
            conn.close();

            res.redirect("/");
            return "";
        });
        
       Spark.post("/uusi/:id", (req, res) -> {
            // Avataan yhteys tietokantaan
            Connection conn = getConnection();
            // Tehdään kysely
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Vastaus (vastausteksti, oikein, id) VALUES (?, ?, ?)");
            stmt.setString(1, req.queryParams("vastausteksti"));
            stmt.setBoolean(2, true);
            stmt.setInt(3, Integer.parseInt(req.params(":id")));
//            int osote = Integer.parseInt(req.params(":id"));
            stmt.executeUpdate();
            // Suljetaan yhteys tietokantaan
            conn.close();

            res.redirect("/kysymykset/");
            return "";
        });
        
        Spark.get("/kysymykset/:id", (req, res) -> {
            List<Vastaus> vastaukset = new ArrayList<>();
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT id, kurssi, aihe, teksti FROM Kysymys WHERE id = (?)");
            statement.setInt(1, Integer.parseInt(req.params(":id")));
            ResultSet tulokset = statement.executeQuery();
            PreparedStatement stmt = conn.prepareStatement("SELECT vastausid, vastausteksti, oikein FROM Vastaus WHERE id = (?)");
            stmt.setInt(1, Integer.parseInt(req.params(":id")));
            ResultSet vastaus = stmt.executeQuery();
            Kysymys muisti = new Kysymys(tulokset.getInt("id"), tulokset.getString("kurssi"), tulokset.getString("aihe"), tulokset.getString("teksti"));
            while (vastaus.next()) {
                vastaukset.add(new Vastaus(vastaus.getInt("vastausid"), vastaus.getString("vastausteksti"), vastaus.getBoolean("oikein")));
            }
            HashMap map = new HashMap<>();
            map.put("vastaus", muisti);
            map.put("vastaukset", vastaukset);
            conn.close();
            return new ModelAndView(map, "kysymykset");
        }, new ThymeleafTemplateEngine());

        Spark.post("/poista/:vastausid", (req, res) -> {
            // Avataan yhteys tietokantaan
            Connection conn = getConnection();
            // tee kysely
            PreparedStatement statement = conn.prepareStatement("SELECT id FROM Vastaus WHERE vastausid = ?");
            statement.setInt(1, Integer.parseInt(req.params(":vastausid")));
            ResultSet id_raw = statement.executeQuery();
            int id = id_raw.getInt("id");
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM Vastaus WHERE vastausid = ?");
            stmt.setInt(1, Integer.parseInt(req.params(":vastausid")));
            stmt.executeUpdate();
            // Suljetaan yhteys tietokantaan
            conn.close();
            res.redirect("/kysymykset/" + id);
            return "";
        });
        
        Spark.post("/delete/:id", (req, res) -> {
            // Avataan yhteys tietokantaan
            Connection conn = getConnection();
            // tee kysely
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
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
            if (dbUrl != null && dbUrl.length() > 0) {
                return DriverManager.getConnection(dbUrl);
            }
            return DriverManager.getConnection("jdbc:sqlite:kysymykset.db");
        }

}
