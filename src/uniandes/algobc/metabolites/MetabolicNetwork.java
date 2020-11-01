package uniandes.algobc.metabolites;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a metabolic network of reactions on metabolites
 * @author Jorge Duitama
 */
public class MetabolicNetwork {

	private Map<String,Enzyme> enzymes = new TreeMap<String,Enzyme>(); 
	private Map<String,Metabolite> metabolites = new TreeMap<String,Metabolite>();
	private Set<String> compartments = new TreeSet<String>();
	private Map<String,Reaction> reactions = new TreeMap<String,Reaction>();

	private Map<Metabolite, Map<Metabolite, Long>> network = new HashMap<>();

	/**
	 * Adds a new gene product that can catalyze reactions
	 * @param enzyme New gene product
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

	/**
	 * (Homework. Exercise 2.a)
	 * @return Set of metabolites that participate as reactants in the reaction
	 */
	public Set<Metabolite> getReactantMetabolites () {
		return reactions.values().stream()
				.flatMap(r -> r.getReactants().stream().map(ReactionComponent::getMetabolite))
				.collect(Collectors.toSet());
	}

	/**
	 * (Homework. Exercise 2.b)
	 * @return Set of metabolites that participate as products in the reaction
	 */
	public Set<Metabolite> getProductMetabolites () {
		return reactions.values().stream()
				.flatMap(r -> r.getProducts().stream().map(ReactionComponent::getMetabolite))
				.collect(Collectors.toSet());
	}

	private long getReactionsHaving (Metabolite reactant, Metabolite product) {
		return reactions.values().stream().filter(r ->
					r.getReactants().stream().anyMatch(c -> c.getMetabolite().equals(reactant)) &&
					r.getProducts().stream().anyMatch(c -> c.getMetabolite().equals(product))
				).count();
	}

	/**
	 * (Homework. Exercise 2.b) Builds a directed graph where each node is a Metabolite, and
	 * there is an edge from node v to node u if v is a reactant and node u is a product in some number
	 * of reactions; the weight of that each is the number of reactions
	 */
	public void buildMetabolicNetwork () {
		Set<Metabolite> reactants = getReactantMetabolites();
		Set<Metabolite> products = getProductMetabolites();
		for (Metabolite r : reactants) {
			for (Metabolite p : products) {
				long numReactions = getReactionsHaving(r, p);
				if (numReactions > 0) {
					network.compute(r, (k, edges) -> {
						if (edges == null) {
							Map<Metabolite, Long> adj = new HashMap<>();
							adj.put(p, numReactions);
							return adj;
						} else {
							edges.put(p, numReactions);
							return edges;
						}
					});
				}
			}
		}
	}

	public void writeNetwork (String filePath) throws Exception{

		if (network.isEmpty()) {
			throw new Exception("The Network needs to be constructed before writing it in a file");
		}

		File outFile = new File(filePath);
		Files.deleteIfExists(outFile.toPath());
		FileWriter fw = new FileWriter(outFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		network.forEach((reactant, products) -> products.forEach((product, numReactions) -> {
			try {
				String line = String.format(
						"%s\t%s\t%d\n",
						reactant.getId().trim(),
						product.getId().trim(),
						numReactions);
				bw.write(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
		bw.close();
	}

	public static void main(String[] args) throws Exception {
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
		System.out.println();

		Set<Metabolite> reactants = network.getReactantMetabolites();
		System.out.println("There are " + reactants.size() + " metabolites that participate as reactants");
		for (Metabolite m : reactants) {
			System.out.println(m.getId()+" "+m.getName()+" "+m.getCompartment()+" "+m.getChemicalFormula());
		}
		System.out.println();

		Set<Metabolite> products = network.getProductMetabolites();
		System.out.println("There are " + reactants.size() + " metabolites that participate as products");
		for (Metabolite m : products) {
			System.out.println(m.getId()+" "+m.getName()+" "+m.getCompartment()+" "+m.getChemicalFormula());
		}
		System.out.println();

		String filepath = "./data/e_coli_network.out";
		network.buildMetabolicNetwork();
		System.out.println("Writing Metabolic Network into " + filepath);
		network.writeNetwork(filepath);
		System.out.println("Writing successful!!!");
	}
}
