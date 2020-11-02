package com.ae2dms.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for score page
 */
public class ScorePageController {

    @FXML
    private Label moveScore3;

    @FXML
    private Label timeScore3;

    @FXML
    private Label moveScore1;

    @FXML
    private Label timeScore2;

    @FXML
    private Label timeScore1;

    @FXML
    private Label moveScore2;

    /**
     * Show score list
     * @param listTimeScore
     * @param listMoveScore
     */
    public void initScorePage(List<Double> listTimeScore, List<Integer> listMoveScore) {

        List<Label> listTimeLabel = new ArrayList<Label>();
        List<Label> listMoveLabel = new ArrayList<Label>();
        listTimeLabel.add(timeScore1);
        listTimeLabel.add(timeScore2);
        listTimeLabel.add(timeScore3);
        listMoveLabel.add(moveScore1);
        listMoveLabel.add(moveScore2);
        listMoveLabel.add(moveScore3);

        for ( int i = 0; i < listTimeScore.size();i++) {
            if ( i < 3) {
                listTimeLabel.get(i).setText(listTimeScore.get(i).toString());
                listMoveLabel.get(i).setText(listMoveScore.get(i).toString());
            }

        }
    }
}
