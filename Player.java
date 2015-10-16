import java.util.*;
class Player {

    LinkedList<HMM> speciesHMM; // bucket of hmms 

    private static final int FLATLANDS_MOVES = Constants.COUNT_MOVE;

    public Player() {
        speciesHMM = new LinkedList<HMM>();
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
        int numOfStates = 5;
        int numOfEmissions = Constants.COUNT_MOVE;
        int numOfBirds = pState.getNumBirds();
        int t = 60; // When to start check the observations

        int bestBird = 0; // What bird should we shoot?
        double bestBirdProb = 0; // ... and what prob that we hit?
        int bestAction = -1; // Don't shoot if no good action has been found
        HMM bestHMM = new HMM(numOfStates, Constants.COUNT_MOVE);

        for (int b = 0; b < numOfBirds; b++) {
            Bird bird = pState.getBird(b);
            if (bird.isAlive() && bird.getSeqLength() > t) {

                HMM hmm = new HMM(numOfStates, numOfEmissions);

                if (speciesHMM.size() > 0) {
                    if (speciesHMM.getLast() != null) {
                        hmm.copyHMM(speciesHMM.getLast());
                    } else {
                        System.err.printf("null detected: size: %d, bird: %d\n", speciesHMM.size(), b);
                    }
                }

                // Observations
                int[] o = getObservationSequence(bird);

                hmm.estimateModel(o);

                if (bestHMM == null) {
                    bestHMM = hmm;
                }

                double[] stateDist = hmm.getCurrentStateDistribution(o);

                double[] nextEmission = hmm.estimateProbabilityDistributionOfNextEmission(stateDist);

                for (int i = Constants.MOVE_LEFT; i <= Constants.MOVE_RIGHT; i++) {
                    if (nextEmission[i] > bestBirdProb) {
                        bestBirdProb = nextEmission[i];
                        bestAction = i;
                        bestBird = b;
                    }
                }
            }
        }

        speciesHMM.add(bestHMM);

        double shootProb = 0.7;

        if (bestBirdProb > shootProb && pState.getBird(bestBird).getSeqLength() > t) {
            System.err.printf("Shooting bird (Action: %d) %d with prob %.5f at time %d\n", 
                bestAction, bestBird, bestBirdProb, t); 
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
        for (int i = 0; i < lGuess.length; i++) {
            lGuess[i] = Constants.SPECIES_UNKNOWN;
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
