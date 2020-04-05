package com.company;

import com.company.util.SwingConsole;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculatorGui extends JFrame {
    private static final String EXPRESSION_PATTERN = "[0-9[+*()-/.]E]+|\\d";

    private JPanel mainPanel = new JPanel();
    private JTextField display = new JTextField(15);
    private JButton button1 = new JButton(" 1 ");
    private JButton button2 = new JButton(" 2 ");
    private JButton button3 = new JButton(" 3 ");
    private JButton button4 = new JButton("4");
    private JButton button5 = new JButton("5");
    private JButton button6 = new JButton("6");
    private JButton button7 = new JButton("7");
    private JButton button8 = new JButton("8");
    private JButton button9 = new JButton("9");
    private JButton button0 = new JButton("0");
    private JButton buttonPlus = new JButton("+");
    private JButton buttonMinus = new JButton("-");
    private JButton buttonMult = new JButton("*");
    private JButton buttonDivide = new JButton("/");
    private JButton buttonDot = new JButton(".");
    private JButton buttonCompute = new JButton("=");
    private JButton buttonClear = new JButton("c");
    private JButton buttonBracketOpen = new JButton("(");
    private JButton buttonBracketClose = new JButton(")");
    private JButton buttonRedo = new JButton("Redo");
    private JButton buttonUndo = new JButton("Undo");
    private JButton[] buttons = {button1, button2, button3, button4,
            button5, button6, button7, button8, button9, button0, buttonPlus,
            buttonMult, buttonDivide, buttonMinus, buttonCompute,
            buttonBracketOpen, buttonBracketClose, buttonDot, buttonClear};

    private History hist = new History();
    private MathExpressionComputer comp =
            new MathExpressionComputer(EXPRESSION_PATTERN);
    private Color azure = new Color(179, 204, 255);

    {
        Font fontS = new Font("Arial", Font.PLAIN, 23);
        Font fontB = new Font("Arial", Font.PLAIN, 40);
        Font fontUR = new Font("Arial", Font.PLAIN, 14);
        mainPanel.setBackground(azure);
        display.setFont(fontS);
        display.setBorder(new LineBorder(azure, 6));
        for (JButton button : buttons) {
            button.setFont(fontB);
            button.setBorder(new LineBorder(azure, 10));
        }
        buttonClear.setForeground(Color.RED);
        buttonClear.addActionListener(new ClearButtonAction());
        buttonCompute.addActionListener(new ComputeButtonAction());
        buttonUndo.setBorder(new LineBorder(azure, 6));
        buttonUndo.addActionListener(new UndoButtonAction());
        buttonUndo.setFont(fontUR);
        buttonRedo.setBorder(new LineBorder(azure, 6));
        buttonRedo.addActionListener(new RedoButtonAction());
        buttonRedo.setFont(fontUR);
    }

    private class MathExpressionComputer {
        private ScriptEngine engine;
        private Pattern pattern;

        public MathExpressionComputer(String patternStr) {
            ScriptEngineManager mng = new ScriptEngineManager();
            this.engine = mng.getEngineByName("JavaScript");
            this.pattern = Pattern.compile(patternStr);
        }

        private double mathExpressionCompute(String exp) throws ScriptException {
            if (!isMathExpression(exp)) {
                throw new ScriptException("ILLEGAL SYMBOLS");
            }

            double result;
            try {
                result = Double.valueOf(engine.eval(exp).toString());
            } catch (ScriptException e) {
                throw new ScriptException("ILLEGAL FORMAT");
            }

            return result;
        }

        private boolean isMathExpression(String exp) {
            Matcher matcher = pattern.matcher(exp);
            return matcher.matches();
        }
    }

    private class History {
        private ArrayList<String> history = new ArrayList<>();
        private int listCursor = -1;

        private void addRecord(String rec) {
            if (!history.isEmpty() && rec.equals(history.get(history.size() - 1))) {
                return;
            }
            if (listCursor < history.size() - 1) {
                history.subList(listCursor, history.size()).clear();
            }
            listCursor = history.size() - 1;
            history.add(rec);
            listCursor++;
        }

        private String undo() {
            if (listCursor < 1) {
                return null;
            }
            listCursor--;
            return history.get(listCursor);
        }

        private String redo() {
            if (listCursor >= history.size() - 1) {
                return null;
            }
            listCursor++;
            return history.get(listCursor);
        }
    }

    private class MathButtonAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            display.replaceSelection(e.getActionCommand().trim());
            display.requestFocusInWindow();
        }
    }

    private class ClearButtonAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            hist.addRecord(display.getText());
            display.setText("");
            display.requestFocusInWindow();
        }
    }

    private class ComputeButtonAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            String expression = display.getText();
            hist.addRecord(expression);
            String result;
            try {
                result = String.valueOf(comp.mathExpressionCompute(expression));
            } catch (ScriptException except) {
                result = except.getMessage();
            }

            display.setText(result);
            hist.addRecord(result);
        }
    }

    private class UndoButtonAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            String rec = hist.undo();
            if (rec != null) {
                display.setText(rec);
            }
        }
    }

    private class RedoButtonAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            String rec = hist.redo();
            if (rec != null) {
                display.setText(rec);
            }
        }
    }

    public CalculatorGui() {
        setResizable(false);

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new FlowLayout());
        displayPanel.setBackground(Color.BLACK);
        display.setHorizontalAlignment(4);
        displayPanel.add(display);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(5, 4));
        JPanel unReDo = new JPanel();
        unReDo.setBackground(azure);
        unReDo.setLayout(new GridLayout(2, 1));
        unReDo.add(buttonUndo);
        unReDo.add(buttonRedo);
        buttonsPanel.add(unReDo);
        for (JButton button : buttons) {
            if (!(button.getText().equals("=") ||
                    button.getText().equals("c")))
                button.addActionListener(new MathButtonAction());

            buttonsPanel.add(button);
        }

        mainPanel.setLayout(new FlowLayout());
        mainPanel.add(displayPanel);
        mainPanel.add(buttonsPanel);
        add(mainPanel);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingConsole.run(new CalculatorGui(), 330, 435);
    }
}
