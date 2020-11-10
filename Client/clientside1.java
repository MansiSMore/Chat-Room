import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
public class clientside1 implements ActionListener
{
    ArrayList<String> users = new ArrayList();
    JFrame frame;
    JTextArea chat_area;
    JTextField chat_textfield;
    JPanel mid_box;
    int portno;
    BufferedWriter bw;
    BufferedReader br;
    int i = 0;
    JPanel border ;
    JPanel top1;
    JLabel sa;
    JLabel pn;
    JTextField sat;
    JTextField pnt;
    String username;
    String  address;
    int port;
    Boolean isConnected = false;
    JLabel et;
    JPanel top_box;
    JPanel bot_box;    
    Socket sock;
    BufferedReader reader;
    PrintWriter writer;
    //JScrollBar horisb;
    JPanel bot_1;
    JButton login;
    JButton logout;
    JButton whoisin;
    JScrollPane jScrollPane1;
    JLabel uname;
    JTextField name2;
    Client cli;
   
		  
public  clientside1(){
    //@SuppressWarnings("unchecked")
        jScrollPane1=new JScrollPane();
        frame = new JFrame("Chat Client");
        frame.setSize(500,500);
        name2 = new JTextField (20);
        chat_area=new JTextArea(500,500);
        chat_area.append("Welcome to chat room.\n");
        chat_textfield=new JTextField();
        border = new JPanel(new BorderLayout());
        top1 = new JPanel(new FlowLayout());
        sa = new JLabel("Server Address");
         pn = new JLabel("Port Number");
        sat = new JTextField(10);
        pnt = new JTextField(4);
        uname=new JLabel("username:");
        
        top1.add(sa);   top1.add(sat);   top1.add(pn);   top1.add(pnt); top1.add(uname); top1.add(name2);
        et = new JLabel("Enter Text Here");
        top_box = new JPanel();
        mid_box=new JPanel();
        top_box.setLayout(new BoxLayout(top_box, BoxLayout.Y_AXIS));
        top_box.add(top1);
        top_box.add(et);    top_box.add(chat_textfield);
        border.add(top_box, BorderLayout.NORTH);
        border.add(mid_box, BorderLayout.CENTER);   
        mid_box.add(chat_area);        
        
        jScrollPane1.setViewportView(chat_area);
        border.add(jScrollPane1, BorderLayout.CENTER);
        
        bot_box = new JPanel();
        bot_box.setLayout(new BoxLayout(bot_box, BoxLayout.Y_AXIS));
        //horisb = new JScrollBar(JScrollBar.HORIZONTAL);
        bot_1 = new JPanel(new FlowLayout());
        login = new JButton("login");
        login.setActionCommand ("login");
    login.addActionListener (this);
        logout = new JButton("logout");
        logout.setActionCommand ("logout");
    logout.setEnabled (false);
    logout.addActionListener (this);
        whoisin = new JButton("who_is_in");
         whoisin.setActionCommand ("who_is_in");
    whoisin.setEnabled (false);
    whoisin.addActionListener (this);
    chat_textfield.setActionCommand("send");
    chat_textfield.addActionListener(this);
        bot_1.add(login);   bot_1.add(logout);   bot_1.add(whoisin);
       bot_box.add(bot_1);
        border.add(bot_box, BorderLayout.SOUTH);
               frame.add(border, BorderLayout.CENTER);
 
    //logout = new JButton ("Logout");
    
    
   // whoisin = new JButton ("Who is in");
   
        frame.setVisible(true);
 
          frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 //cli = new Client("localhost",1500, "shivali",this);
    frame.setVisible(true);
   
    }
    
    public void actionPerformed(ActionEvent e)
    {
        String ac = e.getActionCommand();
        
        if(ac.equals("login"))
        {
            String serv_add = sat.getText();
            String port_num = pnt.getText();
            int port_number = Integer.parseInt(port_num);
            String username = name2.getText();
             if(serv_add.equals("") || port_num.equals("") || username.equals(""))
             {
                 chat_area.setText("\nLogin Failed. Please provide proper details to login.\n"); 
             }
             else
             {
                 cli = new Client(serv_add, port_number, username, this);
                 login.setEnabled(false);
                 logout.setEnabled(true);
                 whoisin.setEnabled(true);
                 cli.run();
             }
        }
        
        
        if(ac.equals("logout"))
        {
            //save the data and display propper message.
           
            cli.sendMessage("LOGOUT");
             login.setEnabled(true);
            logout.setEnabled(false);
            whoisin.setEnabled(false);
            //cli.disconnect();
            cli.disc();
            chat_area.setText("Logged out successfullly\n");
            
        }
        
        if(ac.equals("who_is_in"))
        {
            // the logic to display  who all are in goes here.
            cli.sendMessage("WHOISIN");
        }
        
        if(ac.equals("send"))
        {
          String smsg = chat_textfield.getText();
          if(!smsg.equals(""))
          {
              cli.sendMessage(smsg);
              chat_textfield.setText("");     
          }
          else
          {
          
            cli.sendMessage("LOGOUT");
             login.setEnabled(true);
            logout.setEnabled(false);
            whoisin.setEnabled(false);
            //cli.disconnect();
            cli.disc();
            chat_area.setText("Logged out successfullly\n");
              // cant do anything.
          }
              
        }
    }
    

    
        void appendRoom(String str) {
        chat_area.append(str);
        chat_area.setCaretPosition(chat_area.getText().length() - 1);
    }

    void appendEvent(String str) {
        chat_area.append(str);
        chat_area.setCaretPosition(chat_area.getText().length() - 1);
    }
  
        public static void main(String [] args)
        {
           
                clientside1 cobj=new clientside1();
                
        }
        
      public void windowClosing(WindowEvent e) 
      {
        // if my client exist
        
        cli.sendMessage("LOGOUT");
        cli.disc();
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
      }
    
    // Ignore the other WindowListener methods
   /* public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
*/

}
    
    
