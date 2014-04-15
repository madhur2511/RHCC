import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class ImagePanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private Image img=null;
	
	public ImagePanel(){}
	
	public ImagePanel(Image img){
	    this.img = img;
	    Dimension size = new Dimension(1000, 600);
	    setPreferredSize(size);
	    setMinimumSize(size);
	    setMaximumSize(size);
	    setSize(size);
	    setLayout(null);
	    setBorder(BorderFactory.createLineBorder(Color.black));
	}
	
    public void updateImage(Image img){
	    this.img = img;
	    validate();
	    repaint();
    }
    
    public Image getImage(){
    	return img;
    }
    
    
	@Override
	public void paintComponent(Graphics g){
		g.drawImage(img, 0, 0, getWidth (), getHeight (), null);
	}
}