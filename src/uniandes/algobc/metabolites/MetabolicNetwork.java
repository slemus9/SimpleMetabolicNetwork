package uniandes.algobc.metabolites;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
/**
 * Represents a metabolic network of reactions on metabolites
 * @author Jorge Duitama
 */
public class MetabolicNetwork {

	private Map<String,Enzyme> enzymes = new TreeMap<String,Enzyme>(); 
	private Map<String,Metabolite> metabolites = new TreeMap<String,Metabolite>();
	private Set<String> compartments = new TreeSet<String>();
	private Map<String,Reaction> reactions = new TreeMap<String,Reaction>();
	/**
	 * Adds a new gene product that can catalyze reactions
	 * @param product New gene product
	 */
	public void addEnzyme(Enzyme enzyme) {
		enzymes.put(enzyme.getId(), enzyme);
	}
	/**
	 * Adds a new metabolite. If a metabolite with the given name is already added, it 
	 * @param metabolite New metabolite
	 */
	public void addMetabolite(Metabolite metabolite) {
		metabolites.put(metabolite.getId(), metabolite);
		compartments.add(metabolite.getCompartment());
	}
	/**
	 * Adds a new reaction
	 * @param r New reaction between metabolites
	 */
	public void addReaction(Reaction r) {
		reactions.put(r.getId(),r);
		
	}
	/**
	 * Returns the gene product with the given id
	 * @param id of the product to search
	 * @return GeneProduct with the given id
	 */
	public Enzyme getEnzyme (String id) {
		return enzymes.get(id);
	}
	/**
	 * Returns the metabolite with the given id
	 * @param id of the metabolite to search
	 * @return Metabolite with the given id
	 */
	public Metabolite getMetabolite (String id) {
		return metabolites.get(id);
	}
	/**
	 * @return List of metabolites in the network
	 */
	public List<Metabolite> getMetabolitesList() {
		return new ArrayList<Metabolite>(metabolites.values());
	}
	/**
	 * @return List of reactions in the network
	 */
	public List<Reaction> getReactionsList () {
		return new ArrayList<Reaction>(reactions.values());
	}
	
	public static void main(String[] args) throws IOException {
		MetabolicNetworkXMLLoader loader = new MetabolicNetworkXMLLoader();
		MetabolicNetwork network = loader.loadNetwork(args[0]);
		System.out.println("Enzymes");
		for(Enzyme enzyme:network.enzymes.values()) {
			System.out.println(enzyme.getId()+" "+enzyme.getName());
		}
		System.out.println();
		
		List<Metabolite> metabolitesList = network.getMetabolitesList();
		System.out.println("Loaded "+metabolitesList.size()+" metabolites: ");
		for(Metabolite m:metabolitesList) {
			System.out.println(m.getId()+" "+m.getName()+" "+m.getCompartment()+" "+m.getChemicalFormula());
		}
		System.out.println();
		List<Reaction> reactions = network.getReactionsList();
		System.out.println("Loaded "+reactions.size()+" reactions");
		for(Reaction r:reactions) {
			System.out.println(r.getId()+" "+r.getName()+" "+r.getReactants().size()+" "+r.getProducts().size()+" "+r.getEnzymes().size()+" "+r.getLowerBoundFlux()+" "+r.getUpperBoundFlux());
		}
	}
}
