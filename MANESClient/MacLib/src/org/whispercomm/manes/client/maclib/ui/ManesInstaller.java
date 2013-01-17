package org.whispercomm.manes.client.maclib.ui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.whispercomm.manes.client.maclib.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class ManesInstaller extends Activity {
	private static final String TAG = ManesInstaller.class.getSimpleName();

	private static final boolean USE_MARKET = true;
	private static final boolean USE_BROWSER = true;
	private static final boolean USE_DOWNLOAD = true;

	private static final Uri MARKET_URI = Uri
			.parse("market://details?id=org.whispercomm.manes.client.macentity");

	private static final Uri BROWSER_URI = Uri
			.parse("https://play.google.com/store/apps/details?id=org.whispercomm.manes.client.macentity");

	private static final Uri DOWNLOAD_URI = Uri
			.parse("http://whispercomm.org/manes/downloads/manes.apk");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		install();
	}

	private void install() {

		if (USE_MARKET) {
			try {
				installMarket();
				return;
			} catch (ActivityNotFoundException e) {
				// Ignore; next installation method will be tried
			}
		}

		if (USE_BROWSER) {
			try {
				installBrowser();
				return;
			} catch (ActivityNotFoundException e) {
				// Ignore; next installation method will be tried
			}
		}

		if (USE_DOWNLOAD) {
			installDownload();
			return;
		}

		// Couldn't install; display error dialog
		showErrorDialog();
	}

	private void installStarted() {
		finish();
	}

	private void showErrorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.manes)
				.setTitle(R.string.install_failed_dialog_title)
				.setMessage(R.string.install_failed_dialog_message)
				.setNeutralButton(R.string.install_failed_dialog_neutral,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
		builder.create().show();
	}

	private static Intent buildIntent(Uri uri) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
				| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.setData(uri);
		return intent;
	}

	private void installMarket() throws ActivityNotFoundException {
		startActivity(buildIntent(MARKET_URI));
		installStarted();
	}

	private void installBrowser() throws ActivityNotFoundException {
		startActivity(buildIntent(BROWSER_URI));
		installStarted();
	}

	private void installDownload() {
		File savedApk;
		try {
			savedApk = File.createTempFile("manes-client-", ".apk",
					getCacheDir());
			new DownloadTask(savedApk).execute(DOWNLOAD_URI);
		} catch (IOException e) {
			Log.w(TAG, e);
			showFailureMessage();
		}
	}

	private void installLocal(File apk) {
		// Change to world readable
		try {
			Process p = Runtime.getRuntime().exec(
					String.format("chmod 0644 %s", apk.getAbsolutePath()));
			p.waitFor();

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(apk),
					"application/vnd.android.package-archive");
			intent.setComponent(new ComponentName(
					"com.android.packageinstaller",
					"com.android.packageinstaller.PackageInstallerActivity"));
			startActivity(intent);
			installStarted();
		} catch (IOException e) {
			Log.w(TAG, e);
		} catch (InterruptedException e) {
			Log.w(TAG, e);
		}
	}

	private class DownloadTask extends AsyncTask<Uri, Integer, Boolean> {

		private volatile boolean running;

		private File file;
		private ProgressDialog dialog;

		/**
		 * @param file
		 *            the file object to which to write the downloaded file
		 */
		public DownloadTask(File file) {
			this.running = true;
			this.file = file;
			this.dialog = new ProgressDialog(ManesInstaller.this);
			this.dialog.setIcon(R.drawable.manes);
			this.dialog
					.setTitle(R.string.manes_installer_progress_dialog_title);
			this.dialog.setMessage(getResources().getString(
					R.string.manes_installer_progress_dialog_message));
			this.dialog.setIndeterminate(false);
			this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			this.dialog
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							cancel(true);
						}
					});
		}

		@Override
		protected void onPreExecute() {
			dialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			dialog.setProgress(progress[0]);
		}

		@Override
		protected void onCancelled() {
			running = false;
		}

		@Override
		protected Boolean doInBackground(Uri... params) {
			if (!running) {
				return false;
			}

			FileOutputStream output = null;
			try {
				output = new FileOutputStream(file);

				URL url = new URL(params[0].toString());
				URLConnection connection = url.openConnection();

				int size = connection.getContentLength();
				dialog.setMax((int) Math.ceil(size / 1000.0));

				InputStream input = new BufferedInputStream(url.openStream());
				return copy(input, output, size);
			} catch (MalformedURLException e) {
				// Should be caught in testing.
				throw new RuntimeException(e);
			} catch (IOException e) {
				Log.w(TAG, e);
				return false;
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						// Ignore
					}
				}
			}

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			dialog.dismiss();
			if (success) {
				installLocal(file);
			} else {
				showFailureMessage();
			}
		}

		private boolean copy(InputStream input, OutputStream output,
				long expectedSize) throws IOException {
			byte data[] = new byte[1024];
			long copied = 0;

			int count;
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
				copied += count;
				publishProgress((int) copied / 1000);
				if (!running) {
					return false;
				}
			}
			return true;
		}

	}

	private void showFailureMessage() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				ManesInstaller.this);
		builder.setIcon(R.drawable.manes);
		builder.setTitle(R.string.manes_installer_download_failed_dialog_title);
		builder.setMessage(R.string.manes_installer_download_failed_dialog_message);
		builder.setNeutralButton(
				R.string.manes_installer_download_failed_dialog_neutral,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		builder.create().show();
	}

}
