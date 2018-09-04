package com.jforexcn.inbox.indicator;

/**
 * Created by simple(simple.continue@gmail.com) on 09/04/2018.
 */


import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import com.dukascopy.api.*;
import java.awt.BorderLayout;

public class SwingTest implements IStrategy {
    private IUserInterface userInterface;
    private JPanel myTab;
    private MySpecialPanel myPanel;
    private int counter = 0;
    private String key = "EUR/USD PriceCount";
    public void onStart(IContext context) throws JFException {
        this.userInterface = context.getUserInterface();

        myTab = userInterface.getMainTab(key);
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                myPanel = new MySpecialPanel();
                myTab.add(myPanel);
            }
        });
    }

    public void onAccount(IAccount account) throws JFException {
    }

    public void onMessage(IMessage message) throws JFException {
    }

    public void onStop() throws JFException {
        userInterface.removeMainTab(key);
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
        if (myPanel != null) {
            myPanel.updateLabel(getLabel(counter++));
            if (counter > 6) counter = 0;
        }
    }

    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
    }

    private String getLabel(int counter){
        StringBuffer newLabel = new StringBuffer();
        for (int i=1; i<=13; i++){
            if (counter != i)
                newLabel.append("1");
            else
                newLabel.append(i);
        }
        return newLabel.toString();
    }

    class MySpecialPanel extends JPanel{
        JLabel label;

        MySpecialPanel() {
            super(new BorderLayout());
//            this.label = new JLabel();
//            this.label.setBounds(new Rectangle(100, 100, 100, 100));
//            this.add(label);




            //创建表头
            String[] columnNames = { "First Name", "Last Name", "Sport",
                    "# of Years", "Vegetarian" };

            //创建显示数据
            Object[][] data = {
                    { "Kathy", "Smith", "Snowboarding", new Integer(5),
                            new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },
                    { "Kathy", "Smith", "Snowboarding", new Integer(5),
                            new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },{ "Kathy", "Smith", "Snowboarding", new Integer(5),
                    new Boolean(false) },
                    { "John", "Doe", "Rowing", new Integer(3), new Boolean(true) },
                    { "Sue", "Black", "Knitting", new Integer(2),
                            new Boolean(false) },
                    { "Jane", "White", "Speed reading", new Integer(20),
                            new Boolean(true) },
                    { "Joe", "Brown", "Pool", new Integer(10), new Boolean(false) }
            };

 /*
  * JTable还提供了一个重载的构造方法,传入两个Vector
  * JTable(Vector rowData, Vector columnNames)
  *
  */

            final JTable table = new JTable(data, columnNames);

            table.setBackground(Color.YELLOW);

            //table.setPreferredScrollableViewportSize(new Dimension(500, 0));

            // Create the scroll pane and add the table to it.
            //这也是官方建议使用的方式，否则表头不会显示，需要单独获取到TableHeader自己手动地添加显示
            JScrollPane scrollPane = new JScrollPane(table);

            add(scrollPane);


            JPanel panel2 = new JPanel();
            this.add(panel2,BorderLayout.SOUTH);
            JButton btn1 = new JButton("表格填充整个视图");
            JButton btn2 = new JButton("表格不添加整个视图(默认不填充)");
            panel2.add(btn1);
            panel2.add(btn2);

            btn1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //设置表格填充整个视图,在默认情况下,如果表格的大小小于视图(窗体),你会发现下面的内容是其他颜色,可以将上面的yellow去掉做个比较
                    table.setFillsViewportHeight(true);
                }
            });

            btn2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    table.setFillsViewportHeight(false);
                }
            });


        }

        public void updateLabel(final String value){
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    if (label!=null)
                        MySpecialPanel.this.label.setText(value);
                }
            });
        }
    }

}
