package com.xsimo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * @author Simon Arame
 *
 */
public class MyJWS extends JFrame{
	private static final long serialVersionUID = -3246226270054064224L;
	private JPanel mainPane;
	private JPanel bPanel;
	private JButton buttonGenerate;
	private JLabel distanceLabel;
	private Point[] points;
	private Point[] PY;
	int indice1;
	int indice2;
	private double distMin;
	private static int NOMBRE_DE_POINTS = 12;

	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2D = (Graphics2D)g;
		g2D.setColor(Color.BLUE);
		for(int i = 0 ; i< NOMBRE_DE_POINTS;i++){
			g.fillOval((int)points[i].getX()-2,(int)points[i].getY()-2,5,5);
		}
		g2D.setColor(Color.RED);
		java.awt.geom.Line2D.Double line = new java.awt.geom.Line2D.Double(points[indice1],points[indice2]);
		java.awt.BasicStroke bs = new java.awt.BasicStroke(2);
		g2D.setStroke(bs);
		g2D.draw(line);
		String distMinRounded = ""+(double)((int)(distMin*100))/100.0;
		distanceLabel.setText("min distance = "+distMinRounded+" pixels");
	}
	public MyJWS(){

		//THIS
		super("l'algorithme des 2 points les plus proches");
		
		//TWEAK to get screensize
		JFrame frame = new JFrame();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		Dimension screen = frame.getSize();
		frame.setVisible(false);
		
		//THIS
		this.setSize(screen);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setLayout(new BorderLayout());
		this.getContentPane().setBackground(Color.WHITE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//MAIN
		mainPane = new JPanel();
		mainPane.setSize(new Dimension(screen.width,screen.height-80));
		mainPane.setBackground(new Color(235, 235, 235));
		this.getContentPane().add(mainPane,BorderLayout.CENTER);

		//bPANEL
		bPanel = new JPanel();
		bPanel.setPreferredSize(new Dimension(screen.width,70));
		bPanel.setBackground(Color.BLUE);
		bPanel.setLayout(new BorderLayout());
		this.getContentPane().add(bPanel,BorderLayout.SOUTH);

		//BOUTON
		buttonGenerate = new JButton("new");
		buttonGenerate.addActionListener(new PointsFieldGen());
		//buttonGenerate.setSize(new Dimension(50,50));
		bPanel.add(buttonGenerate,BorderLayout.WEST);
		
		//LABEL
		distanceLabel = new JLabel();
		distanceLabel.setForeground(Color.WHITE);
		bPanel.add(distanceLabel,BorderLayout.CENTER);
		
		//POINTS
		points = new Point[NOMBRE_DE_POINTS];
		generer();
		distMin = PtProche();
	}

	/**
	 * Important : l'utilisation de SwingUtilities.invokeLater
	 * @param args
	 */
	public static void main(String [] args){
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MyJWS m = new MyJWS();
				m.setVisible(true);
			}
		});
	}
	class PointsFieldGen implements ActionListener{
		public void actionPerformed(ActionEvent e){
			generer();
			distMin = PtProche();
			repaint();
		}
	}
	public void generer(){
		java.util.Random r = new java.util.Random();
		for(int i = NOMBRE_DE_POINTS-1;i>=0;i--){
			points[i] = new Point();
			points[i].setLocation(r.nextInt(this.mainPane.getWidth()),r.nextInt(this.mainPane.getHeight()));
		}
		indice2 = indice1;
	}
	public double PtProche(){
		PY = new Point[NOMBRE_DE_POINTS];
		PY = points.clone();
		sort(0,NOMBRE_DE_POINTS-1);
		sortX(0,NOMBRE_DE_POINTS-1);
		Couple c = PtProcheRec(points,PY);

		for(int i = 0 ; i<NOMBRE_DE_POINTS;i++){
			if(c.p1.getX()==points[i].getX() && c.p1.getY()==points[i].getY()){
				indice1 = i;
			}
			if(c.p2.getX()==points[i].getX() && c.p2.getY()==points[i].getY()){
				indice2 = i;
			}
		}
		return c.d;
	}
	public Couple PtProcheRec(Point [] PX, Point [] PY){
		if(PX.length>4){
			Division div = diviserPoint(PX,PY);
			Couple c1 = PtProcheRec(div.PGX,div.PGY);
			Couple c2 = PtProcheRec(div.PDX,div.PDY);
			Couple cr = null;
			if(c1.d<c2.d){
				cr = c1.clone();
			}else{
				cr = c2.clone();
			}
			Point[] PYY = bandeVert(PY,div.l,cr.d);
			Couple cm = rechVert(PYY,cr.d);
			if(cm.d<cr.d){
				return cm;
			}else{
				return cr;
			}
		}else{
			return naif(PX);
		}
	}
	public Point[] bandeVert(Point [] PY,Point l, double delta){
		java.util.ArrayList<Integer> a = new java.util.ArrayList<Integer>();
		for(int j = 0; j<PY.length;j++){
			if(PY[j].getX()>=l.getX()-delta && PY[j].getX()<= l.getX()+delta){
				a.add(j);
			}
		}
		Point[] PYY = new Point[a.size()];
		for(int k = 0; k<a.size();k++){
			PYY[k] = PY[a.get(k)];
		}
		return PYY;
	}
	public Couple rechVert(Point[] PYY, double delta){
		double min = delta;
		Couple c = new Couple();
		c.d = delta;
		for(int i = 0; i<PYY.length;i++){
			for(int j=i+1;j<i+8 && j< PYY.length;j++){
				double d = distance(PYY[i],PYY[j]);
				if(d<min){
					min=d;
					c.d = d;
					c.p1 = PYY[i];
					c.p2 = PYY[j];
				}
			}
		}
		return c;
	}
	public double distance(Point p1, Point p2){
		double dx = p2.getX()-p1.getX();
		double dy = p2.getY()-p1.getY();
		double dist = java.lang.Math.sqrt(dx*dx+dy*dy);
		return dist;
	}
	public Couple naif(Point[] PX){
		Couple c = new Couple();
		double min = (double)Integer.MAX_VALUE;
		for(int i = 0; i< PX.length-1;i++){
			for(int j = i+1;j<PX.length;j++){
				double dist = distance(PX[i],PX[j]);
				if(dist<min){
					min = dist;
					c.d = dist;
					c.p1 = PX[i];
					c.p2 = PX[j];
				}
			}
		}
		return c;
	}
	public Division diviserPoint(Point[] PX, Point[] PY){
		Division div = new Division();
		Point split = PX[PX.length/2];
		div.l = split;
		div.PGX = new Point[PX.length/2];
		for(int i = 0 ; i< PX.length/2; i++){
			div.PGX[i] = (Point)PX[i].clone();
		}
		div.PGY = new Point[PX.length/2];
		if(PX.length %2 == 0){
			div.PDX = new Point[PX.length/2];
			for(int i = PX.length/2 ; i< PX.length; i++){
				div.PDX[i-PX.length/2] = (Point)PX[i].clone();
			}
			div.PDY = new Point[PX.length/2];
		}else{
			div.PDX = new Point[(PX.length/2)+1];
			for(int i = PX.length/2 ; i < PX.length;i++){
				div.PDX[i-PX.length/2] = (Point)PX[i].clone();
			}
			div.PDY = new Point[(PX.length/2)+1];
		}
		int i = 0;
		int j = 0;
		for(int k = 0 ; k< PX.length; k++){
			if(PY[k].getX()<split.getX() || (PY[k].getX() == split.getX() && PY[k].getY() < split.getY())){
				div.PGY[i]=(Point)PY[k].clone();
				i++;
			}else{
				try{
					div.PDY[j]=(Point)PY[k].clone();
				}catch(Exception e){
					System.out.println("oups");
				}
				j++;
			}
		}
		return div;
	}
	class Couple{
		public Point p1;
		public Point p2;
		public double d;
		public Couple clone(){
			Couple c = new Couple();
			c.p1 = (Point)this.p1.clone();
			c.p2 = (Point)this.p2.clone();
			c.d = new java.lang.Double(this.d);
			return c;
		}
	}
	class Division{
		Point[] PGX;
		Point[] PDX;
		Point[] PGY;
		Point[] PDY;
		Point l;
	}
	public void sort(int p, int q){
		if(p<q){
			int r = part(p,q);
			sort(p,r-1);
			sort(r+1,q);
		}
	}
	public int part(int p, int q){
		Point pivot = PY[p];
		int i = p;
		for(int j = p+1; j<=q ; j++){
			if(PY[j].getY()<=pivot.getY()){
				i=i+1;
				Point temp = PY[i];
				PY[i] = PY[j];
				PY[j] = temp;
			}
		}
		Point temp = PY[i];
		PY[i] = PY[p];
		PY[p] = temp;
		return i;
	}
	public void sortX(int p, int q){
		if(p<q){
			int r = partX(p,q);
			sortX(p,r-1);
			sortX(r+1,q);
		}
	}
	public int partX(int p, int q){
		Point pivot = points[p];
		int i = p;
		for(int j = p+1; j<=q ; j++){
			if(points[j].getX()<pivot.getX()||(points[j].getX()==pivot.getX()&&points[j].getY()<pivot.getY())){
				i=i+1;
				Point temp = points[i];
				points[i] = points[j];
				points[j] = temp;
			}
		}
		Point temp = points[i];
		points[i] = points[p];
		points[p] = temp;
		return i;
	}
}