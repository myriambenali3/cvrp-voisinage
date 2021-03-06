package algorithms;

import customexceptions.*;
import model.Itinéraire;
import model.Solution;
import utilitaires.Utilitaire;

import java.util.ArrayList;
import java.util.Random;

/**
 * Classe contenant des algorithmes liés à la recherche Tabou
 */
public class Tabou
{
    /**
     * Un booléen qui permet de définir si les nouvelles solutions
     * créées au sein de cette classe sont instanciées avec un véhicule
     * à capacité infinie (true) ou non (false).
     * Notamment utile pour la génération GÉNÉRATION.ALÉATOIRE_UNIQUE
     * et le tabouSearchAvecItinéraireUnique.
     * @see Génération
     * @see Tabou
     */
    private static boolean camionÀcapacitéInfinie = false;

    /**
     * Effectue un tabou search sur une solution initiale donnée.
     * @param solutionInitiale la solution initiale, de départ, qui sera optimisée puis retournée.
     * @param tailleMaximaleListeTabou la taille maximale que prendra la liste de tabou.
     * @param nbIterMax le nombre d'itérations maximal de l'algorithme.
     * @param nbSolutionsVoisinesChaqueIter le nombre de solutions voisines devant être trouvées à chaque itération.
     * @param transfo le type de transformation à appliquer lors de la recherche de nouveaux voisins.
     * @param typeDeRechercheVoisinage le type de recherche de voisinage à appliquer.
     * @return la solution initiale optimisée.
     * @throws ItinéraireTooSmallException en cas d'itinéraire trop petit pour du 2-opt.
     * @throws VehiculeCapacityOutOfBoundsException lorsque la capacité maximale des véhicules est dépassée pendant les transformations.
     * @throws UnhandledTransformationException en cas de transformation non gérée.
     * @throws ListOfClientsIsEmptyException en cas de génération de liste de clients vide lors des transformations.
     * @throws UnhandledTypeDeRechercheVoisinageException en cas de type de recherche de voisinage non géré.
     */
    public static Solution tabouSearch(Solution solutionInitiale, int tailleMaximaleListeTabou, int nbIterMax, int nbSolutionsVoisinesChaqueIter, Transformation transfo, TypeDeRechercheVoisinage typeDeRechercheVoisinage) throws ItinéraireTooSmallException, VehiculeCapacityOutOfBoundsException, UnhandledTransformationException, ListOfClientsIsEmptyException, UnhandledTypeDeRechercheVoisinageException
    {
        Solution solutionMin = solutionInitiale;
        double fitnessMinimale = solutionMin.getOptimisationGlobale();
        ArrayList<Solution> listeTabou = new ArrayList<Solution>();
        //System.out.println("Fitness minimales (Tabou) : ");
        Solution solutionSwap = null;
        for(int i = 0; i < nbIterMax; i++)
        {
            // on récupère le voisinage
            ArrayList<Solution> voisinage = Tabou.getSolutionsVoisines(solutionMin, transfo, typeDeRechercheVoisinage, nbSolutionsVoisinesChaqueIter, solutionSwap);
            // duquel on enlève l'ensemble des transformations (solutions) interdites
            voisinage.removeAll(listeTabou);


            // et on récupère la meilleure solution de ce voisinage
            Solution meilleureSolutionVoisine = new Solution();
            meilleureSolutionVoisine.setOptimisationGlobale(Double.MAX_VALUE);
            for(Solution s:voisinage)
            {
                if(s.getOptimisationGlobale() < meilleureSolutionVoisine.getOptimisationGlobale())
                {
                    meilleureSolutionVoisine = s;
                }
            }

            //System.out.println(meilleureSolutionVoisine.getOptimisationGlobale());
            double fitnessCourante = meilleureSolutionVoisine.getOptimisationGlobale();
            // la différence de fitness
            double deltaF =  fitnessCourante - fitnessMinimale;
            // si le delta est positif et que la liste de tabou contient encore de la place
            if(deltaF > 0 && listeTabou.size() + voisinage.size() < tailleMaximaleListeTabou)
            {
                listeTabou.addAll(voisinage);
            }
            // si le delta est positif et que la liste de tabou n'a plus de place
            else if(deltaF > 0 && listeTabou.size() + voisinage.size() > tailleMaximaleListeTabou)
            {
                int espaceRequis = (listeTabou.size() + voisinage.size()) - tailleMaximaleListeTabou;
                // on supprime les "espaceRequis" anciens éléments.
                listeTabou = new ArrayList<Solution>(listeTabou.subList(espaceRequis, listeTabou.size() - 1));
                // et on ajoute les nouveaux éléments à la fin.
                listeTabou.addAll(voisinage);
            }

            if(fitnessCourante < fitnessMinimale)
            {
                solutionMin = meilleureSolutionVoisine;
                fitnessMinimale = fitnessCourante;
                // copie de la meilleure solution voisine
                solutionSwap = new Solution(meilleureSolutionVoisine);
                // display console
                if(!camionÀcapacitéInfinie)
                {
                    //System.out.println("Itération " + i + ", nouvelle fitness tabou : " + fitnessMinimale);
                }
                else
                {
                    //System.out.println("Itération " + i + ", nouvelle fitness globale avant découpage tabou : " + fitnessMinimale);
                }
            }
        }
        // display
        if(camionÀcapacitéInfinie)
        {
            //System.out.println("---Fin du tabou sur itinéraire unique.---");
        }
        else
        {
            //System.out.println("---Fin du tabou sur itinéraires normaux---");
        }
        return solutionMin;
    }

