import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Core calculator functions
class ScientificCalculator {
    public double add(double a, double b) { return a + b; }
    public double subtract(double a, double b) { return a - b; }
    public double multiply(double a, double b) { return a * b; }

    public double divide(double a, double b) {
        if (b == 0) throw new ArithmeticException("Cannot divide by zero");
        return a / b;
    }

    public double power(double a, double b) { return Math.pow(a, b); }
    public double sqrt(double x) {
        if (x < 0) throw new ArithmeticException("Square root of negative number");
        return Math.sqrt(x);
    }

    public double sin(double x) { return Math.sin(Math.toRadians(x)); }
    public double cos(double x) { return Math.cos(Math.toRadians(x)); }
    public double tan(double x) { return Math.tan(Math.toRadians(x)); }

    public double log(double x) {
        if (x <= 0) throw new ArithmeticException("Logarithm undefined for <= 0");
        return Math.log10(x);
    }

    public double ln(double x) {
        if (x <= 0) throw new ArithmeticException("Natural log undefined for <= 0");
        return Math.log(x);
    }
}

// Calculator logic and state management
class CalculatorEngine {
    private StringBuilder currentInput;
    private double firstOperand;
    private String pendingOperator;
    private boolean waitingForSecondOperand;

    private final ScientificCalculator calculator = new ScientificCalculator();
    private final DecimalFormat df = new DecimalFormat("#.##########");

    public CalculatorEngine() { reset(); }

    public void reset() {
        currentInput = new StringBuilder("0");
        firstOperand = 0;
        pendingOperator = "";
        waitingForSecondOperand = false;
    }

    public void clearEntry() { currentInput = new StringBuilder("0"); }

    public String getCurrentDisplayValue() {
        return currentInput.toString();
    }

    public String getHistoryDisplayValue() {
        return pendingOperator.isEmpty() ? "" : df.format(firstOperand) + " " + pendingOperator;
    }

    public void inputDigit(String digit) {
        if (waitingForSecondOperand) {
            currentInput = new StringBuilder(digit);
            waitingForSecondOperand = false;
        } else {
            if (currentInput.toString().equals("0")) {
                currentInput = new StringBuilder(digit);
            } else {
                currentInput.append(digit);
            }
        }
    }

    public void inputDecimal() {
        if (!currentInput.toString().contains(".")) {
            currentInput.append(".");
        }
        waitingForSecondOperand = false;
    }

    public void inputBackspace() {
        if (currentInput.length() > 1) {
            currentInput.deleteCharAt(currentInput.length() - 1);
        } else {
            currentInput = new StringBuilder("0");
        }
    }

    public void setOperator(String operator) {
        if (!pendingOperator.isEmpty() && !waitingForSecondOperand) {
            calculate();
        }
        firstOperand = Double.parseDouble(currentInput.toString());
        pendingOperator = operator;
        waitingForSecondOperand = true;
    }

    public void applyUnaryFunction(String functionName) {
        double value = Double.parseDouble(currentInput.toString());
        double result = switch (functionName) {
            case "√" -> calculator.sqrt(value);
            case "sin" -> calculator.sin(value);
            case "cos" -> calculator.cos(value);
            case "tan" -> calculator.tan(value);
            case "log" -> calculator.log(value);
            case "ln" -> calculator.ln(value);
            case "±" -> value * -1;
            case "%" -> value / 100.0;
            default -> value;
        };
        currentInput = new StringBuilder(df.format(result));
        waitingForSecondOperand = true;
    }

    public void calculate() {
        if (pendingOperator.isEmpty() || waitingForSecondOperand) return;

        double secondOperand = Double.parseDouble(currentInput.toString());
        double result;

        result = switch (pendingOperator) {
            case "+" -> calculator.add(firstOperand, secondOperand);
            case "-" -> calculator.subtract(firstOperand, secondOperand);
            case "×" -> calculator.multiply(firstOperand, secondOperand);
            case "÷" -> calculator.divide(firstOperand, secondOperand);
            case "^" -> calculator.power(firstOperand, secondOperand);
            default -> throw new IllegalStateException("Unknown operator: " + pendingOperator);
        };

        currentInput = new StringBuilder(df.format(result));
        pendingOperator = "";
        waitingForSecondOperand = true;
    }
}

// Swing GUI
public class ScientificCalculatorApp extends JFrame {
    private final JTextField displayField;
    private final JLabel historyLabel;
    private final CalculatorEngine engine;

