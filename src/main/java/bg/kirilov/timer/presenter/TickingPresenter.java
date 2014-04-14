package bg.kirilov.timer.presenter;

import bg.kirilov.timer.calculator.MoneyPerSecondCalculator;
import bg.kirilov.timer.ui.TickingView;
import bg.kirilov.timer.ui.TickingViewImpl;

import javax.swing.*;

/**
 * This handles the business logic and inner state of a TickingView
 *
 * @author Leni Kirilov
 * @date 4/14/2014
 */
public class TickingPresenter {

    private int numberPeople;
    private double payRate;
    private boolean clockTicking;
    private CalculatingPerSecondThread tickerThread;

    private TickingView view;

    public TickingPresenter(TickingView view) {
        this.view = view;
    }

    public boolean isClockTicking() {
        return clockTicking;
    }

    private void startClock() {
        clockTicking = true;
        MoneyPerSecondCalculator calculator = new MoneyPerSecondCalculator(numberPeople, payRate);
        tickerThread = new CalculatingPerSecondThread(
                (CalculatingPerSecondView) view,
                Formaters.getNumberFormatter(),
                calculator);
        tickerThread.start();

        //update view
        view.startClock();
    }

    private void stopClock() {
        clockTicking = false;
        tickerThread.stopThread();

        //updating view
        view.stopClock();
    }

    private void resumeClock() {
        clockTicking = true;
        tickerThread.resumeClock();

        //updating view
        view.resumeClock();
    }

    private void resumeIfPaused() {
        if (tickerThread.isPaused()) {
            tickerThread.dieAtResume();
            resumeClock();
        }
    }

    private void pauseClock() {
        tickerThread.setPaused(true);
        view.pauseClock();
    }

    /**
     * Resets the internal state of both the presenter and the view <br>
     * Usually used after stopClock().
     */

    private void resetClock() {
        tickerThread = null;
        numberPeople = 0;
        payRate = 0.0;

        //update view
        view.resetClock();
    }

    /**
     * Gathers and formats information regarding the current session.<br>
     * Information regarding the number of participants; pay rates; <br>
     * time for the session and total sum is displayed.<br>
     * <p/>
     * The results are displayed as a message dialog.
     *
     * @param tickingView
     */
    //TODO extract result page into another ReportView class
    private void showResultsPage(TickingView tickingView) {
        StringBuilder report = new StringBuilder("Result of session:\n");
        report.append("--------\n")
                .append("Number of participants: ").append(numberPeople).append("\n")
                .append("Pay rate per hour: ").append(payRate).append("\n")
                .append("--------\n")
                .append("Total time (HH:MM:ss) : ").append(tickerThread.getCurrentTimeFormatted()).append("\n")
                .append("Total cost: ").append(tickerThread.getFinalAmount());

        tickingView.showReport(report.toString());
    }

    /**
     * The actions that are done when the "PAUSE"/"RESUME" button is clicked.<br>
     * They depend on the current state of the clock - RUNNING/PAUSED/STOPPED.
     */
    public void pauseButtonActionPerformed() {
        if (tickerThread.isPaused()) {
            resumeClock();
        } else {
            pauseClock();
        }
    }

    /**
     * The actions that are done when the "START/"STOP" button is clicked.<br>
     * They depend on the current state of the clock - PAUSED/RUNNING/STOPPED.
     *
     * @param numberParticipantsRaw
     * @param payRateRaw
     */
    //TODO most tricky method here! carefully should be refactored and being dependant on fewer booleans
    public void startButtonActionPerformed(String numberParticipantsRaw, String payRateRaw) {//GEN-FIRST:event_startButtonActionPerformed
        if (isClockTicking()) { //if true, the button says STOP
            if (view.askIfWantToAbort()) {
                resumeIfPaused();
                view.setInput(true);
                stopClock();
                if (view.askIfWantReport()) {
                    showResultsPage(view);
                }
                resetClock();
            }
        } else { //if false, the button says START
            boolean isInputValid = validateInput(numberParticipantsRaw, payRateRaw);

            if (isInputValid) {
                view.setInput(false);
                startClock();
            }
        }
    }//GEN-LAST:event_startButtonActionPerformed

    /**
     * Performs various checks on the input. Checks for integer and floating<br>
     * point numbers and to be positive.<br>
     * Checks input, creates MessageDialog to inform when the input is incorrect.<br>
     * If correct input is entered the corresponding labels will be updated.<br>
     *
     * @return - true if the input is correct
     */
    //TODO extract input parsing to another class?
    //TODO single purpose method principle is broken here! - this checks AND shows warning AND updates view! Split to several methods
    private boolean validateInput(String peopleCountRaw, String payRateRaw) {
        boolean correctInput = false;
        try {
            numberPeople = Integer.parseInt(peopleCountRaw);
            payRate = Double.parseDouble(payRateRaw);

            if (payRate <= 0 || numberPeople <= 0) {
                //TODO do not use exceptions for program flow!
                throw new Exception("Positive numbers are required!");
            }

            correctInput = true;
        } catch (Exception exc) {
            //TODO the view should define this dialog
            JOptionPane.showMessageDialog((TickingViewImpl) view,
                    "Incorrect input! Required integer for number of people and float point number for rate.",
                    exc.getMessage(),
                    JOptionPane.WARNING_MESSAGE);
            numberPeople = 0;
            payRate = 0.0d;
        }

        view.setNumberPeople(numberPeople);
        view.setPayRate(payRate);
        return correctInput;
    }
}
