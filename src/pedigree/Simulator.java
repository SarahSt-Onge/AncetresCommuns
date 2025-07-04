package pedigree;

import java.util.*;

/**
 * Classe responsable de la simulation de la population et des événements.
 */
class Simulator {
    private final MinHeap<Event> eventQueue = new MinHeap<Event>(Comparator.naturalOrder());
    private final ArrayList<Sim> living = new ArrayList<>();
    private final Random rng = new Random();

    private final AgeModel ageModel;
    private final double reproductionRate;
    private final double fidelity;

    /**
     * Constructeur du simulateur.
     */
    public Simulator(AgeModel ageModel, double r, double fidelity) {
        this.ageModel = ageModel;
        this.reproductionRate = r;
        this.fidelity = fidelity;
    }

    /**
     * Lance la simulation avec n individus jusqu'à un temps maximal.
     */
    public void simulate(int n, double tMax) {
        for (int i = 0; i < n; i++) {
            Sim.Sex sex = rng.nextBoolean() ? Sim.Sex.F : Sim.Sex.M;
            Sim founder = new Sim(sex);
            double deathTime = ageModel.randomAge(rng);
            founder.setDeathTime(deathTime);
            eventQueue.insert(new Event(0.0, founder, EventType.BIRTH));
        }

        while (!eventQueue.isEmpty()) {
            Event e = eventQueue.extractMin();
            if (e.time > tMax) break;
            if (e.time >= e.subject.getDeathTime()) continue;

            switch (e.type) {
                case BIRTH -> handleBirth(e);
                case DEATH -> handleDeath(e);
                case REPRODUCTION -> handleReproduction(e);
            }
        }
    }

    /**
     * Gère la naissance d'un individu.
     */
    private void handleBirth(Event e) {
        Sim baby = e.subject;
        living.add(baby);

        double deathTime = e.time + ageModel.randomAge(rng);
        baby.setDeathTime(deathTime);
        eventQueue.insert(new Event(deathTime, baby, EventType.DEATH));

        if (baby.getSex() == Sim.Sex.F) {
            double wait = ageModel.randomWaitingTime(rng, reproductionRate);
            eventQueue.insert(new Event(e.time + wait, baby, EventType.REPRODUCTION));
        }
    }

    /**
     * Gère la mort d'un individu.
     */
    private void handleDeath(Event e) {
        living.remove(e.subject);
    }

    /**
     * Gère la reproduction d'un individu.
     */
    private void handleReproduction(Event e) {
        Sim mother = e.subject;
        if (!living.contains(mother) || !mother.isMatingAge(e.time)) return;

        Sim father = selectPartner(mother, e.time);
        if (father == null) return;

        Sim.Sex babySex = rng.nextBoolean() ? Sim.Sex.F : Sim.Sex.M;
        Sim baby = new Sim(mother, father, e.time, babySex);

        eventQueue.insert(new Event(e.time, baby, EventType.BIRTH));

        mother.setMate(father);
        father.setMate(mother);

        double wait = ageModel.randomWaitingTime(rng, reproductionRate);
        eventQueue.insert(new Event(e.time + wait, mother, EventType.REPRODUCTION));
    }

    /**
     * Sélectionne un partenaire masculin pour la reproduction.
     */
    private Sim selectPartner(Sim mother, double time) {
        Sim partner = mother.getMate();

        if (mother.isInARelationship(time) && rng.nextDouble() < fidelity) {
            return partner;
        }

        List<Sim> candidates = new ArrayList<>();
        for (Sim s : living) {
            if (s.getSex() == Sim.Sex.M && s.isMatingAge(time)) {
                candidates.add(s);
            }
        }

        if (candidates.isEmpty()) return null;
        return candidates.get(rng.nextInt(candidates.size()));
    }

    /**
     * Suit la lignée paternelle pour déterminer la coalescence.
     */
    public List<Map.Entry<Double, Integer>> tracePaternalCoalescence(List<Sim> population) {
        return traceLineage(population, false);
    }

    /**
     * Suit la lignée maternelle pour déterminer la coalescence.
     */
    public List<Map.Entry<Double, Integer>> traceMaternalCoalescence(List<Sim> population) {
        return traceLineage(population, true);
    }

    /**
     * Fonction générique pour suivre une lignée.
     */
    private List<Map.Entry<Double, Integer>> traceLineage(List<Sim> population, boolean isMother) {
        List<Map.Entry<Double, Integer>> result = new ArrayList<>();
        MinHeap<Sim> heap = new MinHeap<>(Comparator.comparingDouble(Sim::getBirthTime).reversed());
        Set<Sim> seen = new HashSet<>();

        for (Sim sim : population) {
            if ((isMother && sim.getSex() == Sim.Sex.F) || (!isMother && sim.getSex() == Sim.Sex.M)) {
                heap.insert(sim);
                seen.add(sim);
            }
        }

        result.add(Map.entry(0.0, seen.size()));

        while (heap.size() > 1) {
            Sim current = heap.extractMin();
            Sim parent = isMother ? current.getMother() : current.getFather();

            if (parent != null) {
                if (!seen.contains(parent)) {
                    seen.add(parent);
                    heap.insert(parent);
                } else {
                    int newSize = seen.size() - 1;
                    result.add(Map.entry(current.getBirthTime(), newSize));
                    seen.remove(current);
                }
            }
        }

        return result;
    }

    /**
     * Retourne la liste des individus encore en vie.
     */
    public List<Sim> getLiving() {
        return new ArrayList<>(living);
    }
}
