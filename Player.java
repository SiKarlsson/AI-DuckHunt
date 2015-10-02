import java.util.*;
class Player {

    HashMap<Integer, LinkedList<HMM>> speciesHMM; // bucket of hmms for each species
    HashMap<Integer, HMM> roundHMMs; // HMMs generated in a single round

    public Player() {
        speciesHMM = new HashMap<Integer, LinkedList<HMM>>();
        for (int i = 0; i < Constants.COUNT_SPECIES; i++) {
            speciesHMM.put(i, new LinkedList<HMM>());
        }
    }

    /**
     * Shoot!
     *
     * This is the function where you start your work.
     *
     * You will receive a variable pState, which contains information about all
     * birds, both dead and alive. Each bird contains all past moves.
     *
     * The state also contains the scores for all players and the number of
     * time steps elapsed since the last time this function was called.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
     */
    public Action shoot(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to get the best action.
         * This skeleton never shoots.
         */
        int numOfBirds = pState.getNumBirds();
        int numOfStates = 10;
        int t = 100 - numOfBirds; // When to start shooting

        int bestBird = 0; // What bird should we shoot?
        double bestBirdProb = 0; // ... and what prob that we hit?
        int bestAction = -1; // Don't shoot if no good action has been found

        roundHMMs = new HashMap<Integer, HMM>();

        for (int b = 0; b < numOfBirds; b++) {
            Bird bird = pState.getBird(b);
            if (bird.getSeqLength() > t && bird.isAlive()) {

                HMM hmm = new HMM(numOfStates, Constants.COUNT_MOVE);
                // Observations
                int[] o = getObservationSequence(bird);

                hmm.estimateModel(o);

                roundHMMs.put(b, hmm);

                int[] stateSeq = hmm.estimateStateSequence(o);

                int lastState = stateSeq[stateSeq.length - 1];

                double[] stateDist = hmm.A[lastState];

                double[] nextEmission = hmm.estimateProbabilityDistributionOfNextEmission(stateDist);

                for (int i = 0; i < Constants.COUNT_MOVE; i++) {
                    if (nextEmission[i] > bestBirdProb) {
                        bestBirdProb = nextEmission[i];
                        bestAction = i;
                        bestBird = b;
                    }
                }
            }
        }

        LinkedList<HMM> storkHMMList = speciesHMM.get(Constants.SPECIES_BLACK_STORK);

        for (HMM hmm : storkHMMList) {
            double prob = hmm.estimateProbabilityOfEmissionSequence(getObservationSequence(pState.getBird(bestBird)));
            if (prob > 0.7) {
                System.err.printf("Black stork!");
                return cDontShoot;
            }
        }

        if (bestBirdProb > 0.6) {
            System.err.printf("Shooting bird (Action: %d) %d with prob %.5f\n", bestAction, bestBird, bestBirdProb);
            return new Action(bestBird, bestAction);
        } else {
            return cDontShoot;
        }
    }

    /**
     * Guess the species!
     * This function will be called at the end of each round, to give you
     * a chance to identify the species of the birds for extra points.
     *
     * Fill the vector with guesses for the all birds.
     * Use SPECIES_UNKNOWN to avoid guessing.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return a vector with guesses for all the birds
     */
    public int[] guess(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to guess the species of
         * each bird. This skeleton makes no guesses, better safe than sorry!
         */

        Random rand = new Random();

        int[] lGuess = new int[pState.getNumBirds()];
        for (int i = 0; i < pState.getNumBirds(); ++i) {
            Bird bird = pState.getBird(i);
            if (pState.getRound() == 0) {
                lGuess[i] = rand.nextInt(Constants.COUNT_SPECIES - 1);
            } else {
                int[] o = getObservationSequence(bird);
                double bestProb = -Integer.MAX_VALUE;
                int species = -1;
                for (int j = 0; j < Constants.COUNT_SPECIES; j++) {
                    // Get all HMMs we have for species j
                    LinkedList<HMM> hmmList = speciesHMM.get(j);
                    // Iterate through all 
                    for (HMM hmm : hmmList) {
                        if (hmm != null) {
                            double prob = Math.abs(Math.log(hmm.estimateProbabilityOfEmissionSequence(o)));
                            if (prob < Integer.MAX_VALUE && prob > bestProb) {
                                //System.err.printf("Found a better guess for %d: %d\n", i, j);
                                bestProb = prob;
                                species = j;
                            }
                        }
                    }
                }
                lGuess[i] = species;
            }
        }

        return lGuess;
    }

    /**
     * If you hit the bird you were trying to shoot, you will be notified
     * through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pBird the bird you hit
     * @param pDue time before which we must have returned
     */
    public void hit(GameState pState, int pBird, Deadline pDue) {
        System.err.println("HIT BIRD!!!");
    }

    /**
     * If you made any guesses, you will find out the true species of those
     * birds through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pSpecies the vector with species
     * @param pDue time before which we must have returned
     */
    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {
        for (int i = 0; i < pSpecies.length; i++) {
            Bird bird = pState.getBird(i);
            if (speciesHMM.get(pSpecies[i]) != null) {
                speciesHMM.get(pSpecies[i]).add(roundHMMs.get(i));
            }
            /*int[] o = getObservationSequence(bird);
            for (int j = 0; j < o.length; j++) {
                speciesObservations.get(pSpecies[i]).add(o[j]);
            }
            HMM hmm = speciesHMM.get(pSpecies[i]);
            int[] allObs = new int[speciesObservations.size()];
            for (int j = 0; j < speciesObservations.size(); j++) {
                allObs[j] = speciesObservations.get(pSpecies[i]).get(j);
            }
            hmm.estimateModel(allObs);*/
        }
    }

    int[] getObservationSequence(Bird bird) {
        int[] o = new int[bird.getSeqLength()];
        for (int i = 0; i < bird.getSeqLength(); i++) {
            if (bird.wasAlive(i)) o[i] = bird.getObservation(i);
        }

        return o;
    }

    public static final Action cDontShoot = new Action(-1, -1);
}