    /**
     * Cette méthode permet de gérer les solutions à itinéraires unique, dans le cas d'une génération ALÉATOIRE_UNIQUE.
     * Il faut donc l'utiliser lorsque la solution contient un itinéraire unique. Cette méthode est appelée dans le main
     * uniquement lorsque le type de génération est passé à ALÉATOIRE_UNIQUE.
     *
     * @param solutionInitiale la solution initiale constituée d'un itinéraire unique.
     * @param tailleMaximaleListeTabou la taille maximale que prendra la liste de tabou.
     * @param nbIterMax le nombre d'itérations maximal de l'algorithme.
     * @param nbSolutionsVoisinesChaqueIter le nombre de solutions voisines devant être trouvées à chaque itération.
     * @param transfo le type de transformation à appliquer lors de la recherche de nouveaux voisins.
     * @param typeDeRechercheVoisinage le type de recherche de voisinage à appliquer.
     * @param doubleTabou si un tabou doit être effectué après avoir recoupé l'itinéraire unique en plusieurs itinéraires (true) ou non (false)
     * @return la solution initiale optimisée.
     * @throws InvalidParameterForTabuSearchWithItinéraireUnique en cas de paramètre invalide pour le tabou search avec itinéraire unique.
     * @throws UnhandledTypeDeRechercheVoisinageException si le type de recherche de voisinage n'est pas géré.
     * @throws ItinéraireTooSmallException si itinéraire trop petit (2-opt).
     * @throws UnhandledTransformationException si transformation non gérée.
     * @throws ListOfClientsIsEmptyException si liste de clients vide.
     * @throws VehiculeCapacityOutOfBoundsException si véhicule dépasse la capacité en deuxième phase de 2opt.
     * @throws SubdivisionAlgorithmException si problème dans la subdivision en itinéraires plus petits.
     */
    public static Solution tabouSearchAvecItinéraireUnique(Solution solutionInitiale, int tailleMaximaleListeTabou, int nbIterMax, int nbSolutionsVoisinesChaqueIter, Transformation transfo, TypeDeRechercheVoisinage typeDeRechercheVoisinage, boolean doubleTabou) throws InvalidParameterForTabuSearchWithItinéraireUnique, UnhandledTypeDeRechercheVoisinageException, ItinéraireTooSmallException, UnhandledTransformationException, ListOfClientsIsEmptyException, VehiculeCapacityOutOfBoundsException, SubdivisionAlgorithmException
    {
        // on check si la solution est bien constituée d'un itinéraire unique. Sinon, exception et impossible de continuer car cas non attendu.
        if(solutionInitiale.getItinéraires().size() == 1)
        {
            // la capacité des camions est infinie car on est en génération aléatoire unique
            camionÀcapacitéInfinie = true;

            //solutionInitiale.getItinéraires().get(0).getVéhicule().switchCapacitéInfinie();
            Solution solAvecUniqueItinéraire = Tabou.tabouSearch(solutionInitiale, tailleMaximaleListeTabou, nbIterMax, nbSolutionsVoisinesChaqueIter, transfo, typeDeRechercheVoisinage);
            // on découpe la solution en itinéraires respectant les règles métiers.
            Solution nouvelleSolutionRespectantLesRègles = Utilitaire.subdiviserSolutionItinéraireUniqueEnPlusieursItinéraires(solAvecUniqueItinéraire);

            // on passe ce booléen à false une fois que notre solution respecte les règles métier.
            camionÀcapacitéInfinie = false;

            // Si on effectue un double tabou, cela signifie que l'on effectue une nouvelle fois une recherche tabou
            // après avoir découpé l'itinéraire unique de la solution initiales en plus petits itinéraires qui respectent
            // les règles métier.
            if(doubleTabou)
            {
                return Tabou.tabouSearch(nouvelleSolutionRespectantLesRègles, tailleMaximaleListeTabou, nbIterMax, nbSolutionsVoisinesChaqueIter, transfo, typeDeRechercheVoisinage);
            }
            // on retourne la solution.
            return nouvelleSolutionRespectantLesRègles;
        }
        else
        {
            throw new InvalidParameterForTabuSearchWithItinéraireUnique("La solution ne contient pas un unique itinéraire pour cette méthode de recherche" +
                    " tabou très spécifique. Utiliser tabuSearch à la place.");
        }
    }

