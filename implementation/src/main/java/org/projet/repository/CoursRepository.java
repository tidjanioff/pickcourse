package org.projet.repository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.projet.model.Cours;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;



/**
 * Cette classe permet de récupérer les informations relatives aux Cours depuis le cache local.
 * L'éligibilité reste une requête directe vers Planifium.
 * Elle implémente l'interface IRepository ( qui pourrait donc être implémentée par d'autres reposotiries
 * communiquant avec d'autres sources de données), afin de réduire le couplage avec Planifium.
 * CoursRepository sera un Singleton car on ne veut avoir qu'une instance ( ça ne sert à rien d'en avoir plusieurs).
 */
public class CoursRepository implements IRepository {
    private static CoursRepository instance;
    private final CatalogCacheRepository catalogCacheRepository;

    private CoursRepository() {
        this(new CatalogCacheRepository());
    }

    public CoursRepository(CatalogCacheRepository catalogCacheRepository) {
        this.catalogCacheRepository = catalogCacheRepository;
    }
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
     * Cette méthode permet de récupérer un Cours depuis le cache local.
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
        if (semester != null && !semester.isEmpty() && !"true".equalsIgnoreCase(includeScheduleBool)) {
            return Optional.empty();
        }

        boolean includeSchedule = "true".equalsIgnoreCase(includeScheduleBool);
        return catalogCacheRepository.findCoursesBy(param, value, includeSchedule, semester);
    }

    /**
     * Cette méthode permet de récupérer tous les ids de Cours du cache local.
     * Nous n'avions pas trouvé de routes permettant de récupérer directement tous les ids de Cours,
     * du coup on passe par une route qui permet d'avoir tous les cours par programmes, ce qui
     * prend malheureusement beaucoup de temps.
     * @return String contenant tous les cours de l'UdeM présents dans le cache local.
     * @throws Exception en cas d'erreur
     */

    public Optional<List<String>> getAllCoursesId() throws Exception {
        return catalogCacheRepository.findAllCourseIds();
    }

    /**
     * Cette methode retourne une liste qui contient des clés valeurs avec l'id des programmes et le nom.
     * @return Une liste clés valeurs avec l'id des programmes et le nom.
     **/
    public List<Map<String,String>> getAllPrograms() {
        return catalogCacheRepository.findAllPrograms();
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
        return catalogCacheRepository.coursesForProgramJson(programID);
    }

    /**
     * Cette méthode permet de fetch les schedules.
     * @param courseID id du cours dont on veut fetch les schedules.
     * @param semester session en question.
     * @return un InputStream.
     * @throws Exception en cas d'erreur
     */

    public InputStream fetchSchedules(String courseID, String semester) throws Exception{
        return catalogCacheRepository.schedulesAsInputStream(courseID, semester);
    }

}
