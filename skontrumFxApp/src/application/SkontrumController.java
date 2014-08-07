package application;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

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

import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import sound.Sound;
import storage.Storage;

public class SkontrumController implements Initializable {

	private String user, barcode;
	private ObservableList<String> codeList;
	private Timer timer;
	private TimerTask task;
	private Sound sound;
	private Storage storage;

	@FXML
	private Button newFile, sendFile, clean;
	@FXML
	private ListView<String> output;
	@FXML
	private TextField input;
	@FXML
	private Label status, counter;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Optional<String> username = getUserLogin();
		if (username.isPresent() && username.get().length() != 0) {
			user = username.get();
			setStatus(user);
		} else {
			// no login = quit
			System.exit(0);
		}

		appendTooltips();
		sound = Sound.getInstance();

		codeList = FXCollections.observableArrayList();
		output.setItems(codeList);

		input.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				barcode = newValue;

				// schedule new task when barcode starts appearing
				if (newValue.length() == 1) {
					input.setStyle("-fx-background-color: red;");

					// create task and schedule it
					timer = new Timer("rolex");
					task = new BarcodeTimer();
					timer.schedule(task, 2000);
				}
			}
		});

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				input.requestFocus();
			}
		});

		storage = new Storage(user + "-" + getTimestamp());
		storage.createFile();
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
		input.setEditable(true);
		input.setStyle(null);
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

				// update UI on FX thread
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						int alreadyScanned = 0;
						// add to list if not already added
						if (!codeList.contains(barcode)) {
							codeList.add(barcode);

							// Write new barcode to file
							storage.appendBarcode(barcode);
							// TODO what if writing to file failed?

							alreadyScanned = codeList.size();
							counter.setText(String.valueOf(alreadyScanned));
						} else {
							System.out.println("Taki kod jest już na liście.");
							// TODO Play some sound?
						}
						// scroll listview to make last item visible
						output.scrollTo(alreadyScanned);

						cleanInputAction(null);

						if (alreadyScanned == 3 || alreadyScanned == 5 || alreadyScanned == 10) {
							showPrizeDialog(alreadyScanned);
						}
					}
				});
			}
		}.start();

	}

	/**
	 * Sets input not editable and warn user with sound.
	 */
	private void lockDownInput() {

		// update UI on FX thread
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				input.setEditable(false);
			}
		});

		// notify user with some awesome sound ;-)
		sound.getCatYelling().play();
	}

	/**
	 * Shows simple prize dialogs.
	 *
	 * @param scannedCout
	 *            Sum of scanned barcodes by user, prize depends on it.
	 */
	private void showPrizeDialog(int scannedCout) {
		Dialogs dialog = Dialogs.create();
		dialog.style(DialogStyle.NATIVE);
		dialog.title("Gratulacje");
		switch (scannedCout) {
		case 3:
			dialog.message("Czas na przerwę!\nPrzeskanowanych książek: " + scannedCout + ".");
			break;
		case 5:
			dialog.message("Czas na kawę!\nPrzeskanowanych książek: " + scannedCout + ".");
			break;
		case 10:
			dialog.message("Zwolnij skanowanie!\nPrzeskanowanych książek: " + scannedCout + ".");
			break;
		default:
			break;
		}
		dialog.showInformation();
	}

	/**
	 * Simple task to handle entered/scanned barcode.
	 */
	private class BarcodeTimer extends TimerTask {

		@Override
		public void run() {
			System.out.println(barcode);
			if (barcode.length() == 8) {
				// TODO Verify barcode
				addNewCode(barcode);
			} else {
				lockDownInput();
			}
			timer.cancel();
		}

	}

	private Optional<String> getUserLogin() {
		Dialogs dialog = Dialogs.create();
		dialog.style(DialogStyle.NATIVE);
		dialog.title("Nazwa użytkownika");
		dialog.message("Wprowadź nazwę użytkownika");
		return dialog.showTextInput();
	}

	public void setStatus(String newStatus) {
		// update UI on FX thread
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				status.setText(newStatus);
			}
		});
	}

	/**
	 * Get current date and time in format which can be used in filename.
	 *
	 * @return current datetime
	 */
	private String getTimestamp() {
		LocalDateTime localDateTime = LocalDateTime.now(ZoneId.systemDefault());
		return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
	}
}
