package net.osmand.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import net.osmand.data.LatLon;

public class MapInformationLayer implements MapPanelLayer {

	private MapPanel map;
	
	private JLabel gpsLocation;
	private JButton areaButton;
	

	@Override
	public void destroyLayer() {
	}

	@Override
	public void initLayer(final MapPanel map) {
		this.map = map;
		BoxLayout layout = new BoxLayout(map, BoxLayout.LINE_AXIS);
		map.setLayout(layout);
		map.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));
		
		gpsLocation = new JLabel();
		gpsLocation.setOpaque(false);
		updateLocationLabel();
		
		JButton zoomIn = new JButton("+"); //$NON-NLS-1$
		JButton zoomOut = new JButton("-"); //$NON-NLS-1$
		JButton offline = new JButton("*"); //$NON-NLS-1$
		areaButton = new JButton();
		areaButton.setAction(new AbstractAction(Messages.getString("MapInformationLayer.PRELOAD.AREA")){ //$NON-NLS-1$
			private static final long serialVersionUID = -5512220294374994021L;

			@Override
			public void actionPerformed(ActionEvent e) {
				new TileBundleDownloadDialog(map, map).showDialog();
			}
			
		});
		zoomIn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				map.setZoom(map.getZoom() + 1);
			}
		});
		zoomOut.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				map.setZoom(map.getZoom() - 1);
			}
		});
		offline.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!QtCorePanel.isUnix()) {
					JOptionPane.showMessageDialog(OsmExtractionUI.MAIN_APP.getFrame(), "Native rendering supported only on Linux", "Info", JOptionPane.ERROR_MESSAGE);
					return;
				}
				NativePreferencesDialog dlg = new NativePreferencesDialog(map);
				dlg.showDialog();
				if (dlg.isOkPressed()) {
					String folder = DataExtractionSettings.getSettings().getQtLibFolder();
					if (folder.equals("")) {
						folder = new File("lib-gl").getAbsolutePath();
					}
					QtCorePanel.loadNative(folder);
					
					final QtCorePanel sample = new QtCorePanel(new LatLon(map.getLatitude(), map.getLongitude()), map
							.getZoom());
					sample.setRenderingProperties(DataExtractionSettings.getSettings().getRenderingProperties());
					sample.setRenderingStyleFile(DataExtractionSettings.getSettings().getRenderXmlPath());
					Frame frame = sample.showFrame(800, 600);
					frame.addWindowListener(new java.awt.event.WindowAdapter() {
						@Override
						public void windowClosing(java.awt.event.WindowEvent e) {
							map.loadSettingsLocation();
							map.refresh();
						}
					});
					
				}
			}
		});
		
		map.add(gpsLocation);
		map.add(Box.createHorizontalGlue());
		map.add(areaButton);
		map.add(zoomIn);
		map.add(zoomOut);
		map.add(offline);
		gpsLocation.setAlignmentY(Component.TOP_ALIGNMENT);
		areaButton.setVisible(false);
		areaButton.setAlignmentY(Component.TOP_ALIGNMENT);
		zoomOut.setAlignmentY(Component.TOP_ALIGNMENT);
		zoomIn.setAlignmentY(Component.TOP_ALIGNMENT);
		offline.setAlignmentY(Component.TOP_ALIGNMENT);
		
		JPopupMenu popupMenu = map.getPopupMenu();
		Action selectMenu= new AbstractAction("Select point...") {
			private static final long serialVersionUID = -3022499800877796459L;

			@Override
			public void actionPerformed(ActionEvent e) {
				SelectPointDialog dlg = new SelectPointDialog(map, new LatLon(map.getLatitude(), map.getLongitude()));
				dlg.showDialog();
				LatLon l = dlg.getResult();
				if(l != null){
					map.setLatLon(l.getLatitude(), l.getLongitude());
					map.setZoom(15);
				}
			}

			
		};
		popupMenu.add(selectMenu);

	}
	
	public void setAreaButtonVisible(boolean b){
		areaButton.setVisible(b);
	}
	
	public void setAreaActionHandler(Action a){
		areaButton.setAction(a);
	}
	
	private void updateLocationLabel(){
		double latitude = map.getLatitude();
		double longitude = map.getLongitude();
		int zoom = map.getZoom();
//		gpsLocation.setText(MessageFormat.format("Lat : {0,number,#.####}, lon : {1,number,#.####}, zoom : {2}", latitude, longitude, zoom)); //$NON-NLS-1$
	}
	@Override
	public void prepareToDraw() {
		updateLocationLabel();
		
	}

	@Override
	public void paintLayer(Graphics2D g) {
		g.setColor(Color.black);
		g.fillOval((int)map.getCenterPointX() - 2,(int) map.getCenterPointY() - 2, 4, 4);
		g.drawOval((int)map.getCenterPointX() - 2,(int) map.getCenterPointY() - 2, 4, 4);
		g.drawOval((int)map.getCenterPointX() - 5,(int) map.getCenterPointY()- 5, 10, 10);
		
	}
	

}
