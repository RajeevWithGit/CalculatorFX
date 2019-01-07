package calculatorfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CalculatorController implements Initializable {
// ------------------------------ FIELDS ------------------------------

    private static final String ALL_CLEAR = "AC";
    private static final String BACKSPACE = "Backspace";
    private static final String CLEAR = "C";
    private static final String DOT = ".";
    private static final BigDecimal MINUS_ONE = new BigDecimal("-1");
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final String ZERO = "0";

    @FXML
    Button clearButton;

    @FXML
    Label displayLabel;

    private Map<String, Operation> operationMap;
    private String operator;
    private BigDecimal value;
    private boolean waitingForOperand;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Initializable ---------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        operationMap = new HashMap<>();
        operationMap.put("+", BigDecimal::add);
        operationMap.put("-", BigDecimal::subtract);
        operationMap.put("*", this::multiply);
        operationMap.put("/", this::divide);
        operationMap.put("=", (previousValue, nextValue) -> nextValue);

        clearAll();
    }

// -------------------------- OTHER METHODS --------------------------

    @FXML
    public void onButtonPressed(ActionEvent event) {
        String value = ((Button) event.getSource()).getText();

        if ("×".equals(value)) {
            value = "*";
        } else if ("÷".equals(value)) {
            value = "/";
        } else if ("−".equals(value)) {
            value = "-";
        } else if ("●".equals(value)) {
            value = DOT;
        }
        onKeyPress(value);
    }

    public void onKeyPressed(KeyEvent event) {
        String value;
        if (KeyCode.ENTER.equals(event.getCode())) {
            value = "=";
        } else if (KeyCode.DELETE.equals(event.getCode())) {
            if (ALL_CLEAR.equals(clearButton.getText())) {
                value = ALL_CLEAR;
            } else {
                value = CLEAR;
            }
        } else if (KeyCode.BACK_SPACE.equals(event.getCode())) {
            value = BACKSPACE;
        } else {
            value = event.getText();
        }
        onKeyPress(value);
    }

    private void clearAll() {
        operator = null;
        value = null;
        waitingForOperand = false;
        clearDisplay();
    }

    private void clearDisplay() {
        setDisplayValue(ZERO);
        setClearValue(ALL_CLEAR);
    }

    private void clearLastChar() {
        String currentValue = getDisplayValue();
        if (currentValue.length() == 1) {
            clearDisplay();
        } else {
            setDisplayValue(currentValue.substring(0, currentValue.length() - 1));
        }
    }

    private BigDecimal divide(BigDecimal previousValue, BigDecimal nextValue) {
        return previousValue.divide(nextValue, 4, RoundingMode.HALF_UP);
    }

    private String getDisplayValue() {
        return displayLabel.getText();
    }

    private void inputDigit(String digit) {
        if (waitingForOperand) {
            waitingForOperand = false;
            setDisplayValue(digit);
            setClearValue(CLEAR);
        } else {
            if (ZERO.equals(getDisplayValue())) {
                setDisplayValue(digit);
                setClearValue(CLEAR);
            } else {
                setDisplayValue(getDisplayValue() + digit);
            }
        }
    }

    private void inputDot() {
        String value = getDisplayValue();
        if (!value.contains(DOT)) {
            setDisplayValue(getDisplayValue() + DOT);
            waitingForOperand = false;
        }
    }

    private void inputPercent() {
        String currentValue = getDisplayValue();
        if (ZERO.equals(currentValue)) {
            return;
        }

        BigDecimal newValue = multiply(value, divide(new BigDecimal(currentValue), ONE_HUNDRED));
        setDisplayValue(newValue.toPlainString());
        waitingForOperand = false;
    }

    private BigDecimal multiply(BigDecimal previousValue, BigDecimal nextValue) {
        return previousValue.multiply(nextValue).stripTrailingZeros();
    }

    private void onKeyPress(String value) {
        if (value.matches("\\d")) {
            inputDigit(value);
        } else if (DOT.equals(value)) {
            inputDot();
        } else if ("%".equals(value)) {
            inputPercent();
        } else if (value.matches("\\+|-|\\*|/|=")) {
            performOperation(value);
        } else if ("±".equals(value)) {
            toggleSign();
        } else if (ALL_CLEAR.equals(value)) {
            clearAll();
        } else if (CLEAR.equals(value)) {
            clearDisplay();
        } else if (BACKSPACE.equals(value)) {
            clearLastChar();
        }
    }

    private void performOperation(String nextOperator) {
        if (waitingForOperand) {
            return;
        }
        BigDecimal inputValue = new BigDecimal(getDisplayValue());
        if (value == null) {
            value = inputValue;
        } else if (operator != null) {
            BigDecimal currentValue = new BigDecimal(getDisplayValue());
            value = operationMap.get(operator).calculate(value, currentValue);
            setDisplayValue(value.toPlainString());
        }
        waitingForOperand = true;
        operator = nextOperator;
    }

    private void setClearValue(String value) {
        clearButton.setText(value);
    }

    private void setDisplayValue(String value) {
        displayLabel.setText(value);
    }

    private void toggleSign() {
        setDisplayValue(new BigDecimal(getDisplayValue()).multiply(MINUS_ONE).toPlainString());
    }

    interface Operation {
        BigDecimal calculate(BigDecimal previousValue, BigDecimal nextValue);
    }
}
