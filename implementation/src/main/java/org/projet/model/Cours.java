package org.projet.model;
import java.util.List;
import java.util.Map;

/**
 * Cette classe permet de définir l'entité Cours.
 */
public class Cours {
    private String id;     //Id du cours
    private String description;     //Description du cours
    private String name;
    private String scheduledSemester;   //Trimestres où le cours est offert
    private String[] prerequisite_courses;
    private String[] equivalent_courses;
    private String[] concomitant_courses;
    private String udemWebsite;         //Site Web attache au cours.
    private float credits;
    private String requirement_text;
    private Map<String, Boolean> available_terms;
    private Map<String, Boolean> available_periods;
    private List<Schedule> schedules;

    /**
     * Représente l’horaire d’un cours pour une session donnée,
     * incluant les sections et les activités associées.
     */
    public static class Schedule {
        private String _id;
        private String sigle;
        private String name;
        private String semester;
        private List<Section> sections;
        private String fetch_date;
        private int semester_int;
        /**
         * Constructeur vide requis pour le mapping JSON et l’instanciation automatique.
         */
        public Schedule() {
        }
        /**
         * Retourne la valeur numérique représentant la session académique.
         * @return session sous forme d’entier.
         */
        public int getSemester_int() {
            return semester_int;
        }

        /**
         * Définit la liste des sections associées à cet horaire.
         * @param sections liste des sections.
         */
        public void setSections(List<Section> sections) {
            this.sections = sections;
        }
        /**
         * Retourne la date à laquelle les informations d’horaire ont été récupérées.
         * @return date de récupération des données.
         */
        public String getFetch_date() {
            return fetch_date;
        }
        /**
         * Retourne l’identifiant unique de l’horaire.
         * @return identifiant de l’horaire.
         */
        public String get_id() {
            return _id;
        }
        /**
         * Définit l’identifiant unique de l’horaire.
         * @param _id identifiant de l’horaire.
         */
        public void set_id(String _id) {
            this._id = _id;
        }
        /**
         * Retourne le sigle du cours associé à cet horaire.
         * @return sigle du cours.
         */
        public String getSigle() {
            return sigle;
        }
        /**
         * Définit le sigle du cours associé à cet horaire.
         * @param sigle sigle du cours.
         */
        public void setSigle(String sigle) {
            this.sigle = sigle;
        }
        /**
         * Retourne le nom du cours associé à cet horaire.
         * @return nom du cours.
         */
        public String getName() {
            return name;
        }

