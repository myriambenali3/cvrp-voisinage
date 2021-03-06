package model;

import model.graph.Sommet;

import java.util.ArrayList;
import java.util.Set;

/**
 * Représente une solution, constituée d'itinéraires.
 */
public class Solution
{
    /**
     * Un ensemble d'itinéraires . Il n'existe pas deux itinéraires similaires, d'où l'utilisation d'un java.util.Set.
     */
    private ArrayList<Itinéraire> itinéraires;

    /**
     * La somme de la longueur totale de tous les itinéraires.
     */
    private double optimisationGlobale;


    /**
     * Initialise un objet de type Solution
     * @param _itinéraires L'ensemble des itinéraires pour une solution
     */
    public Solution(ArrayList<Itinéraire> _itinéraires)
    {
        // initialise l'attribut
        this.itinéraires = _itinéraires;

        // calcul de la longueur globale de la tournée
        this.recalculerLongueurGlobale();
    }

    /**
     * Constructeur vide
     */
    public Solution() {
        this.itinéraires = new ArrayList<>();
        this.optimisationGlobale = 0;
    }

    /**
     * Constructeur de copie. Il permet de créer une copie d'une Solution.
     * @param s Solution que l'on veut copier.
     */
    public Solution(Solution s) {
        this.itinéraires = new ArrayList<Itinéraire>();
        for(Itinéraire i: s.getItinéraires()) {
            this.itinéraires.add(new Itinéraire(i));
        }
        this.optimisationGlobale = s.getOptimisationGlobale();
    }

    /**
     * Ajoute une tournée à l'ensemble des solutions.
     * @param t la tournée à ajouter.
     */
    public void ajouterTournée(Itinéraire t)
    {
        // si l'ensemble ne contient pas la tournée à ajouter
        if(!this.itinéraires.contains(t))
        {
            // on ajoute la tournée à l'ensemble
            this.itinéraires.add(t);
            // on ajoute la longueur totale de la tournée à la longueur globale de la solution.
            this.optimisationGlobale += t.getLongueurTotale();
        }
        // si elle est déjà dans la tournée, exception car cas unexpected.
        else
        {
            throw new IllegalArgumentException("La tournée est déjà présente dans l'ensemble des itinéraires");
        }
    }


    /**
     * Retire une tournée de l'ensemble des solutions.
     * @param t la tournée à retirer.
     */
    public void retirerTournée(Itinéraire t)
    {
        // si l'ensemble contient bien la tournée à retirer
        if(this.itinéraires.contains(t))
        {
            // on la retire
            this.itinéraires.remove(t);
            // et on soustrait la longueur totale de la tournée à la longueur globale de la solution.
            this.optimisationGlobale -= t.getLongueurTotale();
        }
    }

    /**
     * Calcule la longueur globale de l'ensemble des itinéraires.
     * @return la longueur totale des itinéraires de l'ensemble.
     */
    public void recalculerLongueurGlobale()
    {
        double optimisationGlobaleRecalculée = 0.00;
        for(Itinéraire t : this.itinéraires)
        {
            optimisationGlobaleRecalculée += t.getLongueurTotale();
        }
        this.optimisationGlobale = optimisationGlobaleRecalculée;
    }

    /**
     * Récupère l'ensemble des itinéraires de la solution courante.
     * @return les itinéraires.
     */
    public ArrayList<Itinéraire> getItinéraires()
    {
        return this.itinéraires;
    }

    /**
     * Récupère la distance à parcourir pour effectuer l'ensemble des itinéraires.
     * @return la somme de la longueur des itinéraires.
     */
    public double getOptimisationGlobale()
    {
        return this.optimisationGlobale;
    }

    public void setOptimisationGlobale(double optimisationGlobale)
    {
        this.optimisationGlobale = optimisationGlobale;
    }
}
