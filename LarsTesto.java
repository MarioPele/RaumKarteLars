import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import com.fazecast.jSerialComm.SerialPort;

public class LarsTesto extends JFrame {

	static int bstart, bx, by, bz;
	static int mouseXlast, mouseYlast, mouseXnow, mouseYnow, mouseDist;

	static int[][] arrayKoordinaten = new int[900][900];
	static int[] arrayXKoordinate = new int[810000];
	static int[] arrayXKoordinate1 = new int[810000];
	static int[] arrayXKoordinate2 = new int[810000];
	static int[] arrayYKoordinate = new int[810000];
	static int[] arrayYKoordinate1 = new int[810000];
	static int[] arrayYKoordinate2 = new int[810000];
	static int[] arrayZustandKoordinate = new int[810000];

	//set comPort wichtig sonst funktioniert nicht
	static int comPortNr = 1;

	static boolean showline = false;
	static boolean läuft = false;

	JLabel[] jl = new JLabel[6];

	Timer t;

	static MouseListener m;

	public static void main(String[] args) {

		LarsTesto lt = new LarsTesto();
		lt.jFrameErzeugen();
		lt.jButtonshowlineErzeugen();
		lt.jButtonAutoErzeugen(1000);
		lt.jButtonRefreshErzeugen();
		lt.jButtonResetErzeugen();

		lt.jLabelErzeugen(0, "Durchläufe:");
		lt.jLabelErzeugen(1, "MausPosJetzt: ");
		lt.jLabelErzeugen(2, "MausPosLetzte: ");
		lt.jLabelErzeugen(3, "MausPosDiff: ");
		lt.jLabelErzeugen(4, "ComPort: " + comPortNr);
		lt.mouseErzeugen();
		lt.addMouseListener(m);


		SerialPort comPort = SerialPort.getCommPorts()[comPortNr];
		comPort.openPort();

		comPort.setComPortParameters(115200, 8, 1, 0);
		comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		InputStream in = comPort.getInputStream();

		int k = 0;
		try {
			for (int j = 0; j < 810000; ++j) {
				if ((byte) in.read() == -86) {
					arrayXKoordinate1[j] = (byte) in.read();
					arrayXKoordinate2[j] = (byte) in.read();
					arrayYKoordinate1[j] = (byte) in.read();
					arrayYKoordinate2[j] = (byte) in.read();
					arrayZustandKoordinate[j] = (byte) in.read();

					twoBytesToIntX(j);
					twoBytesToIntY(j);

				}
				if (k == 1000) {
					System.out.println(j);

					k = 0;
				}
				k++;

			}
			in.close();
		} catch (Exception e) {
		}

		comPort.closePort();
	}