        /**
         * Définit le nom du cours associé à cet horaire.
         * @param name nom du cours.
         */
        public void setName(String name) {
            this.name = name;
        }
        /**
         * Retourne la session académique associée à cet horaire.
         * @return session académique.
         */
        public String getSemester() {
            return semester;
        }
        /**
         * Définit la session académique associée à cet horaire.
         * @param semester session académique.
         */
        public void setSemester(String semester) {
            this.semester = semester;
        }
        /**
         * Retourne la liste des sections associées à cet horaire.
         * @return liste des sections.
         */
        public List<Section> getSections() {
            return sections;
        }
        /**
         * Retourne une représentation textuelle complète de l’horaire du cours,
         * incluant les sections, les volets et les activités associées.
         *
         * @return chaîne de caractères représentant l’horaire.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(semester).append("\n");

            if (sections != null) {
                for (Section sec : sections) {
                    sb.append("Section ").append(sec.getName()).append("\n");

                    if (sec.getVolets() != null) {
                        for (Volet v : sec.getVolets()) {

                            if (v.getActivities() != null) {
                                for (Activity a : v.getActivities()) {
                                    sb.append(String.join("/", a.getDays()))
                                            .append(" ")
                                            .append(a.getStart_time()).append("–").append(a.getEnd_time())
                                            .append(" (").append(a.getMode()).append(")")
                                            .append("\n");
                                }
                            }
                        }
                    }
                }
            }

            return sb.toString().trim();
        }
        /**
         * Retourne une représentation textuelle de l’horaire du cours
         * pour une session académique spécifique.
         *
         * @param semesterRecherche session recherchée.
         * @return chaîne représentant l’horaire correspondant à la session,
         *         ou un message indiquant qu’aucun horaire n’est disponible.
         */
        public String toStringPourSemester(String semesterRecherche) {
            StringBuilder sb = new StringBuilder();

            if (!this.semester.equalsIgnoreCase(semesterRecherche)) {
                return "Aucun schedule pour le semester " + semesterRecherche;
            }

            sb.append("Semester ").append(this.semester).append("\n");

            if (sections != null) {
                for (Section sec : sections) {
                    sb.append("Section ").append(sec.getName()).append("\n");

                    if (sec.getVolets() != null) {
                        for (Volet v : sec.getVolets()) {

                            if (v.getActivities() != null) {
                                for (Activity a : v.getActivities()) {
                                    sb.append(String.join("/", a.getDays()))
                                            .append(" ")
                                            .append(a.getStart_time()).append("–").append(a.getEnd_time())
                                            .append(" (").append(a.getMode()).append(")")
                                            .append("\n");
                                }
                            }
                        }
                    }
                }
            }

            return sb.toString().trim();
        }


    }
    /**
     * Représente une section d’un cours, avec ses enseignants,
     * sa capacité et ses différents volets.
     */
    public static class Section {
        private String number_inscription;
        private List<String> teachers;
        private String capacity;
        private List<Volet> volets;
        private String name;
        /**
         * Constructeur vide requis pour le mapping JSON et l’instanciation automatique.
         */
        public Section() {

        }
        /**
         * Retourne le numéro d’inscription de la section.
         * @return numéro d’inscription.
         */
        public String getNumber_inscription() {
            return number_inscription;
        }
        /**
         * Définit le numéro d’inscription de la section.
         * @param number_inscription numéro d’inscription de la section.
         */
        public void setNumber_inscription(String number_inscription) {
            this.number_inscription = number_inscription;
        }
        /**
         * Retourne la liste des enseignants responsables de la section.
         * @return liste des enseignants.
         */
        public List<String> getTeachers() {
            return teachers;
        }
        /**
         * Définit la liste des enseignants responsables de la section.
         * @param teachers liste des enseignants.
         */
        public void setTeachers(List<String> teachers) {
            this.teachers = teachers;
        }
        /**
         * Retourne la capacité maximale de la section.
         * @return capacité de la section.
         */
        public String getCapacity() {
            return capacity;
        }
        /**
         * Définit la capacité maximale de la section.
         * @param capacity capacité de la section.
         */
        public void setCapacity(String capacity) {
            this.capacity = capacity;
        }
        /**
         * Retourne la liste des volets associés à la section.
         * @return liste des volets.
         */
        public List<Volet> getVolets() {
            return volets;
        }   
        /**
         * Définit la liste des volets associés à la section.
         * @param volets liste des volets.
         */
        public void setVolets(List<Volet> volets) {
            this.volets = volets;
        }
        /**
         * Retourne le nom de la section.
         * @return nom de la section.
         */
        public String getName() {
            return name;
        }
        /**
         * Définit le nom de la section.
         * @param name nom de la section.
         */
        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Représente un volet d’enseignement d’une section
     * (ex : cours magistral, laboratoire, TP).
     */
    public static class Volet {
        private String name;
        private List<Activity> activities;

        /**
         * Constructeur vide requis pour le mapping JSON et l’instanciation automatique.
         */
        public Volet() {

        }
        /**
         * Retourne le nom du volet.
         * @return nom du volet.
         */
        public String getName() {
            return name;
        }
        /**
         * Définit le nom du volet.
         * @param name nom du volet.
         */
        public void setName(String name) {
            this.name = name;
        }
        /**
         * Retourne la liste des activités associées à ce volet.
         * @return liste des activités.
         */
        public List<Activity> getActivities() {
            return activities;
        }
        /**
         * Définit la liste des activités associées à ce volet.
         * @param activities liste des activités.
         */
        public void setActivities(List<Activity> activities) {
            this.activities = activities;
        }
    }
    /**
     * Représente une activité pédagogique associée à un volet,
     * incluant l’horaire, le lieu et le mode d’enseignement.
     */
    public static class Activity {
        private List<String> days;
        private String start_time;
        private String end_time;
        private String start_date;
        private String end_date;
        private String campus;
        private String place;
        private String pavillon_name;
        private String room;
        private String mode;
        /**
         * Constructeur vide requis pour le mapping JSON et l’instanciation automatique.
         */
        public Activity(){
        }
        /**
         * Retourne la liste des jours durant lesquels l’activité a lieu.
         * @return liste des jours de l’activité.
         */
        public List<String> getDays() {
            return days;
        }
        /**
         * Définit la liste des jours durant lesquels l’activité a lieu.
         * @param days liste des jours de l’activité.
         */
        public void setDays(List<String> days) {
            this.days = days;
        }
        
