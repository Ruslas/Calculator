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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculatorGui extends JFrame {
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

        private void printState() {
            System.out.println(history);
            System.out.println("listCursor = " + listCursor);
            System.out.println("history.size() = " + history.size());
        }
    }

    private History hist = new History();


    {
        Font fontS = new Font("Arial", Font.PLAIN, 35);
        Font fontB = new Font("Arial", Font.PLAIN, 60);
        display.setFont(fontS);
        for (JButton button : buttons) {
            button.setFont(fontB);
            button.setBorder(new LineBorder(new Color(179, 204, 255), 10));
        }
        buttonClear.setForeground(Color.RED);
        buttonClear.addActionListener(new ClearButtonAction());
        buttonCompute.addActionListener(new ComputeButtonAction());
        buttonUndo.setBorder(new LineBorder(new Color(179, 204, 255), 6));
        buttonUndo.addActionListener(new UndoButtonAction());
        buttonRedo.setBorder(new LineBorder(new Color(179, 204, 255), 6));
        buttonRedo.addActionListener(new RedoButtonAction());
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
            hist.printState();
            display.setText("");
            display.requestFocusInWindow();
        }
    }

    private class ComputeButtonAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            String expression = display.getText();
            hist.addRecord(expression);
            hist.printState();
            String result;
            try {
                result = String.valueOf(mathExpressionCompute(expression));
            } catch (ScriptException except) {
                result = except.getMessage();
            }

            display.setText(result);
            hist.addRecord(result);
            hist.printState();
        }
    }

    private class UndoButtonAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            String rec = hist.undo();
            hist.printState();
            if (rec != null) {
                display.setText(rec);
            }
        }
    }

    private class RedoButtonAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            String rec = hist.redo();
            hist.printState();
            if (rec != null) {
                display.setText(rec);
            }
        }
    }

    public CalculatorGui() {
        setResizable(false);

        JPanel screenPanel = new JPanel();
        screenPanel.setLayout(new FlowLayout());
        display.setHorizontalAlignment(4);
        screenPanel.add(display);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(5, 4));
        JPanel unReDo = new JPanel();
        unReDo.setBackground(new Color(179, 204, 255));
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
        mainPanel.add(screenPanel);
        mainPanel.add(buttonsPanel);
        add(mainPanel);
    }

    private static double mathExpressionCompute(String exp) throws ScriptException {
        if (!isMathExpression(exp)) {
            throw new ScriptException("ILLEGAL SYMBOLS");
        }
        ScriptEngineManager mng = new ScriptEngineManager();
        ScriptEngine engine = mng.getEngineByName("JavaScript");

        double result;
        try {
            result = Double.valueOf(engine.eval(exp).toString());
        } catch (ScriptException e) {
            throw new ScriptException("ILLEGAL FORMAT");
        }

        return result;
    }

    private static boolean isMathExpression(String exp) {
        String patternStr = "[0-9[+*()-/.]]+|\\d";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(exp);
        return matcher.matches();
    }

    public static void main(String[] args) {
        SwingConsole.run(new CalculatorGui(), 500, 570);
    }
}
