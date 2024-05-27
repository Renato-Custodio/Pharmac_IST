package pt.ulisboa.tecnico.cmov.pharmacist.ui.seed;

import java.util.Map;

public class ReviewsList {
    private Map<String, Map<String, Integer>> scores;

    public Map<String, Map<String, Integer>> getScores() {
        return scores;
    }

    public void setScores(Map<String, Map<String, Integer>> scores) {
        this.scores = scores;
    }
}