	public void jFrameErzeugen() {
		setSize(1300, 900);
		setLayout(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}

	public void jButtonAutoErzeugen(int repaintMS) {
		JButton b = new JButton();
		b.setBounds(900, 450, 400, 25);
		b.setText("auto");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!läuft) {
					t = new Timer();
					t.scheduleAtFixedRate(new TimerTask() {

						@Override
						public void run() {
							repaint();
						}
					}, 0, repaintMS);

					b.setText("stop");
					läuft = true;
				}else if(läuft) {
					t.cancel();
					b.setText("auto");
					läuft=false;
				}

			}
		});
		b.setVisible(true);
		add(b);

	}

	public void jButtonRefreshErzeugen() {
		JButton b = new JButton();
		b.setBounds(900, 425, 400, 25);
		b.setText("repaint");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		b.setVisible(true);
		add(b);

	}
	
	public void jButtonResetErzeugen() {
		JButton b = new JButton();
		b.setBounds(900, 475, 400, 25);
		b.setText("reset");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (int j = 0; j < 810000; ++j) {
					arrayZustandKoordinate[j]=0;
				}
				repaint();
			}
		});
		b.setVisible(true);
		add(b);

	}

	public void jButtonshowlineErzeugen() {
		JButton b = new JButton();
		b.setBounds(900, 400, 400, 25);
		b.setText("Linie anzeigen");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!showline) {
					showline = true;
					b.setText("Linie verbergen");
				} else {
					showline = false;
					b.setText("Linie anzeigen");
				}
				repaint();
			}
		});
		b.setVisible(true);
		add(b);
	}

	public void jLabelErzeugen(int labelNr, String text) {
		jl[labelNr] = new JLabel();
		jl[labelNr].setBounds(900, 100 + labelNr * 25, 400, 25);
		jl[labelNr].setText(text);
		jl[labelNr].setVisible(true);
		add(jl[labelNr]);
	}

	public void paint(Graphics g) {
		g.setColor(Color.red);
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;

		g.drawRect(449, 449, 3, 3);

		for (int i = 0; i < 10000; i++) {
			if (arrayZustandKoordinate[i] == 1) {
				g.setColor(Color.blue);
				g.drawRect(arrayXKoordinate[i], arrayYKoordinate[i], 1, 1);
			} else {
				g.setColor(Color.white);
				g.drawRect(arrayXKoordinate[i], arrayYKoordinate[i], 1, 1);
			}
			g.setColor(Color.red);
		}

		if (showline)
			g.drawLine(mouseXlast / 2, mouseYlast / 2, mouseXnow / 2, mouseYnow / 2);
	}

	public static void twoBytesToIntX(int stelle) {
		int t = arrayXKoordinate1[stelle];
		String aa;
		if (t >= 0) {

			String a = Integer.toBinaryString(t);
			if (a.length() == 0) {
				a = "00000000";
			} else if (a.length() == 1) {
				a = "0000000" + a;
			} else if (a.length() == 2) {
				a = "000000" + a;
			} else if (a.length() == 3) {
				a = "00000" + a;
			} else if (a.length() == 4) {
				a = "0000" + a;
			} else if (a.length() == 5) {
				a = "000" + a;
			} else if (a.length() == 6) {
				a = "00" + a;
			} else if (a.length() == 7) {
				a = "0" + a;
			}
			aa = a;

		} else {
			String a = Integer.toBinaryString(t - t - t);
			if (a.length() == 0) {
				a = "10000000";
			} else if (a.length() == 1) {
				a = "1000000" + a;
			} else if (a.length() == 2) {
				a = "100000" + a;
			} else if (a.length() == 3) {
				a = "10000" + a;
			} else if (a.length() == 4) {
				a = "1000" + a;
			} else if (a.length() == 5) {
				a = "100" + a;
			} else if (a.length() == 6) {
				a = "10" + a;
			} else if (a.length() == 7) {
				a = "1" + a;
			}
			aa = a;
		}

		int tt = arrayXKoordinate2[stelle];
		String bb;
		if (tt >= 0) {

			String b = Integer.toBinaryString(tt);
			if (b.length() == 0) {
				b = "00000000";
			} else if (b.length() == 1) {
				b = "0000000" + b;
			} else if (b.length() == 2) {
				b = "000000" + b;
			} else if (b.length() == 3) {
				b = "00000" + b;
			} else if (b.length() == 4) {
				b = "0000" + b;
			} else if (b.length() == 5) {
				b = "000" + b;
			} else if (b.length() == 6) {
				b = "00" + b;
			} else if (b.length() == 7) {
				b = "0" + b;
			}
			bb = b;
		} else {

			String b = Integer.toBinaryString(tt - tt - tt);
			if (b.length() == 0) {
				b = "10000000";
			} else if (b.length() == 1) {
				b = "1000000" + b;
			} else if (b.length() == 2) {
				b = "100000" + b;
			} else if (b.length() == 3) {
				b = "10000" + b;
			} else if (b.length() == 4) {
				b = "1000" + b;
			} else if (b.length() == 5) {
				b = "100" + b;
			} else if (b.length() == 6) {
				b = "10" + b;
			} else if (b.length() == 7) {
				b = "1" + b;
			}
			bb = b;
		}

		String c = bb + aa;

		int i = c.charAt(0);

		if (i == 49) {
			String cc = c.substring(1);
			int ii = Integer.parseInt(cc, 2);
			i = ii;
		} else if (i == 48) {
			String cc = c.substring(1);
			int ii = Integer.parseInt(cc, 2);
			i = -ii;
		}

		if (i < 0)
			i = i + 9255;
		else
			i = i + 9000;

		i = i / 20;
		arrayXKoordinate[stelle] = i;

	}

	public static void twoBytesToIntY(int stelle) {

		int t = arrayYKoordinate1[stelle];
		String aa;
		if (t >= 0) {

			String a = Integer.toBinaryString(t);
			if (a.length() == 0) {
				a = "00000000";
			} else if (a.length() == 1) {
				a = "0000000" + a;
			} else if (a.length() == 2) {
				a = "000000" + a;
			} else if (a.length() == 3) {
				a = "00000" + a;
			} else if (a.length() == 4) {
				a = "0000" + a;
			} else if (a.length() == 5) {
				a = "000" + a;
			} else if (a.length() == 6) {
				a = "00" + a;
			} else if (a.length() == 7) {
				a = "0" + a;
			}
			aa = a;

		} else {
			String a = Integer.toBinaryString(t - t - t);
			if (a.length() == 0) {
				a = "10000000";
			} else if (a.length() == 1) {
				a = "1000000" + a;
			} else if (a.length() == 2) {
				a = "100000" + a;
			} else if (a.length() == 3) {
				a = "10000" + a;
			} else if (a.length() == 4) {
				a = "1000" + a;
			} else if (a.length() == 5) {
				a = "100" + a;
			} else if (a.length() == 6) {
				a = "10" + a;
			} else if (a.length() == 7) {
				a = "1" + a;
			}
			aa = a;
		}

		int tt = arrayYKoordinate2[stelle];
		String bb;
		if (tt >= 0) {

			String b = Integer.toBinaryString(tt);
			if (b.length() == 0) {
				b = "00000000";
			} else if (b.length() == 1) {
				b = "0000000" + b;
			} else if (b.length() == 2) {
				b = "000000" + b;
			} else if (b.length() == 3) {
				b = "00000" + b;
			} else if (b.length() == 4) {
				b = "0000" + b;
			} else if (b.length() == 5) {
				b = "000" + b;
			} else if (b.length() == 6) {
				b = "00" + b;
			} else if (b.length() == 7) {
				b = "0" + b;
			}
			bb = b;
		} else {

			String b = Integer.toBinaryString(tt - tt - tt);
			if (b.length() == 0) {
				b = "10000000";
			} else if (b.length() == 1) {
				b = "1000000" + b;
			} else if (b.length() == 2) {
				b = "100000" + b;
			} else if (b.length() == 3) {
				b = "10000" + b;
			} else if (b.length() == 4) {
				b = "1000" + b;
			} else if (b.length() == 5) {
				b = "100" + b;
			} else if (b.length() == 6) {
				b = "10" + b;
			} else if (b.length() == 7) {
				b = "1" + b;
			}
			bb = b;
		}

		String c = bb + aa;

		int i = c.charAt(0);

		if (i == 49) {
			String cc = c.substring(1);
			int ii = Integer.parseInt(cc, 2);
			i = -ii;
		} else if (i == 48) {
			String cc = c.substring(1);
			int ii = Integer.parseInt(cc, 2);
			i = ii;
		}
		if (i < 0)
			i = i + 9255;
		else
			i = i + 9000;
		i = i / 20;

		arrayYKoordinate[stelle] = i;

	}

	public void twoBytesToIntTest() {
		int t = 140;
		String aa;
		if (t >= 0) {

			String a = Integer.toBinaryString(t);
			if (a.length() == 1) {
				a = "0000000" + a;
			} else if (a.length() == 2) {
				a = "000000" + a;
			} else if (a.length() == 3) {
				a = "00000" + a;
			} else if (a.length() == 4) {
				a = "0000" + a;
			} else if (a.length() == 5) {
				a = "000" + a;
			} else if (a.length() == 6) {
				a = "00" + a;
			} else if (a.length() == 7) {
				a = "0" + a;
			}
			aa = a;

		} else {
			String a = Integer.toBinaryString(t - t - t);
			if (a.length() == 1) {
				a = "1000000" + a;
			} else if (a.length() == 2) {
				a = "100000" + a;
			} else if (a.length() == 3) {
				a = "10000" + a;
			} else if (a.length() == 4) {
				a = "1000" + a;
			} else if (a.length() == 5) {
				a = "100" + a;
			} else if (a.length() == 6) {
				a = "10" + a;
			} else if (a.length() == 7) {
				a = "1" + a;
			}
			aa = a;
		}

		int tt = 3;
		String bb;
		if (tt >= 0) {

			String b = Integer.toBinaryString(tt);
			if (b.length() == 1) {
				b = "0000000" + b;
			} else if (b.length() == 2) {
				b = "000000" + b;
			} else if (b.length() == 3) {
				b = "00000" + b;
			} else if (b.length() == 4) {
				b = "0000" + b;
			} else if (b.length() == 5) {
				b = "000" + b;
			} else if (b.length() == 6) {
				b = "00" + b;
			} else if (b.length() == 7) {
				b = "0" + b;
			}
			bb = b;
		} else {

			String b = Integer.toBinaryString(tt - tt - tt);
			if (b.length() == 1) {
				b = "1000000" + b;
			} else if (b.length() == 2) {
				b = "100000" + b;
			} else if (b.length() == 3) {
				b = "10000" + b;
			} else if (b.length() == 4) {
				b = "1000" + b;
			} else if (b.length() == 5) {
				b = "100" + b;
			} else if (b.length() == 6) {
				b = "10" + b;
			} else if (b.length() == 7) {
				b = "1" + b;
			}
			bb = b;
		}

		String c = bb + aa;

		int i = c.charAt(0);

		if (i == 49) {
			String cc = c.substring(1);
			int ii = Integer.parseInt(cc, 2);
			i = -ii;
		} else if (i == 48) {
			String cc = c.substring(1);
			int ii = Integer.parseInt(cc, 2);
			i = ii;
		}

		System.out.println(i);
	}

	public void mouseErzeugen() {
		m = new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {

				mouseXlast = mouseXnow;
				mouseYlast = mouseYnow;
				mouseXnow = e.getX() * 2;
				mouseYnow = e.getY() * 2;

				jl[1].setText("MausPosJetzt: x" + mouseXnow + "cm y" + mouseYnow + "cm");
				jl[2].setText("MausPosLetzte: x" + mouseXlast + "cm y" + mouseYlast + "cm");
				jl[3].setText("MausPosDiff: " + pythagoras() + "cm");

				repaint();

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		};
		repaint();
	}

	public int pythagoras() {
		int xdiff;
		if (mouseXlast > mouseXnow)
			xdiff = mouseXlast - mouseXnow;
		else
			xdiff = mouseXnow - mouseXlast;

		int ydiff;
		if (mouseYlast > mouseYnow)
			ydiff = mouseYlast - mouseYnow;
		else
			ydiff = mouseYnow - mouseYlast;

		int squareofxdiff = xdiff * xdiff;
		int squareofydiff = ydiff * ydiff;
		int squareofbothdiffs = squareofxdiff + squareofydiff;

		int rootofsum = (int) Math.sqrt(squareofbothdiffs);

		return rootofsum;
	}

}
