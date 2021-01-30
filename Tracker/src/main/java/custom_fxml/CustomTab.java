package custom_fxml;

import java.util.ArrayList;
import java.util.Comparator;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.util.Pair;
import model.Interpret;
import model.Interpret.ProgramData;
import model.Tracker;

public class CustomTab extends Tab {
	protected static Interpret interpret = Tracker.getInstance().getInterpret();
	protected static ArrayList<ProgramData> programDatabase = interpret.getDataDatabase();

	public CustomTab(String label, String fxmlResourcePath) {
		super(label);
		FXMLLoader fxmlLoader;
		try {
			fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlResourcePath));
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			setClosable(false);
			fxmlLoader.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
