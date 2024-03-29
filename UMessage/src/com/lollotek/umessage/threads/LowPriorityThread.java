package com.lollotek.umessage.threads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;

import org.apache.http.HttpException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.classes.DumpDB;
import com.lollotek.umessage.classes.ExponentialQueueTime;
import com.lollotek.umessage.classes.HttpResponseUmsg;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.managers.ConfigurationManager;
import com.lollotek.umessage.managers.DropboxManager;
import com.lollotek.umessage.managers.SynchronizationManager;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class LowPriorityThread extends Thread {

	private static final String TAG = LowPriorityThread.class.getName() + ":\n";

	private LowPriorityThreadHandler lowPriorityThreadHandler = null;
	private Handler mainThreadHandler = null;
	private ExponentialQueueTime timeQueue;
	private final long TIME_MINUTE = 60, TIME_HOUR = 3600, TIME_DAY = 86400,
			TIME_DUMP_DB = 86400, TIME_CHECK_USER_IMAGES_UPDATED = 86400;

	private Bundle request, response;

	public LowPriorityThread(Handler handler) {
		mainThreadHandler = handler;

		long[] time = new long[5];
		time[0] = 1;
		time[1] = 10;
		time[2] = TIME_MINUTE;
		time[3] = TIME_HOUR;
		time[4] = TIME_DAY;
		timeQueue = new ExponentialQueueTime(time);
	}

	public void run() {
		Looper.prepare();

		lowPriorityThreadHandler = new LowPriorityThreadHandler();
		mainThreadHandler.obtainMessage(
				MessageTypes.RECEIVE_LOW_PRIORITY_THREAD_HANDLER,
				lowPriorityThreadHandler).sendToTarget();

		lowPriorityThreadHandler.obtainMessage(
				MessageTypes.CHECK_FOR_USER_IMAGES_UPDATED).sendToTarget();

		Looper.loop();
	}

	private class LowPriorityThreadHandler extends Handler {

		private File mainFolder, myNewProfileImage;
		private Provider p;

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Message m, syncMsg;
			Bundle b, bnd;
			HttpResponseUmsg httpResult = new HttpResponseUmsg();

			JSONObject parameters, result;

			p = new Provider(UMessageApplication.getContext());

			AndroidAuthSession sessionDropbox;
			DropboxAPI<AndroidAuthSession> mApi;

			switch (msg.what) {
			case MessageTypes.DESTROY:
				mainThreadHandler = null;
				lowPriorityThreadHandler = null;
				Looper.myLooper().quit();

				break;

			case MessageTypes.CHECK_FOR_USER_IMAGES_UPDATED:
				Cursor users = p.getTotalUser();

				if (users == null) {
					addToQueue(msg, TIME_CHECK_USER_IMAGES_UPDATED, 4, true,
							false);
					break;
				}

				while (users.moveToNext()) {
					b = new Bundle();
					b.putString("prefix", users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_PREFIX)));
					b.putString("num", users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_NUM)));
					m = new Message();
					m.what = MessageTypes.DOWNLOAD_USER_IMAGE;
					m.setData(b);
					lowPriorityThreadHandler.sendMessage(m);
				}

				if (users != null) {
					users.close();
				}

				addToQueue(msg, TIME_CHECK_USER_IMAGES_UPDATED, 4, true, false);

				break;

			case MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC",
							Toast.LENGTH_SHORT).show();
				}

				String imageUrl = (String) msg.obj;

				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());
				myNewProfileImage = new File(mainFolder.toString()
						+ Settings.MY_PROFILE_IMAGE_SRC);
				try {
					Utility.downloadFileFromUrl(
							UMessageApplication.getContext(),
							myNewProfileImage, imageUrl);
				} catch (HttpException e) {
					Utility.reportError(UMessageApplication.getContext(), e,
							TAG + ": handleMessage():DOWNLOAD_MY_PROFILE_IMAGE");

					addToQueue(msg, TIME_HOUR, 4, false, false);
				}
				break;

			case MessageTypes.UPLOAD_MY_PROFILE_IMAGE:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "UPLOAD_MY_PROFILE_IMAGE", Toast.LENGTH_SHORT)
							.show();
				}

				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());

				myNewProfileImage = new File(mainFolder.toString()
						+ Settings.MY_PROFILE_IMAGE_SRC);

				try {

					request = new Bundle();
					request.putBoolean(ConfigurationManager.SESSION_ID, true);
					response = ConfigurationManager.getValues(request);

					result = Utility.uploadImageProfile(UMessageApplication
							.getContext(), Settings.SERVER_URL,
							myNewProfileImage, response.getString(
									ConfigurationManager.SESSION_ID, ""));

					if (Settings.debugMode) {
						Toast.makeText(UMessageApplication.getContext(),
								TAG + result.toString(), Toast.LENGTH_SHORT)
								.show();
					}
					if ((result == null)
							|| (result.getString("errorCode").equals("KO"))) {
						addToQueue(msg, TIME_MINUTE, 4, false, false);
					} else if (result.getString("errorCode").equals("OK")
							&& result.getBoolean("isSessionValid")) {
						request = new Bundle();
						request.putBoolean(
								ConfigurationManager.PROFILE_IMAGE_TO_UPLOAD,
								false);
						if (!ConfigurationManager.saveValues(request)) {
							Utility.reportError(
									UMessageApplication.getContext(),
									new Exception(
											"Configurazione non scritta: onHandleMessage() UPLOAD_MY_PROFILE_IMAGE LowPriorityThread.java "),
									TAG);
						}

					} else if (!result.getBoolean("isSessionValid")) {
						request = new Bundle();
						request.putString(ConfigurationManager.SESSION_ID, "");
						if (!ConfigurationManager.saveValues(request)) {
							Utility.reportError(
									UMessageApplication.getContext(),
									new Exception(
											"Configurazione non scritta: onHandleMessage() UPLOAD_MY_PROFILE_IMAGE LowPriorityThread.java "),
									TAG);
						}

						if (Settings.debugMode) {
							Toast.makeText(UMessageApplication.getContext(),
									TAG + "sessione non valida... azzerata!",
									Toast.LENGTH_SHORT).show();
						}

					}

				} catch (Exception e) {
					Utility.reportError(UMessageApplication.getContext(), e,
							TAG + ": handleMessage():UPLOAD_MY_PROFILE_IMAGE");

					addToQueue(msg, TIME_HOUR, 4, false, false);
				}
				break;

			case MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_USER_IMAGE_FROM_SRC",
							Toast.LENGTH_SHORT).show();
				}
				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());

				bnd = msg.getData();

				String userImageUrl = (String) msg.obj;
				String newUserImageFileName = userImageUrl
						.substring(userImageUrl.indexOf("+"));
				int posNumDataSeparator = newUserImageFileName.indexOf("_");
				String newImageName = "/"
						+ newUserImageFileName
								.substring(0, posNumDataSeparator) + ".jpg";
				long newImageData = Long.parseLong(newUserImageFileName
						.substring(posNumDataSeparator + 1,
								newUserImageFileName.length() - 4));
				File userImage = new File(mainFolder.toString()
						+ Settings.CONTACT_PROFILE_IMAGES_FOLDER + newImageName);

				Cursor userInfo = null;
				try {
					userInfo = p.getUserInfo(bnd.getString("prefix"),
							bnd.getString("num"));

					if (!userInfo.moveToNext()) {
						break;
					}

					String imageSrc = userInfo.getString(userInfo
							.getColumnIndex(DatabaseHelper.KEY_IMGSRC));

					long imageData = Long.parseLong(userInfo.getString(userInfo
							.getColumnIndex(DatabaseHelper.KEY_IMGDATA)));

					if ((imageSrc.equals("0")) || (imageData == 0)
							|| (imageData != newImageData)) {
						if (Utility.downloadFileFromUrl(
								UMessageApplication.getContext(), userImage,
								userImageUrl)) {
							p.updateUserImage(bnd.getString("prefix"),
									bnd.getString("num"), newImageName,
									newImageData);
						}
					}
				} catch (Exception e) {
					Utility.reportError(
							UMessageApplication.getContext(),
							e,
							TAG
									+ ": handleMessage():DOWNLOAD_USER_IMAGE_FROM_SRC");
					addToQueue(msg, TIME_HOUR, 4, false, false);
				} finally {
					if (userInfo != null) {
						userInfo.close();
					}
				}

				break;

			case MessageTypes.DOWNLOAD_USER_IMAGE:

				bnd = msg.getData();

				try {
					// debug
					if (Settings.debugMode) {
						Toast.makeText(
								UMessageApplication.getContext(),
								TAG + "DOWNLOAD_USER_IMAGE "
										+ bnd.getString("num"),
								Toast.LENGTH_SHORT).show();
					}

					lowPriorityThreadHandler
							.removeMessages(MessageTypes.DOWNLOAD_USER_IMAGE);

					parameters = new JSONObject();
					parameters.accumulate("action", "CHECK_USER_REGISTERED");
					parameters.accumulate("prefix", bnd.getString("prefix"));
					parameters.accumulate("num", bnd.getString("num"));
					parameters.accumulate("anonymous", "yes");

					result = Utility.doPostRequest(Settings.SERVER_URL,
							parameters);

					if (result == null) {
						break;
					}

					if ((!result.getString("errorCode").equals("OK"))
							|| !result.getBoolean("isRegistered")) {
						break;
					}

					// Controllo adesso singolo contatto se immagine profilo
					// da scaricare/aggiornare
					userImageUrl = result.getString("imageProfileSrc");
					if (userImageUrl.length() > 2) {
						userImageUrl = userImageUrl.substring(2);
						m = new Message();
						m.what = MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC;
						m.obj = Settings.SERVER_URL + userImageUrl;
						m.setData(bnd);

						lowPriorityThreadHandler.sendMessage(m);
					}

				} catch (Exception e) {
					Utility.reportError(UMessageApplication.getContext(), e,
							TAG + ": handleMessage():DOWNLOAD_USER_IMAGE");
					addToQueue(msg, TIME_HOUR, 4, false, false);
				}
				break;

			case MessageTypes.DOWNLOAD_ALL_USERS_IMAGES:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_ALL_USERS_IMAGES",
							Toast.LENGTH_SHORT).show();
				}
				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());

				users = p.getTotalUser();

				while (users.moveToNext()) {

					String prefix = users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_PREFIX));
					String num = users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_NUM));
					String imageSrc = users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_IMGSRC));

					long imageData = Long.parseLong(users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_IMGDATA)));

					try {

						parameters = new JSONObject();
						result = new JSONObject();

						parameters
								.accumulate("action", "CHECK_USER_REGISTERED");
						parameters.accumulate("prefix", prefix);
						parameters.accumulate("num", num);
						parameters.accumulate("anonymous", "yes");

						result = Utility.doPostRequest(Settings.SERVER_URL,
								parameters);

						String newUserImageUrl;
						if (!result.getString("errorCode").equals("OK")) {
							continue;
						}

						newUserImageUrl = result.getString("imageProfileSrc");

						if (newUserImageUrl.length() == 0) {
							continue;
						}

						newUserImageUrl = newUserImageUrl.substring(2);

						newUserImageFileName = newUserImageUrl
								.substring(newUserImageUrl.indexOf("+"));

						posNumDataSeparator = newUserImageFileName.indexOf("_");
						newImageName = "/"
								+ newUserImageFileName.substring(0,
										posNumDataSeparator) + ".jpg";
						newImageData = Long.parseLong(newUserImageFileName
								.substring(posNumDataSeparator + 1,
										newUserImageFileName.length() - 4));
						userImage = new File(mainFolder.toString()
								+ Settings.CONTACT_PROFILE_IMAGES_FOLDER
								+ newImageName);

						if ((imageSrc.equals("0")) || (imageData == 0)
								|| (imageData != newImageData)) {
							if (Utility.downloadFileFromUrl(
									UMessageApplication.getContext(),
									userImage, Settings.SERVER_URL
											+ newUserImageUrl)) {
								p.updateUserImage(prefix, num, newImageName,
										newImageData);
							}

						}

					} catch (Exception e) {
						Utility.reportError(
								UMessageApplication.getContext(),
								e,
								TAG
										+ ": handleMessage():DOWNLOAD_ALL_USERS_IMAGES");

					}

				}

				if (users != null) {
					users.close();
				}
				// addToQueue(msg, TIME_DAY, 4, true, false);

				break;

			case MessageTypes.MAKE_DB_DUMP:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "MAKE_DB_DUMP", Toast.LENGTH_SHORT).show();
				}

				lowPriorityThreadHandler
						.removeMessages(MessageTypes.MAKE_DB_DUMP);

				Calendar c = Calendar.getInstance();
				long dataDump = c.getTimeInMillis();
				Cursor cd = p.makeDumpDB();

				if ((cd == null) || (!cd.moveToFirst())) {
					// probabilmente db vuoto
					addToQueue(msg, TIME_DUMP_DB, 4, true, false);
					break;
				}

				request = new Bundle();
				request.putBoolean(ConfigurationManager.PREFIX, true);
				request.putBoolean(ConfigurationManager.NUM, true);
				request.putBoolean(ConfigurationManager.LAST_DATA_DUMP_DB, true);
				response = ConfigurationManager.getValues(request);

				boolean forceDBDump = msg.getData().getBoolean("forceDBDump",
						false);

				if (((dataDump - response.getLong(
						ConfigurationManager.LAST_DATA_DUMP_DB, 0)) < TIME_DUMP_DB * 1000)
						&& !forceDBDump) {
					addToQueue(
							msg,
							TIME_DUMP_DB
									- (dataDump - response
											.getLong(
													ConfigurationManager.LAST_DATA_DUMP_DB,
													0)), 4, true, false);
					if (cd != null) {
						cd.close();
					}
					break;
				}

				DumpDB dump = new DumpDB(cd, String.valueOf(dataDump),
						response.getString(ConfigurationManager.PREFIX, ""),
						response.getString(ConfigurationManager.NUM, ""));
				if (dump.buildChatsListFromCursor()) {

				} else {
					// Errore??
					if (cd != null) {
						cd.close();
					}
					addToQueue(msg, TIME_DUMP_DB, 4, true, false);
					break;
				}

				if (cd != null) {
					cd.close();
				}

				dump.reset();

				JSONObject dumpRoot = new JSONObject();

				try {
					dumpRoot.accumulate("myPrefix", dump.myPrefix);
					dumpRoot.accumulate("myNum", dump.myNum);
					dumpRoot.accumulate("data", dump.dataDumpDB);
				} catch (Exception e) {
					// Errore??
					addToQueue(msg, TIME_DUMP_DB, 4, true, false);
					break;
				}

				while (dump.moveToNextChat()) {
					JSONObject dumpChat = new JSONObject();
					Bundle infoChat = dump.getInfoChat();

					try {
						dumpChat.accumulate("destPrefix",
								infoChat.get("prefix"));
						dumpChat.accumulate("destNum", infoChat.get("num"));
					} catch (Exception e) {
						// Errore??
						addToQueue(msg, TIME_DUMP_DB, 4, true, false);
						break;
					}

					while (dump.moveToNextMessageInChat()) {
						JSONObject dumpMessage = new JSONObject();
						Bundle infoMessage = dump.getInfoMessageInChat();

						String dataMessage = infoMessage.getString("data");

						if (dataDump < Long.parseLong(dataMessage)) {
							continue;
						}

						try {
							dumpMessage.accumulate("direction",
									infoMessage.getString("direction"));
							dumpMessage.accumulate("data",
									infoMessage.getString("data"));
							dumpMessage.accumulate("status",
									infoMessage.getString("status"));
							dumpMessage.accumulate("read",
									infoMessage.getString("read"));
							dumpMessage.accumulate("type",
									infoMessage.getString("type"));
							dumpMessage.accumulate("tag",
									infoMessage.getString("tag"));
							dumpMessage.accumulate("message",
									infoMessage.getString("message"));
						} catch (Exception e) {
							// Errore??
							addToQueue(msg, TIME_DUMP_DB, 4, true, false);
							break;
						}

						try {
							dumpChat.accumulate("Message", dumpMessage);
						} catch (Exception e) {
							// Errore??
							addToQueue(msg, TIME_DUMP_DB, 4, true, false);
							break;
						}
					}

					try {
						dumpRoot.accumulate("Chat", dumpChat);
					} catch (Exception e) {
						// Errore??
						addToQueue(msg, TIME_DUMP_DB, 4, true, false);
						break;
					}
				}

				File mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());
				File dumpFile = new File(mainFolder.toString()
						+ Settings.DUMP_DB_BASE_FILE_NAME + "_" + dataDump);

				if (Utility.saveDumpDB(UMessageApplication.getContext(),
						dumpRoot, dumpFile)) {

					request = new Bundle();
					request.putLong(ConfigurationManager.LAST_DATA_DUMP_DB,
							dataDump);
					if (!ConfigurationManager.saveValues(request)) {
						Utility.reportError(
								UMessageApplication.getContext(),
								new Exception(
										"Configurazione non scritta: onHandleMessage() MAKE_DB_DUMP LowPriorityThread.java "),
								TAG);
					}

					// Dropbox authentication
					sessionDropbox = DropboxManager.getInstance(
							UMessageApplication.getContext()).buildSession();
					mApi = new DropboxAPI<AndroidAuthSession>(sessionDropbox);
					// Fine Dropbox authentication

					// Upload nuovo bk su dropbox
					if (mApi.getSession().isLinked()) {
						try {
							FileInputStream inputStream = new FileInputStream(
									dumpFile);
							Entry response = mApi.putFile("/"
									+ Settings.DUMP_DB_BASE_FILE_NAME + "_"
									+ dataDump, inputStream, dumpFile.length(),
									null, null);
						} catch (Exception e) {

						}
					}
					// Fine upload nuovo bk su dropbox

					syncMsg = new Message();
					syncMsg.what = MessageTypes.DROPBOX_REFRESH;
					b = new Bundle();
					b.putBoolean("forceReloadAllData", true);
					b.putString("lastLocalBkData", String.valueOf(dataDump));
					syncMsg.setData(b);
					SynchronizationManager.getInstance()
							.onSynchronizationFinish(syncMsg);

				} else {
					Toast.makeText(UMessageApplication.getContext(),
							"Errore salvataggio dump su file",
							Toast.LENGTH_SHORT).show();
				}

				b = msg.getData();
				b.remove("forceDBDump");
				msg.setData(b);
				addToQueue(msg, TIME_DUMP_DB, 4, true, false);

				break;

			// Scarico ultimo file bk online su dropbox
			// costruisco dumpdb object con liste conversazioni e messaggi
			// lo passo a metodo del provider che integra con db locale i
			// messaggi non gia presenti localmente
			// eventualmente creo bk locale aggiornato dopo la synch
			case MessageTypes.START_DROPBOX_SYNCHRONIZATION:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "START_DROPBOX_SYNCHRONIZATION",
							Toast.LENGTH_SHORT).show();
				}

				lowPriorityThreadHandler
						.removeMessages(MessageTypes.START_DROPBOX_SYNCHRONIZATION);

				// Dropbox authentication
				sessionDropbox = DropboxManager.getInstance(
						UMessageApplication.getContext()).buildSession();
				mApi = new DropboxAPI<AndroidAuthSession>(sessionDropbox);
				// Fine Dropbox authentication

				// Check user logged
				if (!mApi.getSession().isLinked()) {
					Toast.makeText(UMessageApplication.getContext(),
							"Dropbox user not logged in...", Toast.LENGTH_SHORT)
							.show();
					break;
				}
				// Fine check user logged

				// Download last dropbox db dump, se file non gia esistente
				// localmente
				try {
					DropboxAPI.Entry entries = mApi.metadata("/", 100, null,
							true, null);
					String datalastDropboxDBDump = "0", fileToDownload = "";
					for (DropboxAPI.Entry e : entries.contents) {
						String fileName = e.fileName();
						if (fileName.startsWith(Settings.DUMP_DB_BASE_FILE_NAME
								.substring(1))) {
							String data = fileName.substring(fileName
									.lastIndexOf("_") + 1);
							if (Long.parseLong(datalastDropboxDBDump) < Long
									.parseLong(data)) {
								datalastDropboxDBDump = data;
								fileToDownload = fileName;
							}
						} else {
							continue;
						}
					}

					if (datalastDropboxDBDump.equals("0")
							|| fileToDownload.equals("")) {
						Toast.makeText(UMessageApplication.getContext(),
								"Nessu backup trovato", Toast.LENGTH_SHORT)
								.show();
						break;
					}

					mainFolder = Utility.getMainFolder(UMessageApplication
							.getContext());
					dumpFile = new File(mainFolder.toString()
							+ Settings.DUMP_DB_BASE_FILE_NAME + "_"
							+ datalastDropboxDBDump);

					if (!dumpFile.isFile()) {

						// File file = new File("/magnum-opus.txt");
						FileOutputStream outputStream = new FileOutputStream(
								dumpFile);
						DropboxFileInfo info = mApi.getFile("/"
								+ fileToDownload, null, outputStream, null);

						// debug
						Toast.makeText(UMessageApplication.getContext(),
								"File " + fileToDownload + " scaricato",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(UMessageApplication.getContext(),
								"File da scaricare gia esistente",
								Toast.LENGTH_SHORT).show();
					}

				} catch (Exception e) {
					Toast.makeText(UMessageApplication.getContext(),
							e.toString(), Toast.LENGTH_SHORT).show();
					break;
				}
				// Fine download last dropbox db dump

				// Check se file appena scaricato relativo a utente attualmente
				// loggato in UMessage
				JSONObject dropBoxDBDumpJSON;
				try {
					dropBoxDBDumpJSON = new JSONObject(
							Utility.getStringFromFile(dumpFile.toString()));
					request = new Bundle();
					request.putBoolean(ConfigurationManager.PREFIX, true);
					request.putBoolean(ConfigurationManager.NUM, true);
					response = ConfigurationManager.getValues(request);

					if (!response.getString(ConfigurationManager.PREFIX, "")
							.equals(dropBoxDBDumpJSON.getString("myPrefix"))
							|| !response
									.getString(ConfigurationManager.NUM, "")
									.equals(dropBoxDBDumpJSON
											.getString("myNum"))) {
						Toast.makeText(UMessageApplication.getContext(),
								"File di backup non compatibile",
								Toast.LENGTH_SHORT).show();
						dumpFile.delete();
						break;
					}

				} catch (Exception e) {
					Toast.makeText(UMessageApplication.getContext(),
							e.toString(), Toast.LENGTH_SHORT).show();
					break;
				}
				// Fine check file sca-ricato relativo a utente attualmente
				// loggato in UMessage

				// Passo JSONObject a metodo Provider che scorre l'oggetto e
				// inserisce nel db locale i messaggi eventualmente mancanti
				if (p.synchronizeDB(dropBoxDBDumpJSON)) {
					Toast.makeText(UMessageApplication.getContext(),
							"Sincronizzazione effettuata", Toast.LENGTH_SHORT)
							.show();

					m = new Message();
					m.what = MessageTypes.MAKE_DB_DUMP;
					b = new Bundle();
					b.putBoolean("forceDBDump", true);
					m.setData(b);

					addToQueue(m, 0, 4, true, true);
				}
				// Fine integrazione db locale

				break;
			}
		}

		// Aggiunge il messaggio alla coda, specificando il messaggio, il tempo
		// di delay in secondi (se = 0 nessun delay, se = -1 delay deciso dalla
		// scala temporale a seconda del parametro arg1, arg2 conta quante volte
		// si � tentata esecuzione su attuale valore temporale, senza successo)
		// e se rimuovere prima
		// tutti
		// i messaggi
		// dello stesso tipo presenti in coda
		private void addToQueue(Message msg, long timeDelay,
				int maxTimeDelayQueue, boolean removePendingMessages,
				boolean atFrontQueue) {

			Message newMsg = new Message();
			newMsg.what = msg.what;
			newMsg.arg1 = msg.arg1;
			newMsg.arg2 = msg.arg2;
			newMsg.obj = msg.obj;
			newMsg.replyTo = msg.replyTo;
			newMsg.setData(msg.getData());

			if (removePendingMessages) {
				lowPriorityThreadHandler.removeMessages(msg.what);
			}

			if (timeDelay == -1) {
				if ((++newMsg.arg2) > 4) {
					newMsg.arg2 = 0;
					newMsg.arg1 = timeQueue.toNext(newMsg.arg1);
				}
				if (newMsg.arg1 > maxTimeDelayQueue) {
					newMsg.arg1 = maxTimeDelayQueue;
				}

				lowPriorityThreadHandler.sendMessageDelayed(newMsg,
						timeQueue.getTime(newMsg.arg1) * 1000);
			} else if (timeDelay > 0) {
				lowPriorityThreadHandler.sendMessageDelayed(newMsg,
						timeDelay * 1000);
			} else {
				if (atFrontQueue) {
					lowPriorityThreadHandler.sendMessageAtFrontOfQueue(newMsg);
				} else {
					lowPriorityThreadHandler.sendMessage(newMsg);
				}
			}

		}

	}
}
