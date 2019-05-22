import com.clou.uhf.G3Lib.CLReader;
import com.clou.uhf.G3Lib.ClouInterface.IAsynchronousMessage;
import com.clou.uhf.G3Lib.Protocol.Tag_Model;

import java.io.*;
import java.util.Calendar;
import java.util.Date;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;


public class MainFrame implements IAsynchronousMessage {

    static JTable EPCtags;
    static RFIDTableModel MyModel;
    static RFIDRenderer MyRenderer;
    String[] ModelEPC = new String[40];
    int ModelEPCount = 0;
    boolean ExcessTag = true;
    boolean conn,toolSet,FileParsed;
    static String tcpParam = "192.168.1.116:9090";
    static String commParam = "COM12:115200";
    
    JLabel ReaderStatus,IsReading,IsSet,Excess;
    JTextField TimerValue;
    JPanel mainPanel;
    JButton StartBtn;
    JSpinner spinner,tmrVal;

    ImageIcon yes,no;
    static File model;
    int[] HighlightConstraints = new int[100];
    int[] ScanStartTime = new int[3];
    int[] CurrTime = new int[3];
    int ScanTime = 10;
    String timeStr;
    Calendar current;
    javax.swing.Timer timer;


    public void saveResults(){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(model));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void parseFile(){

        for (int i = 0; i < ModelEPC.length; ++i) ModelEPC[i] ="";
        ModelEPCount = 0;
        MyRenderer.initValues();
        for (int g = 0; g < 100; g++) HighlightConstraints[g] = 0;
        MyModel.setRowCount(0);

        try {
            BufferedReader br = new BufferedReader(new FileReader(model));
            String st;
            while ((st = br.readLine()) != null) {
                String[] data = st.split(",");
                UpdateArray(data[0], data[1]);
            }
            FileParsed = true;
            if (conn) StartBtn.setEnabled(true);
            mainPanel.repaint();
            br.close();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void LoadRes(){
        try {
            yes = new ImageIcon(ImageIO.read(MainFrame.class.getResource("res/gc.png")));
            no = new ImageIcon(ImageIO.read(MainFrame.class.getResource("res/rc.png")));

        } catch (Exception e){
            System.out.println("Exception "+e.getMessage());
        }
    }

    public void PickAFile() {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "CSV file", "csv");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose to open this file: " +
                        chooser.getSelectedFile().getName());
                model = chooser.getSelectedFile();
                parseFile();
            }
    }

    public MainFrame(){
        LoadRes();
        createGUI();
        MyRenderer.initValues();
    }

    public void restart(){
        if (conn) {
            changeStatus(IsSet,false);
            changeStatus(Excess,false);
            StartBtn.setEnabled(false);
            spinner.setEnabled(false);
            changeStatus(IsReading,true);
            for (int i = 0; i < ModelEPC.length; ++i)
                if (HighlightConstraints[i] == 3) {
                    ModelEPC[i] = "";
                    ModelEPCount--;
                    MyModel.removeRow(ModelEPCount);
                }

            for (int g = 0; g < ModelEPCount; g++) HighlightConstraints[g] = 2;
            MyRenderer.setConstraints(HighlightConstraints);
            mainPanel.repaint();
            //CLReader._Tag6C.GetEPC_TID(tcpParam, 1, 1);
            CLReader._Tag6C.GetEPC_TID(commParam, 1, 1);
            
            current = Calendar.getInstance();
            current.setTime(new Date());
            ScanStartTime[0] = current.get(Calendar.MINUTE);
            ScanStartTime[1] = current.get(Calendar.SECOND);
            ScanStartTime[2] = current.get(Calendar.MILLISECOND);

            ScanStartTime[1] = ScanStartTime[1] + ScanTime;
            if (ScanStartTime[1] - 60 > 0){
                ScanStartTime[0]++;
                ScanStartTime[1] = ScanStartTime[1] - 60;
            }

            ActionListener listener = new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    current = Calendar.getInstance();
                    current.setTime(new Date());
                    CurrTime[0] = ScanStartTime[0] - current.get(Calendar.MINUTE);
                    CurrTime[1] = ScanStartTime[1] - current.get(Calendar.SECOND);
                    CurrTime[2] = ScanStartTime[2] - current.get(Calendar.MILLISECOND);

                    if (CurrTime[1] < 0){
                        CurrTime[0]--;
                        CurrTime[1] = CurrTime[1] + 60;
                    }

                    if (CurrTime[0]<=0 && CurrTime[1]<=0 && CurrTime[2]<=0) {
                        for (int g = 0; g < 100; g++) {
                            System.out.println(HighlightConstraints[g]);
                            if (HighlightConstraints[g] == 2) {
                                changeStatus(IsSet, false);
                                toolSet = true;
                            }
                            if (HighlightConstraints[g] == 3) {
                                changeStatus(IsSet, false);
                                changeStatus(Excess,true);
                                toolSet = true;
                            }
                        }
                        if (!toolSet) changeStatus(IsSet,true);
                        toolSet = false;
                        changeStatus(IsReading,false);
                        timer.stop();
                        StartBtn.setEnabled(true);
                        TimerValue.setText("0:00:000");
                        spinner.setEnabled(true);
                        //CLReader.Stop(tcpParam);
                        CLReader.Stop(commParam);
                    } else {
                        if (CurrTime[2]<0){
                            CurrTime[1]--;
                            CurrTime[2] = CurrTime[2] + 1000;
                        }
                        timeStr = String.format("%d:%02d:%02d", CurrTime[0], CurrTime[1], CurrTime[2]);
                        TimerValue.setText(timeStr);
                    }

                }
            };

            timer = new javax.swing.Timer(100, listener);
            timer.setInitialDelay(0);
            timer.start();
        }
    }

    public void createGUI() {
        JFrame frame = new JFrame("RFID Alpha 0.2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel();

        MyModel = new RFIDTableModel();
        String[] columnNames = {"Number",
                "EPC",
                "Instrument"};
        MyModel.setColumnIdentifiers(columnNames);
        mainPanel.setLayout(new BorderLayout());
        EPCtags = new JTable(MyModel);

        EPCtags.getColumnModel().getColumn(0).setPreferredWidth(64);
        EPCtags.getColumnModel().getColumn(1).setPreferredWidth(608);
        EPCtags.getColumnModel().getColumn(2).setPreferredWidth(608);

        MyRenderer = new RFIDRenderer();
        EPCtags.setDefaultRenderer(Object.class, MyRenderer);

        final JScrollPane scrollPane = new JScrollPane(EPCtags);
        mainPanel.add(scrollPane,BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setSize(200,600);
        panel.setLayout(null);

        // ----------пїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅ

        StartBtn = new JButton("Start");
        StartBtn.setSize(110,30);
        StartBtn.setLocation(10,35);
        StartBtn.setEnabled(false);
        StartBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {restart();}
        });

        panel.add(StartBtn);

        // ----------пїЅпїЅпїЅпїЅпїЅпїЅ STOP

        //JButton stopBtn = new JButton("Stop");
        //stopBtn.setSize(110,30);
        //stopBtn.setLocation(10,50);
        //stopBtn.addActionListener(new ActionListener()
        //{
        //    @Override
        //    public void actionPerformed(ActionEvent e) {
        //        CLReader.Stop(tcpParam);
                //EPCtags.setText(""); пїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ
                //MyModel.setRowCount(0);
                //clearArray();
        //    }
        //});

        //panel.add(stopBtn);


        TimerValue = new JTextField();
        TimerValue.setText("0:00:000");
        TimerValue.setFont(new Font(TimerValue.getName(), Font.PLAIN, 14));
        TimerValue.setSize(100,30);
        TimerValue.setLocation(10,110);
        TimerValue.setMargin(new Insets(0,20,0,0));
        panel.add(TimerValue);



        JLabel antennaPwr = new JLabel();
        antennaPwr.setText("Считать мощность антенны :");
        antennaPwr.setFont(new Font(antennaPwr.getName(), Font.PLAIN, 14));
        antennaPwr.setSize(200,25);
        antennaPwr.setLocation(130,5);
        panel.add(antennaPwr);

        JLabel Tmrhelp = new JLabel();
        Tmrhelp.setText("Р’СЂРµРјСЏ СЃРєР°РЅР° :");
        Tmrhelp.setFont(new Font(Tmrhelp.getName(), Font.PLAIN, 14));
        Tmrhelp.setSize(130,25);
        Tmrhelp.setLocation(10,85);
        panel.add(Tmrhelp);

        JLabel Tmrhelp2 = new JLabel();
        Tmrhelp2.setText("Р�Р·РјРµРЅРёС‚СЊ :");
        Tmrhelp2.setFont(new Font(Tmrhelp2.getName(), Font.PLAIN, 14));
        Tmrhelp2.setSize(130,25);
        Tmrhelp2.setLocation(130,85);
        panel.add(Tmrhelp2);


        SpinnerModel value =
                new SpinnerNumberModel(5, //initial value
                        0, //minimum value
                        33, //maximum value
                        1); //step
        spinner = new JSpinner(value);
        spinner.setSize(100,30);
        spinner.setLocation(140,35);
        spinner.setEnabled(false);
        spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int curValue = (Integer) spinner.getValue();
                Runnable tt = new ThreadTalk(curValue, tcpParam);
                new Thread(tt).start();
            }
        });
        panel.add(spinner);

        SpinnerModel value2 =
                new SpinnerNumberModel(10, //initial value
                        1, //minimum value
                        59, //maximum value
                        1); //step
        tmrVal = new JSpinner(value2);
        tmrVal.setSize(100,30);
        tmrVal.setLocation(140,110);
        tmrVal.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ScanTime = (Integer) tmrVal.getValue();
            }
        });
        panel.add(tmrVal);

        /*JButton readPwr = new JButton("Read Antenna 1 Power");
        readPwr.setSize(180,30);
        readPwr.setLocation(200,35);
        readPwr.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                String curpower = CLReader.GetPower(tcpParam);
                String[][] resArray = new String[2][2];

                if (curpower != "Failed to get!") {
                    String[] pairs = curpower.split("&");
                    for (int i = 0; i < pairs.length; i++) {
                        String pair = pairs[i];
                        String[] keyValue = pair.split(",");
                        resArray[i][0] = keyValue[0];
                        resArray[i][1] = keyValue[1];
                    }
                    spinner.setValue(Integer.parseInt(resArray[0][1]));
                }
            }
        });
        panel.add(readPwr);*/


        JButton connectbtn = new JButton("Connect");
        connectbtn.setSize(110,30);
        connectbtn.setLocation(280,35);
        connectbtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TCPConnect(tcpParam);
            	COMConnect();
            }
        });
        panel.add(connectbtn);


        JButton disconnectBtn = new JButton("Disconnect");
        disconnectBtn.setSize(110,30);
        disconnectBtn.setLocation(280,73);
        disconnectBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TCPDisconnect(tcpParam);
            	readerDisconnect();
            }
        });
        panel.add(disconnectBtn);


        JButton chooseBtn = new JButton("Choose file");
        chooseBtn.setSize(110,30);
        chooseBtn.setLocation(280,110);
        chooseBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                PickAFile();
            }
        });
        panel.add(chooseBtn);


        JLabel RdyLabel = new JLabel("Р“РѕС‚РѕРІ :");
        RdyLabel.setFont(new Font(RdyLabel.getName(), Font.PLAIN, 14));
        RdyLabel.setSize(110,25);
        RdyLabel.setLocation(440,33);
        panel.add(RdyLabel);

        ReaderStatus = new JLabel();
        if (no != null){ReaderStatus.setIcon(no);}
        ReaderStatus.setSize(45,45);
        ReaderStatus.setLocation(510,25);
        panel.add(ReaderStatus);

        JLabel Readinglabel = new JLabel("РЎС‡РёС‚С‹РІР°РЅРёРµ :");
        Readinglabel.setFont(new Font(Readinglabel.getName(), Font.PLAIN, 14));
        Readinglabel.setSize(110,25);
        Readinglabel.setLocation(420,80);
        panel.add(Readinglabel);

        IsReading = new JLabel();
        if (no != null){IsReading.setIcon(no);}
        IsReading.setSize(45,45);
        IsReading.setLocation(510,70);
        panel.add(IsReading);

        JLabel SetLabel = new JLabel("РљРѕРјРїР»РµРєС‚ :");
        SetLabel.setFont(new Font(SetLabel.getName(), Font.PLAIN, 14));
        SetLabel.setSize(110,25);
        SetLabel.setLocation(430,125);
        panel.add(SetLabel);

        IsSet = new JLabel();
        if (no != null){IsSet.setIcon(no);}
        IsSet.setSize(45,45);
        IsSet.setLocation(510,115);
        panel.add(IsSet);

        JLabel ExcessLabel = new JLabel("Р�Р·Р±С‹С‚РѕРє :");
        ExcessLabel.setFont(new Font(ExcessLabel.getName(), Font.PLAIN, 14));
        ExcessLabel.setSize(110,25);
        ExcessLabel.setLocation(433,170);
        panel.add(ExcessLabel);

        Excess = new JLabel();
        if (no != null){Excess.setIcon(no);}
        Excess.setSize(45,45);
        Excess.setLocation(510,160);
        panel.add(Excess);


        mainPanel.add(panel);

        frame.getContentPane().add(mainPanel);//
        frame.setPreferredSize(new Dimension(1280, 720));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void changeStatus(JLabel label,boolean status){
        if (status){
            label.setIcon(yes);
        } else {
            label.setIcon(no);
        }
    }

    public void UpdateArray(String EPC, String instrument){
        ModelEPC[ModelEPCount] = EPC;
        ModelEPCount++;
        Object[] row = { ModelEPCount, EPC, instrument };
        MyModel.addRow(row);
    }

    public void checkEPC(String TagEPC){
        for(int index = 0; index < ModelEPC.length; ++index)
            if (TagEPC.equals(ModelEPC[index])) {
                if (HighlightConstraints[index] != 3) HighlightConstraints[index] = 1;
                ExcessTag = false;
            }
        if (ExcessTag)
        {
            ModelEPC[ModelEPCount] = TagEPC;
            HighlightConstraints[ModelEPCount] = 3;
            ModelEPCount++;

            Object[] row = { ModelEPCount, TagEPC, "unknown" };
            MyModel.addRow(row);
        }
        MyRenderer.setConstraints(HighlightConstraints);
        ExcessTag = true;
        mainPanel.repaint();
    }
    
    public boolean TCPConnect(String tcpParam){

        conn = CLReader.CreateTcpConn(tcpParam,this);
        try {
            if(conn){
                if (FileParsed) StartBtn.setEnabled(true);
                System.out.println("connection success...");
                changeStatus(ReaderStatus,true);
                readAntennaPwr();
                spinner.setEnabled(true);
                return true;
            } else {
                System.out.println("connection failure!");
                changeStatus(ReaderStatus,false);
                return false;
            }
        } catch (Exception var5) {
            System.out.println("Exception?" + var5.getMessage());
            return false;
        }

    }

    public boolean COMConnect() {
    	conn = CLReader.CreateSerialConn(commParam,this);
    	try {
    		if(conn){
    			System.out.println("connection success...");
    			CLReader._Tag6C.GetEPC_TID(commParam, 1, 1);
    			return true;
    		} else {
    			System.out.println("connection failure!");
    			return false;
    		}
    		} catch (Exception e) {
    			System.out.println("Exception " + e.getMessage());
    			return false;
    		}
    }
    
    public void TCPDisconnect(String tcpParam){
        CLReader.CloseConn(tcpParam);
        changeStatus(ReaderStatus,false);
        StartBtn.setEnabled(false);
        spinner.setEnabled(false);
    }

    public void readerDisconnect() {
    	CLReader.CloseConn(commParam);
        //CLReader.CloseAllConnect();
    }
    
    public void readAntennaPwr(){
        String curpower = CLReader.GetPower(tcpParam);
        String[][] resArray = new String[2][2];

        if (curpower != "Failed to get!") {
            String[] pairs = curpower.split("&");
            for (int i = 0; i < pairs.length; i++) {
                String pair = pairs[i];
                String[] keyValue = pair.split(",");
                resArray[i][0] = keyValue[0];
                resArray[i][1] = keyValue[1];
            }
            spinner.setValue(Integer.parseInt(resArray[0][1]));
        }
    }

    public static void main(String[] args) {
        new MainFrame();
        //System.getProperties().list(System.out);
    }

    @Override
    public void OutPutEPC(Tag_Model model) {
        //System.out.println("EPC "+ model._EPC + "\n");//+ " TID" + model._TID + "\n");
        checkEPC(model._EPC);
    }

}
