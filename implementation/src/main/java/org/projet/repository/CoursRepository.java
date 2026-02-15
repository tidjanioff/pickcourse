package org.projet.repository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.projet.model.Cours;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;



/**
 * Cette classe permet de communiquer avec l'API Planifium pour récupérer les informations relatives aux Cours.
 * Elle implémente l'interface IRepository ( qui pourrait donc être implémentée par d'autres reposotiries
 * communiquant avec d'autres sources de données), afin de réduire le couplage avec Planifium.
 * CoursRepository sera un Singleton car on ne veut avoir qu'une instance ( ça ne sert à rien d'en avoir plusieurs).
 */
public class CoursRepository implements IRepository {
    private static CoursRepository instance;
    private CoursRepository() {}
    /**
     * Retourne l’unique instance du CoursRepository (Singleton).
     * @return instance unique de CoursRepository.
     */
    public static CoursRepository getInstance() {
        if (instance == null) {
            instance = new CoursRepository();
        }
        return instance;
    }

    /**
     * Cette méthode permet de vérifier si le sigle est complet ( utile pour la recherche).
     * @param value sigle
     * @return booléen indiquant si le sigle est complet ou non.
     */

    private boolean isSigleComplet(String value) {
        // Ex: IFT1025, MAT1600, IFT2255, etc.
        return value != null && value.matches("^[A-Z]{3}\\d{4}$");
    }

