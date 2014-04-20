//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Toolkit;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import javax.imageio.ImageIO;
//import javax.swing.BorderFactory;
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//
//public class GUI extends JFrame implements ActionListener{
//	private static final long serialVersionUID = 1L;
//	
//	private Main m;
//	private JPanel buttonpanel;
//	private ImagePanel ip1;
//	//private ImagePanel ip2;
//	//private ImagePanel ip3;
//	private JButton start;
//	private InputStream in;
//	private JButton stop;
//
//	public GUI(Main m){
//		this.m = m;
//		this.setTitle("Realtime Content Collaboration");
//		Toolkit tk = Toolkit.getDefaultToolkit();
//		Dimension screen = tk.getScreenSize();
//		int width = screen.width;
//		int height = screen.height;
//		this.setSize(width/2,height/2);
//		this.setLocation(width/2-this.getSize().width/2, height/2-this.getSize().height/2);
//	}
//
//	public void updateImages(byte[] i1){
//		try
//	      {
//	        in = new ByteArrayInputStream(i1);
//	        BufferedImage  bufImage = ImageIO.read(in);
//	        ip1.updateImage(bufImage);
//	      }
//	      catch(Exception ex)
//	      {
//	        ex.printStackTrace();
//	      }   
//	}
//	
//	public void make() {
//		buttonpanel = new JPanel();
//		buttonpanel.setLayout(null);
//		
//		ip1 = new ImagePanel();
//		//ip1.updateImage(new ImageIcon("fig/Me.png").getImage());
//		ip1.setBounds(150,10,200,160);
//		ip1.setBorder(BorderFactory.createLineBorder(Color.black));
//		this.add(ip1);
//		
////		ip2 = new ImagePanel();
////		ip2.updateImage(new ImageIcon("fig/Me.png").getImage());
////		ip2.setBounds(400,10,200,160);
////		ip2.setBorder(BorderFactory.createLineBorder(Color.black));
////		this.add(ip2);
////		
////		ip3 = new ImagePanel();
////		ip3.updateImage(new ImageIcon("fig/Me.png").getImage());
////		ip3.setBounds(275,180,200,160);
////		ip3.setBorder(BorderFactory.createLineBorder(Color.black));
////		this.add(ip3);
//		
//		start = new JButton("Start");
//		start.setBounds(10, 10, 70, 30);
//		start.addActionListener(this);
//		
//		stop = new JButton("Stop");
//		stop.setBounds(10, 50, 70, 30);
//		stop.addActionListener(this);
//		
//		buttonpanel.add(start);
//		buttonpanel.add(stop);
//		
//		this.add(buttonpanel);
//	}
//	
//	public void actionPerformed(ActionEvent ae){
//		JButton ref = (JButton)ae.getSource();
//		if(ref == start){
//			//m.start();
//		}else if(ref == stop){
//			//m.stop();
//		}
//	}
//}
//