    private static final Color BG_COLOR = new Color(45, 52, 54);
    private static final Color DISPLAY_BG_COLOR = new Color(33, 33, 33);
    private static final Color BTN_TEXT_COLOR = Color.WHITE;
    private static final Color NUM_BTN_COLOR = new Color(178, 190, 195);
    private static final Color OP_BTN_COLOR = new Color(255, 118, 117);
    private static final Color FUNC_BTN_COLOR = new Color(9, 132, 227);
    private static final Color CTRL_BTN_COLOR = new Color(108, 92, 231);
    private static final Font DISPLAY_FONT = new Font("Consolas", Font.BOLD, 36);
    private static final Font HISTORY_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 20);

    public ScientificCalculatorApp() {
        engine = new CalculatorEngine();
        setTitle("Scientific Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        // Display Panel
        JPanel displayPanel = new JPanel(new BorderLayout(5, 5));
        displayPanel.setBackground(DISPLAY_BG_COLOR);
        displayPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        historyLabel = new JLabel(" ");
        historyLabel.setFont(HISTORY_FONT);
        historyLabel.setForeground(Color.LIGHT_GRAY);
        historyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        displayPanel.add(historyLabel, BorderLayout.NORTH);

        displayField = new JTextField("0");
        displayField.setFont(DISPLAY_FONT);
        displayField.setForeground(Color.WHITE);
        displayField.setBackground(DISPLAY_BG_COLOR);
        displayField.setHorizontalAlignment(SwingConstants.RIGHT);
        displayField.setEditable(false);
        displayField.setBorder(null);
        displayPanel.add(displayField, BorderLayout.CENTER);
        add(displayPanel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        buttonsPanel.setBackground(BG_COLOR);
        buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1;
        gbc.weighty = 1;

        String[][] buttons = {
            {"sin", "cos", "tan", "AC", "⌫"},
            {"log", "ln", "^", "√", "÷"},
            {"7", "8", "9", "±", "×"},
            {"4", "5", "6", "%", "-"},
            {"1", "2", "3", "=", "+"},
            {"0", ".", "", "", ""}
        };

        // Special handling to make "=" button span 3 columns
        gbc.gridheight = 1;
        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                String label = buttons[row][col];
                if (!label.isEmpty()) {
                    gbc.gridx = col;
                    gbc.gridy = row;
                    gbc.gridwidth = 1; // Reset gridwidth
                    if (label.equals("=")) {
                        gbc.gridx = 3;
                        gbc.gridy = 4;
                        gbc.gridheight = 2;
                    } else {
                        gbc.gridheight = 1;
                    }
                     if (label.equals("0")) {
                        gbc.gridwidth = 2;
                    }
                    JButton btn = createStyledButton(label);
                    buttonsPanel.add(btn, gbc);
                    // Reset gridwidth if it was changed
                    if (gbc.gridwidth > 1 || gbc.gridheight > 1) {
                         gbc.gridwidth = 1;
                         gbc.gridheight = 1;
                    }
                }
            }
        }

        add(buttonsPanel, BorderLayout.CENTER);

        // Keyboard Support
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { handleKeyPress(e); }
        });
        setFocusable(true);
        pack();
        setSize(460, 650);
        setLocationRelativeTo(null);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setForeground(BTN_TEXT_COLOR);

        if (text.matches("[0-9]")) {
            button.setBackground(NUM_BTN_COLOR);
            button.setForeground(Color.BLACK);
        } else if ("+-×÷^".contains(text)) {
            button.setBackground(OP_BTN_COLOR);
        } else if ("=".equals(text)) {
            button.setBackground(new Color(253, 151, 114)); // Brighter color for equals
        } else if ("AC⌫".contains(text)) {
            button.setBackground(CTRL_BTN_COLOR);
        } else {
            button.setBackground(FUNC_BTN_COLOR);
        }

        button.addActionListener(e -> handleButtonClick(text));
        return button;
    }

    private void handleButtonClick(String cmd) {
        try {
            if (cmd.matches("[0-9]")) engine.inputDigit(cmd);
            else switch (cmd) {
                case "." -> engine.inputDecimal();
                case "AC" -> engine.reset();
                case "⌫" -> engine.inputBackspace();
                case "+", "-", "×", "÷", "^" -> engine.setOperator(cmd);
                case "=" -> engine.calculate();
                case "√", "sin", "cos", "tan", "log", "ln", "±", "%" -> engine.applyUnaryFunction(cmd);
            }
            
            updateDisplay();
        } catch (ArithmeticException ex) {
            displayField.setText("Error");
            historyLabel.setText(ex.getMessage());
            engine.reset();
        } catch (Exception ex) {
            displayField.setText("Error");
            historyLabel.setText("Invalid operation");
            engine.reset();
        }
    }

    private void handleKeyPress(KeyEvent e) {
        char c = e.getKeyChar();
        int k = e.getKeyCode();
        try {
            if (Character.isDigit(c)) engine.inputDigit(String.valueOf(c));
            else switch (c) {
                case '.' -> engine.inputDecimal();
                case '+' -> engine.setOperator("+");
                case '-' -> engine.setOperator("-");
                case '*' -> engine.setOperator("×");
                case '/' -> engine.setOperator("÷");
                case '^' -> engine.setOperator("^");
                case '%' -> engine.applyUnaryFunction("%");
            }
            switch (k) {
                case KeyEvent.VK_ENTER, KeyEvent.VK_EQUALS -> engine.calculate();
                case KeyEvent.VK_BACK_SPACE -> engine.inputBackspace();
                case KeyEvent.VK_ESCAPE -> engine.reset();
            }
            // **FIX APPLIED HERE**: Only update display on success
            updateDisplay();
        } catch (Exception ex) {
            displayField.setText("Error");
            historyLabel.setText("Invalid operation");
            engine.reset();
        }
    }

    private void updateDisplay() {
        if (!displayField.getText().startsWith("Error")) {
            displayField.setText(engine.getCurrentDisplayValue());
        }
        historyLabel.setText(engine.getHistoryDisplayValue());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new ScientificCalculatorApp().setVisible(true));
    }
}