    /**
     * Algorithme permettant de trouver les solutions voisines d'une solution initiale. Pour trouver une solution
     * voisine, on chosisit d'appliquer sur chaque itinéraire de la solution une transformation donnée. On peut imaginer
     * d'autres moyen
     * @param solutionInitiale la solutionInitiale.
     * @param transfo le type de transformation à appliquer.
     * @param typeDeRechercheVoisinage le type de recherche de voisinage à appliquer.
     * @param nbSolutionsVoisines le nombre de solutions voisines à trouver.
     * @param previousBestSolution Une COPIE de la dernière meilleure solution voisine. Laisser à null si aucune meilleure solution trouvée.
     * @return les solutions voisines de la solution initiale.
     * @throws UnhandledTypeDeRechercheVoisinageException en cas de type de recherche de voisinage non géré.
     * @throws UnhandledTransformationException en cas de transformation non gérée.
     * @throws VehiculeCapacityOutOfBoundsException lorsque la capacité maximale des véhicules est dépassée pendant les transformations.
     * @throws ListOfClientsIsEmptyException en cas de génération de liste de clients vide lors des transformations.
     * @throws ItinéraireTooSmallException en cas d'itinéraire trop petit pour du 2-opt.
     *
     */
    public static ArrayList<Solution> getSolutionsVoisines(Solution solutionInitiale, Transformation transfo, TypeDeRechercheVoisinage typeDeRechercheVoisinage,int nbSolutionsVoisines, Solution previousBestSolution) throws UnhandledTypeDeRechercheVoisinageException, UnhandledTransformationException, VehiculeCapacityOutOfBoundsException, ListOfClientsIsEmptyException, ItinéraireTooSmallException
    {
        switch(typeDeRechercheVoisinage)
        {
            case BASIQUE:
                return Tabou.getSolutionsVoisinesBasicSearch(solutionInitiale, transfo, nbSolutionsVoisines);
            case COMPLEXE:
                return Tabou.getSolutionsVoisinesComplexSearch(solutionInitiale, previousBestSolution, transfo, nbSolutionsVoisines);
            default:
                throw new UnhandledTypeDeRechercheVoisinageException(typeDeRechercheVoisinage, Tabou.class);
        }
    }


    /**
     *
     * Algorithme permettant de trouver les solutions voisines d'une solution voisine avec un type de recherche basique.
     * Une transformation "transfo" sera appliquée sur l'ensemble des itinéraires de la solutionInitiale afin de trouver
     * ses voisins proches.
     * @param solutionInitiale la solution à partir de laquelle chercher les solutions voisines.
     * @param transfo le type de transformation à appliquer sur la solution initiale.
     * @param nbSolutionsVoisines le nombre de solutions voisines à trouver.
     * @return les solutions voisines proches de la solutionInitiale avec une méthode de recherche de voisinage basique.
     * @throws UnhandledTransformationException Si un type de transformation donné n'est pas géré.
     * @throws VehiculeCapacityOutOfBoundsException Si une transformation a donné lieu à un dépassement de la capacité maximale des véhicules.
     * @throws ListOfClientsIsEmptyException Si la transformation 2-opt a tenté de créer un itinéraire avec une liste de clients vide.
     * @throws ItinéraireTooSmallException en cas d'itinéraire trop petit pour du 2-opt.
     */
    private static ArrayList<Solution> getSolutionsVoisinesBasicSearch(Solution solutionInitiale, Transformation transfo, int nbSolutionsVoisines) throws UnhandledTransformationException, VehiculeCapacityOutOfBoundsException, ListOfClientsIsEmptyException, ItinéraireTooSmallException
    {
        ArrayList<Solution> solutionsVoisines = new ArrayList<Solution>();
        for(int i = 0; i < nbSolutionsVoisines; i++)
        {
            Solution voisin = new Solution(solutionInitiale);
            for(int j = 0; j < voisin.getItinéraires().size(); j++)
            {
                Itinéraire it = voisin.getItinéraires().get(j);
                switch(transfo)
                {
                    case TRANSFORMATION_ÉCHANGE:
                        TransformateurItinéraire.transformationÉchange(it);
                        break;
                    case INSERTION_DÉCALAGE:
                        TransformateurItinéraire.insertionDécalage(it);
                        break;
                    case INVERSION:
                        TransformateurItinéraire.inversion(it);
                        break;
                    case TRANSFORMATION_2_OPT:
                        // En backup du 2-opt, on utilise une insertion décalage
                        voisin.getItinéraires().set(voisin.getItinéraires().indexOf(it), TransformateurItinéraire.transformation2opt(it, Transformation.TRANSFORMATION_ÉCHANGE));
                        break;
                    default:
                        throw new UnhandledTransformationException(transfo, Tabou.class);
                }
                voisin.recalculerLongueurGlobale();
            }
            solutionsVoisines.add(voisin);
        }
        return solutionsVoisines;
    }

