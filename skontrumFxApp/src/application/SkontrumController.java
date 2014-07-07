package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

public class SkontrumController implements Initializable {

	private ObservableList<String> codeList;

	@FXML
	private Button newFile, sendFile, clean;
	@FXML
	private ListView<String> output;
	@FXML
	private TextField input;
	@FXML
	private Label status;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		appendTooltips();

		codeList = FXCollections.observableArrayList();
		output.setItems(codeList);

		input.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue.length() == 8) {
					// TODO: validate input
					String code = newValue;
					addNewCode(code);
				} else if (newValue.length() == 0) {
					System.out.println("Wejście puste");
					input.setStyle(null);
				} else if (newValue.length() != 8) {
					input.setStyle("-fx-background-color: red;");
				}
			}
		});

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				input.requestFocus();
			}
		});
	}

	/**
	 * Adds simple tooltips to describe acctions connected with buttons.
	 */
	private void appendTooltips() {
		final Tooltip tooltipNew = new Tooltip("Nowy plik");
		final Tooltip tooltipSend = new Tooltip("Wyślij plik na serwer");
		final Tooltip tooltipClean = new Tooltip("Wyczyść pole z błędnym kodem");

		newFile.setTooltip(tooltipNew);
		sendFile.setTooltip(tooltipSend);
		clean.setTooltip(tooltipClean);
	}

	@FXML
	private void cleanInputAction(ActionEvent event) {
		input.clear();
		input.requestFocus();
	}

	@FXML
	private void newFileAction(ActionEvent event) {
		// TODO Create file which holds scanned barcodes
		System.out.println("Nowy");
	}

	@FXML
	private void sendFileAction(ActionEvent event) {
		System.out.println("Wyślij");
	}

	/**
	 * Adds new scanned or typed barcode to the list.
	 *
	 * @param barcode
	 *            Book barcode which is added to the list.
	 */
	private void addNewCode(String barcode) {

		// separate non-FX thread
		new Thread() {

			@Override
			public void run() {
				// update UI on FX thread
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						input.setStyle("-fx-background-color: green;");
						input.setEditable(false);
					}
				});

				try {
					// Allow user to spot scanned barcode is correct ;-)
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// TODO Write barcode to file

				// update UI on FX thread
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						// add to list
						codeList.add(barcode);
						// scroll listview to make last item visible
						output.scrollTo(codeList.size());

						input.setEditable(true);
						input.clear();
						input.getStyleClass().removeAll("input");
						input.requestFocus();
					}
				});
			}
		}.start();
	}
}