        /**
         * Retourne l’heure de début de l’activité.
         * @return heure de début.
         */
        public String getStart_time() {
            return start_time;
        }
        /**
         * Définit l’heure de début de l’activité.
         * @param start_time heure de début.
         */
        public void setStart_time(String start_time) {
            this.start_time = start_time;
        }
        /**
         * Retourne l’heure de fin de l’activité.
         * @return heure de fin.
         */
        public String getEnd_time() {
            return end_time;
        }
        /**
         * Définit l’heure de fin de l’activité.
         * @param end_time heure de fin.
         */
        public void setEnd_time(String end_time) {
            this.end_time = end_time;
        }
        /**
         * Retourne la date de début de l’activité.
         * @return date de début.
         */
        public String getStart_date() {
            return start_date;
        }
        /**
         * Définit la date de début de l’activité.
         * @param start_date date de début.
         */
        public void setStart_date(String start_date) {
            this.start_date = start_date;
        }
        /**
         * Retourne la date de fin de l’activité.
         * @return date de fin.
         */
        public String getEnd_date() {
            return end_date;
        }
        /**
         * Définit la date de fin de l’activité.
         * @param end_date date de fin.
         */
        public void setEnd_date(String end_date) {
            this.end_date = end_date;
        }
        /**
         * Retourne le campus où se déroule l’activité.
         * @return campus de l’activité.
         */
        public String getCampus() {
            return campus;
        }
        /**
         * Définit le campus où se déroule l’activité.
         * @param campus campus de l’activité.
         */
        public void setCampus(String campus) {
            this.campus = campus;
        }
        /**
         * Retourne le lieu précis de l’activité.
         * @return lieu de l’activité.
         */
        public String getPlace() {
            return place;
        }
        /**
         * Définit le lieu précis de l’activité.
         * @param place lieu de l’activité.
         */
        public void setPlace(String place) {
            this.place = place;
        }
        /**
         * Retourne le nom du pavillon où se déroule l’activité.
         * @return nom du pavillon.
         */
        public String getPavillon_name() {
            return pavillon_name;
        }
        /**
         * Définit le nom du pavillon où se déroule l’activité.
         * @param pavillon_name nom du pavillon.
         */
        public void setPavillon_name(String pavillon_name) {
            this.pavillon_name = pavillon_name;
        }
        /**
         * Retourne la salle où se déroule l’activité.
         * @return salle de l’activité.
         */
        public String getRoom() {
            return room;
        }
        /**
         * Définit la salle où se déroule l’activité.
         * @param room salle de l’activité.
         */
        public void setRoom(String room) {
            this.room = room;
        }   
        /**
         * Retourne le mode d’enseignement de l’activité (présentiel, en ligne, hybride).
         * @return mode d’enseignement.
         */
        public String getMode() {
            return mode;
        }
        /**
         * Définit le mode d’enseignement de l’activité.
         * @param mode mode d’enseignement.
         */
        public void setMode(String mode) {
            this.mode = mode;
        }
    }
    //Constructeur général.

    // jackson a besoin d'un constructeur vide pour faire le mapping
    /**
     * Constructeur vide requis pour le mapping JSON et l’instanciation automatique.
     */
    public Cours(){}
    /**
     * Constructeur complet permettant d’instancier un cours avec
     * l’ensemble de ses informations académiques.
     *
     * @param available_terms trimestres durant lesquels le cours est offert.
     * @param id identifiant du cours.
     * @param description description du cours.
     * @param name nom du cours.
     * @param scheduledSemester session planifiée du cours.
     * @param schedules horaires associés au cours.
     * @param prerequisite_courses cours préalables requis.
     * @param equivalent_courses cours équivalents.
     * @param concomitant_courses cours concomitants.
     * @param udemWebsite lien vers la page officielle du cours.
     * @param credits nombre de crédits du cours.
     * @param requirement_text exigences ou conditions du cours.
     * @param available_periods périodes disponibles.
     */
    public Cours(Map<String, Boolean> available_terms, String id, String description, String name, String scheduledSemester, List<Schedule> schedules, String[] prerequisite_courses, String[] equivalent_courses, String[] concomitant_courses, String udemWebsite, float credits, String requirement_text, Map<String, Boolean> available_periods) {
        this.available_terms = available_terms;
        this.id = id;
        this.description = description;
        this.name = name;
        this.scheduledSemester = scheduledSemester;
        this.schedules = schedules;
        this.prerequisite_courses = prerequisite_courses;
        this.equivalent_courses = equivalent_courses;
        this.concomitant_courses = concomitant_courses;
        this.udemWebsite = udemWebsite;
        this.credits = credits;
        this.requirement_text = requirement_text;
        this.available_periods = available_periods;
    }