    /**
     *
     * Réalise une recherche de solutions voisines complexe, avec des échanges de clients entre itinéraires. Réalise
     * avec une probabilité 1/2 un échange entre itinéraires, sinon un 2-opt a lieu.
     *
     * @param solutionInitiale la solution à partir de laquelle chercher les solutions voisines.
     * @param transfo le type de transformation à appliquer sur la solution initiale.
     * @param nbSolutionsVoisines le nombre de solutions voisines à trouver.
     * @return les solutions voisines proches de la solutionInitiale avec une méthode de recherche de voisinage basique.
     * @throws UnhandledTransformationException Si un type de transformation donné n'est pas géré.
     * @throws VehiculeCapacityOutOfBoundsException Si une transformation a donné lieu à un dépassement de la capacité maximale des véhicules.
     * @throws ListOfClientsIsEmptyException Si la transformation 2-opt a tenté de créer un itinéraire avec une liste de clients vide.
     * @throws ItinéraireTooSmallException en cas d'itinéraire trop petit pour du 2-opt.
     * @throws UnsupportedOperationException en cas de méta transformation non gérée.
     */
    private static ArrayList<Solution> getSolutionsVoisinesComplexSearch(Solution solutionInitiale, Solution solutionSwap, Transformation transfo, int nbSolutionsVoisines) throws UnhandledTransformationException, VehiculeCapacityOutOfBoundsException, ListOfClientsIsEmptyException, ItinéraireTooSmallException, UnsupportedOperationException
    {
        Random r = new Random();
        ArrayList<Solution> solutionsVoisines = new ArrayList<Solution>();
        for(int i = 0; i < nbSolutionsVoisines; i++)
        {
            Solution voisin = new Solution(solutionInitiale);
            for (Itinéraire itinéraireVoisin : voisin.getItinéraires())
            {
                // Uniquement la méta-transformation échange est supportée pour l'instant...
                if(transfo == Transformation.TRANSFORMATION_ÉCHANGE)
                {
                    // On fait de la méta
                    if(r.nextBoolean() && solutionSwap != null)
                    {
                        for(Itinéraire itinéraireSwap:solutionSwap.getItinéraires())
                        {
                            TransformateurEntreItinéraires.métaTransformationÉchange(itinéraireVoisin, itinéraireSwap, 40);
                        }
                    }
                    // Couplé à du 2-opt.
                    else
                    {
                        int  indexOfitinéraireModif = voisin.getItinéraires().indexOf(itinéraireVoisin);
                        Itinéraire itinéraireModif = voisin.getItinéraires().get(indexOfitinéraireModif);
                        itinéraireModif = TransformateurItinéraire.transformation2opt(itinéraireModif, Transformation.TRANSFORMATION_ÉCHANGE);
                        voisin.getItinéraires().set(indexOfitinéraireModif, itinéraireModif);
                    }
                    voisin.recalculerLongueurGlobale();
                }
                else
                {
                    throw new UnhandledTransformationException(transfo, Tabou.class, "Le type de transformation " + transfo.toString()  +" n'est pas disponible " +
                            " pour la méthode getSolutionsVoisinesComplexSearch(). La recherche complexe effectue des méta transformations." +
                            " Utiliser plutôt TRANSFORMATION_ÉCHANGE en paramètre.");
                }
            }
            solutionsVoisines.add(voisin);
        }
        return solutionsVoisines;
    }
}
