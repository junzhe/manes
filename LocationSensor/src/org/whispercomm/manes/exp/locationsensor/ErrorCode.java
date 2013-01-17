package org.whispercomm.manes.exp.locationsensor;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Errors that can be returned by the {@link RemoteMac.Stub} methods.
 * <p>
 * These should be exceptions, but Android does not support throwing exceptions
 * across IPC boundaries.
 * 
 * @author David R. Bild
 * 
 */
public enum ErrorCode implements Parcelable {
	SUCCESS, NOT_REGISTERED;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.name());
	}

	public static final Parcelable.Creator<ErrorCode> CREATOR = new Parcelable.Creator<ErrorCode>() {

		@Override
		public ErrorCode createFromParcel(Parcel source) {
			return ErrorCode.valueOf(source.readString());
		}

		@Override
		public ErrorCode[] newArray(int size) {
			return new ErrorCode[size];
		}

	};
}