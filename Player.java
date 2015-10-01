
class Player {

    public Player() {
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
        int t = 90; // When to start shooting

        int bestBird = 0; // What bird should we shoot?
        double bestBirdProb = 0; // ... and what prob that we hit?
        int bestAction = -1; // 

        for (int b = 0; b < numOfBirds; b++) {
            Bird currBird = pState.getBird(b);
            if (currBird.getSeqLength() > t && !currBird.isDead()) {

                HMM hmm = new HMM(numOfStates, Constants.COUNT_MOVE);

                int[] o = new int[currBird.getSeqLength()];

                for (int i = 0; i < currBird.getSeqLength(); i++) {
                    o[i] = currBird.getObservation(i);
                }

                hmm.estimateModel(o);

                double[] stateDist = hmm.getCurrentStateDistribution(t);

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
        // This line chooses not to shoot.
        //return cDontShoot;
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

        int[] lGuess = new int[pState.getNumBirds()];
        for (int i = 0; i < pState.getNumBirds(); ++i)
            lGuess[i] = Constants.SPECIES_UNKNOWN;
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

    public static final Action cDontShoot = new Action(-1, -1);
}
