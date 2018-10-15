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
            Connection conn = DriverManager.getConnection("jdbc:sqlite:kysymykset.db");
            // Tehdään kysely
            PreparedStatement stmt = conn.prepareStatement("SELECT id, kurssi, aihe, teksti FROM Kysymys");
            ResultSet tulos = stmt.executeQuery();
            // käsittele kyselyn tulokset
            while (tulos.next()) {
                kysymykset.add(new Kysymys(tulos.getString("id"), tulos.getString("kurssi"), tulos.getString("aihe"), tulos.getString("teksti")));
            }
            // sulje yhteys tietokantaan
            conn.close();
            HashMap map = new HashMap<>();
            map.put("lista", kysymykset);

            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        Spark.post("/create", (req, res) -> {
            // Avataan yhteys tietokantaan
            Connection conn = DriverManager.getConnection("jdbc:sqlite:kysymykset.db");
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
        
       Spark.post("/uusi", (req, res) -> {
            // Avataan yhteys tietokantaan
            Connection conn = DriverManager.getConnection("jdbc:sqlite:kysymykset.db");
            // Tehdään kysely
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Vastaus (vastausteksti, oikein, id) VALUES (?, ?, ?)");
            stmt.setString(1, req.queryParams("vastausteksti"));
            stmt.setBoolean(2, true);
            stmt.setString(3, req.queryParams("id"));
            stmt.executeUpdate();
            // Suljetaan yhteys tietokantaan
            conn.close();

            res.redirect("/");
            return "";
        });
        

        Spark.get("~/kysymykset/:id", (req, res) -> {            
            List<Kysymys> vastaukset = new ArrayList<>();
            // Avataan yhteys tietokantaan
            Connection conn = DriverManager.getConnection("jdbc:sqlite:kysymykset.db");
            // Tehdään kysely
            PreparedStatement statement = conn.prepareStatement("SELECT id, kurssi, aihe, teksti FROM Kysymys WHERE id = (?)");
            statement.setString(1, req.queryParams(":id"));
            ResultSet tulos = statement.executeQuery();
            PreparedStatement stmt = conn.prepareStatement("SELECT vastausteksti, oikein FROM Vastaukset WHERE id = (?)");
            ResultSet vastaus = stmt.executeQuery();
            // käsittele kyselyn tulokset
            vastaukset.add(new Kysymys(tulos.getString("id"), tulos.getString("kurssi"), tulos.getString("aihe"), tulos.getString("teksti")));
            while (vastaus.next()) {
                vastaukset.get(0).setVastaukset(vastaus.getString("vastausteksti"), vastaus.getBoolean("oikein"));
            }
            // sulje yhteys tietokantaan
            conn.close();
            HashMap map = new HashMap<>();
            map.put("vastaukset", vastaukset);
            return new ModelAndView(map, "~/kysymykset");
        });
        
        Spark.post("/poista/:vastausid", (req, res) -> {
            // Avataan yhteys tietokantaan
            Connection conn = DriverManager.getConnection("jdbc:sqlite:kysymykset.db");
            // tee kysely
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM Vastaus WHERE vastausid = ?");
            stmt.setString(1, req.params(":vastausid"));
            stmt.executeUpdate();
            // Suljetaan yhteys tietokantaan
            conn.close();
            res.redirect("~/kysymykset");
            return "";
        });
        
        Spark.post("/delete/:id:", (req, res) -> {
            // Avataan yhteys tietokantaan
            Connection conn = DriverManager.getConnection("jdbc:sqlite:kysymykset.db");
            // tee kysely
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM Kysymys WHERE id = ?");
            stmt.setString(1, req.params(":id"));
            stmt.executeUpdate();
            // Suljetaan yhteys tietokantaan
            conn.close();
            res.redirect("/");
            return "";
        });
    }

}
