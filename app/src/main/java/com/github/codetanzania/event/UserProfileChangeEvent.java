package com.github.codetanzania.event;

import com.github.codetanzania.model.Reporter;

public class UserProfileChangeEvent {

    private Reporter mUser;
    private boolean mLanguageChanged;

    public UserProfileChangeEvent( Reporter mUser, boolean mLanguageChanged ) {
        this.mUser = mUser;
        this.mLanguageChanged = mLanguageChanged;
    }

    public Reporter getUser() /* const */ {
        return mUser;
    }

    public boolean languageChanged() /* const */ {
        return mLanguageChanged;
    }
}