    /**
     * Cette méthode permet de récupérer un Cours depuis Planifium.
     * @param param paramètre de la recherche ( id, nom, ou description)
     * @param value valeur de la recherche.
     * @param includeScheduleBool "true" ou "false" dépendamment de si on veut inclure ou non le schedule
     * @param semester le semestre si jamais on veut inclure le schedule
     * @return un type Optional de Cours
     * @throws Exception en cas d'erreur
     */
    public Optional<List<Cours>> getCourseBy(
            String param,
            String value,
            String includeScheduleBool,
            String semester
    ) throws Exception {

        // URL de base commune aux trois requêtes possibles.
        StringBuilder uri = new StringBuilder("https://planifium-api.onrender.com/api/v1/courses");

        boolean isIdExact = false;

        // Cas 1 : recherche par id → /courses/{id}
        if (param.equalsIgnoreCase("id")) {

            if (isSigleComplet(value)) {
                // Sigle complet => /courses/{id}
                uri.append("/").append(value);
                isIdExact = true;
            } else {
                // Sigle partiel => /courses?sigle={id}
                uri.append("?")
                        .append("sigle")
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            }

        } else {
            // Cas 2 : query → /courses?name=xxx ou ?description=xxx etc.
            uri.append("?")
                    .append(param)
                    .append("=")
                    // afin d'encoder les caractères spéciaux car si non ça ne fonctionne pas.
                    // Malgré l'encodage, cela ne fonctionne pas si l'expression recherchée contient des accents.
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }

        // Booleen qui permettra de savoir si on a déjà un query dans la liste de queries afin de commencer par un ?.
        boolean hasQuery = uri.toString().contains("?");

        // Ajouter include_schedule=true si demandé ( si non ce sera null)
        if ("true".equalsIgnoreCase(includeScheduleBool)) {
            uri.append(hasQuery ? "&" : "?");
            uri.append("include_schedule=true");
            hasQuery = true;
        }

        // Ajouter le semester si demandé
        if (semester != null && !semester.isEmpty() && "true".equalsIgnoreCase(includeScheduleBool)) {
            uri.append(hasQuery ? "&" : "?");
            uri.append("schedule_semester=").append(URLEncoder.encode(semester, StandardCharsets.UTF_8));
        } else if (semester != null && !semester.isEmpty() && "false".equalsIgnoreCase(includeScheduleBool)) {
            return Optional.empty();
        }

        // Construire la requête
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uri.toString()))
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // si la requête n'a pas abouti, on retourne un Optional.empty.
        if (response.statusCode() != 200) {
            return Optional.empty();
        }

        // Parsing JSON
        ObjectMapper mapper = new ObjectMapper();
        List<Cours> coursList;
        // on traite ce cas séparemment car la recherche par id ne retourne qu'un cours.
        if (isIdExact) {
            // Parse as JsonNode first to verify shape and presence of id field
            JsonNode root = mapper.readTree(response.body());
            if (!root.isObject() || !root.hasNonNull("id")) {
                return Optional.empty();
            }
            String returnedId = root.path("id").asText(null);
            if (returnedId == null || !returnedId.equalsIgnoreCase(value)) {
                return Optional.empty();
            }
            // Safe to map to Cours now
            Cours cours = mapper.treeToValue(root, Cours.class);
            coursList = List.of(cours);
        }

    /* La recherche par nom ne devrait aussi retourner qu'un cours mais il y a des exemples
        pour lesquels ça en retourne plusieurs ( Programmation 1 -> IFT1016 et IFT1015),
        aussi on peut aussi rechercher par mot-clé pour le nom ( par exemple Programmation).
     */

        else {
            coursList = mapper.readValue(
                    response.body(),
                    mapper.getTypeFactory().constructCollectionType(List.class, Cours.class)
            );
            if (coursList == null || coursList.isEmpty()) {
                return Optional.empty();
            }
        }

        return Optional.of(coursList);
    }

    /**
     * Cette méthode permet de récupérer tous les ids de Cours de Planifium.
     * Nous n'avions pas trouvé de routes permettant de récupérer directement tous les ids de Cours,
     * du coup on passe par une route qui permet d'avoir tous les cours par programmes, ce qui
     * prend malheureusement beaucoup de temps.
     * @return String contenant tous les cours de Planifium ( de l'Udem par ricochet).
     * @throws Exception en cas d'erreur
     */

    public Optional<List<String>> getAllCoursesId() throws Exception {
        // Envoi de la requête permet de récupérer tous les programmes avec des informations détaillées.
        HttpRequest getAllPrograms = HttpRequest.newBuilder()
                .uri(new URI("https://planifium-api.onrender.com/api/v1/programs"))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response =
                httpClient.send(getAllPrograms, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        List<String> allCourses = new ArrayList<>();
        // Le json obtenu contient pour chaque programme une liste de cours qui se trouve dans segments->blocs->courses
        // Parcours de chaque programme
        for (JsonNode program : root) {

            JsonNode segments = program.path("segments");
            if (!segments.isArray()) continue;

            // Parcours des segments
            for (JsonNode segment : segments) {

                JsonNode blocs = segment.path("blocs");
                if (!blocs.isArray()) continue;

                // Parcours des blocs
                for (JsonNode bloc : blocs) {

                    JsonNode courses = bloc.path("courses");
                    if (!courses.isArray()) continue;

                    // Ajout des identifiants de cours
                    for (JsonNode courseId : courses) {
                        allCourses.add(courseId.asText());
                    }
                }
            }
        }
        // set afin de supprimer les doublons car des programmes peuvent avoir des cours en commun.
        Set<String> set = new HashSet<>();
        set.addAll(allCourses);
        List<String> listeSansDoublons = new ArrayList<>(set);


        return Optional.of(listeSansDoublons);

    }

    /**
     * Cette methode retourne une liste qui contient des clés valeurs avec l'id des programmes et le nom.
     * @return Une liste clés valeurs avec l'id des programmes et le nom.
     **/
    public List<Map<String,String>> getAllPrograms() {
        List<Map<String,String>> programmes = new ArrayList<>();
        String BASE_URL = "https://planifium-api.onrender.com/api/v1/programs";
        try{
            HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            ObjectMapper mapper = new ObjectMapper();

            JsonNode json = mapper.readTree(response.toString());
            for(JsonNode program : json) {
                Map<String,String> map = new HashMap<>();
                map.put("id",program.get("id").asText());
                map.put("name",program.get("name").asText());
                programmes.add(map);
            }
        }catch (Exception e) {
            System.out.println("Erreur lors de la récupération des requêtes : " + e.getMessage());
        }
        return programmes;
    }

    /**
     * Cette méthode permet de récupérer le body response de la requête Planifium permet de vérifier
     * l'éligibilité à un cours.
     * @param courseId id du cours
     * @param completedCourses liste de cours complétés
     * @return response body de Planifium
     * @throws Exception en cas d'erreur
     */
    public String getCourseEligibility(String courseId, List<String> completedCourses) throws Exception {

        // Construire l'objet JSON
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode requestBody = mapper.createObjectNode();
        // Les noms des propriétés ont été choisis conformément à ceux du response body.
        requestBody.put("course_id", courseId);
        requestBody.putPOJO("completed_courses", completedCourses);

        String jsonBody = mapper.writeValueAsString(requestBody);

        // Ce bloc de code permet de construire la requête POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://planifium-api.onrender.com/api/v1/courseplan/check-eligibility"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // Récupérer le body
        String responseBody = response.body();
        return responseBody;





    }

    /**
     * Cette méthode permet de récupérer les cours d'un programme à partir de l'id de ce dernier.
     * @param programID id du programme.
     * @return le response body
     * @throws Exception en cas d'erreur.
     */

    public String getCoursesForAProgram(String programID) throws Exception {
        String BASE_URL = "https://planifium-api.onrender.com/api/v1/programs";
        Map<String, String> params = Map.of(
                "programs_list", programID
        );
        URI uri  = getStringBuilder(BASE_URL,params);
        try{
            HttpURLConnection connection = (HttpURLConnection) new URL(uri.toString()).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();



            return response.toString();

    }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cette méthode permet de fetch les schedules.
     * @param courseID id du cours dont on veut fetch les schedules.
     * @param semester session en question.
     * @return un InputStream.
     * @throws Exception en cas d'erreur
     */

    public InputStream fetchSchedules(String courseID, String semester) throws Exception{
        String baseUrl = "https://planifium-api.onrender.com/api/v1/schedules";
        Map<String, String> params = Map.of(
                "courses_list", "[\"" + courseID + "\"]",
                "min_semester", semester
        );

        URI uri = getStringBuilder(baseUrl, params);

        try {
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
return connection.getInputStream();



        }catch( Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     *  Cette methode forme des URL en prenant en compte des paramètres de recherche.
     * @param BASE_URL URL de base sur lequel il faudra appliquer des paramètres.
     * @param params Paramètres qui doivent être ajouté a l'URL pour effectuer une recherche optimal.
     * @return Un URI valide.
     **/
    @NotNull
    private static URI getStringBuilder(String BASE_URL,Map<String, String> params) {
        // Allow overriding the Planifium base host for testing (e.g., local HTTP server)
        String override = System.getProperty("planifium.base");
        if (override != null && !override.isBlank()) {
            try {
                URI orig = URI.create(BASE_URL);
                URI over = URI.create(override);
                String combined = over.toString().replaceAll("/+$", "") + orig.getPath();
                BASE_URL = combined;
            } catch (Exception ignored) {
                // if parsing fails, fall back to the provided BASE_URL
            }
        }



        StringBuilder sb = new StringBuilder(BASE_URL);
        if (params != null && !params.isEmpty()) {
            sb.append("?");
            params.forEach((key, value) -> {
                sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                        .append("&");
            });
            sb.deleteCharAt(sb.length() - 1); // remove trailing &
        }
        return URI.create(sb.toString());
    }

}