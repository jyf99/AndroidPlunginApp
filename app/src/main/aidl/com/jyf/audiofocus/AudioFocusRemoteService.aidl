package com.jyf.audiofocus;
import com.jyf.audiofocus.AudioFocusRemoteChangeListener;

/**
 * all {@link #} arguments to see {@link AudioManage}
 */
interface AudioFocusRemoteService {
    /**
     * Request audio focus.
     * See the {@link AudioFocusRequest} for information about the options available to configure
     * your request, and notification of focus gain and loss.
     * @param focusRequest a {@link AudioFocusRequest} instance used to configure how focus is
     *   requested.
     * @return {@link #AUDIOFOCUS_REQUEST_FAILED}, {@link #AUDIOFOCUS_REQUEST_GRANTED}
     *     or {@link #AUDIOFOCUS_REQUEST_DELAYED}.
     *     <br>Note that the return value is never {@link #AUDIOFOCUS_REQUEST_DELAYED} when focus
     *     is requested without building the {@link AudioFocusRequest} with
     *     {@link AudioFocusRequest.Builder#setAcceptsDelayedFocusGain(boolean)} set to
     *     {@code true}.
     * @throws NullPointerException if passed a null argument with listener
     * @RequiresApi(Build.VERSION_CODES.O)
     */
    int requestAudioFocusRequest(
        AudioFocusRemoteChangeListener listener,
        int focusGain, int usage, int contentType,
        boolean acceptsDelayedFocusGain
    );

    /**
     * Abandon audio focus. Causes the previous focus owner, if any, to receive focus.
     * @return {@link #AUDIOFOCUS_REQUEST_FAILED} or {@link #AUDIOFOCUS_REQUEST_GRANTED}
     * @RequiresApi(Build.VERSION_CODES.O)
     */
    int abandonAudioFocusRequest();

    /**
     * Request audio focus.
     * Send a request to obtain the audio focus
     * @param l the listener to be notified of audio focus changes, Nonull.
     * @param streamType the main audio stream type affected by the focus request. Nullable, default
     *        is {@link #STREAM_MUSIC}
     * @param durationHint use {@link #AUDIOFOCUS_GAIN_TRANSIENT} to indicate this focus request
     *     is temporary, and focus will be abandonned shortly. Examples of transient requests are
     *     for the playback of driving directions, or notifications sounds.
     *     Use {@link #AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK} to indicate also that it's ok for
     *     the previous focus owner to keep playing if it ducks its audio output.
     *     Alternatively use {@link #AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE} for a temporary request
     *     that benefits from the system not playing disruptive sounds like notifications, for
     *     usecases such as voice memo recording, or speech recognition.
     *     Use {@link #AUDIOFOCUS_GAIN} for a focus request of unknown duration such
     *     as the playback of a song or a video. Nullable, default is {@link #AUDIOFOCUS_GAIN}
     * @return {@link #AUDIOFOCUS_REQUEST_FAILED} or {@link #AUDIOFOCUS_REQUEST_GRANTED}
     * @throws NullPointerException if passed a null argument with listener
     * @deprecated use {@link #requestAudioFocus(AudioFocusRequest)}
     */
     int requestAudioFocus(AudioFocusRemoteChangeListener listener, int streamType, int durationHint);

     /**
      * Abandon audio focus. Causes the previous focus owner, if any, to receive focus.
      * @return {@link #AUDIOFOCUS_REQUEST_FAILED} or {@link #AUDIOFOCUS_REQUEST_GRANTED}
      * @deprecated use {@link #abandonAudioFocusRequest(AudioFocusRequest)}
      */
     int abandonAudioFocus();
}