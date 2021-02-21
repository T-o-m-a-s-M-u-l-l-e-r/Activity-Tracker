package custom_fxml;

import java.util.ArrayList;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import model.Interpret;
import model.Interpret.ProgramData;
import model.Tracker;

public class CustomTab extends Tab {
	protected static Interpret interpret;
	protected static ArrayList<ProgramData> programDatabase;

	public CustomTab(String label, String fxmlResourcePath) {
		super(label);
		FXMLLoader fxmlLoader;
		interpret = Tracker.getInstance().getInterpret();
		programDatabase = interpret.getDataDatabase();
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
