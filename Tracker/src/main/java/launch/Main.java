package launch;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import controllers.HistoryTab;
import controllers.StatsTab;
import controllers.TrendsTab;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.Tracker;

public class Main extends Application {
	public static final String APP_NAME = "Tracker";
	public static final String APP_ICON16_PATH = "img/icon16.png";
	public static final String APP_ICON32_PATH = "img/icon32.png";
	public static final String APP_ICON64_PATH = "img/icon64.png";
	public static final String TAB_PANE_CSS_PATH = "css/TabPane.css";
	public static final String GANTT_CHART_CSS_PATH = "css/GanttChart.css";
	public static final String APP_TRAY_ICON_PATH = "img/ms.png";
	public static final String TRENDS_TAB_FXML_PATH = "fxml/TrendsTab.fxml";
	public static final String HISTORY_TAB_FXML_PATH = "fxml/HistoryTab.fxml";
	public static final String STATS_TAB_FXML_PATH = "fxml/StatsTab.fxml";
	public static final String AVERAGE_DAY_CHART_FXML_PATH = "fxml/AverageDayChart.fxml";
	public static final String AVERAGE_WEEK_CHART_FXML_PATH = "fxml/AverageWeekChart.fxml";
	private TabPane tabPane;
	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) {
		try {
			Tracker.getInstance().start();
			this.primaryStage = primaryStage;
			initScene();
			initTrayIcon();
			primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource(APP_ICON16_PATH).toString()));		
			primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource(APP_ICON32_PATH).toString()));		
			primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource(APP_ICON64_PATH).toString()));
			primaryStage.hide();	
			} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initScene(int startingIndex) {
		tabPane = new TabPane(new HistoryTab(), new StatsTab(), new TrendsTab());
		tabPane.getSelectionModel().select(startingIndex);
		tabPane.getStylesheets().add(getClass().getClassLoader().getResource(TAB_PANE_CSS_PATH).toString());
		Scene scene = new Scene(tabPane, 470, 381);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.setTitle(APP_NAME);
		primaryStage.show();
	}

	public void initScene() {
		initScene(0);
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void stop() throws Exception {
		Tracker.getInstance().end();
	}

	private void initTrayIcon() throws IOException, AWTException, URISyntaxException {
		if (SystemTray.isSupported()) {
			Platform.setImplicitExit(false);
			BufferedImage icon = ImageIO.read(getClass().getClassLoader().getResourceAsStream(APP_ICON16_PATH));
			TrayIcon trayIcon = new TrayIcon(icon);
			SystemTray.getSystemTray().add(trayIcon);

			trayIcon.setToolTip(APP_NAME);
			trayIcon.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseClicked(MouseEvent e) {

					if (SwingUtilities.isLeftMouseButton(e)) {

						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								initScene();
							}
						});
					}

				}
			});

			PopupMenu popupMenu = new PopupMenu();
			ObservableList<Tab> tabList = tabPane.getTabs();
			for (Tab tab : tabList) {
				MenuItem menuItem = new MenuItem(tab.getText());
				popupMenu.add(menuItem);
			}
			MenuItem menuItem_quit = new MenuItem("Quit");
			popupMenu.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					String action = e.getActionCommand();
					if (action.equals("Quit")) {
						try {
							stop();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						System.exit(0);
					} else {

						for (int i = 0; i < tabList.size(); i++) {

							if (action.equals(tabList.get(i).getText())) {
								final int index = i;
								Platform.runLater(new Runnable() {

									@Override
									public void run() {
										initScene(index);
									}
								});
								break;
							}

						}

					}

				}
			});
			popupMenu.addSeparator();
			popupMenu.add(menuItem_quit);
			trayIcon.setPopupMenu(popupMenu);
		}
	}

}
