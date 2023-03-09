/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */

package github.daneren2005.dsub.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import github.daneren2005.dsub.R;
import github.daneren2005.dsub.domain.Lyrics;
import github.daneren2005.dsub.domain.MusicDirectory;
import github.daneren2005.dsub.domain.PlayerState;
import github.daneren2005.dsub.service.DownloadFile;
import github.daneren2005.dsub.service.DownloadService;
import github.daneren2005.dsub.service.MusicService;
import github.daneren2005.dsub.service.MusicServiceFactory;
import github.daneren2005.dsub.util.BackgroundTask;
import github.daneren2005.dsub.util.Constants;
import github.daneren2005.dsub.util.TabBackgroundTask;

/**
 * Displays song lyrics.
 *
 * @author Sindre Mehus
 */
public final class LyricsFragment extends SubsonicFragment implements DownloadService.OnSongChangedListener {
	private static final String TAG = "LyricsFragment";
	private TextView artistView;
	private TextView titleView;
	private TextView textView;

	private Lyrics lyrics;

	private DownloadService downloadService;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		if(bundle != null) {
			lyrics = (Lyrics) bundle.getSerializable(Constants.FRAGMENT_LIST);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(Constants.FRAGMENT_LIST, lyrics);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		setTitle(R.string.download_menu_lyrics);
		setSubtitle(null);
		rootView = inflater.inflate(R.layout.lyrics, container, false);
		artistView = (TextView) rootView.findViewById(R.id.lyrics_artist);
		titleView = (TextView) rootView.findViewById(R.id.lyrics_title);
		textView = (TextView) rootView.findViewById(R.id.lyrics_text);

		downloadService = getDownloadService();
		downloadService.addOnSongChangedListener(this, true);

		String artist = getArguments().getString(Constants.INTENT_EXTRA_NAME_ARTIST);
		String title = getArguments().getString(Constants.INTENT_EXTRA_NAME_TITLE);

		if(lyrics == null) {
			load(artist, title);
		} else {
			setLyrics();
		}

		return rootView;
	}

	@Override
	public void onDestroy() {
		downloadService.removeOnSongChangeListener(this);
		super.onDestroy();
	}

	private void load(final String artist, final String title) {
		BackgroundTask<Lyrics> task = new TabBackgroundTask<Lyrics>(this) {
			@Override
			protected Lyrics doInBackground() throws Throwable {
				MusicService musicService = MusicServiceFactory.getMusicService(context);
				return musicService.getLyrics(artist, title, context, this);
			}

			@Override
			protected void done(Lyrics result) {
				lyrics = result;
				setLyrics();
			}
		};
		task.execute();
	}

	private void setLyrics() {
		if (lyrics != null && lyrics.getArtist() != null) {
			artistView.setText(lyrics.getArtist());
			titleView.setText(lyrics.getTitle());
			textView.setText(lyrics.getText());
		} else {
			artistView.setText(R.string.lyrics_nomatch);
		}
	}

	@Override
	public void onSongChanged(final DownloadFile currentPlaying, int currentPlayingIndex, boolean shouldFastForward) {
		Log.d(TAG, "onSongChanged");

		String artist = currentPlaying.getSong().getArtist();
		String title = currentPlaying.getSong().getTitle();

		load(artist, title);
	}

	@Override
	public void onSongsChanged(List<DownloadFile> songs, DownloadFile currentPlaying, int currentPlayingIndex, boolean shouldFastForward) {
		Log.d(TAG, "onSongsChanged");
	}

	@Override
	public void onSongProgress(DownloadFile currentPlaying, int millisPlayed, Integer duration, boolean isSeekable) {

	}

	@Override
	public void onStateUpdate(DownloadFile downloadFile, PlayerState playerState) {

	}

	@Override
	public void onMetadataUpdate(MusicDirectory.Entry entry, int fieldChange) {

	}
}