package com.jyf.audiofocus;

interface AudioFocusRemoteService {
    void requestAudioFocus();
    void abandonAudioFocus();
}