    //Getters et Setters.
    /**
     * Définit l’identifiant du cours.
     * @param id identifiant du cours.
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * Retourne l’identifiant du cours.
     * @return identifiant du cours.
     */
    public String getId() {
        return id;
    }




    /**
     * Retourne la description du cours.
     * @return description du cours.
     */
    public String getDescription() {
        return description;
    }
    /**
     * Retourne la session planifiée du cours.
     * @return session du cours.
     */
    public String getScheduledSemester() {
        return scheduledSemester;
    }
    
    
    /**
     * Retourne le lien vers la page officielle du cours sur le site de l’UdeM.
     * @return lien du site web du cours.
     */
    public String getUdemWebsite() {
        return udemWebsite;
    }

    /**
     * Modifie la description du cours.
     * @param description nouvelle description du cours.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Définit la session planifiée du cours.
     * @param scheduledSemester session du cours.
     */
    public void setScheduledSemester(String scheduledSemester) {
        this.scheduledSemester = scheduledSemester;
    }
    /**
     * Définit le lien vers la page officielle du cours sur le site de l’UdeM.
     * @param udemWebsite lien du site web du cours.
     */
    public void setUdemWebsite(String udemWebsite) {
        this.udemWebsite = udemWebsite;
    }
    /**
     * Retourne le nom du cours.
     * @return nom du cours.
     */
    public String getName() {
        return name;
    }
    /**
     * Définit le nom du cours.
     * @param name nom du cours.
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Retourne la liste des horaires associés au cours.
     * @return liste des horaires.
     */
    public List<Schedule> getSchedules() {
        return schedules;
    }

    /**
     * Définit la liste des horaires associés au cours.
     * @param schedules liste des horaires.
     */
    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }
    /**
     * Retourne la liste des cours préalables requis.
     * @return cours préalables.
     */
    public String[] getPrerequisite_courses() {
        return prerequisite_courses;
    }
    /**
     * Définit la liste des cours préalables requis.
     * @param prerequisite_courses cours préalables.
     */
    public void setPrerequisite_courses(String[] prerequisite_courses) {
        this.prerequisite_courses = prerequisite_courses;
    }
    /**
     * Retourne la liste des cours équivalents.
     * @return cours équivalents.
     */
    public String[] getEquivalent_courses() {
        return equivalent_courses;
    }
    /**
     * Définit la liste des cours équivalents.
     * @param equivalent_courses cours équivalents.
     */
    public void setEquivalent_courses(String[] equivalent_courses) {
        this.equivalent_courses = equivalent_courses;
    }
    /**
     * Retourne la liste des cours concomitants.
     * @return cours concomitants.
     */
    public String[] getConcomitant_courses() {
        return concomitant_courses;
    }
    /**
     * Définit la liste des cours concomitants.
     * @param concomitant_courses cours concomitants.
     */
    public void setConcomitant_courses(String[] concomitant_courses) {
        this.concomitant_courses = concomitant_courses;
    }
    /**
     * Retourne le nombre de crédits du cours.
     * @return nombre de crédits.
     */
    public float getCredits(){
        return credits;
    }
    /**
     * Définit le nombre de crédits du cours.
     * @param credits nombre de crédits.
     */
    public void setCredits(float credits) {
        this.credits = credits;
    }
    /**
     * Retourne le texte décrivant les exigences du cours.
     * @return texte des exigences.
     */
    public String getRequirement_text() {
        return requirement_text;
    }
    /**
     * Définit le texte décrivant les exigences du cours.
     * @param requirement_text texte des exigences.
     */
    public void setRequirement_text(String requirement_text) {
        this.requirement_text = requirement_text;
    }
    /**
     * Retourne les trimestres durant lesquels le cours est offert.
     * @return trimestres disponibles.
     */
    public Map<String, Boolean> getAvailable_terms() {
        return available_terms;
    }
    /**
     * Définit les trimestres durant lesquels le cours est offert.
     * @param available_terms trimestres disponibles.
     */
    public void setAvailable_terms(Map<String, Boolean> available_terms) {
        this.available_terms = available_terms;
    }
    /**
     * Retourne les périodes disponibles pour le cours.
     * @return périodes disponibles.
     */
    public Map<String, Boolean> getAvailable_periods() {
        return available_periods;
    }
    /**
     * Définit les périodes disponibles pour le cours.
     * @param available_periods périodes disponibles.
     */
    public void setAvailable_periods(Map<String, Boolean> available_periods) {
        this.available_periods = available_periods;
    }

}

