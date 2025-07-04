package pedigree;

import java.util.List;
import java.util.Map;

/**
 * Lance la simulation avec les paramètres donnés en argument.
 */

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java pedigree.Main <n> <tMax>");
            System.exit(1);
        }

        int n = Integer.parseInt(args[0]);
        double tMax = Double.parseDouble(args[1]);

        // You can adjust these if needed
        double accidentRate = 0.01;
        double deathRate = 12.5;
        double scale = 100.0;

        AgeModel ageModel = new AgeModel(accidentRate, deathRate, scale);
        double reproductionRate = 0.2; // example value
        double fidelity = 0.9;         // example value

        Simulator sim = new Simulator(ageModel, reproductionRate, fidelity);
        sim.simulate(n, tMax);

        List<Sim> living = sim.getLiving();

        List<Map.Entry<Double, Integer>> paternal = sim.tracePaternalCoalescence(living);
        List<Map.Entry<Double, Integer>> maternal = sim.traceMaternalCoalescence(living);

        System.out.println("Time\tPaternal_Lineages\tMaternal_Lineages");

        int i = 0, j = 0;
        while (i < paternal.size() || j < maternal.size()) {
            double pt = i < paternal.size() ? paternal.get(i).getKey() : Double.MAX_VALUE;
            double mt = j < maternal.size() ? maternal.get(j).getKey() : Double.MAX_VALUE;

            if (Math.abs(pt - mt) < 1e-6) {
                System.out.printf("%.2f\t%d\t\t%d\n", pt, paternal.get(i).getValue(), maternal.get(j).getValue());
                i++; j++;
            } else if (pt < mt) {
                System.out.printf("%.2f\t%d\t\t\n", pt, paternal.get(i).getValue());
                i++;
            } else {
                System.out.printf("%.2f\t\t\t%d\n", mt, maternal.get(j).getValue());
                j++;
            }
        }
    }
}