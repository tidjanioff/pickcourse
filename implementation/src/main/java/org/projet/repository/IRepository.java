package org.projet.repository;

import org.projet.model.Cours;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cette classe représente l'interface repository pour communiquer avec une API eztérieure pour obtenir les informations
 * officielles sur les cours.
 */
public interface IRepository {

    /**
     * Cette méthode permet de récupérer un Cours de la source de données utilisée.
     * @param param paramètre de la recherche ( id, nom, ou description)
     * @param value valeur de la recherche.
     * @param includeScheduleBool "true" ou "false" dépendamment de si on veut inclure ou non le schedule
     * @param semester le semestre si jamais on veut inclure le schedule
     * @return un type Optional de Cours
     * @throws Exception une erreur
     */
    public Optional<List<Cours>> getCourseBy(
            String param,
            String value,
            String includeScheduleBool,
            String semester
    ) throws Exception;

    /**
     * Cette methode retourne une liste qui contient des clés valeurs avec l'id des programmes et le nom.
     * @return Une liste clés valeurs avec l'id des programmes et le nom.
     **/
    public List<Map<String,String>> getAllPrograms();
    /**
     * Cette méthode permet de récupérer tous les ids de Cours de la source.
     * Nous n'avions pas trouvé de routes permettant de récupérer directement tous les ids de Cours,
     * du coup on passe par une route qui permet d'avoir tous les cours par programmes, ce qui
     * prend malheureusement beaucoup de temps.
     * @return String contenant tous les cours de la source.
     * @throws Exception une erreur
     */
    public Optional<List<String>> getAllCoursesId() throws Exception;
    /**
     * Cette méthode permet de récupérer le body response de la requête de la source permet de vérifier
     * l'éligibilité à un cours.
     * @param courseId id du cours
     * @param completedCourses liste de cours complétés
     * @return response body de Planifium
     * @throws Exception une erreur
     */
    public String getCourseEligibility(String courseId, List<String> completedCourses) throws Exception;
    /**
     * Cette méthode permet de récupérer les cours d'un programme à partir de l'id de ce dernier.
     * @param programID id du programme.
     * @return le response body
     * @throws Exception en cas d'erreur.
     */

    public String getCoursesForAProgram(String programID) throws Exception;
    /**
     * Cette méthode permet de fetch les schedules.
     * @param courseID id du cours dont on veut fetch les schedules.
     * @param semester session en question.
     * @return un InputStream.
     * @throws Exception en cas d'erreur
     */

    public InputStream fetchSchedules(String courseID, String semester) throws Exception;


}
